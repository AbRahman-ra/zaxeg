package sa.abrahman.zaxeg.core.model;

public enum InvoiceSubtype {
    STANDARD, SIMPLIFIED;

    public String code() {
        return switch (this) {
            case STANDARD -> "0100000";
            case SIMPLIFIED -> "0200000";
        };
    }
}
