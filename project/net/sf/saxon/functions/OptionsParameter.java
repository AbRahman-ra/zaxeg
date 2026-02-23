/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class OptionsParameter {
    private Map<String, SequenceType> allowedOptions = new HashMap<String, SequenceType>(8);
    private Map<String, Sequence> defaultValues = new HashMap<String, Sequence>(8);
    private Set<String> requiredOptions = new HashSet<String>(4);
    private Map<String, Set<String>> allowedValues = new HashMap<String, Set<String>>(8);
    private String errorCodeForDisallowedValue;
    private String errorCodeForAbsentValue = "SXJE9999";
    private boolean allowCastFromString = false;

    public void addAllowedOption(String name, SequenceType type) {
        this.allowedOptions.put(name, type);
    }

    public void addRequiredOption(String name, SequenceType type) {
        this.allowedOptions.put(name, type);
        this.requiredOptions.add(name);
    }

    public void addAllowedOption(String name, SequenceType type, Sequence defaultValue) {
        this.allowedOptions.put(name, type);
        if (defaultValue != null) {
            this.defaultValues.put(name, defaultValue);
        }
    }

    public void setAllowedValues(String name, String errorCode, String ... values) {
        HashSet<String> valueSet = new HashSet<String>(Arrays.asList(values));
        this.allowedValues.put(name, valueSet);
        this.errorCodeForDisallowedValue = errorCode;
    }

    public Map<String, Sequence> processSuppliedOptions(MapItem supplied, XPathContext context) throws XPathException {
        HashMap<String, Sequence> result = new HashMap<String, Sequence>();
        TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
        for (String string : this.requiredOptions) {
            if (supplied.get(new StringValue(string)) != null) continue;
            throw new XPathException("No value supplied for required option: " + string, this.errorCodeForAbsentValue);
        }
        for (Map.Entry entry : this.allowedOptions.entrySet()) {
            String nominalKey = (String)entry.getKey();
            AtomicValue actualKey = nominalKey.startsWith("Q{") ? new QNameValue(StructuredQName.fromEQName(nominalKey), BuiltInAtomicType.QNAME) : new StringValue(nominalKey);
            SequenceType required = (SequenceType)entry.getValue();
            Sequence actual = supplied.get(actualKey);
            if (actual != null) {
                if (!required.matches(actual, th)) {
                    boolean ok = false;
                    if (actual instanceof StringValue && this.allowCastFromString && required.getPrimaryType() instanceof AtomicType) {
                        try {
                            ConversionRules rules = context.getConfiguration().getConversionRules();
                            actual = Converter.convert((StringValue)actual, (AtomicType)required.getPrimaryType(), rules);
                            ok = true;
                        } catch (XPathException err) {
                            ok = false;
                        }
                    }
                    if (!ok) {
                        RoleDiagnostic role = new RoleDiagnostic(15, nominalKey, 0);
                        role.setErrorCode("XPTY0004");
                        actual = th.applyFunctionConversionRules(actual, required, role, Loc.NONE);
                    }
                }
                actual = actual.materialize();
                Set<String> permitted = this.allowedValues.get(nominalKey);
                if (!(permitted == null || actual instanceof AtomicValue && permitted.contains(((AtomicValue)actual).getStringValue()))) {
                    StringBuilder message = new StringBuilder("Invalid option " + nominalKey + "=" + Err.depictSequence(actual) + ". Valid values are:");
                    int i = 0;
                    for (String v : permitted) {
                        message.append(i++ == 0 ? " " : ", ").append(v);
                    }
                    throw new XPathException(message.toString(), this.errorCodeForDisallowedValue);
                }
                result.put(nominalKey, actual);
                continue;
            }
            Sequence def = this.defaultValues.get(nominalKey);
            if (def == null) continue;
            result.put(nominalKey, def);
        }
        return result;
    }

    public Map<String, Sequence> getDefaultOptions() {
        HashMap<String, Sequence> result = new HashMap<String, Sequence>();
        for (Map.Entry<String, Sequence> entry : this.defaultValues.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public String getErrorCodeForAbsentValue() {
        return this.errorCodeForAbsentValue;
    }

    public void setErrorCodeForAbsentValue(String errorCodeForAbsentValue) {
        this.errorCodeForAbsentValue = errorCodeForAbsentValue;
    }

    public boolean isAllowCastFromString() {
        return this.allowCastFromString;
    }

    public void setAllowCastFromString(boolean allowCastFromString) {
        this.allowCastFromString = allowCastFromString;
    }
}

