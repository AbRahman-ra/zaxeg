package sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.financial;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import lombok.Builder;
import lombok.Data;
import sa.abrahman.zaxeg.infrastructure.out.dto.invoice.depr.UBLandZATCAConstants;

@Data
@Builder
public class PaymentMeansDto {
    // Optional: Only if you want to specify "Cash" (10) or "Bank Transfer" (30)
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.PAYMENT_MEANS_CODE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String paymentMeansCode;

    // Mandatory for Credit/Debit Notes (This holds the Reason for Issuance)
    @JacksonXmlProperty(localName = UBLandZATCAConstants.TAGS.CBC.INSTRUCTION_NOTE, namespace = UBLandZATCAConstants.NAMESPACES.ROOT)
    private String instructionNote;
}
