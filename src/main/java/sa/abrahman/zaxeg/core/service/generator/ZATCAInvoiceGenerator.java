package sa.abrahman.zaxeg.core.service.generator;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorFactory;

@Service
@RequiredArgsConstructor
public class ZATCAInvoiceGenerator implements InvoiceGenerator {
    /**
     * Change bean qualifier to switch adapters
     * @implNote lombok's {@code @RequiredArgsConstructor}
     * doesn't support qualifier-based injection
     */
    private final InvoiceFormatter formatter;
    private final InvoiceValidatorFactory factory;

    @Override
    public String toXML(Invoice invoice) {
        InvoiceValidator validator = factory.getValidatorInstanceFor(invoice.getInvoiceSubtype());
        validator.validate(invoice);
        return formatter.format(invoice);
    }
}
