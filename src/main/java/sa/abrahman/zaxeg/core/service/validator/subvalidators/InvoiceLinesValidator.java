package sa.abrahman.zaxeg.core.service.validator.subvalidators;

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
import sa.abrahman.zaxeg.core.helper.*;
import sa.abrahman.zaxeg.core.model.invoice.metadata.InvoiceTypeTransactions;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxExemptionCode;
import sa.abrahman.zaxeg.core.port.in.payload.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.payload.LinesPayload;
import sa.abrahman.zaxeg.core.port.in.payload.MetadataPayload;
import sa.abrahman.zaxeg.core.port.in.payload.PayloadCommons;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidationRule;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;

@Service(InvoiceValidatorBeanNameResolver.LINES_VALIDATOR)
public class InvoiceLinesValidator implements InvoiceValidator {
    private final String PREDICATE_BR_21 = "br21";
    private final String PREDICATE_BR_22 = "br22";
    private final String PREDICATE_BR_24 = "br24";
    private final String PREDICATE_BR_KSA_EN16391_11 = "brKsaEn16391_11";
    private final String PREDICATE_BR_41 = "br41";
    private final String PREDICATE_AMOUNT_NOT_NULL = "br41";
    private final String PREDICATE_BR_KSA_EN16391_03 = "brKsaEn16391_03";

    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
        // initialization and sanity check
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;
        LinesPayload lines = Optional.ofNullable(payload).map(InvoiceGenerationPayload::getLines).orElse(null);
        if (lines == null)
            throw f.apply("Error Parsing Invoice Lines");

        // helpers


        Predicate<PayloadCommons.AllowanceOrCharge> ruleEn16391_04_05Checker = ac -> {
            BigDecimal baseAmount = Optional.ofNullable(ac.getBaseAmount()).map(PayloadCommons.Amount::getValue).orElse(null);
            return (ac.getPercentage() == null && baseAmount == null) || (ac.getPercentage() != null && baseAmount != null);
        };
        Map<String, Predicate<LinesPayload.InvoiceLine>> predicates = linePredicates();

