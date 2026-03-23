package sa.abrahman.zaxeg.core.port.out;

import sa.abrahman.zaxeg.core.model.invoice.old.Invoice;

public interface InvoiceFormatter {
    String format(Invoice invoice);
}
