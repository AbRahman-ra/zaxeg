package sa.abrahman.zaxeg.core.model.invoice.old.meta;

/**
 * @deprecated
 */
@Deprecated
public enum InvoiceSubtype {
    STANDARD, SIMPLIFIED;

    public String code() {
        return switch (this) {
            case STANDARD -> "0100000";
            case SIMPLIFIED -> "0200000";
        };
    }
}
