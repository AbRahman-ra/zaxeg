package sa.abrahman.zaxeg.core.port.in.payload;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceDocumentType;
import sa.abrahman.zaxeg.core.model.invoice.predefined.InvoiceSubtype;

@Getter
@Builder
@NullMarked
public class MetadataPayload {
    private String invoiceNumber;
    private UUID invoiceUuid;
    private LocalDate issueDate;
    private LocalTime issueTime;

    @Nullable
    private LocalDate supplyDate;

    @Nullable
    private LocalDate supplyEndDate;

    private InvoiceDocumentType invoiceDocumentType;
    private InvoiceTypeTransactions invoiceTypeTransactions;

    @Builder.Default
    private List<String> creditOrDebitNoteIssuanceReasons = List.of();

    @Builder.Default
    private List<String> notes = List.of();
    private Currency invoiceCurrency;
    private Currency taxCurrency;

    @Nullable
    private DocumentReference billingReference;

    @Nullable
    private DocumentReference purchaseOrder;

    @Nullable
    private DocumentReference contract;

    @Getter
    @Builder
    @NullMarked
    public static class InvoiceTypeTransactions {
        private InvoiceSubtype subtype;

        @Builder.Default
        private boolean thirdParty = false;

        @Builder.Default
        private boolean nominal = false;

        @Builder.Default
        private boolean exports = false;

        @Builder.Default
        private boolean summary = false;

        @Builder.Default
        private boolean selfBilled = false;

        public static InvoiceTypeTransactions of(InvoiceSubtype subtype) {
            return builder().subtype(subtype).build();
        }
    }

    @Data
    @NullMarked
    public static class DocumentReference { private final String id; }
}
