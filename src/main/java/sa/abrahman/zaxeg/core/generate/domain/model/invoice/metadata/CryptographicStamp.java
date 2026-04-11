package sa.abrahman.zaxeg.core.generate.domain.model.invoice.metadata;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * @implNote This class is impelmented by Google's Gemini, it might need changes in the future
 */
@Getter
@Builder
public class CryptographicStamp {
    /**
     * The actual ECDSA digital signature of the Invoice Hash. Encoded in Base64.
     */
    private String signatureValue;

    /**
     * The Taxpayer's X.509 Public Certificate (CSID) provided by ZATCA. Encoded in Base64.
     */
    private String certificate;

    /**
     * The Base64 encoded SHA-256 hash of the X.509 Certificate. Required for the <xades:CertDigest> element.
     */
    private String certificateHash;

    /**
     * The exact time the signature was generated. Required for the <xades:SigningTime> element.
     */
    private LocalDateTime signatureTime;

    /**
     * The Issuer Name extracted from the X.509 Certificate. Required for the <xades:IssuerSerial> element.
     */
    private String certificateIssuer;

    /**
     * The Serial Number extracted from the X.509 Certificate. Required for the <xades:IssuerSerial> element.
     */
    private String certificateSerialNumber;
}
