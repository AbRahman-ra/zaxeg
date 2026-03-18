package sa.abrahman.zaxeg.core.port.in;

import sa.abrahman.zaxeg.core.model.invoice.Invoice;

public interface InvoiceGenerator {
    String toXML(Invoice invoice);
}
