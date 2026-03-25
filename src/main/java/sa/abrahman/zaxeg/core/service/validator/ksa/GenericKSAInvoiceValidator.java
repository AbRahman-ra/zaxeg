package sa.abrahman.zaxeg.core.service.validator.ksa;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service(InvoiceValidatorBeanNameResolver.GENERIC_KSA_VALIDATOR)
public class GenericKSAInvoiceValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
    }
}
