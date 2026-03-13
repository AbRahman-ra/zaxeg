/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.util.Iterator;
import java.util.LinkedList;
import org.jaxen.JaxenException;
import org.jaxen.expr.DefaultXPathFactory;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.Predicated;
import org.jaxen.expr.Step;
import org.jaxen.expr.XPathExpr;
import org.jaxen.expr.XPathFactory;
import org.jaxen.saxpath.XPathHandler;

public class JaxenHandler
implements XPathHandler {
    private XPathFactory xpathFactory;
    private XPathExpr xpath;
    protected boolean simplified;
    protected LinkedList stack = new LinkedList();

    public JaxenHandler() {
        this.xpathFactory = new DefaultXPathFactory();
    }

    public void setXPathFactory(XPathFactory xpathFactory) {
        this.xpathFactory = xpathFactory;
    }

    public XPathFactory getXPathFactory() {
        return this.xpathFactory;
    }

    public XPathExpr getXPathExpr() {
        return this.getXPathExpr(true);
    }

    public XPathExpr getXPathExpr(boolean shouldSimplify) {
        if (shouldSimplify && !this.simplified) {
            this.xpath.simplify();
            this.simplified = true;
        }
        return this.xpath;
    }

    public void startXPath() {
        this.simplified = false;
        this.pushFrame();
    }

    public void endXPath() throws JaxenException {
        this.xpath = this.getXPathFactory().createXPath((Expr)this.pop());
        this.popFrame();
    }

    public void startPathExpr() {
        this.pushFrame();
    }

    public void endPathExpr() throws JaxenException {
        FilterExpr filterExpr;
        LocationPath locationPath;
        if (this.stackSize() == 2) {
            locationPath = (LocationPath)this.pop();
            filterExpr = (FilterExpr)this.pop();
        } else {
            Object popped = this.pop();
            if (popped instanceof LocationPath) {
                locationPath = (LocationPath)popped;
                filterExpr = null;
            } else {
                locationPath = null;
                filterExpr = (FilterExpr)popped;
            }
        }
        this.popFrame();
        this.push(this.getXPathFactory().createPathExpr(filterExpr, locationPath));
    }

    public void startAbsoluteLocationPath() throws JaxenException {
        this.pushFrame();
        this.push(this.getXPathFactory().createAbsoluteLocationPath());
    }

    public void endAbsoluteLocationPath() throws JaxenException {
        this.endLocationPath();
    }

    public void startRelativeLocationPath() throws JaxenException {
        this.pushFrame();
        this.push(this.getXPathFactory().createRelativeLocationPath());
    }

    public void endRelativeLocationPath() throws JaxenException {
        this.endLocationPath();
    }

    protected void endLocationPath() throws JaxenException {
        LocationPath path = (LocationPath)this.peekFrame().removeFirst();
        this.addSteps(path, this.popFrame().iterator());
        this.push(path);
    }

    protected void addSteps(LocationPath locationPath, Iterator stepIter) {
        while (stepIter.hasNext()) {
            locationPath.addStep((Step)stepIter.next());
        }
    }

    public void startNameStep(int axis, String prefix, String localName) throws JaxenException {
        this.pushFrame();
        this.push(this.getXPathFactory().createNameStep(axis, prefix, localName));
    }

    public void endNameStep() {
        this.endStep();
    }

    public void startTextNodeStep(int axis) throws JaxenException {
        this.pushFrame();
        this.push(this.getXPathFactory().createTextNodeStep(axis));
    }

    public void endTextNodeStep() {
        this.endStep();
    }

    public void startCommentNodeStep(int axis) throws JaxenException {
        this.pushFrame();
        this.push(this.getXPathFactory().createCommentNodeStep(axis));
    }

    public void endCommentNodeStep() {
        this.endStep();
    }

    public void startAllNodeStep(int axis) throws JaxenException {
        this.pushFrame();
        this.push(this.getXPathFactory().createAllNodeStep(axis));
    }

    public void endAllNodeStep() {
        this.endStep();
    }

    public void startProcessingInstructionNodeStep(int axis, String name) throws JaxenException {
        this.pushFrame();
        this.push(this.getXPathFactory().createProcessingInstructionNodeStep(axis, name));
    }

    public void endProcessingInstructionNodeStep() {
        this.endStep();
    }

    protected void endStep() {
        Step step = (Step)this.peekFrame().removeFirst();
        this.addPredicates(step, this.popFrame().iterator());
        this.push(step);
    }

    public void startPredicate() {
        this.pushFrame();
    }

    public void endPredicate() throws JaxenException {
        Predicate predicate = this.getXPathFactory().createPredicate((Expr)this.pop());
        this.popFrame();
        this.push(predicate);
    }

    public void startFilterExpr() {
        this.pushFrame();
    }

    public void endFilterExpr() throws JaxenException {
        Expr expr = (Expr)this.peekFrame().removeFirst();
        FilterExpr filter = this.getXPathFactory().createFilterExpr(expr);
        Iterator predIter = this.popFrame().iterator();
        this.addPredicates(filter, predIter);
        this.push(filter);
    }

    protected void addPredicates(Predicated obj, Iterator predIter) {
        while (predIter.hasNext()) {
            obj.addPredicate((Predicate)predIter.next());
        }
    }

    protected void returnExpr() {
        Expr expr = (Expr)this.pop();
        this.popFrame();
        this.push(expr);
    }

    public void startOrExpr() {
    }

    public void endOrExpr(boolean create) throws JaxenException {
        if (create) {
            Expr rhs = (Expr)this.pop();
            Expr lhs = (Expr)this.pop();
            this.push(this.getXPathFactory().createOrExpr(lhs, rhs));
        }
    }

    public void startAndExpr() {
    }

    public void endAndExpr(boolean create) throws JaxenException {
        if (create) {
            Expr rhs = (Expr)this.pop();
            Expr lhs = (Expr)this.pop();
            this.push(this.getXPathFactory().createAndExpr(lhs, rhs));
        }
    }

    public void startEqualityExpr() {
    }

    public void endEqualityExpr(int operator) throws JaxenException {
        if (operator != 0) {
            Expr rhs = (Expr)this.pop();
            Expr lhs = (Expr)this.pop();
            this.push(this.getXPathFactory().createEqualityExpr(lhs, rhs, operator));
        }
    }

    public void startRelationalExpr() {
    }

    public void endRelationalExpr(int operator) throws JaxenException {
        if (operator != 0) {
            Expr rhs = (Expr)this.pop();
            Expr lhs = (Expr)this.pop();
            this.push(this.getXPathFactory().createRelationalExpr(lhs, rhs, operator));
        }
    }

    public void startAdditiveExpr() {
    }

    public void endAdditiveExpr(int operator) throws JaxenException {
        if (operator != 0) {
            Expr rhs = (Expr)this.pop();
            Expr lhs = (Expr)this.pop();
            this.push(this.getXPathFactory().createAdditiveExpr(lhs, rhs, operator));
        }
    }

    public void startMultiplicativeExpr() {
    }

    public void endMultiplicativeExpr(int operator) throws JaxenException {
        if (operator != 0) {
            Expr rhs = (Expr)this.pop();
            Expr lhs = (Expr)this.pop();
            this.push(this.getXPathFactory().createMultiplicativeExpr(lhs, rhs, operator));
        }
    }

    public void startUnaryExpr() {
    }

    public void endUnaryExpr(int operator) throws JaxenException {
        if (operator != 0) {
            this.push(this.getXPathFactory().createUnaryExpr((Expr)this.pop(), operator));
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

    public void number(int number) throws JaxenException {
        this.push(this.getXPathFactory().createNumberExpr(number));
    }

    public void number(double number) throws JaxenException {
        this.push(this.getXPathFactory().createNumberExpr(number));
    }

    public void literal(String literal) throws JaxenException {
        this.push(this.getXPathFactory().createLiteralExpr(literal));
    }

    public void variableReference(String prefix, String variableName) throws JaxenException {
        this.push(this.getXPathFactory().createVariableReferenceExpr(prefix, variableName));
    }

    public void startFunction(String prefix, String functionName) throws JaxenException {
        this.pushFrame();
        this.push(this.getXPathFactory().createFunctionCallExpr(prefix, functionName));
    }

    public void endFunction() {
        FunctionCallExpr function = (FunctionCallExpr)this.peekFrame().removeFirst();
        this.addParameters(function, this.popFrame().iterator());
        this.push(function);
    }

    protected void addParameters(FunctionCallExpr function, Iterator paramIter) {
        while (paramIter.hasNext()) {
            function.addParameter((Expr)paramIter.next());
        }
    }

    protected int stackSize() {
        return this.peekFrame().size();
    }

    protected void push(Object obj) {
        this.peekFrame().addLast(obj);
    }

    protected Object pop() {
        return this.peekFrame().removeLast();
    }

    protected boolean canPop() {
        return this.peekFrame().size() > 0;
    }

    protected void pushFrame() {
        this.stack.addLast(new LinkedList());
    }

    protected LinkedList popFrame() {
        return (LinkedList)this.stack.removeLast();
    }

    protected LinkedList peekFrame() {
        return (LinkedList)this.stack.getLast();
    }
}

