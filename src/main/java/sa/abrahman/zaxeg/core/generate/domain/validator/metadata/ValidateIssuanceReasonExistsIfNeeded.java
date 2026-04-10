package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

@Component
@Order(ValidatorsOrderRegistry.METADATA_ISSUANCE_REASONS)
public class ValidateIssuanceReasonExistsIfNeeded implements InvoiceRuleValidator {
    private static final String PATH = "metadata.creditOrDebitNoteIssuanceReasons";
    private static final String RULE = KsaRules.BR_KSA_17;

    @Override
    public FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        boolean isCreditNote = data.getMetadata().getInvoiceDocumentType() == InvoiceDocumentType.CREDIT_NOTE;
        boolean isDebitNote = data.getMetadata().getInvoiceDocumentType() == InvoiceDocumentType.DEBIT_NOTE;
        var reasons = data.getMetadata().getCreditOrDebitNoteIssuanceReasons();

        if ((isCreditNote || isDebitNote) && (reasons == null || reasons.isEmpty())) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
