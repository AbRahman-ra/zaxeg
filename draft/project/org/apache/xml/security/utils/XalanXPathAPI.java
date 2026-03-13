/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 * 
 * Could not load the following classes:
 *  org.apache.xml.utils.PrefixResolver
 *  org.apache.xml.utils.PrefixResolverDefault
 *  org.apache.xpath.Expression
 *  org.apache.xpath.XPath
 *  org.apache.xpath.XPathContext
 *  org.apache.xpath.compiler.FunctionTable
 *  org.apache.xpath.objects.XObject
 */
package org.apache.xml.security.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.transforms.implementations.FuncHere;
import org.apache.xml.security.utils.XPathAPI;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xml.utils.PrefixResolverDefault;
import org.apache.xpath.Expression;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathContext;
import org.apache.xpath.compiler.FunctionTable;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XalanXPathAPI
implements XPathAPI {
    private static Log log = LogFactory.getLog(XalanXPathAPI.class);
    private String xpathStr = null;
    private XPath xpath = null;
    private static FunctionTable funcTable = null;
    private static boolean installed;
    private XPathContext context;

    public NodeList selectNodeList(Node contextNode, Node xpathnode, String str, Node namespaceNode) throws TransformerException {
        XObject list = this.eval(contextNode, xpathnode, str, namespaceNode);
        return list.nodelist();
    }

    public boolean evaluate(Node contextNode, Node xpathnode, String str, Node namespaceNode) throws TransformerException {
        XObject object = this.eval(contextNode, xpathnode, str, namespaceNode);
        return object.bool();
    }

    public void clear() {
        this.xpathStr = null;
        this.xpath = null;
        this.context = null;
    }

    public static synchronized boolean isInstalled() {
        return installed;
    }

    private XObject eval(Node contextNode, Node xpathnode, String str, Node namespaceNode) throws TransformerException {
        if (this.context == null) {
            this.context = new XPathContext((Object)xpathnode);
            this.context.setSecureProcessing(true);
        }
        Node resolverNode = namespaceNode.getNodeType() == 9 ? ((Document)namespaceNode).getDocumentElement() : namespaceNode;
        PrefixResolverDefault prefixResolver = new PrefixResolverDefault(resolverNode);
        if (!str.equals(this.xpathStr)) {
            if (str.indexOf("here()") > 0) {
                this.context.reset();
            }
            this.xpath = this.createXPath(str, (PrefixResolver)prefixResolver);
            this.xpathStr = str;
        }
        int ctxtNode = this.context.getDTMHandleFromNode(contextNode);
        return this.xpath.execute(this.context, ctxtNode, (PrefixResolver)prefixResolver);
    }

    private XPath createXPath(String str, PrefixResolver prefixResolver) throws TransformerException {
        XPath xpath;
        block3: {
            xpath = null;
            Class[] classes = new Class[]{String.class, SourceLocator.class, PrefixResolver.class, Integer.TYPE, ErrorListener.class, FunctionTable.class};
            Object[] objects = new Object[]{str, null, prefixResolver, 0, null, funcTable};
            try {
                Constructor constructor = XPath.class.getConstructor(classes);
                xpath = (XPath)constructor.newInstance(objects);
            } catch (Exception ex) {
                if (!log.isDebugEnabled()) break block3;
                log.debug(ex);
            }
        }
        if (xpath == null) {
            xpath = new XPath(str, null, prefixResolver, 0, null);
        }
        return xpath;
    }

    private static synchronized void fixupFunctionTable() {
        Object[] params;
        Method installFunction;
        Class[] args;
        installed = false;
        if (log.isDebugEnabled()) {
            log.debug("Registering Here function");
        }
        try {
            args = new Class[]{String.class, Expression.class};
            installFunction = FunctionTable.class.getMethod("installFunction", args);
            if ((installFunction.getModifiers() & 8) != 0) {
                params = new Object[]{"here", new FuncHere()};
                installFunction.invoke(null, params);
                installed = true;
            }
        } catch (Exception ex) {
            log.debug("Error installing function using the static installFunction method", ex);
        }
        if (!installed) {
            try {
                funcTable = new FunctionTable();
                args = new Class[]{String.class, Class.class};
                installFunction = FunctionTable.class.getMethod("installFunction", args);
                params = new Object[]{"here", FuncHere.class};
                installFunction.invoke(funcTable, params);
                installed = true;
            } catch (Exception ex) {
                log.debug("Error installing function using the static installFunction method", ex);
            }
        }
        if (log.isDebugEnabled()) {
            if (installed) {
                log.debug("Registered class " + FuncHere.class.getName() + " for XPath function 'here()' function in internal table");
            } else {
                log.debug("Unable to register class " + FuncHere.class.getName() + " for XPath function 'here()' function in internal table");
            }
        }
    }

    static {
        XalanXPathAPI.fixupFunctionTable();
    }
}

