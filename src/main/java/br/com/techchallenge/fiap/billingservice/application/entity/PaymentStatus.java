package br.com.techchallenge.fiap.billingservice.application.entity;

/**
 * Payment status entity with business rules and transitions.
 */
public record PaymentStatus(PaymentStatusEnum status) {

    public static PaymentStatus pending() {
        return new PaymentStatus(PaymentStatusEnum.PENDING);
    }

    public static PaymentStatus processing() {
        return new PaymentStatus(PaymentStatusEnum.PROCESSING);
    }

    public static PaymentStatus paid() {
        return new PaymentStatus(PaymentStatusEnum.PAID);
    }

    public static PaymentStatus failed() {
        return new PaymentStatus(PaymentStatusEnum.FAILED);
    }

    public static PaymentStatus refunded() {
        return new PaymentStatus(PaymentStatusEnum.REFUNDED);
    }

    public boolean isPending() {
        return status == PaymentStatusEnum.PENDING;
    }

    public boolean isProcessing() {
        return status == PaymentStatusEnum.PROCESSING;
    }

    public boolean isPaid() {
        return status == PaymentStatusEnum.PAID;
    }

    public boolean isFailed() {
        return status == PaymentStatusEnum.FAILED;
    }

    public boolean isRefunded() {
        return status == PaymentStatusEnum.REFUNDED;
    }

    public String name() {
        return status.name();
    }
}
