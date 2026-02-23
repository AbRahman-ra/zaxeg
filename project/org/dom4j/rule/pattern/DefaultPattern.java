/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.rule.pattern;

import org.dom4j.Node;
import org.dom4j.NodeFilter;
import org.dom4j.rule.Pattern;

public class DefaultPattern
implements Pattern {
    private NodeFilter filter;

    public DefaultPattern(NodeFilter filter) {
        this.filter = filter;
    }

    public boolean matches(Node node) {
        return this.filter.matches(node);
    }

    public double getPriority() {
        return 0.5;
    }

    public Pattern[] getUnionPatterns() {
        return null;
    }

    public short getMatchType() {
        return 0;
    }

    public String getMatchesNodeName() {
        return null;
    }
}

