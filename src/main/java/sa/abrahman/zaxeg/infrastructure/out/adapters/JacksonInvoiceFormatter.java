package sa.abrahman.zaxeg.infrastructure.out.adapters;


import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.model.invoice.wrapper.Metadata;
import sa.abrahman.zaxeg.core.port.out.InvoiceFormatter;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.XmlInvoice;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata.XmlAdditionalDocumentReference;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata.XmlBillingReference;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata.XmlDocumentReference;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata.XmlDocumentReferenceAttachment;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata.XmlEmbeddedDocumentBinaryObject;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata.XmlInvoiceTypeCode;
import sa.abrahman.zaxeg.infrastructure.out.exception.XMLGenerationException;
import sa.abrahman.zaxeg.infrastructure.out.factory.ZatcaDefaultValues;

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
        try {
            XmlInvoice dto = toDto(invoice);
            return mapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new XMLGenerationException("Failed to serialize ZATCA XML", e);
        }
    }

    private XmlInvoice toDto(Invoice i) {
        // METADATA
        Metadata meta = i.getMetadata();

        String typeCodeValue = String.valueOf(meta.getInvoiceDocumentType().getCode()); // e.g., "388"
        String typeCodeName = meta.getInvoiceTypeTransactions().code();
        XmlInvoiceTypeCode typeCode = new XmlInvoiceTypeCode(typeCodeName, typeCodeValue);

        XmlInvoice.XmlInvoiceBuilder builder = XmlInvoice.builder().id(meta.getInvoiceNumber())
        .uuid(meta.getInvoiceUuid().toString())
        .issueDate(meta.getInvoiceIssueDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
        .issueTime(meta.getInvoiceIssueTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))
        .invoiceTypeCode(typeCode)
        .documentCurrencyCode(meta.getInvoiceCurrency().getCurrencyCode())
        .taxCurrencyCode(meta.getTaxCurrency().getCurrencyCode())
        .notes(meta.getNotes());

        if (meta.getBillingReference() != null) {
            XmlBillingReference billingRef = new XmlBillingReference(new XmlDocumentReference(meta.getBillingReference().getId()));
            builder.billingReference(billingRef);
        }

        if (meta.getPurchaseOrder() != null) {
            XmlDocumentReference purchaseOrder = new XmlDocumentReference(meta.getPurchaseOrder().getId());
            builder.purchaseOrder(purchaseOrder);
        }

        if (meta.getContract() != null) {
            XmlDocumentReference contract = new XmlDocumentReference(meta.getPurchaseOrder().getId());
            builder.contract(contract);
        }

        List<XmlAdditionalDocumentReference> additionalRefs = new ArrayList<>();

        if (meta.getIcv() != null) {
            XmlAdditionalDocumentReference icv = XmlAdditionalDocumentReference.builder()
                    .id(ZatcaDefaultValues.ICV_DOCUMENT_REFERENCE_ID)
                    .uuid(meta.getIcv().toString())
                    .build();
            additionalRefs.add(icv);
        }

        if (meta.getPih() != null && !meta.getPih().isBlank()) {
            XmlEmbeddedDocumentBinaryObject pihBo = XmlEmbeddedDocumentBinaryObject.builder()
                    .mimeCode(MediaType.TEXT_PLAIN_VALUE)
                    .value(meta.getPih())
                    .build();

            XmlDocumentReferenceAttachment pihAttach = XmlDocumentReferenceAttachment.builder()
                    .embeddedDocumentBinaryObject(pihBo)
                    .build();

            XmlAdditionalDocumentReference pih = XmlAdditionalDocumentReference.builder()
                    .id(ZatcaDefaultValues.PIH_DOCUMENT_REFERENCE_ID)
                    .attachment(pihAttach)
                    .build();

            additionalRefs.add(pih);
        }

        if (meta.getQr() != null && !meta.getQr().isBlank()) {
            XmlEmbeddedDocumentBinaryObject qrBo = XmlEmbeddedDocumentBinaryObject.builder()
                    .mimeCode(MediaType.TEXT_PLAIN_VALUE)
                    .value(meta.getQr())
                    .build();

            XmlDocumentReferenceAttachment qrAttach = XmlDocumentReferenceAttachment.builder()
                    .embeddedDocumentBinaryObject(qrBo)
                    .build();

            XmlAdditionalDocumentReference qr = XmlAdditionalDocumentReference.builder()
                    .id(ZatcaDefaultValues.QR_DOCUMENT_REFERENCE_ID)
                    .attachment(qrAttach)
                    .build();

            additionalRefs.add(qr);
        }

        builder.additionalDocumentReferences(additionalRefs);

        return builder.build();
    }
}
