/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.pattern;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.expr.FilterExpr;
import org.jaxen.pattern.AnyNodeTest;
import org.jaxen.pattern.NodeTest;
import org.jaxen.pattern.Pattern;
import org.jaxen.util.SingletonList;

public class LocationPathPattern
extends Pattern {
    private NodeTest nodeTest = AnyNodeTest.getInstance();
    private Pattern parentPattern;
    private Pattern ancestorPattern;
    private List filters;
    private boolean absolute;

    public LocationPathPattern() {
    }

    public LocationPathPattern(NodeTest nodeTest) {
        this.nodeTest = nodeTest;
    }

    public Pattern simplify() {
        if (this.parentPattern != null) {
            this.parentPattern = this.parentPattern.simplify();
        }
        if (this.ancestorPattern != null) {
            this.ancestorPattern = this.ancestorPattern.simplify();
        }
        if (this.filters == null) {
            if (this.parentPattern == null && this.ancestorPattern == null) {
                return this.nodeTest;
            }
            if (this.parentPattern != null && this.ancestorPattern == null && this.nodeTest instanceof AnyNodeTest) {
                return this.parentPattern;
            }
        }
        return this;
    }

    public void addFilter(FilterExpr filter) {
        if (this.filters == null) {
            this.filters = new ArrayList();
        }
        this.filters.add(filter);
    }

    public void setParentPattern(Pattern parentPattern) {
        this.parentPattern = parentPattern;
    }

    public void setAncestorPattern(Pattern ancestorPattern) {
        this.ancestorPattern = ancestorPattern;
    }

    public void setNodeTest(NodeTest nodeTest) throws JaxenException {
        if (!(this.nodeTest instanceof AnyNodeTest)) {
            throw new JaxenException("Attempt to overwrite nodeTest: " + this.nodeTest + " with: " + nodeTest);
        }
        this.nodeTest = nodeTest;
    }

    public boolean matches(Object node, Context context) throws JaxenException {
        Navigator navigator = context.getNavigator();
        if (!this.nodeTest.matches(node, context)) {
            return false;
        }
        if (this.parentPattern != null) {
            Object parent = navigator.getParentNode(node);
            if (parent == null) {
                return false;
            }
            if (!this.parentPattern.matches(parent, context)) {
                return false;
            }
        }
        if (this.ancestorPattern != null) {
            Object ancestor = navigator.getParentNode(node);
            while (!this.ancestorPattern.matches(ancestor, context)) {
                if (ancestor == null) {
                    return false;
                }
                if (navigator.isDocument(ancestor)) {
                    return false;
                }
                ancestor = navigator.getParentNode(ancestor);
            }
        }
        if (this.filters != null) {
            SingletonList list = new SingletonList(node);
            context.setNodeSet(list);
            boolean answer = true;
            Iterator iter = this.filters.iterator();
            while (iter.hasNext()) {
                FilterExpr filter = (FilterExpr)iter.next();
                if (filter.asBoolean(context)) continue;
                answer = false;
                break;
            }
            context.setNodeSet(list);
            return answer;
        }
        return true;
    }

    public double getPriority() {
        if (this.filters != null) {
            return 0.5;
        }
        return this.nodeTest.getPriority();
    }

    public short getMatchType() {
        return this.nodeTest.getMatchType();
    }

    public String getText() {
        String text;
        StringBuffer buffer = new StringBuffer();
        if (this.absolute) {
            buffer.append("/");
        }
        if (this.ancestorPattern != null && (text = this.ancestorPattern.getText()).length() > 0) {
            buffer.append(text);
            buffer.append("//");
        }
        if (this.parentPattern != null && (text = this.parentPattern.getText()).length() > 0) {
            buffer.append(text);
            buffer.append("/");
        }
        buffer.append(this.nodeTest.getText());
        if (this.filters != null) {
            buffer.append("[");
            Iterator iter = this.filters.iterator();
            while (iter.hasNext()) {
                FilterExpr filter = (FilterExpr)iter.next();
                buffer.append(filter.getText());
            }
            buffer.append("]");
        }
        return buffer.toString();
    }

    public String toString() {
        return super.toString() + "[ absolute: " + this.absolute + " parent: " + this.parentPattern + " ancestor: " + this.ancestorPattern + " filters: " + this.filters + " nodeTest: " + this.nodeTest + " ]";
    }

    public boolean isAbsolute() {
        return this.absolute;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    public boolean hasAnyNodeTest() {
        return this.nodeTest instanceof AnyNodeTest;
    }
}

