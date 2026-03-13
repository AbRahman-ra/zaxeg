/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.xpath;

import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.functions.Number_1;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.NumericValue;
import org.xml.sax.InputSource;

public class XPathExpressionImpl
implements XPathExpression {
    private Configuration config;
    private Executable executable;
    private Expression expression;
    private Expression atomizer;
    private SlotManager stackFrameMap;

    protected XPathExpressionImpl(Expression exp, Executable exec) {
        this.expression = exp;
        this.executable = exec;
        this.config = exec.getConfiguration();
    }

    protected void setStackFrameMap(SlotManager map) {
        this.stackFrameMap = map;
    }

    public SlotManager getStackFrameMap() {
        return this.stackFrameMap;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public Object evaluate(Object node, QName qName) throws XPathExpressionException {
        Item contextItem;
        if (node instanceof ZeroOrOne) {
            node = ((ZeroOrOne)node).head();
        }
        if (node instanceof TreeInfo) {
            node = ((TreeInfo)node).getRootNode();
        }
        if (node instanceof NodeInfo) {
            if (!((NodeInfo)node).getConfiguration().isCompatible(this.config)) {
                throw new XPathExpressionException("Supplied node must be built using the same or a compatible Configuration");
            }
            if (node instanceof TreeInfo && ((TreeInfo)node).isTyped() && !this.executable.isSchemaAware()) {
                throw new XPathExpressionException("The expression was compiled to handled untyped data, but the input is typed");
            }
            contextItem = (NodeInfo)node;
        } else if (node instanceof Item) {
            contextItem = (Item)node;
        } else {
            Sequence val;
            JPConverter converter = JPConverter.allocate(node.getClass(), null, this.config);
            try {
                val = converter.convert(node, new EarlyEvaluationContext(this.config));
            } catch (XPathException e) {
                throw new XPathExpressionException("Failure converting a node of class " + node.getClass().getName() + ": " + e.getMessage());
            }
            if (val instanceof NodeInfo) {
                if (!((NodeInfo)val).getConfiguration().isCompatible(this.config)) {
                    throw new XPathExpressionException("Supplied node must be built using the same or a compatible Configuration");
                }
                if (((NodeInfo)val).getTreeInfo().isTyped() && !this.executable.isSchemaAware()) {
                    throw new XPathExpressionException("The expression was compiled to handled untyped data, but the input is typed");
                }
                contextItem = (NodeInfo)val;
            } else {
                throw new XPathExpressionException("Cannot locate an object model implementation for nodes of class " + node.getClass().getName());
            }
        }
        XPathContextMajor context = new XPathContextMajor(contextItem, this.executable);
        context.openStackFrame(this.stackFrameMap);
        try {
            SequenceIterator iter;
            if (qName.equals(XPathConstants.BOOLEAN)) {
                return this.expression.effectiveBooleanValue(context);
            }
            if (qName.equals(XPathConstants.STRING)) {
                iter = this.expression.iterate(context);
                Item first = iter.next();
                if (first == null) {
                    return "";
                }
                return first.getStringValue();
            }
            if (qName.equals(XPathConstants.NUMBER)) {
                Item first;
                if (this.atomizer == null) {
                    this.atomizer = Atomizer.makeAtomizer(this.expression, null);
                }
                if ((first = (iter = this.atomizer.iterate(context)).next()) == null) {
                    return Double.NaN;
                }
                if (first instanceof NumericValue) {
                    return ((NumericValue)first).getDoubleValue();
                }
                DoubleValue v = Number_1.convert((AtomicValue)first, this.getConfiguration());
                return v.getDoubleValue();
            }
            if (qName.equals(XPathConstants.NODE)) {
                iter = this.expression.iterate(context);
                Item first = iter.next();
                if (first instanceof VirtualNode) {
                    return ((VirtualNode)first).getRealNode();
                }
                if (first == null || first instanceof NodeInfo) {
                    return first;
                }
                throw new XPathExpressionException("Expression result is not a node");
            }
            if (qName.equals(XPathConstants.NODESET)) {
                context.openStackFrame(this.stackFrameMap);
                iter = this.expression.iterate(context);
                GroundedValue extent = iter.materialize();
                PJConverter converter = PJConverter.allocateNodeListCreator(this.config, node);
                return converter.convert(extent, Object.class, context);
            }
            throw new IllegalArgumentException("qName: Unknown type for expected result");
        } catch (XPathException e) {
            throw new XPathExpressionException(e);
        }
    }

    @Override
    public String evaluate(Object node) throws XPathExpressionException {
        return (String)this.evaluate(node, XPathConstants.STRING);
    }

    @Override
    public Object evaluate(InputSource inputSource, QName qName) throws XPathExpressionException {
        if (qName == null) {
            throw new NullPointerException("qName");
        }
        try {
            NodeInfo doc = null;
            if (inputSource != null) {
                doc = this.config.buildDocumentTree(new SAXSource(inputSource)).getRootNode();
            }
            return this.evaluate(doc, qName);
        } catch (XPathException e) {
            throw new XPathExpressionException(e);
        }
    }

    @Override
    public String evaluate(InputSource inputSource) throws XPathExpressionException {
        if (inputSource == null) {
            throw new NullPointerException("inputSource");
        }
        try {
            NodeInfo doc = this.config.buildDocumentTree(new SAXSource(inputSource)).getRootNode();
            return (String)this.evaluate(doc, XPathConstants.STRING);
        } catch (XPathException e) {
            throw new XPathExpressionException(e);
        }
    }

    public Expression getInternalExpression() {
        return this.expression;
    }
}

