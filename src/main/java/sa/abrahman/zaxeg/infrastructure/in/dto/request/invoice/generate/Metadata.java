package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.core.service.validator.rules.BusinessIntegrityConstraintRule;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Data
class Metadata implements Payloadable<InvoiceGenerationPayload.Metadata, Void> {
    @NotBlank(message = BusinessIntegrityConstraintRule.BR_02)
    @Schema(title = "Invoice number", description = "A unique identification of the Invoice", requiredMode = RequiredMode.REQUIRED, example = "1234567")
    private String invoiceNumber;

    @Schema(title = "Unique invoice identifier", description = "Globally unique reference identifying the invoice. Note: the system will generate an automatic UUID if there is no provided UUID, However, it's STRINGLY RECOMMENDED to provide a UUID", example = "8f480da0-70f9-4674-8b63-8a3d6ebef9e6")
    private UUID invoiceUuid = UUID.randomUUID();

    @NotNull(message = BusinessIntegrityConstraintRule.BR_03)
    @Schema(title = "Invoice issue date (gregorian calendar)", description = "The date when the Invoice was issued as per Article 53 of the VAT Implementing Regulation", requiredMode = RequiredMode.REQUIRED, example = "2026-01-01")
    private LocalDate issueDate;

    @NotNull(message = "Issue time is required")
    @Schema(title = "Invoice issue time", description = "The time when the invoice was issued.", requiredMode = RequiredMode.REQUIRED, example = "19:48:21")
    private LocalTime issueTime;

    @Schema(title = "Supply date (gregorian calendar)", description = "The date when the supply is performed. For credit and debit notes, it acts as the original supply date.", requiredMode = RequiredMode.NOT_REQUIRED, example = "2026-01-01")
    private LocalDate supplyDate;

    @Schema(title = "Supply end date (gregorian calendar)", description = "Calendar field \"End Date\" for Continuous Supplies.", requiredMode = RequiredMode.NOT_REQUIRED, example = "2026-01-01")
    private LocalDate supplyEndDate;

    @NotNull(message = BusinessIntegrityConstraintRule.BR_04)
    @Schema(title = "Invoice type code", description = "A code specifying the functional type of the Invoice.", requiredMode = RequiredMode.REQUIRED, example = "TAX_INVOICE")
    private InvoiceDocumentType invoiceDocumentType;

    @Valid
    @NotNull(message = BusinessIntegrityConstraintRule.BR_04)
    @Schema(title = "Invoice type transactions", description = "A code of the invoice subtype and invoices transactions.", requiredMode = RequiredMode.REQUIRED, example = "{\n    \"subtype\": \"STANDARD\"\n}")
    private InvoiceTypeTranasctions invoiceTypeTransactions;

    @Schema(title = "Reasons for issuance of credit / debit note", description = """
            Reasons for issuance of credit / debit note as per Article 40 (paragraph 1) and Article 54 (3) of KSA VAT regulations, a Credit and Debit Note is issued for these 5 instances:
            - Cancellation or suspension of the supplies after its occurrence either wholly or partially
            - In case of essential change or amendment in the supply, which leads to the change of the VAT due;
            - Amendment of the supply value which is pre-agreed upon between the supplier and consumer;
            - In case of goods or services refund.
            - In case of change in Seller's or Buyer's information
            """, requiredMode = RequiredMode.NOT_REQUIRED, example = "[\"Cancellation or suspension of the supplies after its occurrence either wholly or partially\"]")
    private List<String> creditOrDebitNoteIssuanceReasons = List.of();

    @Schema(title = "Invoice notes", description = "A textual note that gives unstructured information that is relevant to the Invoice as a whole.", requiredMode = RequiredMode.NOT_REQUIRED, example = "[\"testing invoice\", \"hello world\"]")
    private List<String> notes = List.of();

    @Schema(title = "Invoice currency code (ISO 4217 alpha-3)", description = "The currency in which all Invoice amounts are given, except for the Total VAT amount in accounting currency. If no code is given, the system will fallback to SAR", requiredMode = RequiredMode.NOT_REQUIRED, example = "SAR")
    private Currency invoiceCurrency;

