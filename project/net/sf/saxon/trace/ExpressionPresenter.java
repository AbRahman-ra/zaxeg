/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trace;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.CheckSumFilter;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.value.Whitespace;

public class ExpressionPresenter {
    private Configuration config;
    private Receiver receiver;
    private ComplexContentOutputter cco;
    private int depth = 0;
    private boolean inStartTag = false;
    private String nextRole = null;
    private Stack<Expression> expressionStack = new Stack();
    private Stack<String> nameStack = new Stack();
    private NamespaceMap namespaceMap = NamespaceMap.emptyMap();
    private String defaultNamespace;
    private Options options = new ExportOptions();
    private boolean relocatable = false;

    public ExpressionPresenter() {
    }

    public ExpressionPresenter(Configuration config) {
        this(config, config.getLogger());
    }

    public ExpressionPresenter(Configuration config, StreamResult out) {
        this(config, out, false);
    }

    public ExpressionPresenter(Configuration config, StreamResult out, boolean checksum) {
        this.init(config, out, checksum);
    }

    public void init(Configuration config, StreamResult out, boolean checksum) {
        SerializationProperties props = ExpressionPresenter.makeDefaultProperties(config);
        if (config.getXMLVersion() == 11) {
            if ("JS".equals(((ExportOptions)this.getOptions()).target)) {
                config.getLogger().warning("For target=JS, the SEF file will use XML 1.0, which disallows control characters");
            } else {
                props.setProperty("version", "1.1");
            }
        }
        try {
            this.receiver = config.getSerializerFactory().getReceiver(out, props);
            this.receiver = new NamespaceReducer(this.receiver);
            if (checksum) {
                this.receiver = new CheckSumFilter(this.receiver);
            }
            this.cco = new ComplexContentOutputter(this.receiver);
        } catch (XPathException err) {
            err.printStackTrace();
            throw new InternalError(err.getMessage());
        }
        this.config = config;
        try {
            this.cco.open();
            this.cco.startDocument(0);
        } catch (XPathException err) {
            err.printStackTrace();
            throw new InternalError(err.getMessage());
        }
    }

    public void init(Configuration config, Receiver out, boolean checksum) {
        this.receiver = out;
        this.receiver = new NamespaceReducer(this.receiver);
        if (checksum) {
            this.receiver = new CheckSumFilter(this.receiver);
        }
        this.cco = new ComplexContentOutputter(this.receiver);
        this.config = config;
        try {
            this.cco.open();
            this.cco.startDocument(0);
        } catch (XPathException err) {
            err.printStackTrace();
            throw new InternalError(err.getMessage());
        }
    }

    public ExpressionPresenter(Configuration config, Logger out) {
        this(config, out.asStreamResult());
    }

    public ExpressionPresenter(Configuration config, Receiver receiver) {
        this.config = config;
        this.receiver = receiver;
        this.cco = new ComplexContentOutputter(receiver);
        try {
            this.cco.open();
            this.cco.startDocument(0);
        } catch (XPathException err) {
            err.printStackTrace();
            throw new InternalError(err.getMessage());
        }
    }