        // rules
        CollectionValueValidator.check(lines.getInvoiceLines(), f).notEmpty(InvoiceValidationRule.BR_16)
                .allMatch(predicates.get(PREDICATE_BR_21), InvoiceValidationRule.BR_21)
                .allMatch(predicates.get(PREDICATE_BR_22), InvoiceValidationRule.BR_22)
                .allMatch(predicates.get(PREDICATE_BR_24), InvoiceValidationRule.BR_24)
                .allMatch(predicates.get(PREDICATE_BR_KSA_EN16391_11), InvoiceValidationRule.BR_KSA_EN16391_11)
                .allMatch(predicates.get(PREDICATE_BR_41), InvoiceValidationRule.BR_41)
                .allMatch(l -> predicates.get(PREDICATE_BR_KSA_EN16391_03).test(l), InvoiceValidationRule.BR_KSA_EN16391_03)
                .allMatch(l -> l.getAllowanceCharges() != null && l.getAllowanceCharges().stream().allMatch(ruleEn16391_03Checker), InvoiceValidationRule.BR_KSA_EN16391_03)
                .allMatch(l -> l.getAllowanceCharges() != null && l.getAllowanceCharges().stream().allMatch(ruleEn16391_04_05Checker), InvoiceValidationRule.BR_KSA_EN16391_04_05)
                // .allMatch(l -> l.getVatLineAmount() != null && l.getVatLineAmount()., null)
                ;
    }

    private Map<String, Predicate<LinesPayload.InvoiceLine>> linePredicates() {
        Map<String, Predicate<LinesPayload.Quantity>> qtyPredicates = qtyPredicates();
        Map<String, Predicate<PayloadCommons.Amount>> amountPredicates = amountPredicates();
        Map<String, Predicate<PayloadCommons.AllowanceOrCharge>> allowanceChargePredicates = allowanceChargePredicates();

        Predicate<LinesPayload.InvoiceLine> br21 = l -> Optional.ofNullable(l.getId()).orElse("").isBlank();
        Predicate<LinesPayload.InvoiceLine> br22 = l -> qtyPredicates.get(PREDICATE_BR_22).test(l.getQuantity());
        Predicate<LinesPayload.InvoiceLine> br24 = l -> amountPredicates.get(PREDICATE_BR_24).test(l.getNetAmount());
        Predicate<LinesPayload.InvoiceLine> brKsaEn16391N11 = brKsaEn16391_11Predicate();
        Predicate<LinesPayload.InvoiceLine> brKsaEn16391N03 = l -> allowanceChargePredicates.get(PREDICATE_BR_KSA_EN16391_03).test(l.getAllowanceCharges());
        Predicate<LinesPayload.InvoiceLine> br41 = l -> amountPredicates.get(PREDICATE_AMOUNT_NOT_NULL).test(l.getNetAmount());

        Map<String, Predicate<LinesPayload.InvoiceLine>> cache = new HashMap<>();
        cache.put(PREDICATE_BR_21, br21);
        cache.put(PREDICATE_BR_22, br22);
        cache.put(PREDICATE_BR_24, br24);
        cache.put(PREDICATE_BR_KSA_EN16391_11, brKsaEn16391N11);
        cache.put(PREDICATE_BR_41, br41);
        cache.put(PREDICATE_BR_KSA_EN16391_03, brKsaEn16391N03);
        return cache;
    }

    // Helpers
    private Map<String, Predicate<LinesPayload.Quantity>> qtyPredicates() {
        Predicate<LinesPayload.Quantity> br22 = q -> Optional.ofNullable(q).map(qt -> qt.getCount()).isPresent();

        Map<String, Predicate<LinesPayload.Quantity>> cache = new HashMap<>();
        cache.put(PREDICATE_BR_22, br22);
        return cache;
    }

    private Map<String, Predicate<PayloadCommons.Amount>> amountPredicates() {
        Predicate<PayloadCommons.Amount> br24 = a -> a != null;
        Predicate<PayloadCommons.Amount> amountNotNull = a -> Optional.ofNullable(a).map(am -> am.getValue()).isPresent();

        Map<String, Predicate<PayloadCommons.Amount>> cache = new HashMap<>();
        cache.put(PREDICATE_BR_24, br24);
        cache.put(PREDICATE_AMOUNT_NOT_NULL, amountNotNull);
        return cache;
    }

    private Map<String, Predicate<PayloadCommons.AllowanceOrCharge>> allowanceChargePredicates() {
        Map<String, Predicate<PayloadCommons.Amount>> amountPredicates = amountPredicates();
        Predicate<PayloadCommons.AllowanceOrCharge> br41 = ac -> amountPredicates.get(PREDICATE_AMOUNT_NOT_NULL).test(ac.getAmount());
        Predicate<PayloadCommons.AllowanceOrCharge> brEn16391_03 = ac -> {
            boolean hasAmount = amountPredicates.get(PREDICATE_AMOUNT_NOT_NULL).test(ac.getAmount());
            boolean hasBaseAmount = amountPredicates.get(PREDICATE_AMOUNT_NOT_NULL).test(ac.getBaseAmount());
            BigDecimal percentage = ac.getPercentage();
            if (percentage != null && hasBaseAmount && hasAmount) {
                BigDecimal amount = ac.getAmount().getValue();
                BigDecimal baseAmount = ac.getBaseAmount().getValue();
                BigDecimal expectedAmount = baseAmount.multiply(percentage).divide(new BigDecimal(100));
                return amount.setScale(2, RoundingMode.HALF_UP).compareTo(expectedAmount) == 0;
            }
            return true;
        };

        Map<String, Predicate<PayloadCommons.AllowanceOrCharge>> cache = new HashMap<>();
        cache.put(PREDICATE_AMOUNT_NOT_NULL, br41);
        cache.put(PREDICATE_BR_KSA_EN16391_03, brEn16391_03);
        return cache;
    }

    // Complex Predicates
    Predicate<LinesPayload.InvoiceLine> brKsaEn16391_11Predicate() {
        return l -> {
            BigDecimal providedNetAmount = Optional.ofNullable(l.getNetAmount()).map(PayloadCommons.Amount::getValue).orElse(BigDecimal.ZERO);
            BigDecimal invoicedQuantity = Optional.ofNullable(l.getQuantity()).map(LinesPayload.Quantity::getCount).orElse(BigDecimal.ZERO);
            BigDecimal itemNetPrice = Optional.ofNullable(l.getPrice()).map(LinesPayload.InvoiceLinePrice::getAmount).map(PayloadCommons.Amount::getValue).orElse(BigDecimal.ZERO);
            BigDecimal itemPriceBaseQty = Optional.ofNullable(l.getPrice())
                    .map(LinesPayload.InvoiceLinePrice::getQuantity).map(LinesPayload.Quantity::getCount)
                    .filter(qty -> qty.compareTo(BigDecimal.ZERO) != 0) // Prevent division by zero
                    .orElse(BigDecimal.ONE); // BT-149 defaults to 1 if missing or zero

            List<PayloadCommons.AllowanceOrCharge> lineAllowancesOrCharges = Optional.ofNullable(l.getAllowanceCharges()).orElse(List.of());

            BigDecimal sumCharges = lineAllowancesOrCharges
                    .stream().filter(PayloadCommons.AllowanceOrCharge::isCharge).map(ac -> Optional
                            .ofNullable(ac.getAmount()).map(PayloadCommons.Amount::getValue).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumAllowances = lineAllowancesOrCharges
                    .stream().filter(ac -> !ac.isCharge()).map(ac -> Optional.ofNullable(ac.getAmount())
                            .map(PayloadCommons.Amount::getValue).orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // (ItemNetPrice / ItemPriceBaseQty) * InvoicedQuantity
            // scale of 4 for precision before multiplying
            BigDecimal baseCalculatedAmount = itemNetPrice.divide(itemPriceBaseQty, 4, RoundingMode.HALF_UP).multiply(invoicedQuantity);

            // Final Calculation: Base + Charges - Allowances
            BigDecimal calculatedNetAmount = baseCalculatedAmount.add(sumCharges).subtract(sumAllowances).setScale(2, RoundingMode.HALF_UP);

            // assert
            return providedNetAmount.setScale(2, RoundingMode.HALF_UP).compareTo(calculatedNetAmount) == 0;
        };
    }
}
