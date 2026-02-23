/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaComponent;
import net.sf.saxon.type.SchemaException;

public interface SchemaType
extends SchemaComponent {
    public static final int DERIVATION_RESTRICTION = 1;
    public static final int DERIVATION_EXTENSION = 2;
    public static final int DERIVATION_UNION = 4;
    public static final int DERIVATION_LIST = 8;
    public static final int DERIVE_BY_SUBSTITUTION = 16;

    public String getName();

    public String getTargetNamespace();

    public int getFingerprint();

    public String getDisplayName();

    public StructuredQName getStructuredQName();

    public String getEQName();

    public boolean isComplexType();

    public boolean isSimpleType();

    public boolean isAtomicType();

    public boolean isAnonymousType();

    public int getBlock();

    public SchemaType getBaseType();

    default public SchemaType getNearestNamedType() {
        SchemaType type = this;
        while (type.isAnonymousType()) {
            type = type.getBaseType();
        }
        return type;
    }

    public int getDerivationMethod();

    public int getFinalProhibitions();

    public boolean allowsDerivation(int var1);

    public void analyzeContentExpression(Expression var1, int var2) throws XPathException;

    public AtomicSequence atomize(NodeInfo var1) throws XPathException;

    public boolean isSameType(SchemaType var1);

    public String getDescription();

    public void checkTypeDerivationIsOK(SchemaType var1, int var2) throws SchemaException;

    public String getSystemId();

    public boolean isIdType() throws MissingComponentException;

    public boolean isIdRefType() throws MissingComponentException;
}

