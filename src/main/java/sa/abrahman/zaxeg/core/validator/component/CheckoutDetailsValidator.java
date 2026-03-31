package sa.abrahman.zaxeg.core.validator.component;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.factory.bean.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;

@Service(ValidatorBeansRegistry.CHECKOUT_VALIDATOR)
public class CheckoutDetailsValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
    }
}
