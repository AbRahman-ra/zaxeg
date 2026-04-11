package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

import java.util.Map;
import java.util.Map.Entry;

import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.domain.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.constant.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

@Component
@Order(ValidatorsOrderRegistry.METADATA_BILLING_REF_ID_EXISTS)
public class ValidateBillingReferenceIdExistsIfNeeded implements InvoiceRuleValidator {
    private static final String PATH = "metadata.billingReference.id";
    private static final String RULE = KsaRules.BR_KSA_56;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        boolean isCreditNote = data.getMetadata().getInvoiceDocumentType() == InvoiceDocumentType.CREDIT_NOTE;
        boolean isDebitNote = data.getMetadata().getInvoiceDocumentType() == InvoiceDocumentType.DEBIT_NOTE;
        var billingRef = data.getMetadata().getBillingReference();

        if ((isCreditNote || isDebitNote)
                && (billingRef == null || billingRef.getId() == null || billingRef.getId().isBlank())) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
