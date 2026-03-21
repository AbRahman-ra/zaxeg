package sa.abrahman.zaxeg.core.service.validator;

import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationCommand;

@Component("STANDARD_INVOICE_VALIDATOR")
public abstract class AbstractZATCAInvoiceValidator extends AbstractUBLInvoiceValidator {

    @Override
    protected void validateSpecificRules(InvoiceGenerationCommand invoice) {
        // TODO Auto-generated method stub

    }

}
