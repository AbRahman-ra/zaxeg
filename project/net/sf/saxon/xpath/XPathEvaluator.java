/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.xpath;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.Executable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaException;
import net.sf.saxon.type.Type;
import net.sf.saxon.xpath.JAXPXPathStaticContext;
import net.sf.saxon.xpath.XPathExpressionImpl;
import org.xml.sax.InputSource;

public class XPathEvaluator
implements XPath {
    private Configuration config;
    private JAXPXPathStaticContext staticContext;

    public XPathEvaluator() {
        this(Configuration.newConfiguration());
    }

    public XPathEvaluator(Configuration config) {
        this.config = config;
        this.staticContext = new JAXPXPathStaticContext(config);
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public JAXPXPathStaticContext getStaticContext() {
        return this.staticContext;
    }

    @Override
    public void reset() {
        this.staticContext = new JAXPXPathStaticContext(this.config);
    }

    @Override
    public void setXPathVariableResolver(XPathVariableResolver xPathVariableResolver) {
        this.staticContext.setXPathVariableResolver(xPathVariableResolver);
    }

    @Override
    public XPathVariableResolver getXPathVariableResolver() {
        return this.staticContext.getXPathVariableResolver();
    }

    @Override
    public void setXPathFunctionResolver(XPathFunctionResolver xPathFunctionResolver) {
        this.staticContext.setXPathFunctionResolver(xPathFunctionResolver);
    }

    @Override
    public XPathFunctionResolver getXPathFunctionResolver() {
        return this.staticContext.getXPathFunctionResolver();
    }

    @Override
    public void setNamespaceContext(NamespaceContext namespaceContext) {
        this.staticContext.setNamespaceContext(namespaceContext);
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        return this.staticContext.getNamespaceContext();
    }

    public void importSchema(Source source) throws SchemaException {
        this.staticContext.importSchema(source);
        this.staticContext.setSchemaAware(true);
    }

    @Override
    public XPathExpression compile(String expr) throws XPathExpressionException {
        if (expr == null) {
            throw new NullPointerException("expr");
        }
        try {
            Executable exec = new Executable(this.getConfiguration());
            Expression exp = ExpressionTool.make(expr, this.staticContext, 0, -1, null);
            ExpressionVisitor visitor = ExpressionVisitor.make(this.staticContext);
            ContextItemStaticInfo contextItemType = this.getConfiguration().makeContextItemStaticInfo(Type.ITEM_TYPE, true);
            exp = exp.typeCheck(visitor, contextItemType).optimize(visitor, contextItemType);
            SlotManager map = this.staticContext.getConfiguration().makeSlotManager();
            ExpressionTool.allocateSlots(exp, 0, map);
            XPathExpressionImpl xpe = new XPathExpressionImpl(exp, exec);
            xpe.setStackFrameMap(map);
            return xpe;
        } catch (XPathException e) {
            throw new XPathExpressionException(e);
        }
    }

    @Override
    public Object evaluate(String expr, Object node, QName qName) throws XPathExpressionException {
        XPathExpression exp = this.compile(expr);
        return exp.evaluate(node, qName);
    }

    @Override
    public String evaluate(String expr, Object node) throws XPathExpressionException {
        XPathExpression exp = this.compile(expr);
        return exp.evaluate(node);
    }

    @Override
    public Object evaluate(String expr, InputSource inputSource, QName qName) throws XPathExpressionException {
        if (expr == null) {
            throw new NullPointerException("expr");
        }
        if (inputSource == null) {
            throw new NullPointerException("inputSource");
        }
        if (qName == null) {
            throw new NullPointerException("qName");
        }
        XPathExpression exp = this.compile(expr);
        return exp.evaluate(inputSource, qName);
    }

    @Override
    public String evaluate(String expr, InputSource inputSource) throws XPathExpressionException {
        if (expr == null) {
            throw new NullPointerException("expr");
        }
        if (inputSource == null) {
            throw new NullPointerException("inputSource");
        }
        XPathExpression exp = this.compile(expr);
        return exp.evaluate(inputSource);
    }
}

