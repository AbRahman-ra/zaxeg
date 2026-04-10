package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

import java.time.LocalDate;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.constant.field.InvoiceSubtype;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

@Component
@Order(ValidatorsOrderRegistry.METADATA_SUPPLY_AND_END_DATES)
public class ValidateSupplyAndSupplyEndDatesExistIfneeded implements InvoiceRuleValidator {
    private static final String PATH = "metadata.supplyDate,supplyEndDate";
    private static final String RULE = KsaRules.BR_KSA_72;

    @Override
    public FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        boolean isSimplifiedInvoice = data.getMetadata().getInvoiceTypeTransactions().getSubtype() == InvoiceSubtype.SIMPLIFIED;
        boolean isSummaryInvoice = data.getMetadata().getInvoiceTypeTransactions().isSummary();
        LocalDate supplyDate = data.getMetadata().getSupplyDate();
        LocalDate supplyEndDate = data.getMetadata().getSupplyEndDate();

        if (isSimplifiedInvoice && isSummaryInvoice && (supplyDate == null || supplyEndDate == null)) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
