package sa.abrahman.zaxeg.core.validator.rule;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UblRules {
    public static final String BR_02 = "[BR-02]: An Invoice shall have an Invoice number (BT-1).";
    public static final String BR_03 = "[BR-03]: An Invoice shall have an Invoice issue date (BT-2).";
    public static final String BR_04 = "[BR-04]: An Invoice shall have an Invoice type code (BT-3).";
    public static final String BR_05 = "[BR-05]: An Invoice shall have an Invoice currency code (BT-5).";
    public static final String BR_06 = "[BR-06]: An Invoice shall contain the Seller name (BT-27).";
    public static final String BR_08 = "[BR_08]: An Invoice shall contain the Seller postal address (BG-5).";
    public static final String BR_06_08 = "[BR_06][BR_08]: An Invoice shall contain the Seller name (BT-27). An Invoice shall contain the Seller postal address (BG-5).";
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
    public static final String BR_41_43 = "[BR_41][BR_43]: Each Invoice line allowance/charge (BG-27)/(BG-28) shall have an Invoice line allowance/charge amount (BT136)/(BT-141).";
    public static final String BR_45 = "[BR_45]: Each VAT breakdown (BG-23) shall have a VAT category taxable amount (BT-116).";
    public static final String BR_46 = "[BR_46]: Each VAT breakdown (BG-23) shall have a VAT category tax amount (BT-117).";
    public static final String BR_47 = "[BR_47]: Each VAT breakdown (BG-23) shall be defined through a VAT category code (BT118).";
    public static final String BR_48 = "[BR_48]: Each VAT breakdown (BG-23) shall have a VAT category rate (BT-119), except if the Invoice is not subject to VAT.";
    public static final String BR_49 = "[BR_49]: A Payment instruction (BG-16) shall specify the Payment means type code (BT-81).";
    public static final String BR_53 = "[BR_53]: If the VAT accounting currency code (BT-6) is present, then the Invoice total VAT amount in accounting currency (BT-111) shall be provided.";
    public static final String BR_55 = "[BR_55]: Each Preceding Invoice reference (BG-3) shall contain a Preceding Invoice reference (BT-25).";
}
