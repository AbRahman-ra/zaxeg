package sa.abrahman.zaxeg.infrastructure.out.adapters;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.model.invoice.financial.*;
import sa.abrahman.zaxeg.core.model.invoice.meta.*;
import sa.abrahman.zaxeg.core.model.invoice.party.BusinessParty;
import sa.abrahman.zaxeg.core.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.ZATCAInvoiceDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.*;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.line.*;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.tax.*;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.tax.TaxCategoryDto.TaxSchemeDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.meta.*;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party.*;
import sa.abrahman.zaxeg.infrastructure.out.exception.XMLGenerationException;

@Component
public class JacksonInvoiceFormatter implements InvoiceFormatter {
    private final XmlMapper mapper;

    public JacksonInvoiceFormatter() {
        this.mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @Override
    public String format(Invoice invoice) {
        // invoice subtype and document type
        InvoiceTypeCodeDto invoiceTypeCode = InvoiceTypeCodeDto.builder()
                .name(invoice.getInvoiceSubtype().code())
                .value(invoice.getInvoiceDocumentType().code())
                .build();

        // metadata
        BillingReferenceDto billingReference = Extractor.billingReference(invoice.getBillingReference());
        DeliveryDto delivery = Extractor.delivery(invoice.getSupplyDate());
        PaymentMeansDto paymentMeans = Extractor.paymentMeans(invoice.getPaymentMethod(), invoice.getIssuanceReason());

        // parties
        AccountingPartyDto supplier = Extractor.party(invoice.getSupplier());
        AccountingPartyDto buyer = Extractor.party(invoice.getBuyer());

        // financials
        String docCurrency = invoice.getDocumentCurrency().getCurrencyCode();
        String taxCurrency = invoice.getTaxCurrency().getCurrencyCode();
        LegalMonetaryTotalDto legalMonetaryTotal = Extractor.legalMonetaryTotal(invoice.getFinancials(), docCurrency);
        List<TaxTotalDto> taxtotals = Extractor.taxTotals(invoice.getLines(), invoice.getFinancials(), docCurrency,
                taxCurrency);
        List<InvoiceLineDto> invoiceLines = Extractor.invoiceLines(invoice.getLines(), docCurrency);

        // assemble
        ZATCAInvoiceDto ublDto = ZATCAInvoiceDto.builder()
                // metadata
                .id(invoice.getInvoiceNumber()) // Assuming your core model has this
                .uuid(Optional.ofNullable(invoice.getInvoiceUuid().toString()).orElse(null))
                .issueDate(invoice.getIssueDate().toString())
                .issueTime(invoice.getIssueTime().toString())
                .documentCurrencyCode(invoice.getDocumentCurrency().getCurrencyCode())
                .taxCurrencyCode(invoice.getTaxCurrency().getCurrencyCode())
                .invoiceTypeCode(invoiceTypeCode)
                .billingReference(billingReference)
                .delivery(delivery)

                // parties
                .supplierParty(supplier)
                .buyerParty(buyer)

                // financials
                .paymentMeans(paymentMeans)
                .taxTotals(taxtotals)
                .invoiceLines(invoiceLines)
                .legalMonetaryTotal(legalMonetaryTotal)
                .build();

        try {
            return mapper.writeValueAsString(ublDto);
        } catch (JsonProcessingException e) {
            throw new XMLGenerationException("Failed to serialize ZATCA XML", e);
        }
    }

    // ============ DEV HELPER CLASS ============
    static class Extractor {
        static LegalMonetaryTotalDto legalMonetaryTotal(DocumentFinancials df, String currency) {
            String lineExtension = df.getTotalLineExtensionAmount().toString();
            String taxExclusive = df.getTotalLineExtensionAmount().toString();
            String inclusive = df.getTotalAmountInclusive().toString();
            String prepaid = df.getPrepaidAmount().toString();
            String payable = df.getPayableAmount().toString();
            return LegalMonetaryTotalDto.builder()
                    .lineExtensionAmount(AmountDto.builder().currencyId(currency).value(lineExtension).build())
                    .taxExclusiveAmount(AmountDto.builder().currencyId(currency).value(taxExclusive).build())
                    .taxInclusiveAmount(AmountDto.builder().currencyId(currency).value(inclusive).build())
                    .prepaidAmount(AmountDto.builder().currencyId(currency).value(prepaid).build())
                    .payableAmount(AmountDto.builder().currencyId(currency).value(payable).build())
                    .build();
        }

        static List<TaxTotalDto> taxTotals(List<InvoiceLine> l, DocumentFinancials df, String docCurrency,
                String taxCurrency) {
            // Create a composite key to group by Category AND Exemption Reason
            record TaxGroupKey(TaxCategory category, String exemptionCode, String exemptionText) {
            }
            ;

            Function<InvoiceLine, TaxGroupKey> compositeKey = (line) -> new TaxGroupKey(
                    line.getTaxCategory(),
                    line.getExemptionReasonCode(),
                    line.getExemptionReasonText());

            Map<TaxGroupKey, List<InvoiceLine>> group = l.stream()
                    .collect(Collectors.groupingBy(compositeKey));

            List<TaxSubtotalDto> subtotals = new ArrayList<>();

            for (Map.Entry<TaxGroupKey, List<InvoiceLine>> entry : group.entrySet()) {
                TaxCategory category = entry.getKey().category();
                String exemptionCode = entry.getKey().exemptionCode();
                String exemptionText = entry.getKey().exemptionText();
                List<InvoiceLine> linesInCategory = entry.getValue();

                BigDecimal taxableAmount = linesInCategory.stream()
                        .map(InvoiceLine::getNetPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal taxAmount = linesInCategory.stream()
                        .map(InvoiceLine::getTaxAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                subtotals.add(TaxSubtotalDto.builder()
                        .taxableAmount(
                                AmountDto.builder().currencyId(docCurrency).value(taxableAmount.toString()).build())
                        .taxAmount(AmountDto.builder().currencyId(docCurrency).value(taxAmount.toString()).build())
                        .taxCategory(TaxCategoryDto.builder()
                                .id(category.getCode())
                                .percent(category.getRate().toString())
                                .taxExemptionReasonCode(exemptionCode)
                                .taxExemptionReason(exemptionText)
                                .taxScheme(TaxCategoryDto.TaxSchemeDto.builder().build()) // Defaults to "VAT"
                                .build())
                        .build());
            }

            List<TaxTotalDto> taxTotalsList = new ArrayList<>();

            // Primary Tax Total (in Document Currency, includes subtotals)
            taxTotalsList.add(TaxTotalDto.builder()
                    .taxAmount(AmountDto.builder().currencyId(docCurrency).value(df.getTotalTaxAmount().toString())
                            .build())
                    .taxSubtotals(subtotals)
                    .build());

            // Secondary Tax Total (in Accounting Currency - BR-53)
            // ZATCA strictly dictates that this second block does NOT have subtotals, only
            // the final amount.
            if (df.getTotalTaxAmountInAccountingCurrency() != null) {
                taxTotalsList.add(TaxTotalDto.builder()
                        .taxAmount(AmountDto.builder().currencyId(taxCurrency)
                                .value(df.getTotalTaxAmountInAccountingCurrency().toString())
                                .build())
                        .build());
            }

            return taxTotalsList;
        }

        static List<InvoiceLineDto> invoiceLines(List<InvoiceLine> lines, String currency) {
            if (lines == null || lines.isEmpty())
                return new ArrayList<>();

            return lines.stream().map(line -> {
                // 1. Line-level Tax Total (ZATCA requires the tax total on each line as well)
                TaxTotalDto lineTaxTotal = TaxTotalDto.builder()
                        .taxAmount(
                                AmountDto.builder().currencyId(currency).value(line.getTaxAmount().toString()).build())
                        .build();

                // 2. The Item details
                ItemDto item = ItemDto.builder()
                        .name(line.getName())
                        .classifiedTaxCategory(TaxCategoryDto.builder()
                                .id(line.getTaxCategory().getCode())
                                .percent(line.getTaxCategory().getRate().toString())
                                .taxExemptionReasonCode(line.getExemptionReasonCode())
                                .taxExemptionReason(line.getExemptionReasonText())
                                .taxScheme(TaxCategoryDto.TaxSchemeDto.builder().build())
                                .build())
                        .build();

                QuantityDto quantity = QuantityDto.builder()
                        .unitCode(line.getMeasuringUnit().toString())
                        .value(line.getQuantity().toString())
                        .build();

                // 3. Assemble the Invoice Line DTO
                return InvoiceLineDto.builder()
                        .id(line.getIdentifier())
                        .invoicedQuantity(quantity)
                        .lineExtensionAmount(
                                AmountDto.builder().currencyId(currency).value(line.getNetPrice().toString()).build())
                        .taxTotal(lineTaxTotal)
                        .item(item)
                        .price(PriceDto.builder()
                                .priceAmount(AmountDto.builder().currencyId(currency)
                                        .value(line.getUnitPrice().toString()).build())
                                .build())
                        .build();
            }).collect(Collectors.toList());
        }

        static AccountingPartyDto party(BusinessParty businessParty) {
            if (businessParty == null)
                return null;

            // Build Address
            PostalAddressDto addressDto = null;
            if (businessParty.getAddress() != null) {
                addressDto = PostalAddressDto.builder()
                        .buildingNumber(businessParty.getAddress().getBuildingNumber())
                        .streetName(businessParty.getAddress().getStreetName())
                        .citySubdivisionName(businessParty.getAddress().getDistrict())
                        .cityName(businessParty.getAddress().getCity())
                        .postalZone(businessParty.getAddress().getPostalCode())
                        .country(CountryDto.builder()
                                .identificationCode(businessParty.getAddress().getCountry().getCountry())
                                .build())
                        .build();
            }

            // Build Tax Scheme (Only if VAT is present)
            PartyTaxSchemeDto taxSchemeDto = null;
            if (businessParty.getVatNumber() != null && !businessParty.getVatNumber().isBlank()) {
                taxSchemeDto = PartyTaxSchemeDto.builder()
                        .companyId(businessParty.getVatNumber())
                        .taxScheme(TaxSchemeDto.builder().build()) // Defaults to "VAT"
                        .build();
            }

            // Endpoint and scheme
            EndPointIdDto endpointId = null;
            if (businessParty.getVatNumber() != null && !businessParty.getVatNumber().isBlank()) {
                endpointId = EndPointIdDto.builder()
                        .schemeId(PartySchemeId.VAT)
                        .value(businessParty.getVatNumber())
                        .build();
            } else if (businessParty.getCommercialRegistrationNumber() != null
                    && !businessParty.getCommercialRegistrationNumber().isBlank()) {
                endpointId = EndPointIdDto.builder()
                        .schemeId(PartySchemeId.CRN)
                        .value(businessParty.getCommercialRegistrationNumber())
                        .build();
            }

            // Assemble full Party
            PartyDto party = PartyDto.builder()
                    .endpointId(endpointId)
                    .postalAddress(addressDto)
                    .partyTaxScheme(taxSchemeDto)
                    .partyLegalEntity(PartyLegalEntityDto.builder()
                            .registrationName(businessParty.getRegistrationName())
                            .build())
                    .build();

            return AccountingPartyDto.builder().party(party).build();
        }

        static BillingReferenceDto billingReference(BillingReference ref) {
            if (ref == null || ref.getOriginalInvoiceNumber() == null)
                return null;
            return BillingReferenceDto.builder()
                    .invoiceDocumentReference(DocumentReferenceDto.builder()
                            .id(ref.getOriginalInvoiceNumber())
                            .build())
                    .build();
        }

        static DeliveryDto delivery(LocalDate date) {
            Function<LocalDate, DeliveryDto> mapper = (d) -> DeliveryDto.builder()
                    .actualDeliveryDate(d.toString())
                    .build();
            return Optional.ofNullable(date)
                    .map(mapper)
                    .orElse(null);
        }

        static PaymentMeansDto paymentMeans(PaymentMethod method, String reason) {
            String r = reason == null || reason.isBlank() ? null : reason;
            String code = method != null ? method.code().toString() : PaymentMethod.IN_CASH.code().toString();
            return r == null && method == null
                    ? null
                    : PaymentMeansDto.builder()
                            .paymentMeansCode(code)
                            .instructionNote(reason)
                            .build();
        }
    };
}
