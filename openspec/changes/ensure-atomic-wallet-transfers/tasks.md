# Tasks

## 1. Configuração transacional e ambiente MongoDB

- [x] 1.1 Verificar a compatibilidade do ambiente MongoDB com transações multi-documento e confirmar que a infraestrutura local em Docker usa replica set ou topologia equivalente.
- [x] 1.2 Validar a configuração do MongoDB local para desenvolvimento, incluindo suporte a transações e requisitos mínimos para execução em arquitetura de replica set de nó único.
- [x] 1.3 Definir a fronteira transacional no caso de uso de transferência e garantir que `MongoTransactionManager` coordene a mesma transação para as duas atualizações de carteira e os dois registros de transação.

## 2. Execução da transferência e rollback

- [x] 2.1 Atualizar o caminho de transferência para que débito da carteira de origem, crédito da carteira de destino e criação de transações sejam confirmados em uma única transação MongoDB.
- [x] 2.2 Garantir que o rollback ocorra corretamente em caso de falha antes ou durante a confirmação, sem aplicar alteração financeira parcial observável.
- [x] 2.3 Preservar a validação existente de saldo insuficiente antes de qualquer mutação de saldo e assegurar que falhas de transação sejam retornadas como erro explícito e não como sucesso.
- [x] 2.4 Verificar se a camada de repositório, mapeamento e fluxo de persistência permanecem compatíveis com operações transacionais e com a semântica de consulta associada.

## 3. Concorrência, idempotência e tratamento de falhas

- [ ] 3.1 Definir a política explícita para conflitos de escrita em transferências concorrentes, incluindo falha sem retry indiscriminado e registro da condição para observabilidade.
- [ ] 3.2 Diferenciar falhas antes do commit de resultados incertos durante o commit e garantir que o sistema não afirme sucesso em estados ambíguos.
- [ ] 3.3 Definir um identificador único de operação e um estado persistido da transferência para suportar idempotência segura, rastreabilidade e reconciliação.
- [ ] 3.4 Proibir retries automáticos da transferência inteira sem validação explícita do estado real e sem reprocessamento financeiro indiscriminado.
- [ ] 3.5 Registrar a condição de que a mesma chave de idempotência com payload diferente deve ser rejeitada como uso indevido e não como nova transferência.

## 4. Testes de validação e regressão

- [ ] 4.1 Adicionar testes de integração com MongoDB real, compatível com transações, para cobrir transferência bem-sucedida e saldo insuficiente.
- [ ] 4.2 Cobrir falha intermediária da transação, rollback explícito, resultado incerto de commit e cenário de concorrência entre transferências sobre a mesma carteira ou carteiras relacionadas.
- [ ] 4.3 Cobrir repetição idempotente da mesma operação e rejeição de chave reutilizada com payload divergente.
- [ ] 4.4 Considerar a utilização de `Testcontainers` para validar a execução real do MongoDB em ambiente de teste, sem depender exclusivamente de mocks ou de configuração artificial.
- [ ] 4.5 Executar o conjunto de testes focado em transferências e persistência MongoDB para confirmar que a atomicidade, o rollback, a idempotência, a reconciliação e a política de concorrência atendem ao contrato da mudança.
