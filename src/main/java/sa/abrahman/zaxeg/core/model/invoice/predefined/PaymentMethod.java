package sa.abrahman.zaxeg.core.model.invoice.predefined;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * UN/EDIFACT 4461 Payment Methods
 */
@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    /**
     * Not defined legally enforceable agreement between two or more parties
     * (expressing a contractual right or a right to the payment of money).
     */
    INSTRUMENT_NOT_DEFINED(1),

    /** A credit transaction made through the automated clearing house system. */
    AUTOMATED_CLEARING_HOUSE_CREDIT(2),

    /** A debit transaction made through the automated clearing house system. */
    AUTOMATED_CLEARING_HOUSE_DEBIT(3),

    /**
     * A request to reverse an ACH debit transaction to a demand deposit account.
     */
    ACH_DEMAND_DEBIT_REVERSAL(4),

    /** A request to reverse a credit transaction to a demand deposit account. */
    ACH_DEMAND_CREDIT_REVERSAL(5),

    /**
     * A credit transaction made through the ACH system to a demand deposit account.
     */
    ACH_DEMAND_CREDIT(6),

    /**
     * A debit transaction made through the ACH system to a demand deposit account.
     */
    ACH_DEMAND_DEBIT(7),

    /**
     * Indicates that the bank should hold the payment for collection by the
     * beneficiary or other instructions.
     */
    HOLD(8),

    /**
     * Indicates that the payment should be made using the national or regional
     * clearing.
     */
    NATIONAL_OR_REGIONAL_CLEARING(9),

    /**
     * Payment by currency (including bills and coins) in circulation, including
     * checking account deposits.
     */
    IN_CASH(10),

    /**
     * Alias for {@code IN_CASH}
     */
    CASH(10),

    /** A request to reverse an ACH credit transaction to a savings account. */
    ACH_SAVINGS_CREDIT_REVERSAL(11),

    /** A request to reverse an ACH debit transaction to a savings account. */
    ACH_SAVINGS_DEBIT_REVERSAL(12),

    /** A credit transaction made through the ACH system to a savings account. */
    ACH_SAVINGS_CREDIT(13),

    /** A debit transaction made through the ACH system to a savings account. */
    ACH_SAVINGS_DEBIT(14),

    /**
     * A credit entry between two accounts at the same bank branch. Synonym: house
     * credit.
     */
    BOOKENTRY_CREDIT(15),

    /**
     * A debit entry between two accounts at the same bank branch. Synonym: house
     * debit.
     */
    BOOKENTRY_DEBIT(16),

    /**
     * A credit transaction made through the ACH system to a demand deposit account
     * using the CCD payment format.
     */
    ACH_DEMAND_CCD_CREDIT(17),

    /**
     * A debit transaction made through the ACH system to a demand deposit account
     * using the CCD payment format.
     */
    ACH_DEMAND_CCD_DEBIT(18),

    /**
     * A credit transaction made through the ACH system to a demand deposit account
     * using the CTP payment format.
     */
    ACH_DEMAND_CTP_CREDIT(19),

    /**
     * Payment by a pre-printed form on which instructions are given to an account
     * holder (a bank or building society) to pay a stated sum to a named recipient.
     */
    CHEQUE(20),

    /** Issue of a banker's draft in payment of the funds. */
    BANKER_DRAFT(21),

    /**
     * Cheque drawn by a bank on itself or its agent. A person who owes money to
     * another buys the draft from a bank for cash and hands it to the creditor who
     * need have no fear that it might be dishonoured.
     */
    CERTIFIED_BANKER_DRAFT(22),

    /**
     * Payment by a pre-printed form, which has been completed by a financial
     * institution, on which instructions are given to an account holder (a bank or
     * building society) to pay a stated sum to a named recipient.
     */
    BANK_CHEQUE(23),

    /**
     * Bill drawn by the creditor on the debtor but not yet accepted by the debtor.
     */
    BILL_OF_EXCHANGE_AWAITING_ACCEPTANCE(24),

    /**
     * Payment by a pre-printed form stamped with the paying bank's certification on
     * which instructions are given to an account holder (a bank or building
     * society) to pay a stated sum to a named recipient .
     */
    CERTIFIED_CHEQUE(25),

    /** Indicates that the cheque is given local to the recipient. */
    LOCAL_CHEQUE(26),

    /**
     * A debit transaction made through the ACH system to a demand deposit account
     * using the CTP payment format.
     */
    ACH_DEMAND_CTP_DEBIT(27),

    /**
     * A credit transaction made through the ACH system to a demand deposit account
     * using the CTX payment format.
     */
    ACH_DEMAND_CTX_CREDIT(28),

    /**
     * A debit transaction made through the ACH system to a demand account using the
     * CTX payment format.
     */
    ACH_DEMAND_CTX_DEBIT(29),

    /** Payment by credit movement of funds from one account to another. */
    CREDIT_TRANSFER(30),

    /** Payment by debit movement of funds from one account to another. */
    DEBIT_TRANSFER(31),

    /**
     * A credit transaction made through the ACH system to a demand deposit account
     * using the CCD+ payment format.
     */
    ACH_DEMAND_CCD_PLUS_CREDIT(32),

    /**
     * A debit transaction made through the ACH system to a demand deposit account
     * using the CCD+ payment format.
     */
    ACH_DEMAND_CCD_PLUS_DEBIT(33),

    /**
     * A consumer credit transaction made through the ACH system to a demand deposit
     * or savings account.
     */
    ACH_PPD(34),

    /**
     * A credit transaction made through the ACH system to a demand deposit or
     * savings account.
     */
    ACH_SAVINGS_CCD_CREDIT(35),

    /**
     * A debit transaction made through the ACH system to a savings account using
     * the CCD payment format.
     */
    ACH_SAVINGS_CCD_DEBIT(36),

    /**
     * A credit transaction made through the ACH system to a savings account using
     * the CTP payment format.
     */
    ACH_SAVINGS_CTP_CREDIT(37),

    /**
     * A debit transaction made through the ACH system to a savings account using
     * the CTP payment format.
     */
    ACH_SAVINGS_CTP_DEBIT(38),

    /**
     * A credit transaction made through the ACH system to a savings account using
     * the CTX payment format.
     */
    ACH_SAVINGS_CTX_CREDIT(39),

    /**
     * A debit transaction made through the ACH system to a savings account using
     * the CTX payment format.
     */
    ACH_SAVINGS_CTX_DEBIT(40),

    /**
     * A credit transaction made through the ACH system to asavings account using
     * the CCD+ payment format.
     */
    ACH_SAVINGS_CCD_PLUS_CREDIT(41),

    /**
     * Payment by an arrangement for settling debts that isoperated by the Post
     * Office.
     */
    PAYMENT_TO_BANK_ACCOUNT(42),

    /**
     * A debit transaction made through the ACH system to asavings account using the
     * CCD+ payment format.
     */
    ACH_SAVINGS_CCD_PLUS_DEBIT(43),

    /** Bill drawn by the creditor on the debtor and accepted bythe debtor. */
    ACCEPTED_BILL_OF_EXCHANGE(44),

    /** A referenced credit transfer initiated through home-banking. */
    REFERENCED_HOME_BANKING_CREDIT_TRANSFER(45),

    /** A debit transfer via interbank means. */
    INTERBANK_DEBIT_TRANSFER(46),

    /** A debit transfer initiated through home-banking. */
    HOME_BANKING_DEBIT_TRANSFER(47),

    /**
     * Payment by means of a card issued by a bank or otherfinancial institution.
     */
    BANK_CARD(48),

    /**
     * The amount is to be, or has been, directly debited tothe customer's bank
     * account.
     */
    DIRECT_DEBIT(49),

    /**
     * A method for the transmission of funds through thepostal system rather than
     * through the banking system.
     */
    PAYMENT_BY_POSTGIRO(50),

    /**
     * A French standard procedure that allows a debtor to payan amount due to a
     * creditor. The creditor will forwardit to its bank, which will collect the
     * money on the bankaccount of the debtor.
     */
    FR_NORME_6_97_TELEREGLEMENT_CFONB_OPTION_A(51),

    /**
     * Payment order which requires guaranteed processing bythe most appropriate
     * means to ensure it occurs on therequested execution date, provided that it is
     * issued tothe ordered bank before the agreed cut-off time.
     */
    URGENT_COMMERCIAL_PAYMENT(52),

    /**
     * Payment order or transfer which must be executed, by themost appropriate
     * means, as urgently as possible andbefore urgent commercial payments.
     */
    URGENT_TREASURY_PAYMENT(53),

    /** Payment made by means of credit card. */
    CREDIT_CARD(54),

    /** Payment made by means of debit card. */
    DEBIT_CARD(55),

    /** Payment will be, or has been, made by bankgiro. */
    BANKGIRO(56),

    /**
     * The payment means have been previously agreed betweenseller and buyer and
     * thus are not stated again.
     */
    STANDING_AGREEMENT(57),

    /** Credit transfer inside the Single Euro Payment Area(SEPA) system. */
    SEPA_CREDIT_TRANSFER(58),

    /** Direct debit inside the Single Euro Payment Area (SEPA)system. */
    SEPA_DIRECT_DEBIT(59),

    PROMISSORY_NOTE(60);

    private final Integer code;
}
