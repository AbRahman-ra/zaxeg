package sa.abrahman.zaxeg.infrastructure.out.adapters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import sa.abrahman.zaxeg.core.model.BillingReference;
import sa.abrahman.zaxeg.core.model.BusinessParty;
import sa.abrahman.zaxeg.core.model.DocumentFinancials;
import sa.abrahman.zaxeg.core.model.Invoice;
import sa.abrahman.zaxeg.core.model.InvoiceLine;
import sa.abrahman.zaxeg.core.model.TaxCategory;
import sa.abrahman.zaxeg.core.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLInvoiceDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.AmountDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.LegalMonetaryTotalDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.line.InvoiceLineDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.line.ItemDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.line.PriceDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.line.QuantityDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.tax.TaxCategoryDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.tax.TaxCategoryDto.TaxSchemeDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.tax.TaxSubtotalDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.financial.tax.TaxTotalDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.meta.BillingReferenceDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.meta.DocumentReferenceDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.meta.InvoiceTypeCodeDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party.AccountingPartyDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party.CountryDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party.PartyDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party.PartyLegalEntityDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party.PartyTaxSchemeDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.party.PostalAddressDto;
import sa.abrahman.zaxeg.infrastructure.out.exception.XmlGenerationException;

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

        // parties
        AccountingPartyDto supplier = Extractor.party(invoice.getSupplier());
        AccountingPartyDto buyer = Extractor.party(invoice.getBuyer());

        // financials
        LegalMonetaryTotalDto legalMonetaryTotal = Extractor.legalMonetaryTotal(invoice.getFinancials());
        TaxTotalDto taxtotal = Extractor.taxTotal(invoice.getLines(), invoice.getFinancials().getTotalTaxAmount());
        List<InvoiceLineDto> invoiceLines = Extractor.invoiceLines(invoice.getLines());

        // assemble
        UBLInvoiceDto ublDto = UBLInvoiceDto.builder()
                // metadata
                .id(invoice.getInvoiceNumber()) // Assuming your core model has this
                .issueDate(invoice.getIssueDate().toString())
                .issueTime(invoice.getIssueTime().toString())
                .documentCurrencyCode(invoice.getDocumentCurrency().getCurrencyCode())
                .taxCurrencyCode(invoice.getTaxCurrency().getCurrencyCode())
                .invoiceTypeCode(invoiceTypeCode)
                .billingReference(billingReference)

                // parties
                .supplierParty(supplier)
                .buyerParty(buyer)

                // financials
                .taxTotal(taxtotal)
                .invoiceLines(invoiceLines)
                .legalMonetaryTotal(legalMonetaryTotal)
                .build();

        try {
            return mapper.writeValueAsString(ublDto);
        } catch (JsonProcessingException e) {
            throw new XmlGenerationException("Failed to serialize ZATCA XML", e);
        }
    }

    // ============ DEV HELPER CLASS ============
    static class Extractor {
        static LegalMonetaryTotalDto legalMonetaryTotal(DocumentFinancials df) {
            String lineExtension = df.getTotalLineExtensionAmount().toString();
            String taxExclusive = df.getTotalLineExtensionAmount().toString();
            String inclusive = df.getTotalAmountInclusive().toString();
            String prepaid = df.getPrepaidAmount().toString();
            String payable = df.getPayableAmount().toString();
            return LegalMonetaryTotalDto.builder()
                    .lineExtensionAmount(AmountDto.builder().value(lineExtension).build())
                    .taxExclusiveAmount(AmountDto.builder().value(taxExclusive).build())
                    .taxInclusiveAmount(AmountDto.builder().value(inclusive).build())
                    .prepaidAmount(AmountDto.builder().value(prepaid).build())
                    .payableAmount(AmountDto.builder().value(payable).build())
                    .build();
        }

        static TaxTotalDto taxTotal(List<InvoiceLine> l, BigDecimal totaltax) {
            Map<TaxCategory, List<InvoiceLine>> group = l.stream()
                    .collect(Collectors.groupingBy(InvoiceLine::getTaxCategory));

            List<TaxSubtotalDto> subtotals = new ArrayList<>();

            for (Map.Entry<TaxCategory, List<InvoiceLine>> entry : group.entrySet()) {
                TaxCategory category = entry.getKey();
                List<InvoiceLine> linesInCategory = entry.getValue();

                BigDecimal taxableAmount = linesInCategory.stream()
                        .map(InvoiceLine::getNetPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal taxAmount = linesInCategory.stream()
                        .map(InvoiceLine::getTaxAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                subtotals.add(TaxSubtotalDto.builder()
                        .taxableAmount(AmountDto.builder().value(taxableAmount.toString()).build())
                        .taxAmount(AmountDto.builder().value(taxAmount.toString()).build())
                        .taxCategory(TaxCategoryDto.builder()
                                .id(category.getCode())
                                .percent(category.getRate().toString())
                                .taxScheme(TaxCategoryDto.TaxSchemeDto.builder().build()) // Defaults to "VAT"
                                .build())
                        .build());
            }

            return TaxTotalDto.builder()
                    .taxAmount(
                            AmountDto.builder().value(totaltax.toString()).build())
                    .taxSubtotals(subtotals)
                    .build();
        }

        static List<InvoiceLineDto> invoiceLines(List<InvoiceLine> lines) {
            if (lines == null || lines.isEmpty()) return new ArrayList<>();

            return lines.stream().map(line -> {
                // 1. Line-level Tax Total (ZATCA requires the tax total on each line as well)
                TaxTotalDto lineTaxTotal = TaxTotalDto.builder()
                        .taxAmount(AmountDto.builder().value(line.getTaxAmount().toString()).build())
                        .build();

                // 2. The Item details
                ItemDto item = ItemDto.builder()
                        .name(line.getName())
                        .classifiedTaxCategory(TaxCategoryDto.builder()
                                .id(line.getTaxCategory().getCode())
                                .percent(line.getTaxCategory().getRate().toString())
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
                        .lineExtensionAmount(AmountDto.builder().value(line.getNetPrice().toString()).build())
                        .taxTotal(lineTaxTotal)
                        .item(item)
                        .price(PriceDto.builder()
                                .priceAmount(AmountDto.builder().value(line.getUnitPrice().toString()).build())
                                .build())
                        .build();
            }).collect(Collectors.toList());
        }

        static AccountingPartyDto party(BusinessParty businessParty) {
            if (businessParty == null) return null;

            // 1. Build Address
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

            // 2. Build Tax Scheme (Only if VAT is present)
            PartyTaxSchemeDto taxSchemeDto = null;
            if (businessParty.getVatNumber() != null && !businessParty.getVatNumber().isBlank()) {
                taxSchemeDto = PartyTaxSchemeDto.builder()
                        .companyId(businessParty.getVatNumber())
                        .taxScheme(TaxSchemeDto.builder().build()) // Defaults to "VAT"
                        .build();
            }

            // 3. Assemble full Party
            PartyDto party = PartyDto.builder()
                    .endpointId(businessParty.getVatNumber() != null ? businessParty.getVatNumber() : businessParty.getCommercialRegistrationNumber())
                    .postalAddress(addressDto)
                    .partyTaxScheme(taxSchemeDto)
                    .partyLegalEntity(PartyLegalEntityDto.builder()
                            .registrationName(businessParty.getRegistrationName())
                            .build())
                    .build();

            return AccountingPartyDto.builder().party(party).build();
        }

        static BillingReferenceDto billingReference(BillingReference ref) {
            if (ref == null || ref.getOriginalInvoiceNumber() == null) return null;
            return BillingReferenceDto.builder()
                    .invoiceDocumentReference(DocumentReferenceDto.builder()
                            .id(ref.getOriginalInvoiceNumber())
                            .build())
                    .build();
        }
    };

}
