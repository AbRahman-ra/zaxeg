package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.component.metadata;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sa.abrahman.zaxeg.infrastructure.out.factory.UblInvoiceElements;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class XmlMetadataDelivery {
    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.ACTUAL_DELIVERY_DATE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String actualDeliveryDate;

    @JacksonXmlProperty(localName = UblInvoiceElements.TAGS.CBC.LATEST_DELIVERY_DATE, namespace = UblInvoiceElements.NAMESPACES.ROOT)
    private String latestDeliveryDate;
}
