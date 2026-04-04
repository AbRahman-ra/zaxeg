package sa.abrahman.zaxeg.core.port.in.payload;

import java.util.List;
import java.util.Locale;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sa.abrahman.zaxeg.core.model.invoice.predefined.Scheme;
import sa.abrahman.zaxeg.core.model.invoice.predefined.TaxScheme;

@Getter
@RequiredArgsConstructor
@NullMarked
public class PartiesPayload {
    private final Party seller;

    @Nullable
    private final Party buyer;

    @Data
    @Builder
    @NullUnmarked
    public static class Party {
        @NonNull
        private String name;

        private PartyTaxScheme identification;

        @Builder.Default
        private List<PartyIdentification> otherIds = List.of();

        private Address address;
    }

    @Getter
    @RequiredArgsConstructor
    @NullMarked
    public static class PartyTaxScheme {
        private final String companyId;
        private final TaxScheme taxScheme;

        public static PartyTaxScheme of(String companyId) {
            return new PartyTaxScheme(companyId, TaxScheme.VAT);
        }
    }

    @Getter
    @RequiredArgsConstructor
    @NullMarked
    public static class PartyIdentification {
        private final Scheme schemeId;
        private final String value;
    }

    @Data
    @Builder
    @NullMarked
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
