# Proposal

## Why

As transferências atualmente atualizam tanto as carteiras quanto os registros de transações como operações separadas. Se qualquer etapa falhar após a primeira mutação da carteira, o serviço pode deixar saldos e histórico de transações fora de sincronia, gerando um estado financeiro inconsistente. A mudança é necessária para garantir que uma transferência se comporte como uma única operação financeira atômica.

## What Changes

- Garantir atomicidade para transferências entre carteiras.
- Assegurar que atualizações das carteiras de origem e destino e registros de transações sejam confirmados juntos ou não ocorram.
- Preservar o contrato atual da API enquanto reforça as garantias de consistência financeira.
- Adicionar validação explícita e tratamento de erros para cenários de falha na confirmação da transferência.
- Introduzir rastreabilidade de operação via identificador único para prevenir duplicidade financeira e permitir idempotência segura de transferências repetidas.
- Definir estados explícitos para rollback confirmado, commit incerto e reconciliação de operações sem reprocessamento financeiro indiscriminado.

## Capabilities

### New Capabilities
- `wallet-transfers`: define o contrato para execução atômica de transferências entre carteiras, incluindo consistência de saldo e integridade do livro contábil.

### Modified Capabilities
- Nenhuma.

## Impact

- `TransferUseCase` e interações de repositório relacionadas a transferências.
- Camada de persistência MongoDB para documentos de carteira e transação.
- Tratamento de erros e observabilidade em torno de falhas na confirmação de transferências.
- O comportamento atual da API de transferências permanece o mesmo do ponto de vista do cliente, mas as garantias de confiabilidade e consistência melhoram.
