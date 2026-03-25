package sa.abrahman.zaxeg.core.service.validator;

import lombok.experimental.UtilityClass;

@UtilityClass
public class InvoiceValidationRule {
    // BR - Integrity Constraints
    public static final String BR_02 = "[BR-02]: An Invoice shall have an Invoice number (BT-1).";
    public static final String BR_03 = "[BR-03]: An Invoice shall have an Invoice issue date (BT-2).";
    public static final String BR_04 = "[BR-04]: An Invoice shall have an Invoice type code (BT-3).";
    public static final String BR_05 = "[BR-05]: An Invoice shall have an Invoice currency code (BT-5).";
    public static final String BR_06 = "[BR-06]: An Invoice shall contain the Seller name (BT-27).";
    public static final String BR_08 = "[BR_08]: An Invoice shall contain the Seller postal address (BG-5).";
    public static final String BR_09 = "[BR_09]: The Seller postal address (BG-5) shall contain a Seller country code (BT-40).";
    public static final String BR_10 = "[BR_10]: An Invoice shall contain the Buyer postal address (BG-8). Not applicable for simplified tax invoices and associated credit notes and debit notes (KSA-2, position 1 and 2 = 02).";
    public static final String BR_13 = "[BR_13]: An Invoice shall have the Invoice total amount without VAT (BT-109).";
    public static final String BR_14 = "[BR_14]: An Invoice shall have the Invoice total amount with VAT (BT-112).";
    public static final String BR_15 = "[BR_15]: An Invoice shall have the Amount due for payment (BT-115).";
    public static final String BR_16 = "[BR_16]: An Invoice shall have at least one Invoice line (BG-25)";
    public static final String BR_21 = "[BR_21]: Each Invoice line (BG-25) shall have an Invoice line identifier (BT-126).";
    public static final String BR_22 = "[BR_22]: Each Invoice line (BG-25) shall have an Invoiced quantity (BT-129).";
    public static final String BR_24 = "[BR_24]: Each Invoice line (BG-25) shall have an Invoice line net amount (BT-131).";
    public static final String BR_25 = "[BR_25]: Each Invoice line (BG-25) shall contain the Item name (BT-153).";
    public static final String BR_26 = "[BR_26]: Each Invoice line (BG-25) shall contain the Item net price (BT-146).";
    public static final String BR_31 = "[BR_31]: Each Document level allowance (BG-20) shall have a Document level allowance amount (BT-92).";
    public static final String BR_32 = "[BR_32]: Each Document level allowance (BG-20) shall have a Document level allowance VAT category code (BT-95).";
    public static final String BR_36 = "[BR_36]: Each Document level charge (BG-21) shall have a Document level charge amount (BT99).";
    public static final String BR_37 = "[BR_37]: Each Document level charge (BG-21) shall have a Document level charge VAT category code (BT-102).";
    public static final String BR_41 = "[BR_41]: Each Invoice line allowance (BG-27) shall have an Invoice line allowance amount (BT136).";
    public static final String BR_43 = "[BR_43]: Each Invoice line charge (BG-28) shall have an Invoice line charge amount (BT-141).";
    public static final String BR_45 = "[BR_45]: Each VAT breakdown (BG-23) shall have a VAT category taxable amount (BT-116).";
    public static final String BR_46 = "[BR_46]: Each VAT breakdown (BG-23) shall have a VAT category tax amount (BT-117).";
    public static final String BR_47 = "[BR_47]: Each VAT breakdown (BG-23) shall be defined through a VAT category code (BT118).";
    public static final String BR_48 = "[BR_48]: Each VAT breakdown (BG-23) shall have a VAT category rate (BT-119), except if the Invoice is not subject to VAT.";
    public static final String BR_49 = "[BR_49]: A Payment instruction (BG-16) shall specify the Payment means type code (BT-81).";
    public static final String BR_53 = "[BR_53]: If the VAT accounting currency code (BT-6) is present, then the Invoice total VAT amount in accounting currency (BT-111) shall be provided.";
    public static final String BR_55 = "[BR_55]: Each Preceding Invoice reference (BG-3) shall contain a Preceding Invoice reference (BT-25).";

