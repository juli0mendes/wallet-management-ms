# Spec Delta

## Purpose

Definir o contrato comportamental para transferências atômicas entre carteiras para que saldos, histórico de transações e resultados financeiros permaneçam consistentes, mesmo quando o processo enfrenta falhas, concorrência ou incertezas durante a confirmação.

## ADDED Requirements

### Requirement: Execução atômica de transferência
Uma transferência entre carteiras MUST ser processada como uma única operação financeira e SHALL ser confirmada de forma atômica sobre as duas carteiras e os dois registros de transação, ou nenhuma alteração financeira deve ser persistida.

#### Scenario: Transferência bem-sucedida
- **WHEN** a carteira de origem existe, a carteira de destino existe e a carteira de origem possui saldo suficiente
- **THEN** o sistema MUST debitar a carteira de origem, creditar a carteira de destino, persistir os registros correspondentes da transferência e retornar sucesso somente após a confirmação bem-sucedida da transação MongoDB

#### Scenario: Falha durante a confirmação da transação
- **WHEN** o sistema não consegue concluir a transação MongoDB para uma transferência
- **THEN** o sistema MUST garantir que nenhuma alteração parcial de saldo em nenhuma das duas carteiras seja observável e MUST evitar persistência de histórico parcial de transações

#### Scenario: Saldo insuficiente
- **WHEN** o saldo da carteira de origem é inferior ao valor solicitado da transferência
- **THEN** a transferência MUST ser rejeitada antes de qualquer mutação de saldo e o cliente MUST receber uma resposta de erro indicando saldo insuficiente

### Requirement: Consistência financeira e integridade do livro contábil
Os registros de transação MUST refletir o mesmo resultado financeiro que os saldos das carteiras que descrevem, e a operação SHALL manter a integridade entre débito, crédito e histórico de transações.

#### Scenario: Auditoria de transferência concluída
- **WHEN** uma transferência é concluída com sucesso
- **THEN** os saldos resultantes das carteiras e as entradas de transação associadas MUST descrever as mesmas quantias de débito e crédito, com identificadores de carteira e metadados de transferência consistentes

#### Scenario: Auditoria de transferência falha
- **WHEN** uma transferência falha antes ou durante a confirmação da transação
- **THEN** nenhum saldo de carteira nem registro de transação daquela transferência MUST ser observável como estado parcialmente aplicado

### Requirement: Tratamento de concorrência entre transferências
Quando duas transferências concorrentes atuam sobre a mesma carteira ou sobre carteiras relacionadas, o sistema MUST detectar conflito de escrita e SHALL rejeitar a operação em conflito sem mascarar a falha como sucesso.

#### Scenario: Conflito de escrita simultânea
- **WHEN** duas transferências são executadas ao mesmo tempo sobre a mesma carteira ou sobre carteiras compartilhadas
- **THEN** apenas uma operação MUST completar com sucesso e a outra MUST falhar com erro de conflito transacional, sem retry indiscriminado da operação financeira

#### Scenario: Reexecução não automática
- **WHEN** uma transferência em conflito é rejeitada
- **THEN** o sistema MUST NOT realizar retry automático indiscriminado da operação financeira e MUST registrar a condição de concorrência para diagnóstico e observabilidade

### Requirement: Resultados incertos de commit e rollback explícito
O sistema MUST diferenciar falhas ocorridas antes do commit de situações em que o resultado da transação permanece incerto durante a confirmação, e SHALL tratar essas condições como estados distintos de erro e diagnóstico.

#### Scenario: Falha antes do commit
- **WHEN** a transação falha antes da confirmação final
- **THEN** a operação MUST ser descartada sem aplicar alteração financeira observável e a resposta MUST indicar erro de processamento sem alegar sucesso

#### Scenario: Resultado de commit incerto
- **WHEN** a operação gera exceção durante a confirmação da transação e o resultado final não pode ser confirmado imediatamente
- **THEN** o sistema MUST não afirmar sucesso, MUST registrar a condição de incerteza e MUST tratar a operação como estado indeterminado de commit para reconciliação posterior, sem reprocessamento financeiro indiscriminado
- **AND** MUST considerar `UnknownTransactionCommitResult` apenas quando o MongoDB reporta esse rótulo, sem classificar todas as exceções genéricas como incerteza de confirmação

### Requirement: Correlação inequívoca de lançamentos financeiross
A reconciliação MUST considerar apenas lançamentos associados ao mesmo `transferId` e `operationId` da operação sendo analisada, e SHALL rejeitar qualquer evidência que pertença a outra transferência, mesmo que tenha o mesmo valor, a mesma origem, o mesmo destino ou a mesma descrição.

#### Scenario: Transferência duplicada por similaridade de atributos
- **WHEN** duas operações financeiras possuem a mesma carteira de origem, destino, valor e descrição
- **THEN** o sistema MUST garantir que a reconciliação use apenas os lançamentos que compartilham o mesmo `transferId` e `operationId` da operação em análise, sem cruzar evidências entre transferências diferentes

### Requirement: Ownership seguro e fencing de execução
A execução financeira MUST possuir ownership exclusivo e monotônico por tentativa, e SHALL rejeitar qualquer confirmação de `APPLIED` que não corresponda ao owner ativo da execução atual. A operação MUST permanecer em estado inconclusivo quando não houver autorização válida para concluir o passo financeiro.

#### Scenario: Executor antigo tentando confirmar após rotação de ownership
- **WHEN** uma transferência já teve seu ownership renovado por uma tentativa mais recente
- **THEN** qualquer worker antigo MUST NOT confirmar `APPLIED` nem sobrescrever o estado operacional usando o owner vencido
- **AND** a aplicação MUST validar esse critério atômico dentro da transação financeira e do MongoDB

#### Scenario: Lease expirado durante execução ativa
- **WHEN** o lease de execução expira enquanto a transação financeira ainda está em andamento
- **THEN** o reconciliador MUST NOT assumir a operação como abandonada somente por expiração temporal
- **AND** MUST preservar o estado inconclusivo até que haja evidência operacional suficiente para concluir ou rejeitar a operação

### Requirement: Idempotência e rastreabilidade de transferências
O sistema MUST conservar um identificador único de operação e SHALL rejeitar ou reutilizar de forma segura as requisições repetidas, evitando duplicidade financeira e mantendo as evidências de processamento.

#### Scenario: Idempotência de transferência
- **WHEN** a mesma operação de transferência é repetida com o mesmo identificador e o mesmo payload
- **THEN** o sistema MUST retornar a resposta original ou um resultado equivalente de forma idempotente, sem duplicar saldos ou registros de transação

#### Scenario: Chave de idempotência reutilizada com payload divergente
- **WHEN** a mesma chave de idempotência é reutilizada para uma transferência com conteúdo diferente
- **THEN** o sistema MUST rejeitar a operação com erro de conflito de idempotência e MUST preservar a integridade do primeiro processamento

#### Scenario: Reconciliação de operação em estado indeterminado
- **WHEN** uma transferência entra em `UNKNOWN_COMMIT_RESULT`
- **THEN** o sistema MUST executar reconciliação explícita para determinar se a operação foi aplicada ou se deve permanecer em estado indeterminado, sem repetir a movimentação financeira indiscriminadamente
