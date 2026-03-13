package sa.abrahman.zaxeg.core.service;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.model.Invoice;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;

@Service
public class DummyInvoiceGenerator implements InvoiceGenerator {

    @Override
    public String toXML(Invoice invoice) {
        return String.format("<Invoice><ID>%s</ID><Buyer>%s</Buyer><Total>%s</Total></Invoice>",
                invoice.getInvoiceNumber(),
                invoice.getBuyerName(),
                invoice.getTotalAmount());
    }

}
