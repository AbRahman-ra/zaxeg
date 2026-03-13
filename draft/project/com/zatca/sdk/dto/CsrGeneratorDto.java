/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.dto;

public class CsrGeneratorDto {
    private String csr;
    private String csrPemFormat;
    private String privateKey;
    private String privateKeyPemFormat;
    private String commonName;
    private String serialNumber;
    private String organizationIdentifier;
    private String organizationUnitName;
    private String organizationName;
    private String countryName;
    private String invoiceType;
    private String location;
    private String industry;

    public CsrGeneratorDto() {
    }

    public CsrGeneratorDto(Object commonName, Object serialNumber, Object organizationIdentifier, Object organizationUnitName, Object organizationName, Object countryName, Object invoiceType, Object location, Object industry) {
        this.commonName = (String)commonName;
        this.serialNumber = (String)serialNumber;
        this.organizationIdentifier = (String)organizationIdentifier;
        this.organizationUnitName = (String)organizationUnitName;
        this.organizationName = (String)organizationName;
        this.countryName = (String)countryName;
        this.invoiceType = (String)invoiceType;
        this.location = (String)location;
        this.industry = (String)industry;
    }

    public String getCsr() {
        return this.csr;
    }

    public String getCsrPemFormat() {
        return this.csrPemFormat;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public String getPrivateKeyPemFormat() {
        return this.privateKeyPemFormat;
    }

    public String getCommonName() {
        return this.commonName;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public String getOrganizationIdentifier() {
        return this.organizationIdentifier;
    }

    public String getOrganizationUnitName() {
        return this.organizationUnitName;
    }

    public String getOrganizationName() {
        return this.organizationName;
    }

    public String getCountryName() {
        return this.countryName;
    }

    public String getInvoiceType() {
        return this.invoiceType;
    }

    public String getLocation() {
        return this.location;
    }

    public String getIndustry() {
        return this.industry;
    }

    public void setCsr(String csr) {
        this.csr = csr;
    }

    public void setCsrPemFormat(String csrPemFormat) {
        this.csrPemFormat = csrPemFormat;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public void setPrivateKeyPemFormat(String privateKeyPemFormat) {
        this.privateKeyPemFormat = privateKeyPemFormat;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setOrganizationIdentifier(String organizationIdentifier) {
        this.organizationIdentifier = organizationIdentifier;
    }

    public void setOrganizationUnitName(String organizationUnitName) {
        this.organizationUnitName = organizationUnitName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CsrGeneratorDto)) {
            return false;
        }
        CsrGeneratorDto other = (CsrGeneratorDto)o;
        if (!other.canEqual(this)) {
            return false;
        }
        String this$csr = this.getCsr();
        String other$csr = other.getCsr();
        if (this$csr == null ? other$csr != null : !this$csr.equals(other$csr)) {
            return false;
        }
        String this$csrPemFormat = this.getCsrPemFormat();
        String other$csrPemFormat = other.getCsrPemFormat();
        if (this$csrPemFormat == null ? other$csrPemFormat != null : !this$csrPemFormat.equals(other$csrPemFormat)) {
            return false;
        }
        String this$privateKey = this.getPrivateKey();
        String other$privateKey = other.getPrivateKey();
        if (this$privateKey == null ? other$privateKey != null : !this$privateKey.equals(other$privateKey)) {
            return false;
        }
        String this$privateKeyPemFormat = this.getPrivateKeyPemFormat();
        String other$privateKeyPemFormat = other.getPrivateKeyPemFormat();
        if (this$privateKeyPemFormat == null ? other$privateKeyPemFormat != null : !this$privateKeyPemFormat.equals(other$privateKeyPemFormat)) {
            return false;
        }
        String this$commonName = this.getCommonName();
        String other$commonName = other.getCommonName();
        if (this$commonName == null ? other$commonName != null : !this$commonName.equals(other$commonName)) {
            return false;
        }
        String this$serialNumber = this.getSerialNumber();
        String other$serialNumber = other.getSerialNumber();
        if (this$serialNumber == null ? other$serialNumber != null : !this$serialNumber.equals(other$serialNumber)) {
            return false;
        }
        String this$organizationIdentifier = this.getOrganizationIdentifier();
        String other$organizationIdentifier = other.getOrganizationIdentifier();
        if (this$organizationIdentifier == null ? other$organizationIdentifier != null : !this$organizationIdentifier.equals(other$organizationIdentifier)) {
            return false;
        }
        String this$organizationUnitName = this.getOrganizationUnitName();
        String other$organizationUnitName = other.getOrganizationUnitName();
        if (this$organizationUnitName == null ? other$organizationUnitName != null : !this$organizationUnitName.equals(other$organizationUnitName)) {
            return false;
        }
        String this$organizationName = this.getOrganizationName();
        String other$organizationName = other.getOrganizationName();
        if (this$organizationName == null ? other$organizationName != null : !this$organizationName.equals(other$organizationName)) {
            return false;
        }
        String this$countryName = this.getCountryName();
        String other$countryName = other.getCountryName();
        if (this$countryName == null ? other$countryName != null : !this$countryName.equals(other$countryName)) {
            return false;
        }
        String this$invoiceType = this.getInvoiceType();
        String other$invoiceType = other.getInvoiceType();
        if (this$invoiceType == null ? other$invoiceType != null : !this$invoiceType.equals(other$invoiceType)) {
            return false;
        }
        String this$location = this.getLocation();
        String other$location = other.getLocation();
        if (this$location == null ? other$location != null : !this$location.equals(other$location)) {
            return false;
        }
        String this$industry = this.getIndustry();
        String other$industry = other.getIndustry();
        return !(this$industry == null ? other$industry != null : !this$industry.equals(other$industry));
    }

    protected boolean canEqual(Object other) {
        return other instanceof CsrGeneratorDto;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        String $csr = this.getCsr();
        result = result * 59 + ($csr == null ? 43 : $csr.hashCode());
        String $csrPemFormat = this.getCsrPemFormat();
        result = result * 59 + ($csrPemFormat == null ? 43 : $csrPemFormat.hashCode());
        String $privateKey = this.getPrivateKey();
        result = result * 59 + ($privateKey == null ? 43 : $privateKey.hashCode());
        String $privateKeyPemFormat = this.getPrivateKeyPemFormat();
        result = result * 59 + ($privateKeyPemFormat == null ? 43 : $privateKeyPemFormat.hashCode());
        String $commonName = this.getCommonName();
        result = result * 59 + ($commonName == null ? 43 : $commonName.hashCode());
        String $serialNumber = this.getSerialNumber();
        result = result * 59 + ($serialNumber == null ? 43 : $serialNumber.hashCode());
        String $organizationIdentifier = this.getOrganizationIdentifier();
        result = result * 59 + ($organizationIdentifier == null ? 43 : $organizationIdentifier.hashCode());
        String $organizationUnitName = this.getOrganizationUnitName();
        result = result * 59 + ($organizationUnitName == null ? 43 : $organizationUnitName.hashCode());
        String $organizationName = this.getOrganizationName();
        result = result * 59 + ($organizationName == null ? 43 : $organizationName.hashCode());
        String $countryName = this.getCountryName();
        result = result * 59 + ($countryName == null ? 43 : $countryName.hashCode());
        String $invoiceType = this.getInvoiceType();
        result = result * 59 + ($invoiceType == null ? 43 : $invoiceType.hashCode());
        String $location = this.getLocation();
        result = result * 59 + ($location == null ? 43 : $location.hashCode());
        String $industry = this.getIndustry();
        result = result * 59 + ($industry == null ? 43 : $industry.hashCode());
        return result;
    }

    public String toString() {
        return "CsrGeneratorDto(csr=" + this.getCsr() + ", csrPemFormat=" + this.getCsrPemFormat() + ", privateKey=" + this.getPrivateKey() + ", privateKeyPemFormat=" + this.getPrivateKeyPemFormat() + ", commonName=" + this.getCommonName() + ", serialNumber=" + this.getSerialNumber() + ", organizationIdentifier=" + this.getOrganizationIdentifier() + ", organizationUnitName=" + this.getOrganizationUnitName() + ", organizationName=" + this.getOrganizationName() + ", countryName=" + this.getCountryName() + ", invoiceType=" + this.getInvoiceType() + ", location=" + this.getLocation() + ", industry=" + this.getIndustry() + ")";
    }
}

