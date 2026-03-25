package sa.abrahman.zaxeg.core.service.generator;

import java.util.function.Function;

import lombok.experimental.UtilityClass;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.model.invoice.checkout.LegalMonetaryTotals;
import sa.abrahman.zaxeg.core.model.invoice.common.*;
import sa.abrahman.zaxeg.core.model.invoice.line.*;
import sa.abrahman.zaxeg.core.model.invoice.metadata.DocumentReference;
import sa.abrahman.zaxeg.core.model.invoice.metadata.InvoiceTypeTransactions;
import sa.abrahman.zaxeg.core.model.invoice.party.*;
import sa.abrahman.zaxeg.core.model.invoice.wrapper.*;
import sa.abrahman.zaxeg.core.port.in.payload.*;

@UtilityClass
public class InvoiceFactory {
    public Invoice from(InvoiceGenerationPayload payload) {

        return Invoice.builder()
                .metadata(from(payload.getMetadata()))
                .parties(from(payload.getParties()))
                .lines(from(payload.getLines()))
                .checkout(from(payload.getCheckout()))
                .build();
    }


    /**
     * Map metadata it domain Model
     * @param metadataPayload
     * @return
     */
    public Metadata from(MetadataPayload metadataPayload) {
        Function<MetadataPayload.InvoiceTypeTransactions, InvoiceTypeTransactions> invTypeTransactionMapper = p -> InvoiceTypeTransactions
                .builder()
                .subtype(p.getSubtype())
                .thirdParty(p.isThirdParty())
                .nominal(p.isNominal())
                .exports(p.isExports())
                .summary(p.isSummary())
                .selfBilled(p.isSelfBilled())
                .build();

        Function<MetadataPayload.DocumentReference, DocumentReference> docRefMapper = ref -> new DocumentReference(ref.getId());
        return Metadata.builder()
                .invoiceNumber(metadataPayload.getInvoiceNumber())
                .invoiceUuid(metadataPayload.getInvoiceUuid())
                .invoiceIssueDate(metadataPayload.getIssueDate())
                .invoiceIssueTime(metadataPayload.getIssueTime())
                .invoiceDocumentType(metadataPayload.getInvoiceDocumentType())
                .invoiceTypeTransactions(invTypeTransactionMapper.apply(metadataPayload.getInvoiceTypeTransactions()))
                .notes(metadataPayload.getNotes())
                .invoiceCurrency(metadataPayload.getInvoiceCurrency())
                .taxCurrency(metadataPayload.getTaxCurrency())
                .purchaseOrder(docRefMapper.apply(metadataPayload.getPurchaseOrder()))
                .billingReference(docRefMapper.apply(metadataPayload.getBillingReference()))
                .contract(docRefMapper.apply(metadataPayload.getContract()))
                // phase ii
                .icv(null)
                .pih(null)
                .qr(null)
                .cryptographicStamp(null)
                // end phase ii
                .supplyDate(metadataPayload.getSupplyDate())
                .supplyEndDate(metadataPayload.getSupplyEndDate())
                .creditOrDebitNoteIssuanceReasons(metadataPayload.getCreditOrDebitNoteIssuanceReasons())
                .build();
    }

    /**
     * Map parties to domain model
     * @param payload
     * @return
     */
    public InvoiceParties from(PartiesPayload payload) {
        Function<PartiesPayload.PartyTaxScheme, PartyTaxScheme> taxSchemeMapper = ts -> PartyTaxScheme.of(ts.getCompanyId(), ts.getTaxScheme());
        Function<PartiesPayload.PartyIdentification, PartyIdentification> partyIdMapper = ppid -> PartyIdentification.of(ppid.getSchemeId(), ppid.getValue());
        Function<PartiesPayload.Address, Address> addressMapper = a -> Address.builder()
                .street(a.getStreet())
                .additionalStreet(a.getAdditionalStreet())
                .buildingNumber(a.getBuildingNumber())
                .additionalNumber(a.getAdditionalNumber())
                .city(a.getCity())
                .postalCode(a.getPostalCode())
                .provinceOrState(a.getProvinceOrState())
                .district(a.getDistrict())
                .country(a.getCountry())
                .build();

        Function<PartiesPayload.Party, Party> partyMapper = p -> Party.builder()
                .name(p.getName())
                .identification(taxSchemeMapper.apply(p.getIdentification()))
                .otherIds(p.getOtherIds().stream().map(partyIdMapper).toList())
                .address(addressMapper.apply(p.getAddress()))
                .build();
        return new InvoiceParties(partyMapper.apply(payload.getSeller()), partyMapper.apply(payload.getBuyer()));
    }

