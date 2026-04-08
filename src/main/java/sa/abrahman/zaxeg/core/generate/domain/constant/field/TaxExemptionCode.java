package sa.abrahman.zaxeg.core.generate.domain.constant.field;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TaxExemptionCode {
    /** {@code VATEX-SA-29}: Financial services mentioned in Article 29 of the VAT Regulations */
    FINANCIAL_SERVICES("VATEX-SA-29"),

    /** {@code VATEX-SA-29-7}: Life insurance services mentioned in Article 29 of the VAT Regulations */
    LIFE_INSURANCE("VATEX-SA-29-7"),

    /** {@code VATEX-SA-30}: Real estate transactions mentioned in Article 30 of the VAT Regulations */
    REAL_STATE("VATEX-SA-30"),

    /** {@code VATEX-SA-32}: Export of goods */
    EXPORT_OF_GOODS("VATEX-SA-32"),

    /** {@code VATEX-SA-33}: Export of services */
    EXPORT_OF_SERVICES("VATEX-SA-33"),

    /** {@code VATEX-SA-34-1}: The international transport of Goods */
    INTERNATIONAL_TRANSPORT_GOODS("VATEX-SA-34-1"),

    /** {@code VATEX-SA-34-2}: The international transport of passengers */
    INTERNATIONAL_TRANSPORT_PASSENGERS("VATEX-SA-34-2"),

    /**
     * {@code VATEX-SA-34-3}: Services directly connected and incidental to a Supply of international passenger
     * transport
     */
    INTERNATIONAL_PASSENGER_TRANSPORT_SERVICES("VATEX-SA-34-3"),

    /** {@code VATEX-SA-34-4}: Supply of a qualifying means of transport */
    SUPPLY_OF_TRANSPORT("VATEX-SA-34-4"),

    /**
     * {@code VATEX-SA-34-5}: Any services relating to Goods or passenger transportation, as defined in article twenty
     * five of these Regulations
     */
    GOODS_OR_PASSENGER_TRANSPORTATION("VATEX-SA-34-5"),

    /** {@code VATEX-SA-35}: Medicines and medical equipment */
    MEDICINES_AND_MEDICAL_EQUIPMENT("VATEX-SA-35"),

    /** {@code VATEX-SA-36}: Qualifying metals */
    QUALIFYING_METALS("VATEX-SA-36"),

    /** {@code VATEX-SA-EDU}: Private education to citizen */
    PRIVATE_EDUCATION_TO_CITIZEN("VATEX-SA-EDU"),

    /** {@code VATEX-SA-HEA}: Private healthcare to citizen */
    PRIVATE_HEALTHCARE_TO_CITIZEN("VATEX-SA-HEA"),

    /** {@code VATEX-SA-MLTRY}: supply of qualified military goods */
    MILITARY_GOODS("VATEX-SA-MLTRY"),

    /** {@code VATEX-SA-OOS}: Reason is free text, to be provided by the taxpayer on case to case basis. */
    OUT_OF_SCOPE("VATEX-SA-OOS"),;

    private final String code;
}
