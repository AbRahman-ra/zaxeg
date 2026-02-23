# Scratches

## Validating an invoice

**Validating invoice requires 4 stages:**

1. [Validating XSD](#1-ubl-invoice-elements-order-is-important)
2. Validating EnSchematron
3. Validating KSA Schematron
    1. Validating QR & Signature for Simplified Invoices
4. Validating PIH

## 1. UBL Invoice elements (ORDER IS IMPORTANT)

- REFER TO [THIS DOCS](https://www.datypic.com/sc/ubl21/e-ns39_Invoice.html) FOR TYPES

| serial | element_name | description | attributes | min | max | singular_type | whole_type | type_complexity | required | notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | ext:UBLExtensions | A container for all extensions present in the document. | NULL | 0 | 1 | unknown | unknown | <label style="color: yellow">unknown</label> | <span style="color: lightcoral">false</span> | NULL |
| 2 | cbc:UBLVersionID | Identifies the earliest version of the UBL 2 schema for this document type that defines all of the elements that might be encountered in the current instance. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 3 | cbc:CustomizationID | Identifies a user-defined customization of UBL for a specific use. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 4 | cbc:ProfileID | Identifies a user-defined profile of the customization of UBL being used. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | According to KSA rules, it MUST BE "reporting:1.0" |
| 5 | cbc:ProfileExecutionID | Identifies an instance of executing a profile, to associate all transactions in a collaboration. | NULL | 0 | 1 | string | string | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 6 | cbc:ID | An identifier for this document, assigned by the sender. | NULL | 1 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: chartreuse">true</span> | beep |
| 7 | cbc:CopyIndicator | Indicates whether this document is a copy (true) or not (false). | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 8 | cbc:UUID | A universally unique identifier for an instance of this document. | NULL | 0 | 1 | UUID | UUID | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | According to KSA Rules: The invoice must contain a unique identifier (“UUID”) (KSA-1) given by the machine that issued the document (unique message identifier for interchange process). This value must contain only letters, digits, and dashes. (Note: In Windows OS UUIDs are referred to by the term GUID.) |
| 9 | cbc:IssueDate | The date, assigned by the sender, on which this document was issued. | NULL | 1 | 1 | DATE (yyyy-MM-dd) | DATE (yyyy-MM-dd) | <label style="color: chartreuse">simple</label> | <span style="color: chartreuse">true</span> | beep |
| 10 | cbc:IssueTime | The time, assigned by the sender, at which this document was issued. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | The invoice must contain an Invoice issue times (KSA-25) This value will be in the format: hh:mm:ss for time expressed in AST (UTC+3) or hh:mm:ssZ for time expressed in UTC. Time format is 24-hours |
| 11 | cbc:DueDate | The date on which Invoice is due. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 12 | cbc:InvoiceTypeCode | A code signifying the type of the Invoice. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | [notes](#invoice-type-codes) |
| 13 | cbc:Note | Free-form text pertinent to this document, conveying information that is not contained explicitly in other structures. | NULL | 0 | unbounded | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Only mentioned in one file 'Simplified Invoice' with value 'ABC', no docs for it |
| 14 | cbc:TaxPointDate | The date of the Invoice, used to indicate the point at which tax becomes applicable. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 15 | cbc:DocumentCurrencyCode | A code signifying the default currency for this document. | NULL | 0 | 1 | enum<string> | enum<string> | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Matches [ISO 4217:2015](https://www.iban.com/currency-codes) |
| 16 | cbc:TaxCurrencyCode | A code signifying the currency used for tax amounts in the Invoice. | NULL | 0 | 1 | enum<string> | enum<string> | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Matches [ISO 4217:2015](https://www.iban.com/currency-codes) |
| 17 | cbc:PricingCurrencyCode | A code signifying the currency used for prices in the Invoice. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 18 | cbc:PaymentCurrencyCode | A code signifying the currency used for payment in the Invoice. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 19 | cbc:PaymentAlternativeCurrencyCode | A code signifying the alternative currency used for payment in the Invoice. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 20 | cbc:AccountingCostCode | The buyer's accounting code, applied to the Invoice as a whole. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 21 | cbc:AccountingCost | The buyer's accounting code, applied to the Invoice as a whole, expressed as text. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 22 | cbc:LineCountNumeric | The number of lines in the document. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 23 | cbc:BuyerReference | A reference provided by the buyer used for internal routing of the document. | NULL | 0 | 1 | unknown | unknown | <label style="color: chartreuse">simple</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 24 | cac:InvoicePeriod | A period to which the Invoice applies. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 25 | cac:OrderReference | A reference to the Order with which this Invoice is associated. | NULL | 0 | 1 | string | string | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | max. 127 characters, Never mentioned in samples, complex type in UBL but KSA see it as `{"cbc:ID": string}` |
| 26 | cac:BillingReference | A reference to a billing document associated with this document. | NULL | 0 | unbounded | [type](https://www.datypic.com/sc/ubl21/e-cac_BillingReference.html) | [type[]](https://www.datypic.com/sc/ubl21/e-cac_BillingReference.html) | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | KSA rules see it as `{"cac:InvoiceDocumentReference": {"cbc:ID": string}}` but it's complex than that in UBL |
| 27 | cac:DespatchDocumentReference | A reference to a Despatch Advice associated with this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 28 | cac:ReceiptDocumentReference | A reference to a Receipt Advice associated with this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 29 | cac:StatementDocumentReference | A reference to a Statement associated with this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 30 | cac:OriginatorDocumentReference | A reference to an originator document associated with this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 31 | cac:ContractDocumentReference | A reference to a contract associated with this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples, complex type in UBL but KSA see it as `{"cbc:ID": string}` |
| 32 | cac:AdditionalDocumentReference | A reference to an additional document associated with this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | [notes](#additional-document-reference) |
| 33 | cac:ProjectReference | Information about a project. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | Never mentioned in samples |
| 34 | cac:Signature | A signature applied to this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | STOPPED HERE |
| 35 | cac:AccountingSupplierParty | The accounting supplier party. | NULL | 1 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: chartreuse">true</span> | beep |
| 36 | cac:AccountingCustomerParty | The accounting customer party. | NULL | 1 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: chartreuse">true</span> | beep |
| 37 | cac:PayeeParty | The payee. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 38 | cac:BuyerCustomerParty | The buyer. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 39 | cac:SellerSupplierParty | The seller. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 40 | cac:TaxRepresentativeParty | The tax representative. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 41 | cac:Delivery | A delivery associated with this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 42 | cac:DeliveryTerms | A set of delivery terms associated with this document. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 43 | cac:PaymentMeans | Expected means of payment. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 44 | cac:PaymentTerms | A set of payment terms associated with this document. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 45 | cac:PrepaidPayment | A prepaid payment. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 46 | cac:AllowanceCharge | A discount or charge that applies to a price component. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 47 | cac:TaxExchangeRate | The exchange rate between the document currency and the tax currency. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 48 | cac:PricingExchangeRate | The exchange rate between the document currency and the pricing currency. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 49 | cac:PaymentExchangeRate | The exchange rate between the document currency and the payment currency. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 50 | cac:PaymentAlternativeExchangeRate | The exchange rate between the document currency and the payment alternative currency. | NULL | 0 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 51 | cac:TaxTotal | The total amount of a specific type of tax. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 52 | cac:WithholdingTaxTotal | The total withholding tax. | NULL | 0 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: lightcoral">false</span> | beep |
| 53 | cac:LegalMonetaryTotal | The total amount payable on the Invoice, including Allowances, Charges, and Taxes. | NULL | 1 | 1 | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: chartreuse">true</span> | beep |
| 54 | cac:InvoiceLine | A line describing an invoice item. | NULL | 1 | unbounded | unknown | unknown | <label style="color: lightcoral">complex</label> | <span style="color: chartreuse">true</span>  beep |


### Invoice Type Codes

- For Tax Invoice, code is 388 and subtype is 01. ex. `<cbc:InvoiceTypeCode name=”0100000”>388</cbc:InvoiceTypeCode>`
- For Simplified Tax Invoice, code is 388 and subtype is 02. ex. `<cbc:InvoiceTypeCode name=”0200000”>388</cbc:InvoiceTypeCode>`
- For tax invoice debit note, code is 383 and subtype is 01. ex. `<cbc:InvoiceTypeCode name=”0100000”>383</cbc:InvoiceTypeCode>`
- For simplified debit note, code is 383 and subtype is 02. ex. `<cbc:InvoiceTypeCode name=”0200000”>383</cbc:InvoiceTypeCode>`
- For tax invoice credit note, code is 381 and subtype is 01. ex. `<cbc:InvoiceTypeCode name=”0100000”>381</cbc:InvoiceTypeCode>`
- For simplified credit note, code is 381 and subtype is 02. ex. `<cbc:InvoiceTypeCode name=”0200000”>381</cbc:InvoiceTypeCode>`
- For Prepayment Tax Invoice, code is 386 and subtype is 01. ex. `<cbc:InvoiceTypeCode name=”0100000”>386</cbc:InvoiceTypeCode>`
- For Prepayment Simplified Tax Invoice, code is 386 and subtype is 02. ex. `<cbc:InvoiceTypeCode name=”0200000”>386</cbc:InvoiceTypeCode>`

### Additional Document Reference

- In `/ ubl:Invoice / cac:AdditionalDocumentReference / cac:Attachment / cbc:EmbeddedDocumentBinaryObject`. If the invoice contains the previous invoice hash (KSA-13), this hash must be base64 encoded SHA256. The hash shall be computed using the following method as described in the ds:transforms block in the XML Invoice Specifications:
    1. Remove the `<Invoice><ext:UBLExtensions/>` block
    2. Remove the `<invoice><cac:AdditionalDocumentReference/>` block where `<cbc:ID/> = QR`
    3. Remove the `<invoice><cac:Signature/>` block
    4. Canonicalize the Invoice using the C14N11 standard
    5. Hash the resulting string using SHA256 to a binary object
    6. Base64 encode the binary object to generate the digest value
- For the first invoice, the previous invoice hash is "NWZlY2ViNjZmZmM4NmYzOGQ5NTI3ODZjNmQ2OTZjNzljMmRiYzIzOWRkNGU5MWI0NjcyOWQ3M2EyN2ZiNTdlOQ==", the equivalent for base64 encoded SHA256 of "0" (zero) character
- The document must contain aa QR code (KSA-14), and this code must be base64Binary.
- Mime code must be according to subset of IANA code list.
- Sample
    ```xml
    <cac:AdditionalDocumentReference>
        <cbc:ID>ICV</cbc:ID>
        <cbc:UUID>70</cbc:UUID>
    </cac:AdditionalDocumentReference>

    <!-- Please note that the hash value is a sample value only -->
    <cac:AdditionalDocumentReference>
        <cbc:ID>PIH</cbc:ID>
        <cac:Attachment>
            <cbc:EmbeddedDocumentBinaryObject mimeCode="text/plain">NWZlY2ViNjZmZmM4NmYzOGQ5NTI3ODZjNmQ2OTZjNzljMmRiYzIzOWRkNGU5MWI0NjcyOWQ3M2EyN2ZiNTdlOQ==</cbc:EmbeddedDocumentBinaryObject>
        </cac:Attachment>
    </cac:AdditionalDocumentReference>
    
    <!-- Please note that the signature value is a sample value only -->
    <cac:AdditionalDocumentReference>
            <cbc:ID>QR</cbc:ID>
            <cac:Attachment>
                <cbc:EmbeddedDocumentBinaryObject mimeCode="text/plain">AW/YtNix2YPYqSDYqtmI2LHZitivINin2YTYqtmD2YbZiNmE2YjYrNmK2Kcg2KjYo9mC2LXZiSDYs9ix2LnYqSDYp9mE2YXYrdiv2YjYr9ipIHwgTWF4aW11bSBTcGVlZCBUZWNoIFN1cHBseSBMVEQCDzM5OTk5OTk5OTkwMDAwMwMTMjAyMS0wMS0wNVQwOTozMjo0MAQGMjUwLjAwBQQwLjAwBiw4QURMeEpRejFpOGhTcXFHYUZSWm9SSmtrSGpRWmpvS0J6djVwUG14REdNPQdgTUVZQ0lRQ29yRVJNTmd1TExkeHJtNExPUytFdkdMTW5IUXlWQStsOEhrVFFJSWJvYlFJaEFQUTdNVHpJTXhxU2g4SHlUK0pFUm9Makg0SWxCaFJPOElLTDRPRDlzd3NpCFgwVjAQBgcqhkjOPQIBBgUrgQQACgNCAAShYIprRJr0UgStM6/S4CQLVUgpfFT2c+nHa+V/jKEx6PLxzTZcluUOru0/J2jyarRqE4yY2jyDCeLte3UpP1R4CUcwRQIhALE/ichmnWXCUKUbca3yci8oqwaLvFdHVjQrveI9uqAbAiA9hC4M8jgMBADPSzmd2uiPJA6gKR3LE03U75eqbC/rXA==</cbc:EmbeddedDocumentBinaryObject>
            </cac:Attachment>
    </cac:AdditionalDocumentReference>
    ```

