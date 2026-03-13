/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.sf.saxon.lib.FunctionAnnotationHandler;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;

public class XQueryFunctionAnnotationHandler
implements FunctionAnnotationHandler {
    private static DisallowedCombination[] blackList = new DisallowedCombination[]{new DisallowedCombination(Annotation.SIMPLE, null, "XUST0032", "DV"), new DisallowedCombination(Annotation.UPDATING, null, "XUST0032", "DV"), new DisallowedCombination(Annotation.PUBLIC, null, "XQST0125", "IF"), new DisallowedCombination(Annotation.PRIVATE, null, "XQST0125", "IF"), new DisallowedCombination(Annotation.PRIVATE, Annotation.PRIVATE, "XQST0106", "DF"), new DisallowedCombination(Annotation.PRIVATE, Annotation.PUBLIC, "XQST0106", "DF"), new DisallowedCombination(Annotation.PUBLIC, Annotation.PUBLIC, "XQST0106", "DF"), new DisallowedCombination(Annotation.PUBLIC, Annotation.PRIVATE, "XQST0106", "DF"), new DisallowedCombination(Annotation.PRIVATE, Annotation.PRIVATE, "XQST0116", "DV"), new DisallowedCombination(Annotation.PRIVATE, Annotation.PUBLIC, "XQST0116", "DV"), new DisallowedCombination(Annotation.PUBLIC, Annotation.PUBLIC, "XQST0116", "DV"), new DisallowedCombination(Annotation.PUBLIC, Annotation.PRIVATE, "XQST0116", "DV"), new DisallowedCombination(Annotation.UPDATING, Annotation.UPDATING, "XUST0033", "DF", "IF"), new DisallowedCombination(Annotation.UPDATING, Annotation.SIMPLE, "XUST0033", "DF", "IF"), new DisallowedCombination(Annotation.SIMPLE, Annotation.SIMPLE, "XUST0033", "DF", "IF"), new DisallowedCombination(Annotation.SIMPLE, Annotation.UPDATING, "XUST0033", "DF", "IF")};

    @Override
    public void check(AnnotationList annotations, String construct) throws XPathException {
        for (int i = 0; i < annotations.size(); ++i) {
            Annotation ann = annotations.get(i);
            for (DisallowedCombination dc : blackList) {
                if (!dc.one.equals(ann.getAnnotationQName()) || !dc.where.contains(construct)) continue;
                if (dc.two == null) {
                    throw new XPathException("Annotation %" + ann.getAnnotationQName().getLocalPart() + " is not allowed here", dc.errorCode);
                }
                for (int j = 0; j < i; ++j) {
                    Annotation other = annotations.get(j);
                    if (!dc.two.equals(other.getAnnotationQName())) continue;
                    if (dc.two.equals(ann.getAnnotationQName())) {
                        throw new XPathException("Annotation %" + ann.getAnnotationQName().getLocalPart() + " cannot appear more than once", dc.errorCode);
                    }
                    throw new XPathException("Annotations %" + ann.getAnnotationQName().getLocalPart() + " and " + other.getAnnotationQName().getLocalPart() + " cannot appear together", dc.errorCode);
                }
            }
        }
    }

    @Override
    public String getAssertionNamespace() {
        return "http://www.w3.org/2012/xquery";
    }

    @Override
    public boolean satisfiesAssertion(Annotation assertion, AnnotationList annotationList) {
        return true;
    }

    @Override
    public Affinity relationship(AnnotationList firstList, AnnotationList secondList) {
        return Affinity.OVERLAPS;
    }

    private static class DisallowedCombination {
        public StructuredQName one;
        public StructuredQName two;
        public String errorCode;
        public Set<String> where;

        public DisallowedCombination(StructuredQName one, StructuredQName two, String errorCode, String ... where) {
            this.one = one;
            this.two = two;
            this.errorCode = errorCode;
            this.where = new HashSet<String>(where.length);
            Collections.addAll(this.where, where);
        }
    }
}