    // KSA - Business Rules
    public static final String BR_KSA_03 = "[BR-KSA-03]: The invoice must contain a unique identifier (“UUID”) (KSA-1) given by the machine that issued the document (unique message identifier for interchange process). This value must contain only letters, digits, and dashes. (Note: In Windows OS UUIDs are referred to by the term GUID.)";
    public static final String BR_KSA_04 = "[BR-KSA-04]: The document issue date (BT-2) must be less or equal to the current date";
    public static final String BR_KSA_06 = "[BR-KSA-06]: The invoice transaction code (KSA-2) must exist and respect the following structure: `NNPNESB` where `NN` (positions 1 and 2) = invoice subtype: [`01` for tax invoice, `02` for simplified tax invoice]. `P` (position 3) = '1' if the invoice is 3rd Party invoice, '0' otherwise. `N` (position 4) = '1' if the invoice is Nominal invoice, '0' otherwise. `E` (position 5) = '1' if the invoice is Exports invoice, '0' otherwise. `S` (position 6) = '1' if the invoice is Summary invoice, '0' otherwise. `B` (position 7) = '1' if the invoice is Self billed invoice, '0' otherwise.";
    public static final String BR_KSA_07 = "[BR-KSA-07]: Self-billing is not allowed for export invoices";
    public static final String BR_KSA_15 = "[BR-KSA-15]: The tax invoice ((invoice type code (BT-30) = 388) & (invoice transaction code (KSA-2) has “01” as first 2 digits)) must contain the supply date (KSA-5).";
    public static final String BR_KSA_17 = "[BR-KSA-17]: Debit and credit note (invoice type code (BT-3) is equal to 383 or 381) must contain the reason (KSA-10) for this invoice type issuing.";
    public static final String BR_KSA_27 = "[BR-KSA-27]: The document must contain a QR code (KSA-14), and this code must be base64 encoded.";
    public static final String BR_KSA_33 = "[BR-KSA-33]: Each invoice must have an invoice counter value";
    public static final String BR_KSA_34 = "[BR-KSA-34]: The invoice counter value (KSA-16) contains only digits";
    public static final String BR_KSA_35 = "[BR-KSA-35]: If the invoice contains a supply end date (KSA-24), then the invoice must contain a supply date (KSA-5)";
    public static final String BR_KSA_36 = "[BR-KSA-36]: If the invoice contains a supply end date (KSA-24), then this date must be greater than or equal to the supply date (KSA-5)";
    public static final String BR_KSA_56 = "[BR-KSA-56]: For credit notes ((BT-3) has the value of 381) and debit notes ((BT-3) has the value of 383), the billing reference ID (BT-25) is mandatory";
    public static final String BR_KSA_60 = "[BR-KSA-60]: Cryptographic stamp (KSA-15) must exist in simplified tax invoices and associated credit notes and debit notes (KSA-2, position 1 and 2 = 02)";
    public static final String BR_KSA_61 = "[BR-KSA-61]: Previous invoice hash (KSA-13) must exist in an invoice";
    public static final String BR_KSA_68 = "[BR-KSA-68]: Tax currency code (BT-6) must exist in an invoice";
    public static final String BR_KSA_70 = "[BR-KSA-70]: The invoice must contain an Invoice issue times (KSA-25) This value will be in the format: hh:mm:ss for time expressed in AST or hh:mm:ssZ for time expressed in UTC";
    public static final String BR_KSA_72 = "[BR-KSA-72]: If the Invoice is a simplified invoice type and is a summary invoice (KSA-2, position 1 and 2 = 02, position 6 = 1), then a supply date (KSA-5) and supply end date (KSA-24) must be present";

    // KSA - EN16931 Rules
    public static final String BR_KSA_EN16391_03 = "VAT accounting currency code (BT-6) must be \"SAR\"";
}
