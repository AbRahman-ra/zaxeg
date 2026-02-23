/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.FunctionAnnotationHandler;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.query.Annotation;
import net.sf.saxon.trans.XPathException;

public class AnnotationList
implements Iterable<Annotation> {
    private List<Annotation> list;
    public static AnnotationList EMPTY = new AnnotationList(Collections.emptyList());

    public AnnotationList(List<Annotation> list) {
        this.list = list;
    }

    public static AnnotationList singleton(Annotation ann) {
        return new AnnotationList(Collections.singletonList(ann));
    }

    public void check(Configuration config, String where) throws XPathException {
        Map<String, List<Annotation>> map = this.groupByNamespace();
        for (Map.Entry<String, List<Annotation>> entry : map.entrySet()) {
            FunctionAnnotationHandler handler = config.getFunctionAnnotationHandler(entry.getKey());
            if (handler == null) continue;
            handler.check(new AnnotationList(entry.getValue()), where);
        }
    }

    private Map<String, List<Annotation>> groupByNamespace() {
        HashMap<String, List<Annotation>> result = new HashMap<String, List<Annotation>>();
        for (Annotation ann : this.list) {
            String ns = ann.getAnnotationQName().getURI();
            if (result.containsKey(ns)) {
                ((List)result.get(ns)).add(ann);
                continue;
            }
            ArrayList<Annotation> list = new ArrayList<Annotation>();
            list.add(ann);
            result.put(ns, list);
        }
        return result;
    }

    public AnnotationList filterByNamespace(String ns) {
        ArrayList<Annotation> out = new ArrayList<Annotation>();
        for (Annotation ann : this.list) {
            if (!ann.getAnnotationQName().hasURI(ns)) continue;
            out.add(ann);
        }
        return new AnnotationList(out);
    }

    @Override
    public Iterator<Annotation> iterator() {
        return this.list.iterator();
    }

    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    public int size() {
        return this.list.size();
    }

    public Annotation get(int i) {
        return this.list.get(i);
    }

    public boolean includes(StructuredQName name) {
        for (Annotation a : this.list) {
            if (!a.getAnnotationQName().equals(name)) continue;
            return true;
        }
        return false;
    }

    public boolean includes(String localName) {
        for (Annotation a : this.list) {
            if (!a.getAnnotationQName().getLocalPart().equals(localName)) continue;
            return true;
        }
        return false;
    }

    public boolean equals(Object other) {
        return other instanceof AnnotationList && this.list.equals(((AnnotationList)other).list);
    }

    public int hashCode() {
        return this.list.hashCode();
    }
}

