/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import net.sf.saxon.Configuration;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.SchemaType;
import org.w3c.dom.TypeInfo;

public class TypeInfoImpl
implements TypeInfo {
    private final Configuration config;
    private final SchemaType schemaType;

    public TypeInfoImpl(Configuration config, SchemaType type) {
        this.config = config;
        this.schemaType = type;
    }

    @Override
    public String getTypeName() {
        return this.schemaType.getStructuredQName().getLocalPart();
    }

    @Override
    public String getTypeNamespace() {
        return this.schemaType.getStructuredQName().getURI();
    }

    @Override
    public boolean isDerivedFrom(String typeNamespaceArg, String typeNameArg, int derivationMethod) throws IllegalStateException {
        SchemaType base = this.schemaType.getBaseType();
        int fingerprint = this.config.getNamePool().allocateFingerprint(typeNamespaceArg, typeNameArg);
        if (derivationMethod == 0 || (derivationMethod & this.schemaType.getDerivationMethod()) != 0) {
            if (base.getFingerprint() == fingerprint) {
                return true;
            }
            if (base instanceof AnyType) {
                return false;
            }
            return new TypeInfoImpl(this.config, base).isDerivedFrom(typeNamespaceArg, typeNameArg, derivationMethod);
        }
        return false;
    }
}

