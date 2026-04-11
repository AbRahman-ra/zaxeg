package sa.abrahman.zaxeg.adapter.generate.out.dto.depr;

public final class UBLandZATCAConstants {
    private UBLandZATCAConstants() {}

    public static final class NAMESPACES {
        private NAMESPACES() {}

        public static final String ROOT = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
        public static final String CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
        public static final String CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    }

    public static final class TAGS {
        private TAGS() {}

        public static final String INVOICE = "Invoice";

        public static final class CAC {
            private CAC() {}

            public static final String INVOICE_PERIOD = "cac:InvoicePeriod";
            public static final String ORDER_REFERENCE = "cac:OrderReference";
            public static final String BILLING_REFERENCE = "cac:BillingReference";
            public static final String DESPATCH_DOCUMENT_REFERENCE = "cac:DespatchDocumentReference";
            public static final String RECEIPT_DOCUMENT_REFERENCE = "cac:ReceiptDocumentReference";
            public static final String STATEMENT_DOCUMENT_REFERENCE = "cac:StatementDocumentReference";
            public static final String ORIGINATOR_DOCUMENT_REFERENCE = "cac:OriginatorDocumentReference";
            public static final String CONTRACT_DOCUMENT_REFERENCE = "cac:ContractDocumentReference";
            public static final String ADDITIONAL_DOCUMENT_REFERENCE = "cac:AdditionalDocumentReference";
            public static final String PROJECT_REFERENCE = "cac:ProjectReference";
            public static final String SIGNATURE = "cac:Signature";
            public static final String PARTY = "cac:Party";
            public static final String ACCOUNTING_SUPPLIER_PARTY = "cac:AccountingSupplierParty";
            public static final String ACCOUNTING_CUSTOMER_PARTY = "cac:AccountingCustomerParty";
            public static final String PAYEE_PARTY = "cac:PayeeParty";
            public static final String BUYER_CUSTOMER_PARTY = "cac:BuyerCustomerParty";
            public static final String SELLER_SUPPLIER_PARTY = "cac:SellerSupplierParty";
            public static final String TAX_REPRESENTATIVE_PARTY = "cac:TaxRepresentativeParty";
            public static final String DELIVERY = "cac:Delivery";
            public static final String DELIVERY_TERMS = "cac:DeliveryTerms";
            public static final String PAYMENT_MEANS = "cac:PaymentMeans";
            public static final String PAYMENT_TERMS = "cac:PaymentTerms";
            public static final String PREPAID_PAYMENT = "cac:PrepaidPayment";
            public static final String ALLOWANCE_CHARGE = "cac:AllowanceCharge";
            public static final String TAX_EXCHANGE_RATE = "cac:TaxExchangeRate";
            public static final String PRICING_EXCHANGE_RATE = "cac:PricingExchangeRate";
            public static final String PAYMENT_EXCHANGE_RATE = "cac:PaymentExchangeRate";
            public static final String PAYMENT_ALTERNATIVE_EXCHANGE_RATE = "cac:PaymentAlternativeExchangeRate";
            public static final String TAX_TOTAL = "cac:TaxTotal";
            public static final String TAX_CATEGORY = "cac:TaxCategory";
            public static final String TAX_SUBTOTAL = "cac:TaxSubtotal";
            public static final String WITHHOLDING_TAX_TOTAL = "cac:WithholdingTaxTotal";
            public static final String LEGAL_MONETARY_TOTAL = "cac:LegalMonetaryTotal";
            public static final String INVOICE_LINE = "cac:InvoiceLine";
            public static final String TAX_SCHEME = "cac:TaxScheme";
            public static final String CLASSIFIED_TAX_CATEGORY = "cac:ClassifiedTaxCategory";
            public static final String ITEM = "cac:Item";
            public static final String PRICE = "cac:Price";
            public static final String POSTAL_ADDRESS = "cac:PostalAddress";
            public static final String PARTY_TAX_SCHEME = "cac:PartyTaxScheme";
            public static final String PARTY_LEGAL_ENTITY = "cac:PartyLegalEntity";
            public static final String INVOICE_DOCUMENT_REFERENCE = "cac:InvoiceDocumentReference";
        }

        public static final class CBC {
            private CBC() {}

