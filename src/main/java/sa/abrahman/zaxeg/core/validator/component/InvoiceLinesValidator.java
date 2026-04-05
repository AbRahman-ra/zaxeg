package sa.abrahman.zaxeg.core.validator.component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.factory.bean.ValidatorBeansRegistry;
import sa.abrahman.zaxeg.core.helper.*;
import sa.abrahman.zaxeg.core.model.invoice.metadata.InvoiceTypeTransactions;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.payload.LinesPayload;
import sa.abrahman.zaxeg.core.port.in.payload.MetadataPayload;
import sa.abrahman.zaxeg.core.port.in.payload.PayloadCommons;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.validator.rule.KsaRules;
import sa.abrahman.zaxeg.core.validator.rule.UblRules;

@Service(ValidatorBeansRegistry.LINES_VALIDATOR)
public class InvoiceLinesValidator implements InvoiceValidator {
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
        // initialization and sanity check
        List<LinesPayload.InvoiceLine> lines = payload.getLines().getInvoiceLines();
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;

        // // helpers
        // Predicate<PayloadCommons.AllowanceOrCharge> hasChargeIndicator = ac -> Optional.ofNullable(ac).map(a -> a.isCharge()).isPresent();
        // Predicate<LinesPayload.Quantity> hasQuantity = q -> Optional.ofNullable(q).map(qt -> qt.getCount()).isPresent();

        // // rules
        CollectionValueValidator.check(lines, f).notEmpty(UblRules.BR_16)
                .allMatch(li -> li != null && li.getId() != null && !li.getId().isBlank(), UblRules.BR_21)
                .allMatch(li -> li.getQuantity().getCount() != null, UblRules.BR_22)
                .allMatch(li -> li.getQuantity().getCount().compareTo(BigDecimal.ZERO) >= 0, KsaRules.BR_KSA_F_04)
                .allMatch(li -> li.getNetAmount() != null && li.getNetAmount().getValue() != null, UblRules.BR_24)
                .allMatch(li -> li.getNetAmount().getValue().compareTo(BigDecimal.ZERO) >= 0, KsaRules.BR_KSA_F_04)
                .allMatch(li -> lineNetAmountIntegrityPredicate().test(li), KsaRules.BR_KSA_EN16931_11)
                .allMatch(allowanceChargeAmountExists(), UblRules.BR_41_43)
                .allMatch(lineAllowanceChargesAmountIntegrityPredicate(), KsaRules.BR_KSA_EN16931_03)
                .allMatch(baseAmountIntegrityWhenPercentageIsProvided(), KsaRules.BR_KSA_EN16931_04_05)
                ;
    }

    private Predicate<LinesPayload.InvoiceLine> lineNetAmountIntegrityPredicate() {
        return li -> {
            // no need for null check because it's done in earlier rule
            BigDecimal providedNetAmount = li.getNetAmount().getValue();
            BigDecimal quantity = li.getQuantity().getCount();
            BigDecimal itemPrice = li.getPrice().getAmount().getValue();
            BigDecimal baseQty = Optional.ofNullable(li.getPrice().getQuantity()).map(q -> q.getCount())
                    .orElse(BigDecimal.ONE);

            BigDecimal sumCharges = Optional.of(li.getAllowanceCharges()).orElse(List.of()).stream()
                    .filter(ac -> ac.isCharge()).map(ac -> Optional.ofNullable(ac.getAmount())
                            .map(PayloadCommons.Amount::getValue).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumAllowances = Optional.of(li.getAllowanceCharges()).orElse(List.of()).stream()
                    .filter(ac -> !ac.isCharge()).map(ac -> Optional.ofNullable(ac.getAmount())
                            .map(PayloadCommons.Amount::getValue).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // calculate
            BigDecimal expectedNetAmount = itemPrice.divide(baseQty, 4, RoundingMode.HALF_UP).multiply(quantity)
                    .add(sumCharges).subtract(sumAllowances).setScale(2, RoundingMode.HALF_UP);

            return providedNetAmount.setScale(2, RoundingMode.HALF_UP).compareTo(expectedNetAmount) == 0;
        };
    }

    private Predicate<LinesPayload.InvoiceLine> lineAllowanceChargesAmountIntegrityPredicate() {
        return li -> {
            Predicate<PayloadCommons.AllowanceOrCharge> integrity = ac -> {
                if (ac.getBaseAmount() != null && ac.getPercentage() != null) {
                    BigDecimal expected = ac.getBaseAmount().getValue()
                            .multiply(ac.getPercentage())
                            .divide(new BigDecimal(100))
                            .setScale(2, RoundingMode.HALF_UP);
                    return expected.compareTo(ac.getAmount().getValue()) == 0;
                }
                return true;
            };
            return li.getAllowanceCharges().stream().allMatch(integrity::test);
        };
    }

    private Predicate<LinesPayload.InvoiceLine> baseAmountIntegrityWhenPercentageIsProvided() {
        return li -> {
            Predicate<PayloadCommons.AllowanceOrCharge> integrity = ac -> ac.getPercentage() == null
                    || (ac.getPercentage() != null && ac.getBaseAmount() != null && ac.getBaseAmount().getValue() != null);
            return li.getAllowanceCharges().stream().allMatch(integrity::test);
        };
    }

    private Predicate<LinesPayload.InvoiceLine> allowanceChargeAmountExists() {
        return li -> {
            Predicate<PayloadCommons.AllowanceOrCharge> integrity = ac -> ac.getAmount() != null && ac.getAmount().getValue() != null;
            return li.getAllowanceCharges().stream().allMatch(integrity::test);
        };
    }
}
