package sa.abrahman.zaxeg.core.service.generator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.factory.bean.InvoiceValidatorBeanNameResolver;
import sa.abrahman.zaxeg.core.factory.model.InvoiceFactory;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;

@Service
public class ZatcaInvoiceGenerator implements InvoiceGenerator {
    private final InvoiceValidator validator;
    private final InvoiceFormatter formatter;

    public ZatcaInvoiceGenerator(
            @Qualifier(InvoiceValidatorBeanNameResolver.FULL_INVOICE_VALIDATOR) InvoiceValidator validator,
            InvoiceFormatter formatter) {
        this.formatter = formatter;
        this.validator = validator;
    }

    @Override
    public String handle(InvoiceGenerationPayload payload) {
        validator.validate(payload);
        Invoice invoice = InvoiceFactory.from(payload);
        return formatter.format(invoice);
    }
}
