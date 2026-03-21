package sa.abrahman.zaxeg.core.service.validator;

import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;

@Component("SIMPLIFIED_INVOICE_VALIDATOR")
public class SimplifiedInvoiceValidator implements InvoiceValidator {

    @Override
    public void validate(InvoiceGenerationPayload invoice) {
        // // BR-KSA-25: Issue Date cannot be in the future
        // DateValueValidator.check(invoice.getIssueDate(),
        // InvoiceRuleViolationException::new)
        // .notInFuture("BR-KSA-25: Invoice Issue Date cannot be in the future.");

        // // Buyer info is optional in simplified notes
        // String buyerVat =
        // Optional.ofNullable(invoice.getBuyer()).map(BusinessParty::getVatNumber).orElse(null);
        // if (buyerVat != null) {
        // StringValueValidator.check(buyerVat, InvoiceRuleViolationException::new)
        // .hasLength(15, "BR-KSA-14: If provided, VAT number must be exactly 15
        // digits.")
        // .startsAndEndsWith("3", "BR-KSA-14: VAT number must be starting and ending
        // with '3'.");
        // }
    }

}
