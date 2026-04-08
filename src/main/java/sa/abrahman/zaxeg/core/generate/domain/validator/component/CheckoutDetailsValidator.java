package sa.abrahman.zaxeg.core.generate.domain.validator.component;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.generate.domain.constant.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceValidator;

@Service(ValidatorBeansRegistry.CHECKOUT_VALIDATOR)
public class CheckoutDetailsValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
    }
}
