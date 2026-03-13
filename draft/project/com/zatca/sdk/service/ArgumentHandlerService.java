/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service;

import com.zatca.config.ResourcesPaths;
import com.zatca.sdk.dto.ApplicationPropertyDto;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ArgumentHandlerService {
    private static final Logger LOG = Logger.getLogger(ArgumentHandlerService.class);
    private ResourcesPaths paths = ResourcesPaths.getInstance();

    public ApplicationPropertyDto loadGlobalSetting(String[] args) {
        if (args.length == 0) {
            this.displayUsage();
            System.exit(1);
        }
        Logger.getLogger("com.zatca.sdk").setLevel(Level.INFO);
        LOG.debug("load global setting");
        ApplicationPropertyDto configuration = new ApplicationPropertyDto();
        try {
            for (int index = 0; index < args.length; ++index) {
                String param = args[index];
                if ("-csr".equalsIgnoreCase(param)) {
                    configuration.setGenerateCsr(true);
                    continue;
                }
                if ("-privateKey".equalsIgnoreCase(param)) {
                    configuration.setPrivateKeyFileName(args[++index]);
                    continue;
                }
                if ("-generatedCsr".equalsIgnoreCase(param)) {
                    configuration.setCsrFileName(args[++index]);
                    continue;
                }
                if ("-csrConfig".equalsIgnoreCase(param)) {
                    configuration.setCsrConfigFileName(args[++index]);
                    continue;
                }
                if ("-qr".equalsIgnoreCase(param)) {
                    configuration.setGenerateQr(true);
                    continue;
                }
                if ("-invoice".equalsIgnoreCase(param)) {
                    configuration.setInvoiceFileName(args[++index]);
                    continue;
                }
                if ("-invoiceWithQr".equalsIgnoreCase(param)) {
                    configuration.setOutputInvoiceFileName(args[++index]);
                    continue;
                }
                if ("-sign".equalsIgnoreCase(param)) {
                    configuration.setGenerateSignature(true);
                    continue;
                }
                if ("-signedInvoice".equalsIgnoreCase(param)) {
                    configuration.setOutputInvoiceFileName(args[++index]);
                    continue;
                }
                if ("-invoiceRequest".equalsIgnoreCase(param)) {
                    configuration.setGenerateInvoiceRequest(true);
                    continue;
                }
                if ("-apiRequest".equalsIgnoreCase(param)) {
                    configuration.setInvoiceRequestFileName(args[++index]);
                    continue;
                }
                if ("-pem".equalsIgnoreCase(param)) {
                    configuration.setOutputPemFormat(true);
                    continue;
                }
                if ("-nonprod".equalsIgnoreCase(param)) {
                    configuration.setNonPrdServer(true);
                    continue;
                }
                if ("-validate".equalsIgnoreCase(param)) {
                    configuration.setValidateInvoice(true);
                    continue;
                }
                if ("-generateHash".equalsIgnoreCase(param)) {
                    configuration.setGenerateHash(true);
                    continue;
                }
                if ("-debug".equalsIgnoreCase(param)) {
                    Logger.getLogger("com.zatca.sdk").setLevel(Level.DEBUG);
                    continue;
                }
                if (!"-help".equalsIgnoreCase(param)) continue;
                this.displayUsage();
                System.exit(1);
            }
            if (!this.validateArguments(configuration)) {
                this.displayUsage();
                System.exit(1);
            }
            return configuration;
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            this.displayUsage();
            System.exit(1);
            return null;
        }
    }

    private void displayUsage() {
        boolean fileContent = false;
        try {
            Path filePath = Path.of((String)this.paths.getUsagePathFile(), (String[])new String[0]);
            String content = Files.readString((Path)filePath);
            if (content != null && !content.trim().isEmpty()) {
                fileContent = true;
                System.out.println(content.toString());
            }
        } catch (Exception e) {
            fileContent = false;
        }
        if (!fileContent) {
            StringBuilder display = new StringBuilder();
            display.append("\n");
            display.append(" [-csr]\t\t\t\t\t................ : flag used to generate csr and private key.\n");
            display.append(" -privateKey <fileName> \t................ : The name of the private key output file.\n");
            display.append(" -generatedCsr <fileName> \t\t................ : The name of the csr output file.\n");
            display.append(" -csrConfig <fileName>\t \t................ : The name of the csr configuration file.\n");
            display.append(" -invoice <filename>\t\t\t................ : The name of the invoice file.\n");
            display.append(" [-qr] \t\t\t\t\t................ : flag used to generate qr.\n");
            display.append(" [-sign] \t\t\t\t................ : flag used to sign invoice.\n");
            display.append(" -signedInvoice <fileName> \t\t................ : The name of the signed invoice output file.\n");
            display.append(" [-invoiceRequest]\t\t\t................ : flag used to generate invoice request.\n");
            display.append(" -apiRequest <fileName>\t................ : The name of the invoice json request output file. \n");
            display.append(" [-generateHash]\t\t\t\t\t................ : flag used to generate new hash for the provided invoice.\n");
            display.append(" [-pem]\t\t\t\t\t............... : flag used to generate csr and private key in pem format.\n");
            display.append(" [-validate]\t\t\t\t................ : flag used to validate invoice.\n");
            display.append(" [-nonprod] \t\t\t\t................ : flag pointing to use the csr and private key on non production server.\n");
            display.append(" [-help] \t\t\t\t................ : flag used to display this help menu and exit.\n");
            System.out.println(display.toString());
        }
    }

    public boolean validateArguments(ApplicationPropertyDto configuration) {
        String fileName;
        LOG.debug("validate mandatory configuration");
        if (configuration.isGenerateCsr()) {
            if (configuration.getCsrConfigFileName() == null || configuration.getCsrConfigFileName().trim().isEmpty()) {
                LOG.error("failed to generate csr [Mandatory input '-csrConfig' is missing]");
                return false;
            }
            fileName = this.prepareFileName(configuration.getCsrConfigFileName());
            if (fileName == null) {
                LOG.error("failed to generate csr [csr configuration file is not found]");
                return false;
            }
            configuration.setCsrConfigFileName(fileName);
            if (configuration.getPrivateKeyFileName() == null || configuration.getPrivateKeyFileName().trim().isEmpty()) {
                configuration.setPrivateKeyFileName(Path.of((String)"", (String[])new String[0]).toAbsolutePath().toString() + File.separatorChar + String.format("%s-%s.%s", "generated-private-key", new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()), "key"));
            } else {
                configuration.setPrivateKeyFileName(this.prepareOutputFileName(configuration.getPrivateKeyFileName()));
            }
            if (configuration.getCsrFileName() == null || configuration.getCsrFileName().trim().isEmpty()) {
                configuration.setCsrFileName(Path.of((String)"", (String[])new String[0]).toAbsolutePath().toString() + File.separatorChar + String.format("%s-%s.%s", "generated-csr", new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()), "csr"));
            } else {
                configuration.setCsrFileName(this.prepareOutputFileName(configuration.getCsrFileName()));
            }
        }
        if (configuration.isGenerateHash()) {
            if (configuration.getInvoiceFileName() == null || configuration.getInvoiceFileName().trim().isEmpty()) {
                LOG.error("failed to generate hash [Mandatory input '-invoice' is missing]");
                return false;
            }
            fileName = this.prepareFileName(configuration.getInvoiceFileName());
            if (fileName == null) {
                LOG.error("failed to generate hash [invoice file is not found]");
                return false;
            }
            configuration.setInvoiceFileName(fileName);
        }
        if (configuration.isGenerateInvoiceRequest()) {
            if (configuration.getInvoiceFileName() == null || configuration.getInvoiceFileName().trim().isEmpty()) {
                LOG.error("failed to generate invoice request [Mandatory input '-invoice' is missing]");
                return false;
            }
            fileName = this.prepareFileName(configuration.getInvoiceFileName());
            if (fileName == null) {
                LOG.error("failed to generate invoice request [invoice file is not found]");
                return false;
            }
            configuration.setInvoiceFileName(fileName);
            if (configuration.getInvoiceRequestFileName() == null || configuration.getInvoiceRequestFileName().trim().isEmpty()) {
                configuration.setInvoiceRequestFileName(Path.of((String)"", (String[])new String[0]).toAbsolutePath().toString() + File.separatorChar + String.format("%s-%s.%s", "generated-json-request", new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()), "json"));
            } else {
                configuration.setInvoiceRequestFileName(this.prepareOutputFileName(configuration.getInvoiceRequestFileName()));
            }
        }
        if (configuration.isGenerateSignature()) {
            if (configuration.getInvoiceFileName() == null || configuration.getInvoiceFileName().trim().isEmpty()) {
                LOG.error("failed to sign invoice [Mandatory input '-invoice' is missing]");
                return false;
            }
            fileName = this.prepareFileName(configuration.getInvoiceFileName());
            if (fileName == null) {
                LOG.error("failed to sign invoice [invoice file is not found]");
                return false;
            }
            configuration.setInvoiceFileName(fileName);
            if (configuration.getOutputInvoiceFileName() == null || configuration.getOutputInvoiceFileName().trim().isEmpty()) {
                configuration.setOutputInvoiceFileName(Path.of((String)"", (String[])new String[0]).toAbsolutePath().toString() + File.separatorChar + new File(configuration.getInvoiceFileName()).getName().replace(".xml", "_signed.xml"));
            } else {
                configuration.setOutputInvoiceFileName(this.prepareOutputFileName(configuration.getOutputInvoiceFileName()));
            }
        }
        if (configuration.isValidateInvoice()) {
            if (configuration.getInvoiceFileName() == null || configuration.getInvoiceFileName().trim().isEmpty()) {
                LOG.error("failed to generate hash [Mandatory input '-invoice' is missing]");
                return false;
            }
            fileName = this.prepareFileName(configuration.getInvoiceFileName());
            if (fileName == null) {
                LOG.error("failed to generate hash [invoice file is not found]");
                return false;
            }
            configuration.setInvoiceFileName(fileName);
        }
        if (configuration.isGenerateInvoiceRequest()) {
            if (configuration.getInvoiceFileName() == null || configuration.getInvoiceFileName().trim().isEmpty()) {
                LOG.error("failed to generate qr [Mandatory input '-invoice' is missing]");
                return false;
            }
            fileName = this.prepareFileName(configuration.getInvoiceFileName());
            if (fileName == null) {
                LOG.error("failed to generate qr [invoice file is not found]");
                return false;
            }
            configuration.setInvoiceFileName(fileName);
        }
        return true;
    }

    private String prepareOutputFileName(String path) {
        Boolean result = this.checkOutputFilePath(path);
        if (result.booleanValue()) {
            return path;
        }
        return Path.of((String)"", (String[])new String[0]).toAbsolutePath().toString() + File.separatorChar + path;
    }

    private String prepareFileName(String path) {
        Boolean result = this.checkFilePath(path);
        if (result.booleanValue()) {
            return path;
        }
        result = this.checkFilePath(Path.of((String)"", (String[])new String[0]).toAbsolutePath().toString() + File.separatorChar + path);
        if (result.booleanValue()) {
            return Path.of((String)"", (String[])new String[0]).toAbsolutePath().toString() + File.separatorChar + path;
        }
        return null;
    }

    private boolean checkOutputFilePath(String path) {
        try {
            File file = new File(path);
            return file.getParent() != null;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean checkFilePath(String path) {
        try {
            File file = new File(path);
            return file.exists();
        } catch (Exception ex) {
            return false;
        }
    }
}

