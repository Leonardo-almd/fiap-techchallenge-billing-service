# language: pt
Funcionalidade: Fluxo de Aprovação de Orçamento
  Como um administrador do sistema
  Eu quero aprovar orçamentos criados
  Para que o cliente possa prosseguir com o pagamento

  Cenário: Aprovar orçamento pendente com sucesso
    Dado que existe um orçamento com status "PENDING_APPROVAL"
    Quando eu solicito a aprovação do orçamento
    Então o orçamento deve ter status "APPROVED"
    E o evento "BudgetApprovedEvent" deve ser publicado
    E a data de atualização deve ser recente

  Cenário: Rejeitar aprovação de orçamento já aprovado
    Dado que existe um orçamento com status "APPROVED"
    Quando eu solicito a aprovação do orçamento
    Então devo receber um erro de "InvalidDataException"
    E a mensagem de erro deve conter "not pending approval"

  Cenário: Aprovar orçamento que não existe
    Dado que não existe um orçamento com ID "NON-EXISTENT"
    Quando eu solicito a aprovação do orçamento "NON-EXISTENT"
    Então devo receber um erro de "NotFoundException"
    E a mensagem de erro deve conter "Budget not found"
