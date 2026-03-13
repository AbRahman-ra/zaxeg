/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.XmlProcessingError;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;

public class ErrorExpression
extends Expression {
    private XmlProcessingError exception;
    private Expression original;

    public ErrorExpression() {
        this("Unspecified error", "XXXX9999", false);
    }

    public ErrorExpression(String message, String errorCode, boolean isTypeError) {
        this(new XmlProcessingIncident(message, errorCode));
        ((XmlProcessingIncident)this.exception).setTypeError(isTypeError);
    }

    public ErrorExpression(XmlProcessingError exception) {
        this.exception = exception;
    }

    public XmlProcessingError getException() {
        return this.exception;
    }

    public boolean isTypeError() {
        return this.exception.isTypeError();
    }

    public String getMessage() {
        return this.exception.getMessage();
    }

    public String getErrorCodeLocalPart() {
        return this.exception.getErrorCode().getLocalName();
    }

    public void setOriginalExpression(Expression original) {
        this.original = original;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this;
    }

    @Override
    public int getImplementationMethod() {
        return 3;
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        if (this.exception != null) {
            XPathException err = new XPathException(this.exception.getMessage());
            err.setLocation(this.exception.getLocation());
            err.maybeSetLocation(this.getLocation());
            if (this.exception.getErrorCode() != null) {
                err.setErrorCodeQName(this.exception.getErrorCode().getStructuredQName());
            }
            err.maybeSetContext(context);
            err.setIsTypeError(this.exception.isTypeError());
            throw err;
        }
        XPathException err = XPathException.fromXmlProcessingError(this.exception);
        err.setLocation(this.getLocation());
        err.setXPathContext(context);
        throw err;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        this.evaluateItem(context);
        return null;
    }

    @Override
    public ItemType getItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public int computeCardinality() {
        return 57344;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ErrorExpression e2 = new ErrorExpression(this.exception);
        e2.setOriginalExpression(this.original);
        ExpressionTool.copyLocationInfo(this, e2);
        return e2;
    }

    @Override
    public String getExpressionName() {
        return "errorExpr";
    }

    @Override
    public String toString() {
        if (this.original != null) {
            return this.original.toString();
        }
        return "error(\"" + this.getMessage() + "\")";
    }

    @Override
    public String toShortString() {
        if (this.original != null) {
            return this.original.toShortString();
        }
        return "error(\"" + this.getMessage() + "\")";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("error", this);
        destination.emitAttribute("message", this.exception.getMessage());
        destination.emitAttribute("code", this.exception.getErrorCode().getLocalName());
        destination.emitAttribute("isTypeErr", this.exception.isTypeError() ? "0" : "1");
        destination.endElement();
    }
}