    /**
     * Map lines to domain model
     * @param payload
     * @return
     */
    public InvoiceLineWrapper from(LinesPayload payload) {
        Function<LinesPayload.Quantity, Quantity> qtyMapper = q -> Quantity.of(q.getCount(), q.getUnit());
        Function<LinesPayload.ItemPartyIdentifier, ItemPartyIdentifier> partyIdMapper = id -> new ItemPartyIdentifier(id.getId());

        Function<LinesPayload.InvoiceLineItem, InvoiceLineItem> itemMapper = i -> InvoiceLineItem.builder()
                .name(i.getName())
                .itemBuyerIdentifier(partyIdMapper.apply(i.getItemBuyerIdentifier()))
                .itemSellerIdentifier(partyIdMapper.apply(i.getItemSellerIdentifier()))
                .itemStandardIdentifier(partyIdMapper.apply(i.getItemStandardIdentifier()))
                .classifiedTaxCategory(from(i.getClassifiedTaxCategory()))
                .build();

        Function<LinesPayload.InvoiceLinePrice, InvoiceLinePrice> priceMapper = p -> InvoiceLinePrice.builder()
                .priceAmount(from(p.getAmount()))
                .baseQuantity(qtyMapper.apply(p.getQuantity()))
                .allowanceCharge(from(p.getAllowanceOrCharge()))
                .build();

        Function<LinesPayload.InvoiceLine, InvoiceLine> linesMapper = l -> InvoiceLine.builder()
                .id(l.getId())
                .quantity(qtyMapper.apply(l.getQuantity()))
                .netAmount(from(l.getNetAmount()))
                .allowanceCharges(l.getAllowanceCharges().stream().map(InvoiceFactory::from).toList())
                .vatLineAmount(from(l.getVatLineAmount()))
                .item(itemMapper.apply(l.getItem()))
                .price(priceMapper.apply(l.getPrice()))
                .build();
        return InvoiceLineWrapper.of(payload.getInvoiceLines().stream().map(linesMapper).toList());
    }

    /**
     * Map checkout details to domain model
     * @param payload
     * @return
     */
    public CheckoutDetails from(CheckoutDetailsPayload payload) {
        Function<CheckoutDetailsPayload.LegalMonetaryTotals, LegalMonetaryTotals> mapper = t -> LegalMonetaryTotals.builder()
                .lineExtensionAmount(from(t.getLineExtensionAmount()))
                .documentLevelAllowanceChargeTotalAmount(from(t.getDocumentLevelAllowanceChargeTotalAmount()))
                .invoiceTotalAmountWithoutVAT(from(t.getInvoiceTotalAmountWithoutVAT()))
                .totalInclusiveAmount(from(t.getTotalInclusiveAmount()))
                .prepaidAmount(from(t.getPrepaidAmount()))
                .payableAmount(from(t.getPayableAmount()))
                .build();

        return CheckoutDetails.builder()
                .paymentMeansType(payload.getPaymentMeansType())
                .paymentTerms(payload.getPaymentTerms())
                .paymentAccountIdentifier(payload.getPaymentAccountIdentifier())
                .documentLevelAllowanceCharges(payload.getDocumentLevelAllowanceCharges().stream().map(InvoiceFactory::from).toList())
                .legalMonetaryTotals(mapper.apply(payload.getLegalMonetaryTotals()))
                .invoiceTaxTotals(from(payload.getInvoiceTaxTotals()))
                .invoiceTaxTotalsInAccountingCurrency(from(payload.getInvoiceTaxTotalsInAccountingCurrency()))
                .build();
    }

    // ==================== HELPER MAPPERS FROM COMMON TYPES ==================== //
    public Amount from(PayloadCommons.Amount amount) {
        return Amount.of(amount.getValue(), amount.getCurrency());
    }

    public AllowanceOrCharge from(PayloadCommons.AllowanceOrCharge payload) {
        return AllowanceOrCharge.builder()
                .isCharge(payload.isCharge())
                .percentage(payload.getPercentage())
                .amount(from(payload.getAmount()))
                .baseAmount(from(payload.getBaseAmount()))
                .taxCategory(from(payload.getTaxCategory()))
                .build();
    }

    public TaxCategory from(PayloadCommons.TaxCategory payload) {
        return TaxCategory.builder()
                .categoryCode(payload.getCategoryCode())
                .taxExemptionReasonCode(payload.getTaxExemptionReasonCode())
                .taxExemptionReason(payload.getTaxExemptionReason())
                .scheme(payload.getScheme())
                .build();
    }

    public TaxTotal from(PayloadCommons.TaxTotal payload) {
        return TaxTotal.builder()
                .taxAmount(from(payload.getTaxAmount()))
                .roundingAmount(from(payload.getRoundingAmount()))
                .taxSubtotal(payload.getTaxSubtotal().stream().map(InvoiceFactory::from).toList())
                .build();
    }

    public TaxSubtotal from(PayloadCommons.TaxSubtotal payload) {
        return TaxSubtotal.builder()
                .taxableAmount(from(payload.getTaxableAmount()))
                .taxAmount(from(payload.getTaxAmount()))
                .taxCategory(from(payload.getTaxCategory()))
                .build();
    }
}