    @NotNull(message = BusinessIntegrityConstraintRule.BR_KSA_68)
    @Schema(title = "Tax currency code (ISO 4217 alpha-3)", description = "The currency used for VAT accounting and reporting purposes as accepted or required in the country of the Seller", requiredMode = RequiredMode.NOT_REQUIRED, example = "SAR")
    private Currency taxCurrency;

    @Schema(title = "Billing reference ID", description = "The sequential number (Invoice number BT-1) of the original invoice(s) that the credit/debit note is related to.", requiredMode = RequiredMode.NOT_REQUIRED, example = "{\n    \"id\": \"123456\"\n}")
    private DocumentReference billingReference;

    @Schema(title = "Purchase order ID", description = "An identifier of a referenced purchase order, issued by the Buyer.", requiredMode = RequiredMode.NOT_REQUIRED, example = "{\n    \"id\": \"ORD1234\"\n}")
    private DocumentReference purchaseOrder;

    @Schema(title = "Contract ID", description = "The identification of a contract.", requiredMode = RequiredMode.NOT_REQUIRED, example = "{\n    \"id\": \"1234567890\"\n}")
    private DocumentReference contract;

    @NotNull(message = BusinessIntegrityConstraintRule.BR_KSA_33)
    @Positive(message = BusinessIntegrityConstraintRule.BR_KSA_34)
    @Schema(title = "Invoice counter value", description = "Invoice counter value", requiredMode = RequiredMode.REQUIRED, example = "1")
    private Long icv;

    @NotBlank(message = BusinessIntegrityConstraintRule.BR_KSA_61)
    @Schema(title = "Previous invoice hash", description = "The base64 encoded SHA256 hash of the previous invoice. For the first invoice, the previous invoice hash is \"NWZlY2ViNjZmZmM4NmYzOGQ5NTI3ODZjNmQ2OTZjNzljMmRiYzIzOWRkNGU5MWI0NjcyOWQ3M2EyN2ZiNTdlOQ==\", the equivalent for base64 encoded SHA256 of \"0\" (zero) character.", requiredMode = RequiredMode.REQUIRED, example = "NWZlY2ViNjZmZmM4NmYzOGQ5NTI3ODZjNmQ2OTZjNzljMmRiYzIzOWRkNGU5MWI0NjcyOWQ3M2EyN2ZiNTdlOQ==")
    private String pih;

    @NotBlank(message = BusinessIntegrityConstraintRule.BR_KSA_27)
    @Schema(title = "Invoice QR code", requiredMode = RequiredMode.REQUIRED, example = "AQkxMjM0NTY3ODkCCjEyLzEyLzIwMjADBDEwMDADAzE1MPaIn2Z2jg6VqWvWV6IrZZNzLF7xvZrWXW5xRV5yFY2xFu0ycXOiyqV0k7Wsh6b1IcE2Tfzap1AQAQVsktmsv1FFQ1MxIAAAAGKblFMh9nFRSn8tvftXqo9zRSz2VEAPITSZ3W7UDHKhUx+7yXGijLtJSZGXMOc+jpKwARzDl68GmmRd75NWdOs=")
    private String qr;

    @Valid
    @NotNull(message = BusinessIntegrityConstraintRule.BR_KSA_60)
    @Schema(title = "Cryptographic stamp", description = "Invoice cryptographic stamp", requiredMode = RequiredMode.REQUIRED)
    private CryptographicStamp cryptographicStamp;

    @Override
    public InvoiceGenerationPayload.Metadata toPayload(Void d) {
        return InvoiceGenerationPayload.Metadata.builder()
                .invoiceNumber(invoiceNumber)
                .invoiceUuid(invoiceUuid)
                .issueDate(issueDate)
                .issueTime(issueTime)
                .supplyDate(supplyDate)
                .supplyEndDate(supplyEndDate)
                .invoiceDocumentType(invoiceDocumentType)
                .invoiceTypeTransactions(invoiceTypeTransactions.toPayload(null))
                .creditOrDebitNoteIssuanceReasons(creditOrDebitNoteIssuanceReasons)
                .notes(notes)
                .invoiceCurrency(invoiceCurrency)
                .taxCurrency(taxCurrency)
                .billingReference(billingReference == null ? null : billingReference.toPayload(null))
                .purchaseOrder(purchaseOrder == null ? null : purchaseOrder.toPayload(null))
                .contract(contract == null ? null : contract.toPayload(null))
                .icv(icv)
                .pih(pih)
                .qr(qr)
                .cryptographicStamp(cryptographicStamp.toPayload(null))
                .build();
    }

