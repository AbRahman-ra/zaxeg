/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zatca.config.ResourcesPaths;
import java.nio.file.Paths;
import java.util.Map;

public class Config {
    static final String XSD_PATH = "xsdPath";
    static final String EN_SCHEMATRON_PATH = "enSchematron";
    static final String ZATCA_SCHEMATRON_PATH = "zatcaSchematron";
    static final String CERT_PATH = "certPath";
    static final String INPUT_PATH = "inputPath";
    static final String OUTPUT_PATH = "outputPath";
    static final String PIH_PATH = "pihPath";
    static final String CERTIFICATE_PASSWORD = "certPassword";
    static final String PRIVATE_KEY_PATH = "privateKeyPath";
    static final String USAGE_PATH_FILE = "usagePathFile";

    public static void main(String[] args) throws Exception {
        Config conf = new Config();
        conf.readResourcesPaths();
    }

    public static ResourcesPaths readResourcesPaths() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ResourcesPaths resourcesPaths = ResourcesPaths.getInstance();
        Map map = mapper.readValue(Paths.get(System.getenv("SDK_CONFIG"), new String[0]).toFile(), Map.class);
        for (Map.Entry entry : map.entrySet()) {
            if (XSD_PATH.equals(entry.getKey())) {
                resourcesPaths.xsdPth = (String)entry.getValue();
            }
            if (EN_SCHEMATRON_PATH.equals(entry.getKey())) {
                resourcesPaths.enSchematronPath = (String)entry.getValue();
            }
            if (ZATCA_SCHEMATRON_PATH.equals(entry.getKey())) {
                resourcesPaths.zatcaSchematronPath = (String)entry.getValue();
            }
            if (CERT_PATH.equals(entry.getKey())) {
                resourcesPaths.certificatePath = (String)entry.getValue();
            }
            if (PIH_PATH.equals(entry.getKey())) {
                resourcesPaths.pihPath = (String)entry.getValue();
            }
            if (CERTIFICATE_PASSWORD.equals(entry.getKey())) {
                resourcesPaths.certPassword = (String)entry.getValue();
            }
            if (PRIVATE_KEY_PATH.equals(entry.getKey())) {
                resourcesPaths.privateKeyPath = (String)entry.getValue();
            }
            if (INPUT_PATH.equals(entry.getKey())) {
                resourcesPaths.inputPath = (String)entry.getValue();
            }
            if (OUTPUT_PATH.equals(entry.getKey())) {
                resourcesPaths.outputPath = (String)entry.getValue();
            }
            if (!USAGE_PATH_FILE.equals(entry.getKey())) continue;
            resourcesPaths.usagePathFile = (String)entry.getValue();
        }
        return resourcesPaths;
    }
}

