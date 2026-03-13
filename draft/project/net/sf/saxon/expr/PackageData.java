/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.TypeAliasManager;

public class PackageData {
    protected Configuration config;
    private HostLanguage hostLanguage;
    private boolean isSchemaAware;
    private DecimalFormatManager decimalFormatManager = null;
    protected KeyManager keyManager = null;
    private AccumulatorRegistry accumulatorRegistry = null;
    private List<GlobalVariable> globalVariables = new ArrayList<GlobalVariable>();
    private SlotManager globalSlotManager;
    private int localLicenseId = -1;
    private String targetEdition;
    private boolean relocatable;
    private TypeAliasManager typeAliasManager;

    public PackageData(Configuration config) {
        if (config == null) {
            throw new NullPointerException();
        }
        this.config = config;
        this.targetEdition = config.getEditionCode();
        this.globalSlotManager = config.makeSlotManager();
    }

    public PackageData(PackageData p) {
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public void setConfiguration(Configuration configuration) {
        this.config = configuration;
    }

    public HostLanguage getHostLanguage() {
        return this.hostLanguage;
    }

    public boolean isXSLT() {
        return this.hostLanguage == HostLanguage.XSLT;
    }

    public void setHostLanguage(HostLanguage hostLanguage) {
        this.hostLanguage = hostLanguage;
    }

    public void setLocalLicenseId(int id) {
        this.localLicenseId = id;
    }

    public int getLocalLicenseId() {
        return this.localLicenseId;
    }

    public void setTargetEdition(String edition) {
        this.targetEdition = edition;
    }

    public String getTargetEdition() {
        return this.targetEdition;
    }

    public boolean isRelocatable() {
        return this.relocatable;
    }

    public void setRelocatable(boolean relocatable) {
        this.relocatable = relocatable;
    }

    public boolean isSchemaAware() {
        return this.isSchemaAware;
    }

    public void setSchemaAware(boolean schemaAware) {
        this.isSchemaAware = schemaAware;
    }

    public DecimalFormatManager getDecimalFormatManager() {
        if (this.decimalFormatManager == null) {
            this.decimalFormatManager = new DecimalFormatManager(this.hostLanguage, 31);
        }
        return this.decimalFormatManager;
    }

    public void setDecimalFormatManager(DecimalFormatManager manager) {
        this.decimalFormatManager = manager;
    }

    public KeyManager getKeyManager() {
        if (this.keyManager == null) {
            this.keyManager = new KeyManager(this.getConfiguration(), this);
        }
        return this.keyManager;
    }

    public void setKeyManager(KeyManager manager) {
        this.keyManager = manager;
    }

    public AccumulatorRegistry getAccumulatorRegistry() {
        return this.accumulatorRegistry;
    }

    public void setAccumulatorRegistry(AccumulatorRegistry accumulatorRegistry) {
        this.accumulatorRegistry = accumulatorRegistry;
    }

    public SlotManager getGlobalSlotManager() {
        return this.globalSlotManager;
    }

    public void setGlobalSlotManager(SlotManager manager) {
        this.globalSlotManager = manager;
    }

    public void addGlobalVariable(GlobalVariable variable) {
        this.globalVariables.add(variable);
    }

    public List<GlobalVariable> getGlobalVariableList() {
        return this.globalVariables;
    }

    public void setTypeAliasManager(TypeAliasManager manager) {
        this.typeAliasManager = manager;
    }

    public TypeAliasManager obtainTypeAliasManager() {
        if (this.typeAliasManager == null) {
            this.typeAliasManager = this.config.makeTypeAliasManager();
        }
        return this.typeAliasManager;
    }
}

