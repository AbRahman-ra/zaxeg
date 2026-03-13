/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.configuration.enums;

import com.zatca.configuration.enums.CommandEnum;

public class Configuration {
    private static Configuration instance;
    private boolean checkVersion;
    private String cliVersion;
    private boolean inVerboseMode;
    private boolean inHelpMode;
    private boolean inInfoMode;
    private boolean generateQrCode;
    private boolean generateSignature;
    private String invoicePath;
    private String stampCertificatePath;
    private String stampCertificatePassword;
    private String pih;
    private String qr;
    private String outputPath;
    private CommandEnum commandType;

    private Configuration() {
    }

    public boolean isCheckVersion() {
        return this.checkVersion;
    }

    public void setCheckVersion(boolean checkVersion) {
        this.checkVersion = checkVersion;
    }

    public boolean isInVerboseMode() {
        return this.inVerboseMode;
    }

    public void setInVerboseMode(boolean inVerboseMode) {
        this.inVerboseMode = inVerboseMode;
    }

    public boolean isInHelpMode() {
        return this.inHelpMode;
    }

    public void setInHelpMode(boolean inHelpMode) {
        this.inHelpMode = inHelpMode;
    }

    public String getInvoicePath() {
        return this.invoicePath;
    }

    public void setInvoicePath(String invoicePath) {
        this.invoicePath = invoicePath;
    }

    public String getStampCertificatePath() {
        return this.stampCertificatePath;
    }

    public void setStampCertificatePath(String stampCertificatePath) {
        this.stampCertificatePath = stampCertificatePath;
    }

    public String getPih() {
        return this.pih;
    }

    public void setPih(String pih) {
        this.pih = pih;
    }

    public String getOutputPath() {
        return this.outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public CommandEnum getCommandType() {
        return this.commandType;
    }

    public void setCommandType(CommandEnum commandType) {
        this.commandType = commandType;
    }

    public String getQr() {
        return this.qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public boolean isInInfoMode() {
        return this.inInfoMode;
    }

    public void setInInfoMode(boolean inInfoMode) {
        this.inInfoMode = inInfoMode;
    }

    public String getCliVersion() {
        return this.cliVersion;
    }

    public void setCliVersion(String cliVersion) {
        this.cliVersion = cliVersion;
    }

    public boolean isGenerateQrCode() {
        return this.generateQrCode;
    }

    public void setGenerateQrCode(boolean generateQrCode) {
        this.generateQrCode = generateQrCode;
    }

    public boolean isGenerateSignature() {
        return this.generateSignature;
    }

    public void setGenerateSignature(boolean generateSignature) {
        this.generateSignature = generateSignature;
    }

    public String getStampCertificatePassword() {
        return this.stampCertificatePassword;
    }

    public void setStampCertificatePassword(String stampCertificatePassword) {
        this.stampCertificatePassword = stampCertificatePassword;
    }

    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public String toString() {
        return "Configuration{checkVersion=" + this.checkVersion + ", cliVersion='" + this.cliVersion + "', inVerboseMode=" + this.inVerboseMode + ", inHelpMode=" + this.inHelpMode + ", inInfoMode=" + this.inInfoMode + ", generateQrCode=" + this.generateQrCode + ", generateSignature=" + this.generateSignature + ", invoicePath='" + this.invoicePath + "', stampCertificatePath='" + this.stampCertificatePath + "', stampCertificatePassword='" + this.stampCertificatePassword + "', pih='" + this.pih + "', qr='" + this.qr + "', outputPath='" + this.outputPath + "', commandType=" + this.commandType + "}";
    }
}