    // ==========================================================================
    // ============================= NESTED CLASSES =============================
    // ==========================================================================
    @Data
    private static class InvoiceTypeTranasctions
            implements Payloadable<InvoiceGenerationPayload.Metadata.InvoiceTypeTranasctions, Void> {

        @Schema(title = "Invoice subtype", requiredMode = RequiredMode.REQUIRED, example = "STANDARD")
        private InvoiceSubtype subtype;

        @Schema(title = "Indicates if the document is a third-party invoice", requiredMode = RequiredMode.NOT_REQUIRED, example = "false")
        private boolean thirdParty = false;

        @Schema(title = "Indicates if the document is a nominal invoice", requiredMode = RequiredMode.NOT_REQUIRED, example = "false")
        private boolean nominal = false;

        @Schema(title = "Indicates if the document is an exports invoice", requiredMode = RequiredMode.NOT_REQUIRED, example = "false")
        private boolean exports = false;

        @Schema(title = "Indicates if the document is a summary invoice", requiredMode = RequiredMode.NOT_REQUIRED, example = "false")
        private boolean summary = false;

        @Schema(title = "Indicates if the document is a self-billing invoice", requiredMode = RequiredMode.NOT_REQUIRED, example = "false")
        private boolean selfBilled = false;

        @Override
        public InvoiceGenerationPayload.Metadata.InvoiceTypeTranasctions toPayload(Void d) {
            if (!thirdParty && !nominal && !exports && !summary && !selfBilled)
                return InvoiceGenerationPayload.Metadata.InvoiceTypeTranasctions.of(subtype);

            return InvoiceGenerationPayload.Metadata.InvoiceTypeTranasctions.builder()
                    .subtype(subtype)
                    .thirdParty(thirdParty)
                    .nominal(nominal)
                    .exports(exports)
                    .summary(summary)
                    .selfBilled(selfBilled)
                    .build();
        }
    }

    @Data
    private static class DocumentReference implements Payloadable<InvoiceGenerationPayload.Metadata.DocumentReference, Void> {
        private final String id;

        @Override
        public InvoiceGenerationPayload.Metadata.DocumentReference toPayload(Void d) {
            return new InvoiceGenerationPayload.Metadata.DocumentReference(id);
        }
    }

    @Data
    private static class CryptographicStamp
            implements Payloadable<InvoiceGenerationPayload.Metadata.CryptographicStamp, Void> {

        @Schema(description = "The actual ECDSA digital signature of the Invoice Hash. Encoded in Base64.")
        private String signatureValue;

        @Schema(description = "The Taxpayer's X.509 Public Certificate (CSID) provided by ZATCA. Encoded in Base64.")
        private String certificate;

        @Schema(description = "The Base64 encoded SHA-256 hash of the X.509 Certificate. Required for the <xades:CertDigest> element.")
        private String certificateHash;

        @Schema(description = "The exact time the signature was generated. Required for the <xades:SigningTime> element.")
        private LocalDateTime signatureTime;

        @Schema(description = "The Issuer Name extracted from the X.509 Certificate. Required for the <xades:IssuerSerial> element.")
        private String certificateIssuer;

        @Schema(description = "The Serial Number extracted from the X.509 Certificate. Required for the <xades:IssuerSerial> element.")
        private String certificateSerialNumber;

        @Override
        public InvoiceGenerationPayload.Metadata.CryptographicStamp toPayload(Void d) {
            return InvoiceGenerationPayload.Metadata.CryptographicStamp.builder()
                    .signatureValue(signatureValue)
                    .certificate(certificate)
                    .certificateHash(certificateHash)
                    .signatureTime(signatureTime)
                    .certificateIssuer(certificateIssuer)
                    .certificateSerialNumber(certificateSerialNumber)
                    .build();
        }
    }
}
