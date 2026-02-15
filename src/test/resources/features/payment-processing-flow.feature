# language: pt
@ignore
Funcionalidade: Fluxo de Processamento de Pagamento
  Como um cliente
  Eu quero processar o pagamento de um orçamento aprovado
  Para concluir meu pedido de serviço

  Cenário: Processar pagamento para orçamento aprovado
    Dado que existe um orçamento aprovado "BUDGET-001"
    E não existe pagamento para este orçamento
    Quando eu solicito o processamento do pagamento com método "CREDIT_CARD"
    Então um novo pagamento deve ser criado com status "PROCESSING"
    E o valor do pagamento deve ser igual ao valor do orçamento
    E o pagamento deve ter um ID único

  Cenário: Rejeitar pagamento para orçamento não aprovado
    Dado que existe um orçamento com status "PENDING_APPROVAL"
    Quando eu solicito o processamento do pagamento
    Então devo receber um erro de "InvalidDataException"
    E a mensagem de erro deve conter "must be approved before payment"

  Cenário: Rejeitar pagamento para orçamento já pago
    Dado que existe um orçamento aprovado "BUDGET-001"
    E já existe um pagamento "PAID" para este orçamento
    Quando eu solicito o processamento do pagamento
    Então devo receber um erro de "InvalidDataException"
    E a mensagem de erro deve conter "already paid"

  Cenário: Gateway simula pagamento aprovado
    Dado que o gateway de pagamento está configurado
    Quando eu processo um pagamento de "100.00" com método "PIX"
    Então em alguns segundos o pagamento deve ser aprovado
    E deve ter um ID externo iniciando com "SIM-"
    E deve ter um código de autorização iniciando com "AUTH-"

  Cenário: Gateway simula falha de pagamento
    Dado que o gateway de pagamento está configurado
    E configurado para simular falhas
    Quando eu processo múltiplos pagamentos
    Então eventualmente um pagamento deve falhar
    E deve ter uma razão de falha preenchida
