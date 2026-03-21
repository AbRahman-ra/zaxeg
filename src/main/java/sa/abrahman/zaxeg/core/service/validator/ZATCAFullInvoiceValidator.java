package sa.abrahman.zaxeg.core.service.validator;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;

@Service(InvoiceValidatorBeanNameResolver.FULL_INVOICE_VALIDATOR)
public class ZATCAFullInvoiceValidator implements InvoiceValidator {
    private final InvoiceValidator business;
    private final InvoiceValidator ksa;

    public ZATCAFullInvoiceValidator(
            @Qualifier(InvoiceValidatorBeanNameResolver.GENERIC_BUSINESS_VALIDATOR) InvoiceValidator business,
            @Qualifier(InvoiceValidatorBeanNameResolver.GENERIC_KSA_VALIDATOR) InvoiceValidator ksa) {
        this.business = business;
        this.ksa = ksa;
    }

    @Override
    public void validate(InvoiceGenerationPayload payload) {
        business.validate(payload);
        ksa.validate(payload);
    }
}
