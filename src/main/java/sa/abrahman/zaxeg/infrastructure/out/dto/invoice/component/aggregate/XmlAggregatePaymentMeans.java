package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.aggregate;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Getter;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

@Getter
@Builder
public class XmlAggregatePaymentMeans {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.INSTRUCTION_NOTE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private List<String> creditOrDebitNoteIssuanceReasons;
}
