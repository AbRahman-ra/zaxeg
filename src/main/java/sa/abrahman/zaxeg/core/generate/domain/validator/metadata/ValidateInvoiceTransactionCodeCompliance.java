package sa.abrahman.zaxeg.core.generate.domain.validator.metadata;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import sa.abrahman.zaxeg.core.generate.config.ValidatorsOrderRegistry;
import sa.abrahman.zaxeg.core.generate.domain.contract.InvoiceRuleValidator;
import sa.abrahman.zaxeg.core.generate.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.shared.dto.FailableResult;
import sa.abrahman.zaxeg.shared.constant.rule.KsaRules;

@Component
@Order(ValidatorsOrderRegistry.METADATA_TRANSACTION_CODE_COMPLIANT)
public class ValidateInvoiceTransactionCodeCompliance implements InvoiceRuleValidator {
    private static final String PATH = "metadata.invoiceTransactionCode[selfbilling_and_export]";
    private static final String RULE = KsaRules.BR_KSA_07;

    @Override
    public FailableResult<Entry<String, String>> run(InvoiceGenerationPayload data) {
        var itt = data.getMetadata().getInvoiceTypeTransactions();

        if (itt.isExports() && itt.isSelfBilled()) {
            return FailableResult.failed(Map.entry(PATH, RULE));
        }

        return FailableResult.okay();
    }
}
