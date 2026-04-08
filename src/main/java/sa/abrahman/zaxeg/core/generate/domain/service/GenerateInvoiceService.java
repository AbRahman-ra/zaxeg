package sa.abrahman.zaxeg.core.generate.domain.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.generate.domain.constant.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.generate.domain.factory.InvoiceFactory;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.generate.port.in.InvoiceGenerator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.generate.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidator;

@Service
public class GenerateInvoiceService implements InvoiceGenerator {
    private final InvoiceValidator validator;
    private final InvoiceFormatter formatter;

    public GenerateInvoiceService(@Qualifier(ValidatorBeansRegistry.FULL_INVOICE_VALIDATOR) InvoiceValidator validator,
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
