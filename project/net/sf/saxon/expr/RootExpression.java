/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;

public class RootExpression
extends Expression {
    private boolean contextMaybeUndefined = true;
    private boolean doneWarnings = false;

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        TypeHierarchy th = visitor.getConfiguration().getTypeHierarchy();
        if (contextInfo == null || contextInfo.getItemType() == null || contextInfo.getItemType().equals(ErrorType.getInstance())) {
            XPathException err = new XPathException(this.noContextMessage() + ": the context item is absent");
            err.setErrorCode("XPDY0002");
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        if (!this.doneWarnings && contextInfo.isParentless() && th.relationship(contextInfo.getItemType(), NodeKindTest.DOCUMENT) == Affinity.DISJOINT) {
            visitor.issueWarning(this.noContextMessage() + ": the context item is parentless and is not a document node", this.getLocation());
            this.doneWarnings = true;
        }
        this.contextMaybeUndefined = contextInfo.isPossiblyAbsent();
        if (th.isSubType(contextInfo.getItemType(), NodeKindTest.DOCUMENT)) {
            ContextItemExpression cie = new ContextItemExpression();
            ExpressionTool.copyLocationInfo(this, cie);
            cie.setStaticInfo(contextInfo);
            return cie;
        }
        Affinity relation = th.relationship(contextInfo.getItemType(), AnyNodeTest.getInstance());
        if (relation == Affinity.DISJOINT) {
            XPathException err = new XPathException(this.noContextMessage() + ": the context item is not a node");
            err.setErrorCode("XPTY0020");
            err.setIsTypeError(true);
            err.setLocation(this.getLocation());
            throw err;
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        return this.typeCheck(visitor, contextItemType);
    }

    @Override
    public int computeSpecialProperties() {
        return 25362432;
    }

    public boolean isContextPossiblyUndefined() {
        return this.contextMaybeUndefined;
    }

    protected String noContextMessage() {
        return "Leading '/' selects nothing";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RootExpression;
    }

    @Override
    public final int computeCardinality() {
        return 16384;
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.DOCUMENT;
    }

    @Override
    public UType getStaticUType(UType contextItemType) {
        return UType.DOCUMENT;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    @Override
    public int computeHashCode() {
        return "RootExpression".hashCode();
    }

    public NodeInfo getNode(XPathContext context) throws XPathException {
        Item current = context.getContextItem();
        if (current == null) {
            this.dynamicError("Finding root of tree: the context item is absent", "XPDY0002", context);
        }
        if (current instanceof NodeInfo) {
            NodeInfo doc = ((NodeInfo)current).getRoot();
            if (doc.getNodeKind() != 9) {
                this.dynamicError("The root of the tree containing the context item is not a document node", "XPDY0050", context);
            }
            return doc;
        }
        this.typeError("Finding root of tree: the context item is not a node", "XPTY0020", context);
        return null;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 16;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        RootExpression exp = new RootExpression();
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public Pattern toPattern(Configuration config) throws XPathException {
        return new NodeTestPattern(NodeKindTest.DOCUMENT);
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        if (pathMapNodeSet == null) {
            ContextItemExpression cie = new ContextItemExpression();
            ExpressionTool.copyLocationInfo(this, cie);
            pathMapNodeSet = new PathMap.PathMapNodeSet(pathMap.makeNewRoot(cie));
        }
        return pathMapNodeSet.createArc(1, NodeKindTest.DOCUMENT);
    }

    @Override
    public String toString() {
        return "(/)";
    }

    @Override
    public String getExpressionName() {
        return "root";
    }

    @Override
    public void export(ExpressionPresenter destination) throws XPathException {
        destination.startElement("root", this);
        destination.endElement();
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        return SingletonIterator.makeIterator(this.getNode(context));
    }

    @Override
    public NodeInfo evaluateItem(XPathContext context) throws XPathException {
        return this.getNode(context);
    }

    @Override
    public boolean effectiveBooleanValue(XPathContext context) throws XPathException {
        return this.getNode(context) != null;
    }

    @Override
    public String getStreamerName() {
        return "RootExpression";
    }
}

