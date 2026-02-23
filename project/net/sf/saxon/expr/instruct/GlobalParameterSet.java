/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.Collection;
import java.util.HashMap;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.TypeChecker;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

public class GlobalParameterSet {
    private HashMap<StructuredQName, GroundedValue> params = new HashMap(10);

    public GlobalParameterSet() {
    }

    public GlobalParameterSet(GlobalParameterSet parameterSet) {
        this.params = new HashMap<StructuredQName, GroundedValue>(parameterSet.params);
    }

    public void put(StructuredQName qName, GroundedValue value) {
        if (value == null) {
            this.params.remove(qName);
        } else {
            this.params.put(qName, value);
        }
    }

    public GroundedValue get(StructuredQName qName) {
        return this.params.get(qName);
    }

    public boolean containsKey(StructuredQName qName) {
        return this.params.containsKey(qName);
    }

    public GroundedValue convertParameterValue(StructuredQName qName, SequenceType requiredType, boolean convert, XPathContext context) throws XPathException {
        Sequence val = this.get(qName);
        if (val == null) {
            return null;
        }
        if (requiredType != null) {
            if (convert) {
                RoleDiagnostic role = new RoleDiagnostic(3, qName.getDisplayName(), -1);
                Configuration config = context.getConfiguration();
                val = config.getTypeHierarchy().applyFunctionConversionRules(val, requiredType, role, Loc.NONE);
            } else {
                XPathException err = TypeChecker.testConformance(val, requiredType, context);
                if (err != null) {
                    throw err;
                }
            }
        }
        return val.materialize();
    }

    public void clear() {
        this.params.clear();
    }

    public Collection<StructuredQName> getKeys() {
        return this.params.keySet();
    }

    public int getNumberOfKeys() {
        return this.params.size();
    }
}

