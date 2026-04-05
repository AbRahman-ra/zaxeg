package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate.components;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;
import sa.abrahman.zaxeg.core.port.in.payload.MetadataPayload;
import sa.abrahman.zaxeg.core.validator.rule.ImplicitRules;
import sa.abrahman.zaxeg.core.validator.rule.UblRules;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Data
@NullMarked
@Schema(title = "Document Metadata", description = "Document-level configurations and metadata", requiredMode = RequiredMode.REQUIRED)
public class MetadataRequest implements Payloadable<MetadataPayload, Void> {

    @NotBlank(message = UblRules.BR_02)
    @Schema(title = "Invoice number", description = "A unique identification of the Invoice", requiredMode = RequiredMode.REQUIRED, example = "1234567")
    private String invoiceNumber;

    @Schema(title = "Unique invoice identifier", description = "Globally unique reference identifying the invoice. Note: the system will generate an automatic UUID if there is no provided UUID, However, it's STRINGLY RECOMMENDED to provide a UUID", example = "8f480da0-70f9-4674-8b63-8a3d6ebef9e6")
    private UUID invoiceUuid = UUID.randomUUID();

    @NotNull(message = UblRules.BR_03)
    @Schema(title = "Invoice issue date (gregorian calendar)", description = "The date when the Invoice was issued as per Article 53 of the VAT Implementing Regulation", requiredMode = RequiredMode.REQUIRED, example = "2026-01-01")
    private LocalDate issueDate;

    @NotNull(message = "Issue time is required")
    @Schema(title = "Invoice issue time", description = "The time when the invoice was issued. Provided in hh:mm:ss for time in AST, and hh:mm:ssZ for UTC time zone", requiredMode = RequiredMode.REQUIRED, example = "19:48:21")
    private LocalTime issueTime;

    @Nullable
    @Schema(title = "Supply date (gregorian calendar)", description = "The date when the supply is performed. For credit and debit notes, it acts as the original supply date.", requiredMode = RequiredMode.NOT_REQUIRED, example = "2026-01-01")
    private LocalDate supplyDate;

    @Nullable
    @Schema(title = "Supply end date (gregorian calendar)", description = "Calendar field \"End Date\" for Continuous Supplies.", requiredMode = RequiredMode.NOT_REQUIRED, example = "2026-01-01")
    private LocalDate supplyEndDate;

    @NotNull(message = UblRules.BR_04)
    @Schema(title = "Invoice type code", description = "A code specifying the functional type of the Invoice.", requiredMode = RequiredMode.REQUIRED, example = "TAX_INVOICE")
    private InvoiceDocumentType invoiceDocumentType;

    @Valid
    @NotNull(message = UblRules.BR_04)
    @Schema(title = "Invoice type transactions", description = "A code of the invoice subtype and invoices transactions.", requiredMode = RequiredMode.REQUIRED, example = "{\n    \"subtype\": \"STANDARD\"\n}")
    private InvoiceTypeTransactions invoiceTypeTransactions;

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

    @Nullable
    @Schema(title = "Invoice currency code (ISO 4217 alpha-3)", description = "The currency in which all Invoice amounts are given, except for the Total VAT amount in accounting currency. If no code is given, the system will fallback to SAR", requiredMode = RequiredMode.NOT_REQUIRED, example = "SAR")
    private Currency invoiceCurrency = Currency.getInstance(Invoice.DEFAULT_CURRENCY_CODE);

    @Nullable
    @Valid
    @Schema(title = "Billing reference ID", description = "The sequential number (Invoice number BT-1) of the original invoice(s) that the credit/debit note is related to.", requiredMode = RequiredMode.NOT_REQUIRED, example = "{\n    \"id\": \"123456\"\n}")
    private DocumentReference billingReference;

    @Nullable
    @Valid
    @Schema(title = "Purchase order ID", description = "An identifier of a referenced purchase order, issued by the Buyer.", requiredMode = RequiredMode.NOT_REQUIRED, example = "{\n    \"id\": \"ORD1234\"\n}")
    private DocumentReference purchaseOrder;

    @Nullable
    @Valid
    @Schema(title = "Contract ID", description = "The identification of a contract.", requiredMode = RequiredMode.NOT_REQUIRED, example = "{\n    \"id\": \"1234567890\"\n}")
    private DocumentReference contract;

    @Override
    public MetadataPayload toPayload(Void additionalData) {
        // nullables

        return MetadataPayload.builder().invoiceNumber(this.invoiceNumber).invoiceUuid(this.invoiceUuid)
                .issueDate(this.issueDate).issueTime(this.issueTime).supplyDate(this.supplyDate)
                .supplyEndDate(this.supplyEndDate).invoiceDocumentType(this.invoiceDocumentType)
                .invoiceTypeTransactions(this.invoiceTypeTransactions.toPayload())
                .creditOrDebitNoteIssuanceReasons(this.creditOrDebitNoteIssuanceReasons).notes(this.notes)
                .invoiceCurrency(this.invoiceCurrency != null ? this.invoiceCurrency
                        : Currency.getInstance(Invoice.DEFAULT_CURRENCY_CODE))
                .taxCurrency(Currency.getInstance(Invoice.DEFAULT_CURRENCY_CODE)) // Strictly SAR for ZATCA
                // Clean ternary checks instead of Optionals
                .billingReference(this.billingReference != null ? this.billingReference.toPayload() : null)
                .purchaseOrder(this.purchaseOrder != null ? this.purchaseOrder.toPayload() : null)
                .contract(this.contract != null ? this.contract.toPayload() : null).build();
    }

    // ==========================================================================
    // ============================= NESTED CLASSES =============================
    // ==========================================================================
    @Data
    @NullMarked
    private static class InvoiceTypeTransactions implements Payloadable<MetadataPayload.InvoiceTypeTransactions, Void> {

        @NotNull(message = UblRules.BR_04)
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
        public MetadataPayload.InvoiceTypeTransactions toPayload(Void d) {
            if (!thirdParty && !nominal && !exports && !summary && !selfBilled)
                return MetadataPayload.InvoiceTypeTransactions.of(subtype);

            return MetadataPayload.InvoiceTypeTransactions.builder().subtype(subtype).thirdParty(thirdParty)
                    .nominal(nominal).exports(exports).summary(summary).selfBilled(selfBilled).build();
        }
    }

    @Data
    @NullMarked
    private static class DocumentReference implements Payloadable<MetadataPayload.DocumentReference, Void> {
        @NotBlank(message = ImplicitRules.DOC_REF_ID_NOT_NULL)
        private String id;

        @Override
        public MetadataPayload.DocumentReference toPayload(Void d) {
            return new MetadataPayload.DocumentReference(id);
        }
    }
}
