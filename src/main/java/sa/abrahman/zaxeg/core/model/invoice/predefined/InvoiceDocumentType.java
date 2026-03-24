package sa.abrahman.zaxeg.core.model.invoice.predefined;

/**
 * @see <a href=
 *      "https://unece.org/fileadmin/DAM/trade/untdid/d16b/tred/tred1001.htm">UN/EDIFACT
 *      1001: Document name code</a> for the full list
 */
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
