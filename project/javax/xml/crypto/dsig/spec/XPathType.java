/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.spec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class XPathType {
    private final String expression;
    private final Filter filter;
    private Map nsMap;

    public XPathType(String expression, Filter filter) {
        if (expression == null) {
            throw new NullPointerException("expression cannot be null");
        }
        if (filter == null) {
            throw new NullPointerException("filter cannot be null");
        }
        this.expression = expression;
        this.filter = filter;
        this.nsMap = Collections.EMPTY_MAP;
    }

    public XPathType(String expression, Filter filter, Map namespaceMap) {
        this(expression, filter);
        if (namespaceMap == null) {
            throw new NullPointerException("namespaceMap cannot be null");
        }
        this.nsMap = XPathType.unmodifiableCopyOfMap(namespaceMap);
        for (Map.Entry me : this.nsMap.entrySet()) {
            if (me.getKey() instanceof String && me.getValue() instanceof String) continue;
            throw new ClassCastException("not a String");
        }
    }

    private static Map unmodifiableCopyOfMap(Map map) {
        return Collections.unmodifiableMap(new HashMap(map));
    }

    public String getExpression() {
        return this.expression;
    }

    public Filter getFilter() {
        return this.filter;
    }

    public Map getNamespaceMap() {
        return this.nsMap;
    }

    public static class Filter {
        private final String operation;
        public static final Filter INTERSECT = new Filter("intersect");
        public static final Filter SUBTRACT = new Filter("subtract");
        public static final Filter UNION = new Filter("union");

        private Filter(String operation) {
            this.operation = operation;
        }

        public String toString() {
            return this.operation;
        }
    }
}

