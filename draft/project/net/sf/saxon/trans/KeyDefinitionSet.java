/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.XPathException;

public class KeyDefinitionSet {
    private StructuredQName keyName;
    private int keySetNumber;
    private List<KeyDefinition> keyDefinitions;
    private String collationName;
    private boolean composite;
    private boolean backwardsCompatible;
    private boolean rangeKey;
    private boolean reusable = true;

    public KeyDefinitionSet(StructuredQName keyName, int keySetNumber) {
        this.keyName = keyName;
        this.keySetNumber = keySetNumber;
        this.keyDefinitions = new ArrayList<KeyDefinition>(3);
    }

    public void addKeyDefinition(KeyDefinition keyDef) throws XPathException {
        if (this.keyDefinitions.isEmpty()) {
            this.collationName = keyDef.getCollationName();
            this.composite = keyDef.isComposite();
        } else {
            if (this.collationName == null && keyDef.getCollationName() != null || this.collationName != null && !this.collationName.equals(keyDef.getCollationName())) {
                throw new XPathException("All keys with the same name must use the same collation", "XTSE1220");
            }
            if (keyDef.isComposite() != this.composite) {
                throw new XPathException("All keys with the same name must have the same value for @composite", "XTSE1222");
            }
            List<KeyDefinition> v = this.getKeyDefinitions();
            for (KeyDefinition other : v) {
                if (!keyDef.getMatch().isEqual(other.getMatch()) || !keyDef.getBody().isEqual(other.getBody())) continue;
                return;
            }
        }
        if (keyDef.isBackwardsCompatible()) {
            this.backwardsCompatible = true;
        }
        if (keyDef.isRangeKey()) {
            this.rangeKey = true;
        }
        this.keyDefinitions.add(keyDef);
    }

    public StructuredQName getKeyName() {
        return this.keyName;
    }

    public String getCollationName() {
        return this.collationName;
    }

    public boolean isComposite() {
        return this.composite;
    }

    public int getKeySetNumber() {
        return this.keySetNumber;
    }

    public List<KeyDefinition> getKeyDefinitions() {
        return this.keyDefinitions;
    }

    public boolean isBackwardsCompatible() {
        return this.backwardsCompatible;
    }

    public boolean isRangeKey() {
        return this.rangeKey;
    }

    public void setReusable(boolean reusable) {
        this.reusable = reusable;
    }

    public boolean isReusable() {
        return this.reusable;
    }
}

