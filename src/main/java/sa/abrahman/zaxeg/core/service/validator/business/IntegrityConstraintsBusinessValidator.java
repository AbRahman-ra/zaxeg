package sa.abrahman.zaxeg.core.service.validator.business;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import sa.abrahman.zaxeg.core.exception.InvoiceRuleViolationException;
import sa.abrahman.zaxeg.core.helper.*;
import sa.abrahman.zaxeg.core.model.invoice.meta.InvoiceSubtype;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.contract.InvoiceValidator;
import sa.abrahman.zaxeg.core.service.validator.InvoiceValidatorBeanNameResolver;
import static sa.abrahman.zaxeg.core.service.validator.rules.BusinessIntegrityConstraintRule.*;

/**
 * Business Rules - Conditions (BR-CO): The conditions of each field and its
 * contents
 */
@Service(InvoiceValidatorBeanNameResolver.INTEGRITY_CONSTRAINTS_BUSINESS_VALIDATOR)
public class IntegrityConstraintsBusinessValidator implements InvoiceValidator {

    @Override
    public void validate(InvoiceGenerationPayload payload) {
        // initialization
        Function<String, RuntimeException> h = InvoiceRuleViolationException::new;

        // apply rules
        StringValueValidator.check(payload.getInvoiceNumber(), h).exists(BR_02);
        ObjectValueValidator.check(payload.getIssueDate(), h).exists(BR_03);
        ObjectValueValidator.check(payload.getInvoiceSubtype(), h).exists(BR_04);
        ObjectValueValidator.check(payload.getInvoiceDocumentType(), h).exists(BR_04);
        ObjectValueValidator.check(payload.getDocumentCurrency(), h).exists(BR_05);

        String seller = Optional.ofNullable(payload.getSupplier()).map(s -> s.getRegistrationName()).orElse(null);
        StringValueValidator.check(seller, h).exists(BR_06);

        InvoiceGenerationPayload.AddressPayload sellerAddress = Optional.ofNullable(payload.getSupplier()).map(s -> s.getAddress()).orElse(null);
        ObjectValueValidator.check(sellerAddress, h).exists(BR_08);

        Locale sellerCountry = Optional.ofNullable(sellerAddress).map(a -> a.getCountry()).orElse(null);
        ObjectValueValidator.check(sellerCountry, h).exists(BR_09);

        if (payload.getInvoiceSubtype() != InvoiceSubtype.SIMPLIFIED) {
            InvoiceGenerationPayload.AddressPayload buyerAddress = Optional.ofNullable(payload.getBuyer()).map(b -> b.getAddress()).orElse(null);
            ObjectValueValidator.check(buyerAddress, h).exists(BR_10);
        }

        BigDecimal totalAmountWithoutVat = Optional.ofNullable(payload.getFinancials()).map(f -> f.getTaxExclusiveAmount()).orElse(null);
        ObjectValueValidator.check(totalAmountWithoutVat, h).exists(BR_13);

        BigDecimal totalAmountWithVat = Optional.ofNullable(payload.getFinancials()).map(f -> f.getTotalAmountInclusive()).orElse(null);
        ObjectValueValidator.check(totalAmountWithVat, h).exists(BR_14);

        BigDecimal totalAmountDue = Optional.ofNullable(payload.getFinancials()).map(f -> f.getPayableAmount()).orElse(null);
        ObjectValueValidator.check(totalAmountDue, h).exists(BR_15);

        CollectionValueValidator.check(payload.getLines(), h)
                .exists(BR_16)
                .hasAtleast(1, BR_16)
                .allMatch(l -> l.getIdentifier() != null && !l.getIdentifier().isBlank(), BR_21)
                .allMatch(l -> l.getQuantity() != null, BR_22)
                .allMatch(l -> l.getNetAmount() != null, BR_24)
                .allMatch(l -> l.getName() != null && !l.getName().isBlank(), BR_25)
                .allMatch(l -> l.getUnitPrice() != null, BR_26)
                // .allMatch(l -> l.get, seller)
                ;

        if (payload.getDocumentAllowancesAndOrCharges() != null) {
            CollectionValueValidator.check(payload.getDocumentAllowancesAndOrCharges(), h)
                    .allMatch(ac -> ac.getAmount() != null, String.format("%s | %s", BR_31, BR_36))
                    .allMatch(ac -> ac.getTaxCategory() != null, String.format("%s | %s", BR_32, BR_37))
                    ;
        }
    }
}
