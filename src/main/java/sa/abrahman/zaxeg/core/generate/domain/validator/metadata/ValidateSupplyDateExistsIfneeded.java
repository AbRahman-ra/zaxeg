package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

import java.time.LocalDate;
import java.util.Map;
import java.util.Map.Entry;

import org.jspecify.annotations.NonNull;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.domain.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.constant.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.generate.domain.constant.InvoiceSubtype;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

@Component
@Order(ValidatorsOrderRegistry.METADATA_SUPPLY_DATE)
public class ValidateSupplyDateExistsIfneeded implements InvoiceRuleValidator {
    private static final String PATH = "metadata.supplyDate";
    private static final String RULE = KsaRules.BR_KSA_15;

    @Override
    public @NonNull FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        boolean isStandardInvoice = data.getMetadata().getInvoiceTypeTransactions()
                .getSubtype() == InvoiceSubtype.STANDARD;
        boolean isTaxInvoice = data.getMetadata().getInvoiceDocumentType() == InvoiceDocumentType.TAX_INVOICE;
        LocalDate supplyDate = data.getMetadata().getSupplyDate();

        if (isStandardInvoice && isTaxInvoice && supplyDate == null) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
