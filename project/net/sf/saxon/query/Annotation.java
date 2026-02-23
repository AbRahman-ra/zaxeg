/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.value.StringValue;

public class Annotation {
    public static final StructuredQName UPDATING = new StructuredQName("", "http://www.w3.org/2012/xquery", "updating");
    public static final StructuredQName SIMPLE = new StructuredQName("", "http://www.w3.org/2012/xquery", "simple");
    public static final StructuredQName PRIVATE = new StructuredQName("", "http://www.w3.org/2012/xquery", "private");
    public static final StructuredQName PUBLIC = new StructuredQName("", "http://www.w3.org/2012/xquery", "public");
    private StructuredQName qName = null;
    private List<AtomicValue> annotationParameters = null;

    public Annotation(StructuredQName name) {
        this.qName = name;
    }

    public StructuredQName getAnnotationQName() {
        return this.qName;
    }

    public void addAnnotationParameter(AtomicValue value) {
        if (this.annotationParameters == null) {
            this.annotationParameters = new ArrayList<AtomicValue>();
        }
        this.annotationParameters.add(value);
    }

    public List<AtomicValue> getAnnotationParameters() {
        if (this.annotationParameters == null) {
            this.annotationParameters = new ArrayList<AtomicValue>();
        }
        return this.annotationParameters;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Annotation) || !this.qName.equals(((Annotation)other).qName) || this.getAnnotationParameters().size() != ((Annotation)other).getAnnotationParameters().size()) {
            return false;
        }
        for (int i = 0; i < this.annotationParameters.size(); ++i) {
            if (Annotation.annotationParamEqual(this.annotationParameters.get(i), ((Annotation)other).annotationParameters.get(i))) continue;
            return false;
        }
        return true;
    }

    private static boolean annotationParamEqual(AtomicValue a, AtomicValue b) {
        if (a instanceof StringValue && b instanceof StringValue) {
            return a.getStringValue().equals(b.getStringValue());
        }
        if (a instanceof NumericValue && b instanceof NumericValue) {
            return ((NumericValue)a).getDoubleValue() == ((NumericValue)b).getDoubleValue();
        }
        return false;
    }

    public int hashCode() {
        return this.qName.hashCode() ^ this.annotationParameters.hashCode();
    }
}

