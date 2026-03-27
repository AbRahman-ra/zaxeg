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
    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // TODO Auto-generated method stub
        // initialization and sanity check
        Function<String, InvoiceRuleViolationException> f = InvoiceRuleViolationException::new;
        List<LinesPayload.InvoiceLine> lines = Optional.ofNullable(payload).map(InvoiceGenerationPayload::getLines)
                .map(l -> l.getInvoiceLines()).orElse(List.of());

        // helpers
        Predicate<PayloadCommons.AllowanceOrCharge> hasChargeIndicator = ac -> Optional.ofNullable(ac).map(a -> a.isCharge()).isPresent();
        Predicate<LinesPayload.Quantity> hasQuantity = q -> Optional.ofNullable(q).map(qt -> qt.getCount()).isPresent();
        Predicate<LinesPayload.InvoiceLine> netAmountCalculationChecker = li -> {
            // no need for null check because it's done in earlier rule
            BigDecimal providedNetAmount = li.getNetAmount().getValue();
            BigDecimal quantity = li.getQuantity().getCount();
            BigDecimal itemPrice = li.getPrice().getAmount().getValue();
            BigDecimal baseQty = Optional.ofNullable(li.getPrice().getQuantity()).map(q -> q.getCount()).orElse(BigDecimal.ONE);

            BigDecimal sumCharges = Optional.of(li.getAllowanceCharges())
                    .orElse(List.of())
                    .stream()
                    .filter(ac -> ac.isCharge())
                    .map(ac -> Optional.ofNullable(ac.getAmount())
                    .map(PayloadCommons.Amount::getValue)
                    .orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumAllowances = Optional.of(li.getAllowanceCharges())
                    .orElse(List.of())
                    .stream()
                    .filter(ac -> !ac.isCharge())
                    .map(ac -> Optional.ofNullable(ac.getAmount())
                    .map(PayloadCommons.Amount::getValue)
                    .orElse(BigDecimal.ZERO))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // calculate
            BigDecimal expectedNetAmount = itemPrice
                    .divide(baseQty, 4, RoundingMode.HALF_UP)
                    .multiply(quantity)
                    .add(sumCharges)
                    .subtract(sumAllowances)
                    .setScale(2, RoundingMode.HALF_UP);

            return providedNetAmount.setScale(2, RoundingMode.HALF_UP).compareTo(expectedNetAmount) == 0;
        };

        // rules
        CollectionValueValidator.check(lines, f).notEmpty(InvoiceValidationRule.BR_16)
                .allMatch(li -> Optional.ofNullable(li).map(l -> l.getId()).isPresent(), InvoiceValidationRule.BR_21)
                .allMatch(li -> Optional.ofNullable(li).map(l -> hasQuantity.test(l.getQuantity())).get(), InvoiceValidationRule.BR_22)
                .allMatch(li -> Optional.ofNullable(li).map(l -> l.getNetAmount()).map(a -> a.getValue()).isPresent(), InvoiceValidationRule.BR_24)
                .allMatch(li -> Optional.ofNullable(li).map(l -> l.getAllowanceCharges()).orElse(List.of()).stream().allMatch(hasChargeIndicator), InvoiceValidationRule.BR_KSA_F_02)
                .allMatch(li -> Optional.ofNullable(li).map(l -> netAmountCalculationChecker.test(li)).get(), InvoiceValidationRule.BR_KSA_EN16391_03);
                ;
    }
}