            public static final String UBL_VERSION_ID = "cbc:UBLVersionID";
            public static final String CUSTOMIZATION_ID = "cbc:CustomizationID";
            public static final String PROFILE_ID = "cbc:ProfileID";
            public static final String PROFILE_EXECUTION_ID = "cbc:ProfileExecutionID";
            public static final String ID = "cbc:ID";
            public static final String COPY_INDICATOR = "cbc:CopyIndicator";
            public static final String UUID = "cbc:UUID";
            public static final String ISSUE_DATE = "cbc:IssueDate";
            public static final String ISSUE_TIME = "cbc:IssueTime";
            public static final String DUE_DATE = "cbc:DueDate";
            public static final String INVOICE_TYPE_CODE = "cbc:InvoiceTypeCode";
            public static final String NOTE = "cbc:Note";
            public static final String TAX_POINT_DATE = "cbc:TaxPointDate";
            public static final String DOCUMENT_CURRENCY_CODE = "cbc:DocumentCurrencyCode";
            public static final String TAX_CURRENCY_CODE = "cbc:TaxCurrencyCode";
            public static final String PRICINF_CURRENCY_CODE = "cbc:PricingCurrencyCode";
            public static final String PAYMENT_CURRENCY_CODE = "cbc:PaymentCurrencyCode";
            public static final String PAYMENT_ALTERNATIVE_CURRENCY_CODE = "cbc:PaymentAlternativeCurrencyCode";
            public static final String ACCOUNTING_COST_CODE = "cbc:AccountingCostCode";
            public static final String ACCOUNTING_COST = "cbc:AccountingCost";
            public static final String LINE_COUNT_NUMERIC = "cbc:LineCountNumeric";
            public static final String BUYER_REFERENCE = "cbc:BuyerReference";
            public static final String ENDPOINT_ID = "cbc:EndpointID";
            public static final String TAXABLE_AMOUNT = "cbc:TaxableAmount";
            public static final String TAX_AMOUNT = "cbc:TaxAmount";
            public static final String LINE_EXTENSION_AMOUNT = "cbc:LineExtensionAmount";
            public static final String TAX_EXCLUSIVE_AMOUNT = "cbc:TaxExclusiveAmount";
            public static final String TAX_INCLUSIVE_AMOUNT = "cbc:TaxInclusiveAmount";
            public static final String PREPAID_AMOUNT = "cbc:PrepaidAmount";
            public static final String PAYABLE_AMOUNT = "cbc:PayableAmount";
            public static final String PERCENT = "cbc:Percent";
            public static final String NAME = "cbc:Name";
            public static final String INVOICED_QUANTITY = "cbc:InvoicedQuantity";
            public static final String PRICE_AMOUNT = "cbc:PriceAmount";
            public static final String IDENTIFICATION_CODE = "cbc:IdentificationCode";
            public static final String BUILDING_NUMBER = "cbc:BuildingNumber";
            public static final String STREET_NAME = "cbc:StreetName";
            public static final String CITY_SUBDIVISION_NAME = "cbc:CitySubdivisionName";
            public static final String CITY_NAME = "cbc:CityName";
            public static final String POSTAL_ZONE = "cbc:PostalZone";
            public static final String COUNTRY = "cbc:Country";
            public static final String COMPANY_ID = "cbc:CompanyID";
            public static final String REGISTRATION_NAME = "cbc:RegistrationName";
            public static final String PAYMENT_MEANS_CODE = "cbc:PaymentMeansCode";
            public static final String INSTRUCTION_NOTE = "cbc:InstructionNote";
            public static final String ACTUAL_DELIVERY_DATE = "cbc:ActualDeliveryDate";
            public static final String TAX_EXEMPTION_REASON_CODE = "cbc:TaxExemptionReasonCode";
            public static final String TAX_EXEMPTION_REASON = "cbc:TaxExemptionReason";
            public static final String CHARGE_INDICATOR = "cbc:ChargeIndicator";
            public static final String ALLOWANCE_CHARGE_REASON = "cbc:AllowanceChargeReason";
            public static final String AMOUNT = "cbc:Amount";
        }

        public static final class EXT {
            private EXT() {}

            public static final String UBL_EXTENSIONS = "ext:UBLExtensions";
        }
    }

    public static final class ATTRIBUTES {
        private ATTRIBUTES() {}

        public static final String NAME = "name";
        public static final String CURRENCY_ID = "currencyID";
        public static final String UNIT_CODE = "unitCode";
        public static final String SCHEME_ID = "schemeID";

        public static final class XMLNS {
            private XMLNS() {}

            public static final String CAC = "xmlns:cac";
            public static final String CBC = "xmlns:cbc";
        }
    }

    public static final class DEFAULTS {
        private DEFAULTS() {}

        public static final String PROFILE_ID = "reporting:1.0";
    }
}
