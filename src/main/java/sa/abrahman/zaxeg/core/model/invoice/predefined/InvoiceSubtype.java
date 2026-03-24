package sa.abrahman.zaxeg.core.model.invoice.predefined;

public enum InvoiceSubtype {
    STANDARD, SIMPLIFIED;

    public String code() {
        return switch (this) {
            case STANDARD -> "01";
            case SIMPLIFIED -> "02";
        };
    }
}
