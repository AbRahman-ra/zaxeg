package sa.abrahman.zaxeg.infrastructure.out.adapters;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import sa.abrahman.zaxeg.core.model.BusinessParty;
import sa.abrahman.zaxeg.core.model.Invoice;
import sa.abrahman.zaxeg.core.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.AccountingSupplierPartyDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.AmountDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.InvoiceTypeCodeDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.LegalMonetaryTotalDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.PartyDto;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.UBLInvoiceDto;
import sa.abrahman.zaxeg.infrastructure.out.exception.XmlGenerationException;

@Component
public class JacksonInvoiceFormatter implements InvoiceFormatter {
    private final XmlMapper mapper;

    public JacksonInvoiceFormatter() {
        this.mapper = new XmlMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String format(Invoice invoice) {
        String supplierVat = Optional.ofNullable(invoice.getSupplier())
                .map(BusinessParty::getVatNumber)
                .orElse("");

        String lineExtensionAmount = invoice.getFinancials().getTotalLineExtensionAmount().toString();
        String taxExclusiveAmount = invoice.getFinancials().getTotalLineExtensionAmount().toString();
        String inclusiveAmount = invoice.getFinancials().getTotalAmountInclusive().toString();
        String prepaidAmount = invoice.getFinancials().getPrepaidAmount().toString();
        String payableAmount = invoice.getFinancials().getPayableAmount().toString();

        UBLInvoiceDto ublDto = UBLInvoiceDto.builder()
                // metadata
                .id(invoice.getInvoiceNumber()) // Assuming your core model has this
                .issueDate(invoice.getIssueDate().toString())
                .issueTime(invoice.getIssueTime().toString())
                .documentCurrencyCode(invoice.getDocumentCurrency().getCurrencyCode())
                .taxCurrencyCode(invoice.getTaxCurrency().getCurrencyCode())
                .invoiceTypeCode(InvoiceTypeCodeDto.builder()
                        .name(invoice.getInvoiceSubtype().code())
                        .value(invoice.getInvoiceDocumentType().code())
                        .build())

                // supplier
                .supplierParty(AccountingSupplierPartyDto.builder()
                        .party(PartyDto.builder()
                                .endpointId(supplierVat)
                                .build())
                        .build())

                // financials
                .legalMonetaryTotal(LegalMonetaryTotalDto.builder()
                        .lineExtensionAmount(AmountDto.builder().value(lineExtensionAmount).build())
                        .taxExclusiveAmount(AmountDto.builder().value(taxExclusiveAmount).build())
                        .taxInclusiveAmount(AmountDto.builder().value(inclusiveAmount).build())
                        .prepaidAmount(AmountDto.builder().value(prepaidAmount).build())
                        .payableAmount(AmountDto.builder().value(payableAmount).build())
                        .build())
                .build();

        try {
            return mapper.writeValueAsString(ublDto);
        } catch (JsonProcessingException e) {
            throw new XmlGenerationException("Failed to serialize ZATCA XML", e);
        }
    }
}
