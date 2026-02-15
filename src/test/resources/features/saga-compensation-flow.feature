# language: pt
@ignore
Funcionalidade: Fluxo de Compensação Saga
  Como parte do padrão Saga
  Eu quero executar compensações quando ocorrerem falhas
  Para manter a consistência do sistema distribuído

  @ignore
  Cenário: Estornar pagamento após falha em outro serviço
    Dado que existe um pagamento com status "PAID"
    Quando ocorre uma falha em outro microserviço
    E eu solicito o estorno do pagamento
    Então o pagamento deve ter status "REFUNDED"
    E o evento "PaymentRefundedEvent" deve ser publicado
    E a data de estorno deve ser registrada

  @ignore
  Cenário: Rejeitar estorno de pagamento não pago
    Dado que existe um pagamento com status "PROCESSING"
    Quando eu solicito o estorno do pagamento
    Então devo receber um erro de "InvalidDataException"
    E a mensagem de erro deve conter "Only paid payments can be refunded"

  @ignore
  Cenário: Rejeitar estorno de pagamento que falhou
    Dado que existe um pagamento com status "FAILED"
    Quando eu solicito o estorno do pagamento
    Então devo receber um erro de "InvalidDataException"
    E a mensagem de erro deve conter "Only paid payments can be refunded"

  @ignore
  Esquema do Cenário: Fluxo completo Saga - Criação de Orçamento até Pagamento
    Dado que recebo um evento "ServiceOrderCreatedEvent" para ordem "<orderId>"
    Quando o sistema processa o evento
    Então um orçamento deve ser criado automaticamente com status "PENDING_APPROVAL"
    E o orçamento deve estar vinculado à ordem "<orderId>"
    Quando o administrador aprova o orçamento
    Então o evento "BudgetApprovedEvent" deve ser publicado
    Quando o cliente solicita o pagamento com método "<method>"
    Então um pagamento deve ser criado com status "PROCESSING"
    E após alguns segundos o pagamento deve ser "<finalStatus>"
    E se "<finalStatus>" for "PAID" então "PaymentProcessedEvent" deve ser publicado
    E se "<finalStatus>" for "FAILED" então "PaymentFailedEvent" deve ser publicado

    Exemplos:
      | orderId   | method       | finalStatus |
      | ORDER-001 | CREDIT_CARD  | PAID        |
      | ORDER-002 | PIX          | PAID        |
      | ORDER-003 | DEBIT_CARD   | FAILED      |
