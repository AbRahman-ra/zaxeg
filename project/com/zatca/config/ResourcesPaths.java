/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.config;

public class ResourcesPaths {
    String xsdPth;
    String enSchematronPath;
    String zatcaSchematronPath;
    String certificatePath;
    String privateKeyPath;
    String pihPath;
    String certPassword;
    String inputPath;
    String outputPath;
    String usagePathFile;
    private static ResourcesPaths resourcesPaths;

    public static ResourcesPaths getInstance() {
        if (resourcesPaths == null) {
            resourcesPaths = new ResourcesPaths();
        }
        return resourcesPaths;
    }

    private ResourcesPaths() {
        this.xsdPth = null;
        this.enSchematronPath = null;
        this.zatcaSchematronPath = null;
        this.certificatePath = null;
        this.pihPath = null;
        this.certPassword = null;
        this.privateKeyPath = null;
        this.usagePathFile = null;
    }

    private ResourcesPaths(String xsdPth, String enSchematronPath, String zatcaSchematronPath, String certificatePath, String pihPath, String certPassword, String privateKeyPath, String usagePathFile) {
        this.xsdPth = xsdPth;
        this.enSchematronPath = enSchematronPath;
        this.zatcaSchematronPath = zatcaSchematronPath;
        this.certificatePath = certificatePath;
        this.pihPath = pihPath;
        this.certPassword = certPassword;
        this.privateKeyPath = privateKeyPath;
        this.usagePathFile = usagePathFile;
    }

    public String getPihPath() {
        return this.pihPath;
    }

    public void setPihPath(String pihPath) {
        this.pihPath = pihPath;
    }

    public String getXsdPth() {
        return this.xsdPth;
    }

    public void setXsdPth(String xsdPth) {
        this.xsdPth = xsdPth;
    }

    public String getEnSchematronPath() {
        return this.enSchematronPath;
    }

    public void setEnSchematronPath(String enSchematronPath) {
        this.enSchematronPath = enSchematronPath;
    }

    public String getZatcaSchematronPath() {
        return this.zatcaSchematronPath;
    }

    public void setZatcaSchematronPath(String zatcaSchematronPath) {
        this.zatcaSchematronPath = zatcaSchematronPath;
    }

    public String getCertificatePath() {
        return this.certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public String getCertPassword() {
        return this.certPassword;
    }

    public void setCertPassword(String certPassword) {
        this.certPassword = certPassword;
    }

    public String getPrivateKeyPath() {
        return this.privateKeyPath;
    }

    public String getInputPath() {
        return this.inputPath;
    }

    public void setInputPath(String inputPath) {
        this.inputPath = inputPath;
    }

    public String getOutputPath() {
        return this.outputPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getUsagePathFile() {
        return this.usagePathFile;
    }

    public void setUsagePathFile(String usagePathFile) {
        this.usagePathFile = usagePathFile;
    }

    public static ResourcesPaths getResourcesPaths() {
        return resourcesPaths;
    }

    public static void setResourcesPaths(ResourcesPaths resourcesPaths) {
        ResourcesPaths.resourcesPaths = resourcesPaths;
    }
}

