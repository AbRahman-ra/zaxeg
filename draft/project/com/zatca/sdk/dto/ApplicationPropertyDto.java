/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.dto;

public class ApplicationPropertyDto {
    private boolean generateQr;
    private boolean generateSignature;
    private boolean generateInvoiceRequest;
    private boolean generateCsr;
    private boolean outputPemFormat;
    private boolean validateInvoice;
    private boolean nonPrdServer;
    private boolean generateHash;
    private String invoiceFileName;
    private String outputInvoiceFileName;
    private String invoiceRequestFileName;
    private String csrConfigFileName;
    private String privateKeyFileName;
    private String csrFileName;

    public ApplicationPropertyDto() {
    }

    public ApplicationPropertyDto(boolean generateQr, String invoiceFileName) {
        this.generateQr = generateQr;
        this.invoiceFileName = invoiceFileName;
    }

    public ApplicationPropertyDto(boolean generateSignature, String invoiceFileName, String outputInvoiceFileName) {
        this.generateSignature = generateSignature;
        this.invoiceFileName = invoiceFileName;
        this.outputInvoiceFileName = outputInvoiceFileName;
    }

    public ApplicationPropertyDto(boolean generateInvoiceRequest, String invoiceFileName, String invoiceRequestFileName, boolean generateSignature, String outputInvoiceFileName) {
        this.generateInvoiceRequest = generateInvoiceRequest;
        this.invoiceFileName = invoiceFileName;
        this.invoiceRequestFileName = invoiceRequestFileName;
        this.outputInvoiceFileName = outputInvoiceFileName;
        this.generateSignature = generateSignature;
    }

    public ApplicationPropertyDto(boolean generateHash, String invoiceFileName, String invoiceRequestFileName, String outputInvoiceFileName, boolean generateQr) {
        this.generateHash = generateHash;
        this.invoiceFileName = invoiceFileName;
        this.invoiceRequestFileName = invoiceRequestFileName;
        this.outputInvoiceFileName = outputInvoiceFileName;
        this.generateQr = generateQr;
    }

    public ApplicationPropertyDto(boolean validateInvoice, boolean generateHash, String invoiceFileName, String invoiceRequestFileName, String outputInvoiceFileName, boolean generateQr) {
        this.validateInvoice = validateInvoice;
        this.generateHash = generateHash;
        this.invoiceFileName = invoiceFileName;
        this.invoiceRequestFileName = invoiceRequestFileName;
        this.outputInvoiceFileName = outputInvoiceFileName;
        this.generateQr = generateQr;
    }

    public ApplicationPropertyDto(boolean generateCsr, String csrConfigFileName, String privateKeyFileName, boolean isPemFormat, String csrFileName, boolean isNonPrd) {
        this.generateCsr = generateCsr;
        this.csrConfigFileName = csrConfigFileName;
        this.privateKeyFileName = privateKeyFileName;
        this.outputPemFormat = isPemFormat;
        this.csrFileName = csrFileName;
        this.nonPrdServer = isNonPrd;
    }

    public boolean isGenerateQr() {
        return this.generateQr;
    }

    public boolean isGenerateSignature() {
        return this.generateSignature;
    }

    public boolean isGenerateInvoiceRequest() {
        return this.generateInvoiceRequest;
    }

    public boolean isGenerateCsr() {
        return this.generateCsr;
    }

    public boolean isOutputPemFormat() {
        return this.outputPemFormat;
    }

    public boolean isValidateInvoice() {
        return this.validateInvoice;
    }

    public boolean isNonPrdServer() {
        return this.nonPrdServer;
    }

    public boolean isGenerateHash() {
        return this.generateHash;
    }

    public String getInvoiceFileName() {
        return this.invoiceFileName;
    }

    public String getOutputInvoiceFileName() {
        return this.outputInvoiceFileName;
    }

    public String getInvoiceRequestFileName() {
        return this.invoiceRequestFileName;
    }

    public String getCsrConfigFileName() {
        return this.csrConfigFileName;
    }

    public String getPrivateKeyFileName() {
        return this.privateKeyFileName;
    }

    public String getCsrFileName() {
        return this.csrFileName;
    }

    public void setGenerateQr(boolean generateQr) {
        this.generateQr = generateQr;
    }

    public void setGenerateSignature(boolean generateSignature) {
        this.generateSignature = generateSignature;
    }

    public void setGenerateInvoiceRequest(boolean generateInvoiceRequest) {
        this.generateInvoiceRequest = generateInvoiceRequest;
    }

    public void setGenerateCsr(boolean generateCsr) {
        this.generateCsr = generateCsr;
    }

    public void setOutputPemFormat(boolean outputPemFormat) {
        this.outputPemFormat = outputPemFormat;
    }

    public void setValidateInvoice(boolean validateInvoice) {
        this.validateInvoice = validateInvoice;
    }

    public void setNonPrdServer(boolean nonPrdServer) {
        this.nonPrdServer = nonPrdServer;
    }

    public void setGenerateHash(boolean generateHash) {
        this.generateHash = generateHash;
    }

    public void setInvoiceFileName(String invoiceFileName) {
        this.invoiceFileName = invoiceFileName;
    }

    public void setOutputInvoiceFileName(String outputInvoiceFileName) {
        this.outputInvoiceFileName = outputInvoiceFileName;
    }

    public void setInvoiceRequestFileName(String invoiceRequestFileName) {
        this.invoiceRequestFileName = invoiceRequestFileName;
    }

    public void setCsrConfigFileName(String csrConfigFileName) {
        this.csrConfigFileName = csrConfigFileName;
    }

