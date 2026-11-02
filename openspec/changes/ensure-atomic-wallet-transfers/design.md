# Design

## Contexto

O fluxo atual de transferência em `TransferUseCase` executa várias gravações em sequência: carrega ambas as carteiras, altera os saldos, salva os documentos de ambas as carteiras e, em seguida, cria duas entradas de transação. Não há uma fronteira transacional explícita, o que significa que uma falha parcial pode deixar o estado das carteiras e o histórico de transações inconsistente. Esta mudança endereça esse risco sem alterar a forma externa da API ou a estrutura funcional existente.

## Objetivos / Não objetivos

**Objetivos:**
- Tornar as transferências atômicas sob a perspectiva de saldos de carteira e histórico de transações.
- Preservar o contrato REST existente enquanto melhora a integridade dos dados.
- Garantir que uma transferência com falha não deixe nenhum estado financeiro parcial observável.

**Não objetivos:**
- Redesenhar o modelo de domínio da carteira ou adicionar novas entidades de negócio.
- Introduzir transações distribuídas entre serviços.
- Alterar o esquema público da API além da semântica atual de falha.
- Adicionar idempotência completa ou recuperação automática de operações financeiras parcialmente concluídas.

## Decisões

### 1. Utilizar `MongoTransactionManager` do Spring Data MongoDB
A estratégia principal será centralizar a execução da transferência em uma transação MongoDB gerenciada pelo Spring, por meio de `MongoTransactionManager`. A transação será iniciada no caso de uso de transferência e abrangerá as duas atualizações de saldo e os dois registros de transação.

**Por que isso se encaixa:**
- O sistema já usa o MongoDB como fonte de verdade.
- A operação de transferência toca vários documentos (`wallets` e `transactions`) em uma única unidade funcional.
- O caso de uso de transferência já representa a fronteira de negócio que possui consistência financeira necessária.

**Considerações de implementação:**
- A transação deve ser iniciada no caso de uso e não apenas em um repositório isolado, para preservar a semântica do fluxo financeiro.
- A composição da transação deve incluir: atualização da carteira de origem, atualização da carteira de destino e gravação das duas transações relacionadas.
- O rollback deve ser tratado como consequência de exceções que ocorram antes ou durante a confirmação, conforme o comportamento do driver e do gerenciador transacional do Spring.

### 2. Estabelecer fronteira transacional no caso de uso de transferência
A transação deve ser iniciada no ponto em que a operação de transferência passa a depender de múltiplos documentos e de estados mutáveis de saldo. Essa fronteira será mais estável do que tentar transacionar no nível de cada repositório individual, porque o conceito de integridade financeira pertence à operação de negócio e não à persistência isolada.

**Por que isso se encaixa:**
- A regra de negócio exige que a transferência seja tratada como uma operação financeira indivisível.
- Os repositórios implementam a persistência, mas o caso de uso preserva a visão da operação inteira.
- Isso reduz a chance de consistência parcial em cenários de erro de escrita ou falha de rede.

### 3. Definir a participação dos documentos dentro da mesma transação MongoDB
A mesma transação MongoDB deve abranger:
- atualização do saldo da carteira de origem
- atualização do saldo da carteira de destino
- registro da transação de débito
- registro da transação de crédito

Essa definição precisa ser explícita porque a atomicidade da mudança depende da garantia de que todas essas gravações compartilhem a mesma unidade de trabalho.

**Alternativas consideradas:**
- Atualizar um lado da transação e compensar depois: insuficiente porque deixa janelas de inconsistência observáveis.
- Controlar status por fila ou evento: não atende ao requisito atual de consistência imediata do mesmo fluxo de API.
- Erro de escrita sem transação explícita: rejeitado porque viola a garantia funcional da mudança.

### 4. Concurrência e política de conflitos transacionais
O MongoDB trata conflitos de escrita ao aplicar regras de concorrência por documento e por operação transacional. Em cenários de transações concorrentes sobre a mesma carteira, a operação pode falhar com conflito de gravação, e esse comportamento deve ser tratado como política explícita da aplicação.

**Política proposta:**
- conflitos de escrita em documentos financeiros MUST resultar em falha explícita para a operação em andamento
- a operação MUST NOT ser reexecutada automaticamente de forma indiscriminada
- a aplicação MUST registrar o conflito e devolver erro de operação não concluída para o cliente, sem mascarar a condição como sucesso

**Por que isso se encaixa:**
- retries indiscriminados em operações financeiras podem mascarar inconsistências, duplicar efeitos ou causar loops de tentativa sem benefício real.
- conflitos são sinais de concorrência real e devem ser tratados como eventos de segurança e consistência, não como falhas de infraestrutura triviais.

