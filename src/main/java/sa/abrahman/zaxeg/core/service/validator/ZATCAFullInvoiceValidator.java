package sa.abrahman.zaxeg.core.service.validator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;

@Service(InvoiceValidatorBeanNameResolver.FULL_INVOICE_VALIDATOR)
public class ZATCAFullInvoiceValidator implements InvoiceValidator {
    private final InvoiceValidator metadata;
    private final InvoiceValidator parties;
    private final InvoiceValidator lines;
    private final InvoiceValidator checkout;

    public ZATCAFullInvoiceValidator(
            @Qualifier(InvoiceValidatorBeanNameResolver.METADATA_VALIDATOR) InvoiceValidator metadata,
            @Qualifier(InvoiceValidatorBeanNameResolver.PARTIES_VALIDATOR) InvoiceValidator parties,
            @Qualifier(InvoiceValidatorBeanNameResolver.LINES_VALIDATOR) InvoiceValidator lines,
            @Qualifier(InvoiceValidatorBeanNameResolver.CHECKOUT_VALIDATOR) InvoiceValidator checkout) {

        this.metadata = metadata;
        this.parties = parties;
        this.lines = lines;
        this.checkout = checkout;
    }

    @Override
    public void validate(InvoiceGenerationPayload payload) {
        metadata.validate(payload);
        parties.validate(payload);
        lines.validate(payload);
        checkout.validate(payload);
    }
}
