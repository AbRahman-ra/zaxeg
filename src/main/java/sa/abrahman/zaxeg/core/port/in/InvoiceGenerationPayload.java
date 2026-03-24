package sa.abrahman.zaxeg.core.port.in;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.predefined.*;

@Getter
@RequiredArgsConstructor
public class InvoiceGenerationPayload {
    private final Metadata metadata;
    private final Parties parties;
    private final Lines lines;

    @Getter
    @RequiredArgsConstructor
    public static class Amount {
        private final BigDecimal value;
        private final Currency currency;
    }

    @Data
    @Builder
    public static class Metadata {
        private String invoiceNumber;
        private UUID invoiceUuid;
        private LocalDate issueDate;
        private LocalTime issueTime;
        private LocalDate supplyDate;
        private LocalDate supplyEndDate;
        private InvoiceDocumentType invoiceDocumentType;
        private InvoiceTypeTranasctions invoiceTypeTransactions;
        private List<String> creditOrDebitNoteIssuanceReasons;
        private List<String> notes;
        private Currency invoiceCurrency;

        private Currency taxCurrency;
        private DocumentReference billingReference;
        private DocumentReference purchaseOrder;
        private DocumentReference contract;
        private Long icv;
        private String pih;
        private String qr;
        private CryptographicStamp cryptographicStamp;

        @Data
        @Builder
        public static class InvoiceTypeTranasctions {
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

            public static InvoiceTypeTranasctions of(InvoiceSubtype subtype) {
                return builder().subtype(subtype).build();
            }
        }

        @Data
        public static class DocumentReference {
            private final String id;
        }

        @Data
        @Builder
        public static class CryptographicStamp {
            private String signatureValue;
            private String certificate;
            private String certificateHash;
            private LocalDateTime signatureTime;
            private String certificateIssuer;
            private String certificateSerialNumber;
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class Parties {
        private final Party seller;
        private final Party buyer;

        @Data
        @Builder
        public static class Party {
            private String name;
            private PartyTaxScheme identification;
            private List<PartyIdentification> otherIds;
            private Address address;

            @Getter
            @RequiredArgsConstructor
            public static class PartyTaxScheme {
                private final String companyId;
                private final TaxScheme taxScheme;

                public static PartyTaxScheme of(String companyId) {
                    return new PartyTaxScheme(companyId, TaxScheme.VAT);
                }
            }

            @Getter
            @RequiredArgsConstructor
            public static class PartyIdentification {
                private final Scheme schemeId;
                private final String value;
            }

            @Data
            @Builder
            public static class Address {
                private final String street;
                private final String additionalStreet;
                private final String buildingNumber;
                private final String additionalNumber;
                private final String city;
                private final String postalCode;
                private final String provinceOrState;
                private final String district;
                private final Locale country;
            }
        }
    }

    @Data
    @RequiredArgsConstructor
    public static class Lines {
        private final List<InvoiceLine> invoiceLines;
    }

    @Data
    @Builder
    public static class InvoiceLine {
        private String id;
        private Quantity quantity;
        private Amount netAmount;
        private List<AllowanceOrCharge> allowanceCharges;
        private TaxTotal vatLineAmount;
        private InvoiceLineItem item;
        private InvoiceLinePrice price;

    }

    @Getter
    @Builder
    public static class InvoiceLineItem {
        private String name;
        private ItemPartyIdentifier itemBuyerIdentifier;
        private ItemPartyIdentifier itemSellerIdentifier;
        private ItemPartyIdentifier itemStandardIdentifier;
        private TaxCategory classifiedTaxCategory;
    }

    @Getter
    @Builder
    public static class InvoiceLinePrice {
        private Amount amount;
        private Quantity quantity;
        private AllowanceOrCharge allowanceOrCharge;
    }

    @Getter
    @RequiredArgsConstructor
    public static class ItemPartyIdentifier {
        private final String id;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Quantity {
        private final MeasuringUnit unit;
        private final BigDecimal count;
    }

    @Getter
    @Builder
    public static class AllowanceOrCharge {
        private boolean isCharge;
        private Double percentage;
        private Amount amount;
        private Amount baseAmount;
        private TaxCategory taxCategory;
    }

    @Getter
    @Builder
    public static class TaxCategory {
        private final VATCategory categoryCode;
        private final TaxExemptionCode taxExemptionReasonCode;
        private final String taxExemptionReason;
        private final TaxScheme scheme;
    }

    @Getter
    @Builder
    public static class TaxTotal {
        private Amount taxAmount;
        private Amount roundingAmount;
        private TaxSubtotal taxSubtotal;
    }

    @Getter
    @Builder
    public static class TaxSubtotal {
        private Amount taxableAmount;
        private Amount taxAmount;
        private TaxCategory taxCategory;
    }
}