### 5. Tratamento de falhas: antes do commit e durante o commit
A implementação deve diferenciar claramente:
- falhas ocorridas antes da confirmação, em que a transação ainda não foi committed e a operação pode ser descartada com segurança
- falhas ocorridas durante o processo de confirmação, em que o resultado final pode ficar incerto até que o driver e o ambiente confirmem o estado da transação

**Diretrizes:**
- não assumir que toda exceção significa rollback confirmado
- não tratar qualquer erro de driver como sucesso
- registrar o estado da operação e o tipo de falha para observabilidade e diagnóstico
- usar mecanismos de retry somente quando o driver e o contexto da operação suportarem tal estratégia sem mascarar efeitos financeiros

**Escopo explícito:**
- a idempotência completa da operação de transferência está fora do escopo desta mudança
- a correção inicial visa consistência transacional, não reaproveitamento automático de tentativas duplicadas ou compensação de operações previamente confirmadas

### 6. Infraestrutura e compatibilidade do ambiente MongoDB
A operação de transferência atômica exigirá que o MongoDB esteja configurado como replica set ou cluster compatível com transações multi-documento. Para desenvolvimento local, um replica set de nó único é suficiente, desde que a configuração do ambiente seja compatível com transações do MongoDB.

**Exigências de infraestrutura:**
- o ambiente de execução MUST ser verificado antes da implementação ou deploy em produção
- a validação da compatibilidade transacional MUST incluir a verificação do modo de execução do MongoDB e da topologia suportada
- o ambiente local para testes MUST ser explicitamente documentado como transação habilitada

**Como verificar a compatibilidade:**
- confirmar que a instância MongoDB suporta transações multi-documento
- validar a topologia do cluster/replica set antes da execução do fluxo de transferência
- garantir que a configuração de desenvolvimento defina as condições mínimas para transações, mesmo em ambiente local

### 7. Estratégia de testes transacionais
Os testes desta mudança MUST prever a execução em MongoDB real, em configuração compatível com transações, em vez de depender apenas de mocks ou de um comportamento unitário simplificado. Essa estratégia é essencial para validar a semântica real do driver, do Spring e do mecanismo de commit/rollback.

**Cobertura esperada:**
- transferência bem-sucedida com confirmação completa
- saldo insuficiente rejeitando a operação antes da mutação
- falha intermediária durante a execução da transação
- rollback de transação em caso de erro
- concorrência entre transferências sobre a mesma carteira ou entre carteiras relacionadas

**Alternativa de integração:**
- `Testcontainers` pode ser utilizado para subir uma instância real do MongoDB em ambiente de teste, preservando a validade do comportamento transacional sem depender de uma instalação local manual.

## Riscos / Trade-offs

- [Requisito de replica set ou cluster compatível] → Mitigação: documentar a exigência de implantação do MongoDB e adicionar verificações de inicialização/validação para ambientes compatíveis com transações.
- [Maior latência de gravação] → Mitigação: manter transações de transferência restritas ao mínimo necessário de gravações de carteira e transações para garantir correção.
- [Mais complexidade operacional] → Mitigação: centralizar o tratamento de transação no caso de uso de transferência e validar a execução com testes de integração.
- [Conflitos concorrentes] → Mitigação: falhar de forma explícita e não reexecutar indiscriminadamente operações financeiras.
- [Falhas durante commit] → Mitigação: diferenciar estados de falha antes do commit e durante o commit, registrando a condição para observabilidade.
- [Ambiente local incompatível] → Mitigação: exigir configuração transacional mínima ou ambiente de teste explícito para validação.

## Plano de migração

1. Confirmar que a implantação alvo do MongoDB suporta transações multi-documento.
2. Centralizar a transação na operação de transferência, no caso de uso de aplicação.
3. Garantir que a mesma transação cubra os dois saldos e as duas transações relacionadas.
4. Validar o comportamento em cenários de sucesso, saldo insuficiente, falha intermediária e concorrência.
5. Se o ambiente não suportar transações, falhar de forma clara e não afirmar atomicidade de transferência.

## Perguntas em aberto

- O ambiente de desenvolvimento e testes deve executar o MongoDB com suporte a replica set compatível com transações, ou uma configuração de nó único devidamente validada faz parte do fluxo local padrão?
- O time aceita que retries financeiros automáticos permaneçam fora do escopo desta mudança e sejam tratados em uma evolução posterior, caso sejam necessários?
