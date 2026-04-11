package sa.abrahman.zaxeg.core.generate.domain.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Scheme {
    /** TTax Identification Number */
    TIN("TIN"),

    /** Commercial Registration Number */
    CRN("CRN"),

    /** Momra License */
    MOMRA("MOM"),

    /** MLSD License */
    MLSD("MLS"),

    /** 700 Number */
    N700("700"),

    /** National ID */
    NATIONAL_ID("NAT"),

    /** GCC ID */
    GCC_ID("GCC"),

    /** Iqama Number */
    IQAMA("IQA"),

    /** Passport Number */
    PASSPORT("PAS"),

    /** Sagia License */
    SAGIA("SAG"),

    /** Other OD */
    OTHER_OD("OTH");

    private final String code;
}
