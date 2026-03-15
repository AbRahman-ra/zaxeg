package sa.abrahman.zaxeg.core.service.validator;

import java.util.Optional;

import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.helper.DateValueValidator;
import sa.abrahman.zaxeg.core.helper.StringValueValidator;
import sa.abrahman.zaxeg.core.model.BusinessParty;
import sa.abrahman.zaxeg.core.model.Invoice;

@Component("STANDARD_INVOICE_VALIDATOR")
public class StandardInvoiceValidator implements InvoiceValidator {

    @Override
    public void validate(Invoice invoice) {
        // BR-KSA-25: Issue Date cannot be in the future
        DateValueValidator.check(invoice.getIssueDate(), InvoiceRuleViolationException::new)
                .notInFuture("BR-KSA-25: Invoice Issue Date cannot be in the future.");

        // BR-KSA-14: Standard invoices require a Buyer
        Optional.ofNullable(invoice.getBuyer())
                .orElseThrow(() -> new InvoiceRuleViolationException("BR-KSA-14: Standard Invoices (0100000) must include Buyer Information."));
        
        // BR-KSA-08: Buyer Address is mandatory for Standard Invoices
        Optional.ofNullable(invoice.getBuyer())
                .map(BusinessParty::getAddress).orElseThrow(() -> new InvoiceRuleViolationException("BR-KSA-08: Standard Invoices require a complete Buyer Address."));

        // KSA rules strictly require VAT number or CRN for B2B buyers
        boolean hasVat = invoice.getBuyer().getVatNumber() != null && !invoice.getBuyer().getVatNumber().isBlank();
        boolean hasCrn = invoice.getBuyer().getCommercialRegistrationNumber() != null && !invoice.getBuyer().getCommercialRegistrationNumber().isBlank();
        
        if (!hasVat && !hasCrn) {
            throw new InvoiceRuleViolationException("Standard Invoices require either a Buyer VAT Number or a Commercial Registration Number (CRN).");
        }

        // if vat is present, vat number should be correctly formatted
        if (hasVat) {
            StringValueValidator.check(invoice.getBuyer().getVatNumber(), InvoiceRuleViolationException::new)
                    .hasLength(15, "BR-KSA-14: VAT number must be exactly 15 digits.")
                    .startsAndEndsWith("3", "BR-KSA-14: VAT number must start and end with '3'.");
        }
    }

}
