package sa.abrahman.zaxeg.adapter.generate.in.dto.components;

import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import sa.abrahman.zaxeg.shared.constant.rule.*;
import sa.abrahman.zaxeg.shared.contract.Mapable;
import sa.abrahman.zaxeg.core.generate.domain.constant.Scheme;
import sa.abrahman.zaxeg.core.generate.domain.constant.TaxScheme;
import sa.abrahman.zaxeg.core.generate.domain.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.generate.port.in.payload.PartiesPayload;

@Getter
@NullMarked
public class PartiesRequest implements Mapable<PartiesPayload, Void> {

    @Valid
    @NotNull(message = UblRules.BR_06_08)
    @Schema(title = "Seller Information", requiredMode = RequiredMode.REQUIRED)
    private Party seller;

    @Valid
    @Nullable
    @Schema(title = "Seller Information", requiredMode = RequiredMode.NOT_REQUIRED)
    private Party buyer;

    @Override
    public PartiesPayload mapped(Void additionalData) {
        return new PartiesPayload(seller.mapped(), buyer != null ? buyer.mapped() : null);
    }

    // ==========================================================================
    // ============================= NESTED CLASSES =============================
    // ==========================================================================

    @Getter
    @NullUnmarked
    private static class Party implements Mapable<PartiesPayload.Party, Void> {

        @Schema(title = "Seller/Buyer Name")
        private String name = "";

        @Valid
        @Schema(title = "VAT Information")
        private PartyTaxScheme identification;

        @Valid
        @NonNull
        @Schema(title = "Other Seller/Buyer IDs")
        private List<PartyIdentification> otherIds = List.of();

        @Valid
        @Schema(title = "Seller/Buyer Address")
        private Address address;

        @Override
        public PartiesPayload.Party mapped(Void d) {
            return PartiesPayload.Party.builder().name(this.name)
                    .identification(this.identification != null ? this.identification.mapped() : null)
                    .otherIds(
                            this.otherIds != null ? this.otherIds.stream().map(Mapable::mapped).toList() : List.of())
                    .address(this.address != null ? this.address.mapped() : null).build();
        }

    }

    @Getter
    @NullUnmarked
    private static class Address implements Mapable<PartiesPayload.Address, Void> {
        @Schema(title = "Address - Street", description = "Address line 1 - the main address line in an address", requiredMode = RequiredMode.NOT_REQUIRED, example = "Main Street 1")
        private String street = "";

        @Schema(title = "Address - Additional street", description = "Address line 2 - an additional address line in an address that can be used to give further details supplementing the main line.", requiredMode = RequiredMode.NOT_REQUIRED, example = "PO Box 1234")
        private String additionalStreet = "";

        @Schema(title = "Address - Building number", description = "Address building number", requiredMode = RequiredMode.NOT_REQUIRED, example = "1234")
        private String buildingNumber = "";

        @Schema(title = "Address - Additional number", description = "Address additional number", requiredMode = RequiredMode.NOT_REQUIRED, example = "22")
        private String additionalNumber = "";

        @Schema(title = "Address - City", description = "The common name of the city, town or village, where the Party's address is located.", requiredMode = RequiredMode.NOT_REQUIRED, example = "Riyadh")
        private String city = "";

        @Schema(title = "Address - Buyer postal code", description = "Post code", requiredMode = RequiredMode.NOT_REQUIRED, example = "12345")
        private String postalCode = "";

        @Schema(title = "Address - Province/State", description = "Country subdivision", requiredMode = RequiredMode.NOT_REQUIRED, example = "Riyadh Region")
        private String provinceOrState = "";

        @Schema(title = "Address - District", description = "The name of the subdivision of the Party city, town, or village in which its address is located, such as the name of its district or borough.", requiredMode = RequiredMode.NOT_REQUIRED, example = "District A")
        private String district = "";

        @Schema(title = "Address - Country", description = "ISO Locale and Country Code (spearated either by a hypen or an underscore), if not provided, the system will fallback to English Locale and Saudi Arabia (en-SA)", requiredMode = RequiredMode.NOT_REQUIRED, example = "en-SA")
        private Locale country = Locale.of("", Invoice.DEFAULT_LOCALE_CODE);

        @Override
        public PartiesPayload.Address mapped(Void d) {
            return PartiesPayload.Address.builder().street(street).additionalStreet(additionalStreet)
                    .buildingNumber(buildingNumber).additionalNumber(additionalNumber).city(city).postalCode(postalCode)
                    .provinceOrState(provinceOrState).district(district).country(country).build();
        }
    }

    @Getter
    @AllArgsConstructor
    @NullMarked
    private static class PartyIdentification implements Mapable<PartiesPayload.PartyIdentification, Void> {
        @Valid
        @NotNull(message = KsaRules.BR_KSA_08_14)
        @Schema(title = "Party Identification Key", requiredMode = RequiredMode.REQUIRED, example = "IQAMA")
        private Scheme schemeId;

        @NotBlank(message = KsaRules.BR_KSA_08_14)
        @Schema(title = "Party Identification Value", requiredMode = RequiredMode.REQUIRED, example = "123123123")
        private String value;

        @Override
        public PartiesPayload.PartyIdentification mapped(Void d) {
            return new PartiesPayload.PartyIdentification(schemeId, value);
        }
    }

    @Getter
    @AllArgsConstructor
    @NullMarked
    private static class PartyTaxScheme implements Mapable<PartiesPayload.PartyTaxScheme, Void> {

        @NotBlank(message = KsaRules.BR_KSA_40_44)
        @Schema(title = "Party VAT number value", requiredMode = RequiredMode.REQUIRED, example = "300000000000003")
        private String companyId;

        @Valid
        @Nullable
        @Schema(title = "Party Tax Key (the string value 'VAT')", requiredMode = RequiredMode.NOT_REQUIRED, example = "VAT")
        private TaxScheme taxScheme;

        @Override
        public PartiesPayload.PartyTaxScheme mapped(Void d) {
            return new PartiesPayload.PartyTaxScheme(this.companyId,
                    this.taxScheme != null ? this.taxScheme : TaxScheme.VAT);
        }
    }
}
