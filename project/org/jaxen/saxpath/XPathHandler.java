/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.saxpath;

import org.jaxen.saxpath.SAXPathException;

public interface XPathHandler {
    public void startXPath() throws SAXPathException;

    public void endXPath() throws SAXPathException;

    public void startPathExpr() throws SAXPathException;

    public void endPathExpr() throws SAXPathException;

    public void startAbsoluteLocationPath() throws SAXPathException;

    public void endAbsoluteLocationPath() throws SAXPathException;

    public void startRelativeLocationPath() throws SAXPathException;

    public void endRelativeLocationPath() throws SAXPathException;

    public void startNameStep(int var1, String var2, String var3) throws SAXPathException;

    public void endNameStep() throws SAXPathException;

    public void startTextNodeStep(int var1) throws SAXPathException;

    public void endTextNodeStep() throws SAXPathException;

    public void startCommentNodeStep(int var1) throws SAXPathException;

    public void endCommentNodeStep() throws SAXPathException;

    public void startAllNodeStep(int var1) throws SAXPathException;

    public void endAllNodeStep() throws SAXPathException;

    public void startProcessingInstructionNodeStep(int var1, String var2) throws SAXPathException;

    public void endProcessingInstructionNodeStep() throws SAXPathException;

    public void startPredicate() throws SAXPathException;

    public void endPredicate() throws SAXPathException;

    public void startFilterExpr() throws SAXPathException;

    public void endFilterExpr() throws SAXPathException;

    public void startOrExpr() throws SAXPathException;

    public void endOrExpr(boolean var1) throws SAXPathException;

    public void startAndExpr() throws SAXPathException;

    public void endAndExpr(boolean var1) throws SAXPathException;

    public void startEqualityExpr() throws SAXPathException;

    public void endEqualityExpr(int var1) throws SAXPathException;

    public void startRelationalExpr() throws SAXPathException;

    public void endRelationalExpr(int var1) throws SAXPathException;

    public void startAdditiveExpr() throws SAXPathException;

    public void endAdditiveExpr(int var1) throws SAXPathException;

    public void startMultiplicativeExpr() throws SAXPathException;

    public void endMultiplicativeExpr(int var1) throws SAXPathException;

    public void startUnaryExpr() throws SAXPathException;

    public void endUnaryExpr(int var1) throws SAXPathException;

    public void startUnionExpr() throws SAXPathException;

    public void endUnionExpr(boolean var1) throws SAXPathException;

    public void number(int var1) throws SAXPathException;

    public void number(double var1) throws SAXPathException;

    public void literal(String var1) throws SAXPathException;

    public void variableReference(String var1, String var2) throws SAXPathException;

    public void startFunction(String var1, String var2) throws SAXPathException;

    public void endFunction() throws SAXPathException;
}

