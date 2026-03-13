/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.pattern;

import java.util.LinkedList;
import org.jaxen.JaxenException;
import org.jaxen.JaxenHandler;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.pattern.AnyNodeTest;
import org.jaxen.pattern.LocationPathPattern;
import org.jaxen.pattern.NameTest;
import org.jaxen.pattern.NamespaceTest;
import org.jaxen.pattern.NodeTest;
import org.jaxen.pattern.NodeTypeTest;
import org.jaxen.pattern.Pattern;

public class PatternHandler
extends JaxenHandler {
    private Pattern pattern;

    public Pattern getPattern() {
        return this.getPattern(true);
    }

    public Pattern getPattern(boolean shouldSimplify) {
        if (shouldSimplify && !this.simplified) {
            this.pattern.simplify();
            this.simplified = true;
        }
        return this.pattern;
    }

    public void endXPath() {
        this.pattern = (Pattern)this.pop();
        System.out.println("stack is: " + this.stack);
        this.popFrame();
    }

    public void endPathExpr() {
        LinkedList frame = this.popFrame();
        System.out.println("endPathExpr(): " + frame);
        this.push(frame.removeFirst());
    }

    public void startAbsoluteLocationPath() {
        this.pushFrame();
        this.push(this.createAbsoluteLocationPath());
    }

    public void endAbsoluteLocationPath() throws JaxenException {
        this.endLocationPath();
    }

    public void startRelativeLocationPath() {
        this.pushFrame();
        this.push(this.createRelativeLocationPath());
    }

    public void endRelativeLocationPath() throws JaxenException {
        this.endLocationPath();
    }

    protected void endLocationPath() throws JaxenException {
        LinkedList list = this.popFrame();
        System.out.println("endLocationPath: " + list);
        LocationPathPattern locationPath = (LocationPathPattern)list.removeFirst();
        this.push(locationPath);
        boolean doneNodeTest = false;
        while (!list.isEmpty()) {
            LocationPathPattern parent;
            Object filter = list.removeFirst();
            if (filter instanceof NodeTest) {
                if (doneNodeTest) {
                    parent = new LocationPathPattern((NodeTest)filter);
                    locationPath.setParentPattern(parent);
                    locationPath = parent;
                    doneNodeTest = false;
                    continue;
                }
                locationPath.setNodeTest((NodeTest)filter);
                continue;
            }
            if (filter instanceof FilterExpr) {
                locationPath.addFilter((FilterExpr)filter);
                continue;
            }
            if (!(filter instanceof LocationPathPattern)) continue;
            parent = (LocationPathPattern)filter;
            locationPath.setParentPattern(parent);
            locationPath = parent;
            doneNodeTest = false;
        }
    }

    public void startNameStep(int axis, String prefix, String localName) {
        this.pushFrame();
        short nodeType = 1;
        switch (axis) {
            case 9: {
                nodeType = 2;
                break;
            }
            case 10: {
                nodeType = 13;
            }
        }
        if (prefix != null && prefix.length() > 0 && !prefix.equals("*")) {
            this.push(new NamespaceTest(prefix, nodeType));
        }
        if (localName != null && localName.length() > 0 && !localName.equals("*")) {
            this.push(new NameTest(localName, nodeType));
        }
    }

    public void startTextNodeStep(int axis) {
        this.pushFrame();
        this.push(new NodeTypeTest(3));
    }

    public void startCommentNodeStep(int axis) {
        this.pushFrame();
        this.push(new NodeTypeTest(8));
    }

    public void startAllNodeStep(int axis) {
        this.pushFrame();
        this.push(AnyNodeTest.getInstance());
    }

    public void startProcessingInstructionNodeStep(int axis, String name) {
        this.pushFrame();
        this.push(new NodeTypeTest(7));
    }

    protected void endStep() {
        LinkedList list = this.popFrame();
        if (!list.isEmpty()) {
            this.push(list.removeFirst());
            if (!list.isEmpty()) {
                System.out.println("List should now be empty!" + list);
            }
        }
    }

    public void startUnionExpr() {
    }

    public void endUnionExpr(boolean create) throws JaxenException {
        if (create) {
            Expr rhs = (Expr)this.pop();
            Expr lhs = (Expr)this.pop();
            this.push(this.getXPathFactory().createUnionExpr(lhs, rhs));
        }
    }

    protected Pattern createAbsoluteLocationPath() {
        return new LocationPathPattern(NodeTypeTest.DOCUMENT_TEST);
    }

    protected Pattern createRelativeLocationPath() {
        return new LocationPathPattern();
    }
}

