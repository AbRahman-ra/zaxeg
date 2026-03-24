package sa.abrahman.zaxeg.infrastructure.in.dto.request.invoice.generate;

import java.util.List;
import java.util.Locale;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.Invoice;
import sa.abrahman.zaxeg.core.model.invoice.predefined.Scheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;
import sa.abrahman.zaxeg.core.port.in.InvoiceGenerationPayload;
import sa.abrahman.zaxeg.infrastructure.in.contract.Payloadable;

@Data
class Parties implements Payloadable<InvoiceGenerationPayload.Parties, Void> {

    @Valid
    @NotNull(message = "Seller Information is required")
    @Schema(title = "Seller Information", requiredMode = RequiredMode.NOT_REQUIRED)
    private Party seller;

    @Valid
    @Schema(title = "Seller Information", requiredMode = RequiredMode.REQUIRED)
    private Party buyer;

    @Override
    public InvoiceGenerationPayload.Parties toPayload(Void d) {
        return new InvoiceGenerationPayload.Parties(seller.toPayload(null), buyer.toPayload(null));
    }

    @Data
    private static class Party implements Payloadable<InvoiceGenerationPayload.Parties.Party, Void> {

        @Schema(title = "Seller/Buyer Name")
        private String name = "";

        @Valid
        @Schema(title = "VAT Information")
        private PartyTaxScheme identification;

        @Valid
        @Schema(title = "Other Seller/Buyer IDs")
        private List<PartyIdentification> otherIds = List.of();

        @Valid
        @Schema(title = "Seller/Buyer Address")
        private Address address;

        @Override
        public InvoiceGenerationPayload.Parties.Party toPayload(Void d) {
            return InvoiceGenerationPayload.Parties.Party.builder()
                    .name(name)
                    .identification(identification.toPayload(null))
                    .otherIds(otherIds.stream().map(oid -> oid.toPayload(null)).toList())
                    .address(address.toPayload(null))
                    .build();
        }

        @Data
        private static class PartyTaxScheme implements Payloadable<InvoiceGenerationPayload.Parties.Party.PartyTaxScheme, Void> {

            @NotBlank(message = "Party VAT number must be provided")
            @Schema(title = "Party VAT number value", requiredMode = RequiredMode.REQUIRED, example = "300000000000003")
            private final String companyId;

            @Valid
            @Schema(title = "Party Tax Key (the string value 'VAT')", requiredMode = RequiredMode.NOT_REQUIRED, example = "VAT")
            private final TaxScheme taxScheme;

            @Override
            public InvoiceGenerationPayload.Parties.Party.PartyTaxScheme toPayload(Void d) {
                return new InvoiceGenerationPayload.Parties.Party.PartyTaxScheme(companyId, taxScheme);
            }
        }

        @Getter
        @RequiredArgsConstructor
        private static class PartyIdentification implements Payloadable<InvoiceGenerationPayload.Parties.Party.PartyIdentification, Void> {
            @Valid
            @Schema(title = "Party Identification Key", requiredMode = RequiredMode.REQUIRED, example = "IQAMA")
            private final Scheme schemeId;

            @NotBlank(message = "Party Identification must be provided")
            @Schema(title = "Party Identification Value", requiredMode = RequiredMode.REQUIRED, example = "123123123")
            private final String value;

            @Override
            public InvoiceGenerationPayload.Parties.Party.PartyIdentification toPayload(Void d) {
                return new InvoiceGenerationPayload.Parties.Party.PartyIdentification(schemeId, value);
            }
        }

        @Data
        private static class Address implements Payloadable<InvoiceGenerationPayload.Parties.Party.Address, Void> {
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

            @Schema(title = "Address - Country", description = "ISO Country Code, if not provided, the system will fallback to Saudi Arabia (SA)", requiredMode = RequiredMode.NOT_REQUIRED, example = "SA")
            private Locale country = Locale.of("", Invoice.DEFAULT_CURRENCY);

            @Override
            public InvoiceGenerationPayload.Parties.Party.Address toPayload(Void d) {
                return InvoiceGenerationPayload.Parties.Party.Address.builder()
                        .street(street)
                        .additionalStreet(additionalStreet)
                        .buildingNumber(buildingNumber)
                        .additionalNumber(additionalNumber)
                        .city(city)
                        .postalCode(postalCode)
                        .provinceOrState(provinceOrState)
                        .district(district)
                        .country(country)
                        .build();
            }
        }
    }
}
