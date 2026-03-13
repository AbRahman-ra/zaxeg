/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import net.sf.saxon.query.Annotation;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;

public interface FunctionAnnotationHandler {
    public String getAssertionNamespace();

    public void check(AnnotationList var1, String var2) throws XPathException;

    public boolean satisfiesAssertion(Annotation var1, AnnotationList var2);

    public Affinity relationship(AnnotationList var1, AnnotationList var2);
}

