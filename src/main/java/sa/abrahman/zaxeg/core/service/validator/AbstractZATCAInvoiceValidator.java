package sa.abrahman.zaxeg.core.service.validator;

import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.model.Invoice;

@Component("STANDARD_INVOICE_VALIDATOR")
public abstract class AbstractZATCAInvoiceValidator extends AbstractUBLInvoiceValidatior {

    @Override
    protected void validateSpecificRules(Invoice invoice) {
        // TODO Auto-generated method stub
    }

}
