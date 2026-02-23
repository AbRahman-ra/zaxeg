/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnchorPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.SequenceType;

public class ContextItemExpression
extends Expression {
    private ContextItemStaticInfo staticInfo = ContextItemStaticInfo.DEFAULT;
    private String errorCodeForAbsentContext = "XPDY0002";
    private boolean absentContextIsTypeError = false;

    @Override
    public String getExpressionName() {
        return "dot";
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ContextItemExpression cie2 = new ContextItemExpression();
        cie2.staticInfo = this.staticInfo;
        cie2.setErrorCodeForUndefinedContext(this.errorCodeForAbsentContext, false);
        ExpressionTool.copyLocationInfo(this, cie2);
        return cie2;
    }

    public void setErrorCodeForUndefinedContext(String errorCode, boolean isTypeError) {
        this.errorCodeForAbsentContext = errorCode;
        this.absentContextIsTypeError = isTypeError;
    }

    public String getErrorCodeForUndefinedContext() {
        return this.errorCodeForAbsentContext;
    }

    public void setStaticInfo(ContextItemStaticInfo info) {
        this.staticInfo = info;
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        if (contextInfo.getItemType() == ErrorType.getInstance()) {
            visitor.issueWarning("Evaluation will always fail: there is no context item", this.getLocation());
            ErrorExpression ee = new ErrorExpression("There is no context item", this.getErrorCodeForUndefinedContext(), this.absentContextIsTypeError);
            ee.setOriginalExpression(this);
            ExpressionTool.copyLocationInfo(this, ee);
            return ee;
        }
        this.staticInfo = contextInfo;
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        if (contextItemType == null) {
            XPathException err = new XPathException("The context item is undefined at this point");
            err.setErrorCode(this.getErrorCodeForUndefinedContext());
            err.setIsTypeError(this.absentContextIsTypeError);
            err.setLocation(this.getLocation());
            throw err;
        }
        return this;
    }

    @Override
    public ItemType getItemType() {
        return this.staticInfo.getItemType();
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return contextItemType;
    }

    public boolean isContextPossiblyUndefined() {
        return this.staticInfo.isPossiblyAbsent();
    }

    @Override
    public int computeCardinality() {
        return 16384;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        return p | 0x800000 | 0x10000;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ContextItemExpression;
    }

    @Override
    public int computeHashCode() {
        return "ContextItemExpression".hashCode();
    }

    @Override
    public int getIntrinsicDependencies() {
        return 2;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        if (pathMapNodeSet == null) {
            pathMapNodeSet = new PathMap.PathMapNodeSet(pathMap.makeNewRoot(this));
        }
        return pathMapNodeSet;
    }

    @Override
    public boolean isSubtreeExpression() {
        return true;
    }

    @Override
    public int getNetCost() {
        return 0;
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        return AnchorPattern.getInstance();
    }

    @Override
    public String getStreamerName() {
        return "ContextItemExpr";
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Item item = context.getContextItem();
        if (item == null) {
            this.reportAbsentContext(context);
        }
        return SingletonIterator.makeIterator(item);
    }

    @Override
    public Item evaluateItem(XPathContext context) throws XPathException {
        Item item = context.getContextItem();
        if (item == null) {
            this.reportAbsentContext(context);
        }
        return item;
    }

    private void reportAbsentContext(XPathContext context) throws XPathException {
        if (this.absentContextIsTypeError) {
            this.typeError("The context item is absent", this.getErrorCodeForUndefinedContext(), context);
        } else {
            this.dynamicError("The context item is absent", this.getErrorCodeForUndefinedContext(), context);
        }
    }

    @Override
    public String toString() {
        return ".";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("dot", this);
        ItemType type = this.getItemType();
        if (type != AnyItemType.getInstance()) {
            SequenceType st = SequenceType.makeSequenceType(type, 16384);
            destination.emitAttribute("type", st.toAlphaCode());
        }
        if (this.staticInfo.isPossiblyAbsent()) {
            destination.emitAttribute("flags", "a");
        }
        destination.endElement();
    }

    @Override
    public String toShortString() {
        return ".";
    }
}

