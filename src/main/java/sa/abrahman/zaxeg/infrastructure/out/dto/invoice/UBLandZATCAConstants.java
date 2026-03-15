package sa.abrahman.zaxeg.infrastructure.out.dto.invoice;

public interface UBLandZATCAConstants {
    // Namespaces
    String NAMESPACE_ROOT = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    String NAMESPACE_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    String NAMESPACE_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";

    // Element Tags
    String TAG_INVOICE = "Invoice";
    String TAG_UBL_EXTENSIONS = "ext:UBLExtensions";
    String TAG_UBL_VERSION_ID = "cbc:UBLVersionID";
    String TAG_CUSTOMIZATION_ID = "cbc:CustomizationID";
    String TAG_PROFILE_ID = "cbc:ProfileID";
    String TAG_PROFILE_EXECUTION_ID = "cbc:ProfileExecutionID";
    String TAG_ID = "cbc:ID";
    String TAG_COPY_INDICATOR = "cbc:CopyIndicator";
    String TAG_UUID = "cbc:UUID";
    String TAG_ISSUE_DATE = "cbc:IssueDate";
    String TAG_ISSUE_TIME = "cbc:IssueTime";
    String TAG_DUE_DATE = "cbc:DueDate";
    String TAG_INVOICE_TYPE_CODE = "cbc:InvoiceTypeCode";
    String TAG_NOTE = "cbc:Note";
    String TAG_TAX_POINT_DATE = "cbc:TaxPointDate";
    String TAG_DOCUMENT_CURRENCY_CODE = "cbc:DocumentCurrencyCode";
    String TAG_TAX_CURRENCY_CODE = "cbc:TaxCurrencyCode";
    String TAG_PRICINF_CURRENCY_CODE = "cbc:PricingCurrencyCode";
    String TAG_PAYMENT_CURRENCY_CODE = "cbc:PaymentCurrencyCode";
    String TAG_PAYMENT_ALTERNATIVE_CURRENCY_CODE = "cbc:PaymentAlternativeCurrencyCode";
    String TAG_ACCOUNTING_COST_CODE = "cbc:AccountingCostCode";
    String TAG_ACCOUNTING_COST = "cbc:AccountingCost";
    String TAG_LINE_COUNT_NUMERIC = "cbc:LineCountNumeric";
    String TAG_BUYER_REFERENCE = "cbc:BuyerReference";
    String TAG_INVOICE_PERIOD = "cac:InvoicePeriod";
    String TAG_ORDER_REFERENCE = "cac:OrderReference";
    String TAG_BILLING_REFERENCE = "cac:BillingReference";
    String TAG_DESPATCH_DOCUMENT_REFERENCE = "cac:DespatchDocumentReference";
    String TAG_RECEIPT_DOCUMENT_REFERENCE = "cac:ReceiptDocumentReference";
    String TAG_STATEMENT_DOCUMENT_REFERENCE = "cac:StatementDocumentReference";
    String TAG_ORIGINATOR_DOCUMENT_REFERENCE = "cac:OriginatorDocumentReference";
    String TAG_CONTRACT_DOCUMENT_REFERENCE = "cac:ContractDocumentReference";
    String TAG_ADDITIONAL_DOCUMENT_REFERENCE = "cac:AdditionalDocumentReference";
    String TAG_PROJECT_REFERENCE = "cac:ProjectReference";
    String TAG_SIGNATURE = "cac:Signature";
    String TAG_PARTY = "cac:Party";
    String TAG_ENDPOINT_ID = "cbc:EndpointID";
    String TAG_ACCOUNTING_SUPPLIER_PARTY = "cac:AccountingSupplierParty";
    String TAG_ACCOUNTING_CUSTOMER_PARTY = "cac:AccountingCustomerParty";
    String TAG_PAYEE_PARTY = "cac:PayeeParty";
    String TAG_BUYER_CUSTOMER_PARTY = "cac:BuyerCustomerParty";
    String TAG_SELLER_SUPPLIER_PARTY = "cac:SellerSupplierParty";
    String TAG_TAX_REPRESENTATIVE_PARTY = "cac:TaxRepresentativeParty";
    String TAG_DELIVERY = "cac:Delivery";
    String TAG_DELIVERY_TERMS = "cac:DeliveryTerms";
    String TAG_PAYMENT_MEANS = "cac:PaymentMeans";
    String TAG_PAYMENT_TERMS = "cac:PaymentTerms";
    String TAG_PREPAID_PAYMENT = "cac:PrepaidPayment";
    String TAG_ALLOWANCE_CHARGE = "cac:AllowanceCharge";
    String TAG_TAX_EXCHANGE_RATE = "cac:TaxExchangeRate";
    String TAG_PRICING_EXCHANGE_RATE = "cac:PricingExchangeRate";
    String TAG_PAYMENT_EXCHANGE_RATE = "cac:PaymentExchangeRate";
    String TAG_PAYMENT_ALTERNATIVE_EXCHANGE_RATE = "cac:PaymentAlternativeExchangeRate";
    String TAG_TAX_TOTAL = "cac:TaxTotal";
    String TAG_WITHHOLDING_TAX_TOTAL = "cac:WithholdingTaxTotal";
    String TAG_LEGAL_MONETARY_TOTAL = "cac:LegalMonetaryTotal";
    String TAG_INVOICE_LINE = "cac:InvoiceLine";
    String TAG_LINE_EXTENSION_AMOUNT = "cbc:LineExtensionAmount";
    String TAG_TAX_EXCLUSIVE_AMOUNT = "cbc:TaxExclusiveAmount";
    String TAG_TAX_INCLUSIVE_AMOUNT = "cbc:TaxInclusiveAmount";
    String TAG_PREPAID_AMOUNT = "cbc:PrepaidAmount";
    String TAG_PAYABLE_AMOUNT = "cbc:PayableAmount";

    // Attributes
    String ATTR_XMLNSCAC = "xmlns:cac";
    String ATTR_XMLNS_CBC = "xmlns:cbc"; // Added "_" before CBC to reduce confusion between CAC AND CBC
}
