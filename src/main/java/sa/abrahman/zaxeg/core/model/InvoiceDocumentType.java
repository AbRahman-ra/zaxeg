package sa.abrahman.zaxeg.core.model;

public enum InvoiceDocumentType {
    TAX_INVOICE, DEBIT_NOTE, CREDIT_NOTE, PREPAYMENT_INVOICE;

    public String code() {
        return switch (this) {
            case TAX_INVOICE -> "388";
            case DEBIT_NOTE -> "383";
            case CREDIT_NOTE -> "381";
            case PREPAYMENT_INVOICE -> "386";
        };
    }
}
