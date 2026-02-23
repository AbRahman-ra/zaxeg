/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.FunctionAnnotationHandler;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyFunctionType;
import net.sf.saxon.type.TypeHierarchy;

public class AnyFunctionTypeWithAssertions
extends AnyFunctionType {
    private AnnotationList assertions;
    private Configuration config;

    public AnyFunctionTypeWithAssertions(AnnotationList assertions, Configuration config) {
        this.assertions = assertions;
        this.config = config;
    }

    @Override
    public AnnotationList getAnnotationAssertions() {
        return this.assertions;
    }

    @Override
    public boolean matches(Item item, TypeHierarchy th) throws XPathException {
        return item instanceof Function && AnyFunctionTypeWithAssertions.checkAnnotationAssertions(this.assertions, (Function)item, th.getConfiguration());
    }

    private static boolean checkAnnotationAssertions(AnnotationList assertions, Function item, Configuration config) {
        AnnotationList annotations = item.getAnnotations();
        for (Annotation ann : assertions) {
            boolean ok;
            FunctionAnnotationHandler handler = config.getFunctionAnnotationHandler(ann.getAnnotationQName().getURI());
            if (handler == null || (ok = handler.satisfiesAssertion(ann, annotations))) continue;
            return false;
        }
        return true;
    }
}