    public void setDefaultNamespace(String namespace) {
        this.defaultNamespace = namespace;
        this.namespaceMap = this.namespaceMap.put("", namespace);
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public Options getOptions() {
        return this.options;
    }

    public boolean isRelocatable() {
        return this.relocatable;
    }

    public void setRelocatable(boolean relocatable) {
        this.relocatable = relocatable;
    }

    public static Receiver defaultDestination(Configuration config, Logger out) throws XPathException {
        SerializationProperties props = ExpressionPresenter.makeDefaultProperties(config);
        return config.getSerializerFactory().getReceiver(out.asStreamResult(), props);
    }

    public static SerializationProperties makeDefaultProperties(Configuration config) {
        SerializationProperties props = new SerializationProperties();
        props.setProperty("method", "xml");
        props.setProperty("indent", "yes");
        if (config.isLicensedFeature(8)) {
            props.setProperty("{http://saxon.sf.net/}indent-spaces", "1");
            props.setProperty("{http://saxon.sf.net/}line-length", "4096");
        }
        props.setProperty("omit-xml-declaration", "no");
        props.setProperty("encoding", "utf-8");
        props.setProperty("version", "1.0");
        props.setProperty("{http://saxon.sf.net/}single-quotes", "yes");
        return props;
    }

    public int startElement(String name, Expression expr) {
        String mod;
        Expression parent = this.expressionStack.isEmpty() ? null : this.expressionStack.peek();
        this.expressionStack.push(expr);
        this.nameStack.push("*" + name);
        int n = this._startElement(name);
        if ((parent == null || expr.getRetainedStaticContext() != parent.getRetainedStaticContext()) && expr.getRetainedStaticContext() != null) {
            this.emitRetainedStaticContext(expr.getRetainedStaticContext(), parent == null ? null : parent.getRetainedStaticContext());
        }
        if (!((mod = expr.getLocation().getSystemId()) == null || parent == null || parent.getLocation().getSystemId() != null && parent.getLocation().getSystemId().equals(mod))) {
            this.emitAttribute("module", this.truncatedModuleName(mod));
        }
        int lineNr = expr.getLocation().getLineNumber();
        if (parent == null || parent.getLocation().getLineNumber() != lineNr && lineNr != -1) {
            this.emitAttribute("line", lineNr + "");
        }
        return n;
    }

    private String truncatedModuleName(String module) {
        if (!this.relocatable) {
            return module;
        }
        String[] parts = module.split("/");
        for (int p = parts.length - 1; p >= 0; --p) {
            if (parts[p].isEmpty()) continue;
            return parts[p];
        }
        return module;
    }

    public void emitRetainedStaticContext(RetainedStaticContext sc, RetainedStaticContext parentSC) {
        try {
            if (!(((ExportOptions)this.options).suppressStaticContext || this.relocatable || sc.getStaticBaseUri() == null || parentSC != null && sc.getStaticBaseUri().equals(parentSC.getStaticBaseUri()))) {
                this.emitAttribute("baseUri", sc.getStaticBaseUriString());
            }
            if (!(sc.getDefaultCollationName().equals("http://www.w3.org/2005/xpath-functions/collation/codepoint") || parentSC != null && sc.getDefaultCollationName().equals(parentSC.getDefaultCollationName()))) {
                this.emitAttribute("defaultCollation", sc.getDefaultCollationName());
            }
            if (!(sc.getDefaultElementNamespace().isEmpty() || parentSC != null && sc.getDefaultElementNamespace().equals(parentSC.getDefaultElementNamespace()))) {
                this.emitAttribute("defaultElementNS", sc.getDefaultElementNamespace());
            }
            if (!"http://www.w3.org/2005/xpath-functions".equals(sc.getDefaultFunctionNamespace())) {
                this.emitAttribute("defaultFunctionNS", sc.getDefaultFunctionNamespace());
            }
            if (!(((ExportOptions)this.options).suppressStaticContext || parentSC != null && sc.declaresSameNamespaces(parentSC))) {
                FastStringBuffer fsb = new FastStringBuffer(256);
                Iterator<String> iter = sc.iteratePrefixes();
                while (iter.hasNext()) {
                    String p = iter.next();
                    String uri = sc.getURIForPrefix(p, true);
                    fsb.append(p);
                    fsb.append("=");
                    if (Whitespace.containsWhitespace(uri)) {
                        throw new XPathException("Cannot export a stylesheet if namespaces contain whitespace: '" + uri + "'");
                    }
                    if (uri.equals(NamespaceConstant.getUriForConventionalPrefix(p))) {
                        uri = "~";
                    }
                    fsb.append(uri);
                    fsb.append(" ");
                }
                this.emitAttribute("ns", Whitespace.trim(fsb));
            }
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
    }

    public int startElement(String name) {
        this.nameStack.push(name);
        return this._startElement(name);
    }

    private int _startElement(String name) {
        try {
            if (this.inStartTag) {
                this.cco.startContent();
                this.inStartTag = false;
            }
            NodeName nodeName = this.defaultNamespace == null ? new NoNamespaceName(name) : new FingerprintedQName("", this.defaultNamespace, name);
            this.cco.startElement(nodeName, Untyped.getInstance(), Loc.NONE, 0);
            if (this.nextRole != null) {
                this.emitAttribute("role", this.nextRole);
                this.nextRole = null;
            }
        } catch (XPathException err) {
            err.printStackTrace();
            throw new InternalError(err.getMessage());
        }
        this.inStartTag = true;
        return this.depth++;
    }

    public void setChildRole(String role) {
        this.nextRole = role;
    }

    public void emitAttribute(String name, String value) {
        if (value != null) {
            if (name.equals("module")) {
                value = this.truncatedModuleName(value);
            }
            try {
                this.cco.attribute(new NoNamespaceName(name), BuiltInAtomicType.UNTYPED_ATOMIC, value, Loc.NONE, 0);
            } catch (XPathException err) {
                err.printStackTrace();
                throw new InternalError(err.getMessage());
            }
        }
    }

    public void emitAttribute(String name, StructuredQName value) {
        String attVal = value.getEQName();
        try {
            this.cco.attribute(new NoNamespaceName(name), BuiltInAtomicType.UNTYPED_ATOMIC, attVal, Loc.NONE, 0);
        } catch (XPathException err) {
            err.printStackTrace();
            throw new InternalError(err.getMessage());
        }
    }

    public void namespace(String prefix, String uri) {
        try {
            this.cco.namespace(prefix, uri, 0);
        } catch (XPathException e) {
            e.printStackTrace();
            throw new InternalError(e.getMessage());
        }
    }

    public int endElement() {
        try {
            if (this.inStartTag) {
                this.cco.startContent();
                this.inStartTag = false;
            }
            this.cco.endElement();
        } catch (XPathException err) {
            err.printStackTrace();
            throw new InternalError(err.getMessage());
        }
        String name = this.nameStack.pop();
        if (name.startsWith("*")) {
            this.expressionStack.pop();
        }
        return --this.depth;
    }

    public void startSubsidiaryElement(String name) {
        this.startElement(name);
    }

    public void endSubsidiaryElement() {
        this.endElement();
    }

    public void close() {
        try {
            if (this.receiver instanceof CheckSumFilter) {
                int c = ((CheckSumFilter)this.receiver).getChecksum();
                this.cco.processingInstruction("\u03a3", Integer.toHexString(c), Loc.NONE, 0);
            }
            this.cco.endDocument();
            this.cco.close();
        } catch (XPathException err) {
            err.printStackTrace();
            throw new InternalError(err.getMessage());
        }
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public NamePool getNamePool() {
        return this.config.getNamePool();
    }

    public TypeHierarchy getTypeHierarchy() {
        return this.config.getTypeHierarchy();
    }

    public static String jsEscape(String in) {
        FastStringBuffer out = new FastStringBuffer(in.length());
        block10: for (int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            switch (c) {
                case '\'': {
                    out.append("\\'");
                    continue block10;
                }
                case '\"': {
                    out.append("\\\"");
                    continue block10;
                }
                case '\b': {
                    out.append("\\b");
                    continue block10;
                }
                case '\f': {
                    out.append("\\f");
                    continue block10;
                }
                case '\n': {
                    out.append("\\n");
                    continue block10;
                }
                case '\r': {
                    out.append("\\r");
                    continue block10;
                }
                case '\t': {
                    out.append("\\t");
                    continue block10;
                }
                case '\\': {
                    out.append("\\\\");
                    continue block10;
                }
                default: {
                    if (c < ' ' || c > '\u007f' && c < '\u00a0' || c > '\ud800') {
                        out.append("\\u");
                        StringBuilder hex = new StringBuilder(Integer.toHexString(c).toUpperCase());
                        while (hex.length() < 4) {
                            hex.insert(0, "0");
                        }
                        out.append(hex.toString());
                        continue block10;
                    }
                    out.cat(c);
                }
            }
        }
        return out.toString();
    }

    public static class ExportOptions
    implements Options {
        public String target = "";
        public int targetVersion = 0;
        public StylesheetPackage rootPackage;
        public Map<Component, Integer> componentMap;
        public Map<StylesheetPackage, Integer> packageMap;
        public boolean explaining;
        public boolean suppressStaticContext;
        public boolean addStaticType;
    }

    public static interface Options {
    }
}