    public void setPrivateKeyFileName(String privateKeyFileName) {
        this.privateKeyFileName = privateKeyFileName;
    }

    public void setCsrFileName(String csrFileName) {
        this.csrFileName = csrFileName;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ApplicationPropertyDto)) {
            return false;
        }
        ApplicationPropertyDto other = (ApplicationPropertyDto)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (this.isGenerateQr() != other.isGenerateQr()) {
            return false;
        }
        if (this.isGenerateSignature() != other.isGenerateSignature()) {
            return false;
        }
        if (this.isGenerateInvoiceRequest() != other.isGenerateInvoiceRequest()) {
            return false;
        }
        if (this.isGenerateCsr() != other.isGenerateCsr()) {
            return false;
        }
        if (this.isOutputPemFormat() != other.isOutputPemFormat()) {
            return false;
        }
        if (this.isValidateInvoice() != other.isValidateInvoice()) {
            return false;
        }
        if (this.isNonPrdServer() != other.isNonPrdServer()) {
            return false;
        }
        if (this.isGenerateHash() != other.isGenerateHash()) {
            return false;
        }
        String this$invoiceFileName = this.getInvoiceFileName();
        String other$invoiceFileName = other.getInvoiceFileName();
        if (this$invoiceFileName == null ? other$invoiceFileName != null : !this$invoiceFileName.equals(other$invoiceFileName)) {
            return false;
        }
        String this$outputInvoiceFileName = this.getOutputInvoiceFileName();
        String other$outputInvoiceFileName = other.getOutputInvoiceFileName();
        if (this$outputInvoiceFileName == null ? other$outputInvoiceFileName != null : !this$outputInvoiceFileName.equals(other$outputInvoiceFileName)) {
            return false;
        }
        String this$invoiceRequestFileName = this.getInvoiceRequestFileName();
        String other$invoiceRequestFileName = other.getInvoiceRequestFileName();
        if (this$invoiceRequestFileName == null ? other$invoiceRequestFileName != null : !this$invoiceRequestFileName.equals(other$invoiceRequestFileName)) {
            return false;
        }
        String this$csrConfigFileName = this.getCsrConfigFileName();
        String other$csrConfigFileName = other.getCsrConfigFileName();
        if (this$csrConfigFileName == null ? other$csrConfigFileName != null : !this$csrConfigFileName.equals(other$csrConfigFileName)) {
            return false;
        }
        String this$privateKeyFileName = this.getPrivateKeyFileName();
        String other$privateKeyFileName = other.getPrivateKeyFileName();
        if (this$privateKeyFileName == null ? other$privateKeyFileName != null : !this$privateKeyFileName.equals(other$privateKeyFileName)) {
            return false;
        }
        String this$csrFileName = this.getCsrFileName();
        String other$csrFileName = other.getCsrFileName();
        return !(this$csrFileName == null ? other$csrFileName != null : !this$csrFileName.equals(other$csrFileName));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ApplicationPropertyDto;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * 59 + (this.isGenerateQr() ? 79 : 97);
        result = result * 59 + (this.isGenerateSignature() ? 79 : 97);
        result = result * 59 + (this.isGenerateInvoiceRequest() ? 79 : 97);
        result = result * 59 + (this.isGenerateCsr() ? 79 : 97);
        result = result * 59 + (this.isOutputPemFormat() ? 79 : 97);
        result = result * 59 + (this.isValidateInvoice() ? 79 : 97);
        result = result * 59 + (this.isNonPrdServer() ? 79 : 97);
        result = result * 59 + (this.isGenerateHash() ? 79 : 97);
        String $invoiceFileName = this.getInvoiceFileName();
        result = result * 59 + ($invoiceFileName == null ? 43 : $invoiceFileName.hashCode());
        String $outputInvoiceFileName = this.getOutputInvoiceFileName();
        result = result * 59 + ($outputInvoiceFileName == null ? 43 : $outputInvoiceFileName.hashCode());
        String $invoiceRequestFileName = this.getInvoiceRequestFileName();
        result = result * 59 + ($invoiceRequestFileName == null ? 43 : $invoiceRequestFileName.hashCode());
        String $csrConfigFileName = this.getCsrConfigFileName();
        result = result * 59 + ($csrConfigFileName == null ? 43 : $csrConfigFileName.hashCode());
        String $privateKeyFileName = this.getPrivateKeyFileName();
        result = result * 59 + ($privateKeyFileName == null ? 43 : $privateKeyFileName.hashCode());
        String $csrFileName = this.getCsrFileName();
        result = result * 59 + ($csrFileName == null ? 43 : $csrFileName.hashCode());
        return result;
    }

    public String toString() {
        return "ApplicationPropertyDto(generateQr=" + this.isGenerateQr() + ", generateSignature=" + this.isGenerateSignature() + ", generateInvoiceRequest=" + this.isGenerateInvoiceRequest() + ", generateCsr=" + this.isGenerateCsr() + ", outputPemFormat=" + this.isOutputPemFormat() + ", validateInvoice=" + this.isValidateInvoice() + ", nonPrdServer=" + this.isNonPrdServer() + ", generateHash=" + this.isGenerateHash() + ", invoiceFileName=" + this.getInvoiceFileName() + ", outputInvoiceFileName=" + this.getOutputInvoiceFileName() + ", invoiceRequestFileName=" + this.getInvoiceRequestFileName() + ", csrConfigFileName=" + this.getCsrConfigFileName() + ", privateKeyFileName=" + this.getPrivateKeyFileName() + ", csrFileName=" + this.getCsrFileName() + ")";
    }
}

