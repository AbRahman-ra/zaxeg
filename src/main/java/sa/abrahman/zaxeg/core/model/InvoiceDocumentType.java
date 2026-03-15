package sa.abrahman.zaxeg.core.model;

public enum InvoiceDocumentType {
    INVOICE, DEBIT_NOTE, CREDIT_NOTE;

    public String code() {
        return switch (this) {
            case INVOICE -> "381";
            case DEBIT_NOTE -> "383";
            case CREDIT_NOTE -> "388";
        };
    }
}
