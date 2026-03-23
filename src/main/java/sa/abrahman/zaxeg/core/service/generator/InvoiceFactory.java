package sa.abrahman.zaxeg.core.service.generator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import lombok.experimental.UtilityClass;
import sa.abrahman.zaxeg.core.model.invoice.old.financial.DocumentFinancials;
import sa.abrahman.zaxeg.core.model.invoice.old.financial.InvoiceGlobalPayable;
import sa.abrahman.zaxeg.core.model.invoice.old.financial.InvoiceLine;
import sa.abrahman.zaxeg.core.model.invoice.old.meta.BillingReference;
import sa.abrahman.zaxeg.core.model.invoice.old.Invoice;
import sa.abrahman.zaxeg.core.model.invoice.old.party.Address;
import sa.abrahman.zaxeg.core.model.invoice.old.party.BusinessParty;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload.AddressPayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload.BillingReferencePayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload.FinancialsPayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload.InvoiceGlobalPayablePayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload.LinePayload;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload.PartyPayload;
import sa.abrahman.zaxeg.core.service.processor.FinancialsPayloadCalculator;

@UtilityClass
public class InvoiceFactory {
    public Invoice from(InvoiceGenerationPayload payload) {
        Function<BillingReferencePayload, BillingReference> billingReferenceMapper = b -> BillingReference.builder()
                .originalInvoiceNumber(b.getOriginalInvoiceNumber())
                .build();

        Function<AddressPayload, Address> addressMapper = ac -> Address.builder()
                .buildingNumber(ac.getBuildingNumber())
                .streetName(ac.getStreetName())
                .district(ac.getDistrict())
                .city(ac.getCity())
                .postalCode(ac.getPostalCode())
                .additionalNumber(ac.getAdditionalNumber())
                .build();

        Function<PartyPayload, BusinessParty> partyMapper = c -> BusinessParty.builder()
                .registrationName(c.getRegistrationName())
                .vatNumber(c.getVatNumber())
                .commercialRegistrationNumber(c.getCommercialRegistrationNumber())
                .address(Optional.ofNullable(c.getAddress()).map(addressMapper).orElse(null))
                .build();

        Function<LinePayload, InvoiceLine> lineMapper = l -> {
            BigDecimal netPrice = FinancialsPayloadCalculator.calculateNetAmount(l.getUnitPrice(), l.getQuantity(),
                    l.getLineDiscount());
            BigDecimal taxAmount = FinancialsPayloadCalculator.getTaxAmount(netPrice, l.getTaxCategory());
            return InvoiceLine.builder()
                    .identifier(l.getIdentifier())
                    .name(l.getName())
                    .measuringUnit(l.getMeasuringUnit())
                    .quantity(l.getQuantity())
                    .unitPrice(l.getUnitPrice())
                    .lineDiscount(l.getLineDiscount())
                    .taxCategory(l.getTaxCategory())
                    .exemptionReasonCode(l.getExemptionReasonCode())
                    .exemptionReasonText(l.getExemptionReasonText())
                    .netPrice(netPrice)
                    .taxAmount(taxAmount)
                    .lineTotalInclusive(netPrice.add(taxAmount).setScale(2, RoundingMode.HALF_UP))
                    .build();
        };

        Function<InvoiceGlobalPayablePayload, InvoiceGlobalPayable> allowancesAndOrChargesMapper = ac -> InvoiceGlobalPayable
                .builder()
                .isCharge(ac.isCharge())
                .reason(ac.getReason())
                .amount(ac.getAmount())
                .taxCategory(ac.getTaxCategory())
                .exemptionReasonCode(ac.getExemptionReasonCode())
                .exemptionReasonText(ac.getExemptionReasonText())
                .build();

        BusinessParty supplier = partyMapper.apply(payload.getSupplier());
        BusinessParty buyer = Optional.ofNullable(payload.getBuyer()).map(partyMapper).orElse(null);
        BillingReference breference = Optional.ofNullable(payload.getBillingReference()).map(billingReferenceMapper)
                .orElse(null);

        Function<FinancialsPayload, DocumentFinancials> financialsMapper = f -> DocumentFinancials.builder()
                .totalLineExtensionAmount(f.getTotalLineExtensionAmount())
                .taxExclusiveAmount(f.getTaxExclusiveAmount())
                .totalTaxAmount(f.getTotalTaxAmount())
                .totalAmountInclusive(f.getTotalAmountInclusive())
                .prepaidAmount(f.getPrepaidAmount())
                .payableAmount(f.getPayableAmount())
                .build();

        DocumentFinancials financials = Optional.ofNullable(payload.getFinancials()).map(financialsMapper)
                .orElseThrow(RuntimeException::new);
        return Invoice.builder()
                // metadata
                .invoiceNumber(payload.getInvoiceNumber())
                .invoiceUuid(Optional.ofNullable(payload.getInvoiceUuid()).orElse(UUID.randomUUID()))
                .issueDate(payload.getIssueDate())
                .issueTime(payload.getIssueTime())
                .supplyDate(payload.getSupplyDate())
                .invoiceSubtype(payload.getInvoiceSubtype())
                .invoiceDocumentType(payload.getInvoiceDocumentType())

                // payments and billing
                .billingReference(breference)
                .issuanceReason(payload.getIssuanceReason())
                .paymentMethod(payload.getPaymentMethod())
                .documentCurrency(payload.getDocumentCurrency())
                .taxCurrency(payload.getDocumentCurrency())

                // phase ii
                // .invoiceCounterValue(payload.getInvoiceCounterValue())
                // .previousInvoiceHash(payload.getPreviousInvoiceHash)
                // .cryptographicStamp(payload.getCryptographicStamp)
                // .generatedQrCode(payload.getGeneratedQrCode)

                // parties
                .supplier(supplier)
                .buyer(buyer)

                // lines & financials
                .lines(payload.getLines().stream().map(lineMapper).toList())
                .documentAllowanceCharges(
                        payload.getDocumentAllowancesAndOrCharges()
                                .stream()
                                .map(allowancesAndOrChargesMapper)
                                .toList())
                .financials(financials)
                .build();
    }
}
