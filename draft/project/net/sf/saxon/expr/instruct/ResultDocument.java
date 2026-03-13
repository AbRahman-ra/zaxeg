/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.NamespaceReducer;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.ContextOriginator;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.DocumentInstr;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.InstructionWithComplexContent;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.ValidatingInstruction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.IriToUri;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.ResultDocumentResolver;
import net.sf.saxon.lib.SaxonOutputKeys;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.PrincipalOutputGatekeeper;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLResultDocument;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.type.ErrorType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.Whitespace;

public class ResultDocument
extends Instruction
implements ValidatingInstruction,
InstructionWithComplexContent,
ContextOriginator {
    private Operand hrefOp;
    private Operand formatOp;
    private Operand contentOp;
    private boolean async = false;
    private final Properties globalProperties;
    private final Properties localProperties;
    private ParseOptions validationOptions;
    private final Map<StructuredQName, Operand> serializationAttributes;
    private boolean resolveAgainstStaticBase = false;
    private final CharacterMapIndex characterMapIndex;

    public ResultDocument(Properties globalProperties, Properties localProperties, Expression href, Expression formatExpression, int validationAction, SchemaType schemaType, Map<StructuredQName, Expression> serializationAttributes, CharacterMapIndex characterMapIndex) {
        this.globalProperties = globalProperties;
        this.localProperties = localProperties;
        if (href != null) {
            this.hrefOp = new Operand(this, href, OperandRole.SINGLE_ATOMIC);
        }
        if (formatExpression != null) {
            this.formatOp = new Operand(this, formatExpression, OperandRole.SINGLE_ATOMIC);
        }
        this.setValidationAction(validationAction, schemaType);
        this.serializationAttributes = new HashMap<StructuredQName, Operand>(serializationAttributes.size());
        for (Map.Entry<StructuredQName, Expression> entry : serializationAttributes.entrySet()) {
            this.serializationAttributes.put(entry.getKey(), new Operand(this, entry.getValue(), OperandRole.SINGLE_ATOMIC));
        }
        this.characterMapIndex = characterMapIndex;
        for (Expression e : serializationAttributes.values()) {
            this.adoptChildExpression(e);
        }
    }

    public void setContentExpression(Expression content) {
        this.contentOp = new Operand(this, content, OperandRole.SINGLE_ATOMIC);
    }

    public void setSchemaType(SchemaType type) {
        if (this.validationOptions == null) {
            this.validationOptions = new ParseOptions();
        }
        this.validationOptions.setSchemaValidationMode(8);
        this.validationOptions.setTopLevelType(type);
    }

    @Override
    public SchemaType getSchemaType() {
        return this.validationOptions == null ? null : this.validationOptions.getTopLevelType();
    }

    public boolean isResolveAgainstStaticBase() {
        return this.resolveAgainstStaticBase;
    }

    public ParseOptions getValidationOptions() {
        return this.validationOptions;
    }

    public void setValidationAction(int mode, SchemaType schemaType) {
        boolean preservingTypes;
        boolean bl = preservingTypes = mode == 3 && schemaType == null;
        if (!preservingTypes && this.validationOptions == null) {
            this.validationOptions = new ParseOptions();
            this.validationOptions.setSchemaValidationMode(mode);
            this.validationOptions.setTopLevelType(schemaType);
        }
    }

    @Override
    public int getValidationAction() {
        return this.validationOptions == null ? 3 : this.validationOptions.getSchemaValidationMode();
    }

    public Expression getFormatExpression() {
        return this.formatOp == null ? null : this.formatOp.getChildExpression();
    }

    public void setUseStaticBaseUri(boolean staticBase) {
        this.resolveAgainstStaticBase = staticBase;
    }

    public void setAsynchronous(boolean async) {
        this.async = async;
    }

    public boolean isAsynchronous() {
        return this.async;
    }

    @Override
    public boolean isMultiThreaded(Configuration config) {
        return this.isAsynchronous() && config.isLicensedFeature(1) && config.getBooleanProperty(Feature.ALLOW_MULTITHREADING);
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        String method = this.getStaticSerializationProperty(XSLResultDocument.METHOD);
        boolean contentDependentMethod = method == null && this.formatOp == null && !this.serializationAttributes.containsKey(XSLResultDocument.METHOD);
        boolean buildTree = "yes".equals(this.getStaticSerializationProperty(XSLResultDocument.BUILD_TREE));
        if (buildTree || contentDependentMethod || "xml".equals(method) || "html".equals(method) || "xhtml".equals(method) || "text".equals(method)) {
            try {
                DocumentInstr.checkContentSequence(visitor.getStaticContext(), this.contentOp, this.validationOptions);
            } catch (XPathException err) {
                err.maybeSetLocation(this.getLocation());
                throw err;
            }
        }
        return this;
    }

    @Override
    public int getIntrinsicDependencies() {
        return 0x2000000;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.optimizeChildren(visitor, contextInfo);
        if (this.isAsynchronous()) {
            for (Expression e = this.getParentExpression(); e != null; e = e.getParentExpression()) {
                if (!(e instanceof LetExpression) || !ExpressionTool.dependsOnVariable(this.getContentExpression(), new Binding[]{(LetExpression)e})) continue;
                ((LetExpression)e).setNeedsEagerEvaluation(true);
            }
        }
        return this;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        HashMap<StructuredQName, Expression> map = new HashMap<StructuredQName, Expression>();
        for (Map.Entry<StructuredQName, Operand> entry : this.serializationAttributes.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getChildExpression().copy(rebindings));
        }
        ResultDocument r = new ResultDocument(this.globalProperties, this.localProperties, this.getHref() == null ? null : this.getHref().copy(rebindings), this.getFormatExpression() == null ? null : this.getFormatExpression().copy(rebindings), this.getValidationAction(), this.getSchemaType(), map, this.characterMapIndex);
        ExpressionTool.copyLocationInfo(this, r);
        r.setContentExpression(this.getContentExpression().copy(rebindings));
        r.resolveAgainstStaticBase = this.resolveAgainstStaticBase;
        r.async = this.async;
        return r;
    }

    @Override
    public int getInstructionNameCode() {
        return 193;
    }

    @Override
    public ItemType getItemType() {
        return ErrorType.getInstance();
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> list = new ArrayList<Operand>(6);
        list.add(this.contentOp);
        if (this.hrefOp != null) {
            list.add(this.hrefOp);
        }
        if (this.formatOp != null) {
            list.add(this.formatOp);
        }
        list.addAll(this.serializationAttributes.values());
        return list;
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet result = super.addToPathMap(pathMap, pathMapNodeSet);
        result.setReturnable(false);
        return new PathMap.PathMapNodeSet(pathMap.makeNewRoot(this));
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        this.process(this.getContentExpression(), context);
        return null;
    }

    public void process(Expression content, XPathContext context) throws XPathException {
        this.checkNotTemporaryOutputState(context);
        context.getConfiguration().processResultDocument(this, content, context);
    }

    public void processInstruction(Expression content, XPathContext context) throws XPathException {
        XsltController controller = (XsltController)context.getController();
        assert (controller != null);
        String savedOutputUri = context.getCurrentOutputUri();
        ComplexContentOutputter out = this.processLeft(context);
        boolean failed = false;
        try {
            content.process(out, context);
        } catch (XPathException err) {
            failed = true;
            err.maybeSetContext(context);
            err.maybeSetLocation(this.getLocation());
            throw err;
        } finally {
            block11: {
                try {
                    out.close();
                } catch (XPathException e) {
                    if (failed) break block11;
                    throw e;
                }
            }
        }
        context.setCurrentOutputUri(savedOutputUri);
    }

    public ComplexContentOutputter processLeft(XPathContext context) throws XPathException {
        PrincipalOutputGatekeeper gateKeeper;
        XsltController controller = (XsltController)context.getController();
        Configuration config = controller.getConfiguration();
        this.checkNotTemporaryOutputState(context);
        Properties computedLocalProps = this.gatherOutputProperties(context);
        if (computedLocalProps.getProperty("parameter-document") != null && this.getStaticBaseURIString() != null) {
            try {
                String abs = ResolveURI.makeAbsolute(computedLocalProps.getProperty("parameter-document"), this.getStaticBaseURIString()).toASCIIString();
                computedLocalProps.setProperty("parameter-document", abs);
            } catch (URISyntaxException e) {
                throw XPathException.makeXPathException(e);
            }
        }
        SerializationProperties serParams = new SerializationProperties(computedLocalProps, this.characterMapIndex);
        if (this.validationOptions != null && this.validationOptions.getSchemaValidationMode() != 3) {
            serParams.setValidationFactory(output -> {
                NamespaceReducer nr = new NamespaceReducer(output);
                return config.getDocumentValidator(nr, output.getSystemId(), this.validationOptions, this.getLocation());
            });
        }
        Receiver out = null;
        String hrefValue = "";
        if (this.getHref() != null) {
            hrefValue = IriToUri.iriToUri(this.getHref().evaluateAsString(context)).toString();
        }
        if ((hrefValue.isEmpty() || hrefValue.equals(controller.getBaseOutputURI())) && (gateKeeper = controller.getGatekeeper()) != null) {
            gateKeeper.useAsSecondary();
            out = gateKeeper.makeReceiver(serParams);
        }
        if (out == null) {
            try {
                ResultDocumentResolver resolver = controller.getResultDocumentResolver();
                out = ResultDocument.makeReceiver(hrefValue, this.getStaticBaseURIString(), context, resolver, serParams, this.resolveAgainstStaticBase);
                ResultDocument.traceDestination(context, out);
            } catch (XPathException e) {
                e.maybeSetLocation(this.getLocation());
                e.maybeSetContext(context);
                throw e;
            }
        }
        out.getPipelineConfiguration().setController(controller);
        String systemId = out.getSystemId();
        NamespaceReducer nr = new NamespaceReducer(out);
        ComplexContentOutputter cco = new ComplexContentOutputter(nr);
        cco.setSystemId(systemId);
        context.setCurrentOutputUri(systemId);
        cco.open();
        return cco;
    }

    public CharacterMapIndex getCharacterMapIndex() {
        return this.characterMapIndex;
    }

    private void checkNotTemporaryOutputState(XPathContext context) throws XPathException {
        if (context.getTemporaryOutputState() != 0) {
            XPathException err = new XPathException("Cannot execute xsl:result-document while evaluating xsl:" + context.getNamePool().getLocalName(context.getTemporaryOutputState()));
            err.setErrorCode("XTDE1480");
            err.setLocation(this.getLocation());
            throw err;
        }
    }

    public static Receiver makeReceiver(String hrefValue, String baseURI, XPathContext context, ResultDocumentResolver resolver, SerializationProperties params, boolean resolveAgainstStaticBase) throws XPathException {
        Object resultURI = null;
        Controller controller = context.getController();
        try {
            String base = resolveAgainstStaticBase ? baseURI : controller.getBaseOutputURI();
            try {
                Receiver out = resolver.resolve(context, hrefValue, base, params);
                String systemId = out.getSystemId();
                if (systemId == null) {
                    systemId = ResolveURI.makeAbsolute(hrefValue, base).toASCIIString();
                    out.setSystemId(systemId);
                }
                ResultDocument.checkAcceptableUri(context, systemId);
                return out;
            } catch (XPathException e) {
                throw e;
            } catch (Exception err) {
                err.printStackTrace();
                throw new XPathException("Exception thrown by output resolver", err);
            }
        } catch (TransformerException e) {
            throw XPathException.makeXPathException(e);
        }
    }

    public static void traceDestination(XPathContext context, Result result) {
        Configuration config = context.getConfiguration();
        boolean timing = config.isTiming();
        if (timing) {
            String dest = result.getSystemId();
            if (dest == null) {
                dest = result instanceof StreamResult ? "anonymous output stream" : (result instanceof SAXResult ? "SAX2 ContentHandler" : (result instanceof DOMResult ? "DOM tree" : result.getClass().getName()));
            }
            config.getLogger().info("Writing to " + dest);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void checkAcceptableUri(XPathContext context, String uri) throws XPathException {
        XsltController controller = (XsltController)context.getController();
        assert (controller != null);
        if (uri != null) {
            if (controller.getDocumentPool().find(uri) != null) {
                XPathException err = new XPathException("Cannot write to a URI that has already been read: " + (uri.equals("dummy:/anonymous/principal/result") ? "(implicit output URI)" : uri));
                err.setXPathContext(context);
                err.setErrorCode("XTDE1500");
                throw err;
            }
            DocumentKey documentKey = new DocumentKey(uri);
            XsltController xsltController = controller;
            synchronized (xsltController) {
                if (!controller.checkUniqueOutputDestination(documentKey)) {
                    XPathException err = new XPathException("Cannot write more than one result document to the same URI: " + (uri.equals("dummy:/anonymous/principal/result") ? "(implicit output URI)" : uri));
                    err.setXPathContext(context);
                    err.setErrorCode("XTDE1490");
                    throw err;
                }
                controller.addUnavailableOutputDestination(documentKey);
            }
        }
    }

    public Properties gatherOutputProperties(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        Configuration config = context.getConfiguration();
        Properties computedGlobalProps = this.globalProperties;
        RetainedStaticContext nsResolver = this.getRetainedStaticContext();
        assert (nsResolver != null);
        if (this.getFormatExpression() != null) {
            StructuredQName qName;
            String format = this.getFormatExpression().evaluateAsString(context).toString();
            if (format.startsWith("Q{")) {
                qName = StructuredQName.fromEQName(format);
            } else {
                String[] stringArray;
                try {
                    stringArray = NameChecker.getQNameParts(format);
                } catch (QNameException e) {
                    XPathException err = new XPathException("The requested output format " + Err.wrap(format) + " is not a valid QName");
                    err.maybeSetLocation(this.getFormatExpression().getLocation());
                    err.setErrorCode("XTDE1460");
                    err.setXPathContext(context);
                    throw err;
                }
                String uri = nsResolver.getURIForPrefix(stringArray[0], false);
                if (uri == null) {
                    XPathException err = new XPathException("The namespace prefix in the format name " + format + " is undeclared");
                    err.maybeSetLocation(this.getFormatExpression().getLocation());
                    err.setErrorCode("XTDE1460");
                    err.setXPathContext(context);
                    throw err;
                }
                qName = new StructuredQName(stringArray[0], uri, stringArray[1]);
            }
            computedGlobalProps = ((StylesheetPackage)this.getRetainedStaticContext().getPackageData()).getNamedOutputProperties(qName);
            if (computedGlobalProps == null) {
                XPathException xPathException = new XPathException("There is no xsl:output format named " + format);
                xPathException.setErrorCode("XTDE1460");
                xPathException.setXPathContext(context);
                throw xPathException;
            }
        }
        Properties computedLocalProps = new Properties(computedGlobalProps);
        for (Object k : this.localProperties.keySet()) {
            String key = (String)k;
            StructuredQName qName = StructuredQName.fromClarkName(key);
            try {
                ResultDocument.setSerializationProperty(computedLocalProps, qName.getURI(), qName.getLocalPart(), this.localProperties.getProperty(key), nsResolver, true, config);
            } catch (XPathException e) {
                e.setErrorCode("XTDE0030");
                e.maybeSetLocation(this.getLocation());
                throw e;
            }
        }
        if (!this.serializationAttributes.isEmpty()) {
            for (Map.Entry entry : this.serializationAttributes.entrySet()) {
                String value = ((Operand)entry.getValue()).getChildExpression().evaluateAsString(context).toString();
                String lname = ((StructuredQName)entry.getKey()).getLocalPart();
                String uri = ((StructuredQName)entry.getKey()).getURI();
                try {
                    ResultDocument.setSerializationProperty(computedLocalProps, uri, lname, value, nsResolver, false, config);
                } catch (XPathException e) {
                    e.setErrorCode("XTDE0030");
                    e.maybeSetLocation(this.getLocation());
                    e.maybeSetContext(context);
                    if ("http://saxon.sf.net/".equals(e.getErrorCodeNamespace()) && "SXWN".equals(e.getErrorCodeLocalPart().substring(0, 4))) {
                        XmlProcessingException ee = new XmlProcessingException(e);
                        ee.setWarning(true);
                        controller.getErrorReporter().report(ee);
                        continue;
                    }
                    throw e;
                }
            }
        }
        return computedLocalProps;
    }

    public String getStaticSerializationProperty(StructuredQName name) {
        String clarkName = name.getClarkName();
        String local = this.localProperties.getProperty(clarkName);
        if (local != null) {
            return local;
        }
        if (this.serializationAttributes.containsKey(name)) {
            return null;
        }
        return this.globalProperties.getProperty(clarkName);
    }

    public static void setSerializationProperty(Properties details, String uri, String lname, String value, NamespaceResolver nsResolver, boolean prevalidated, Configuration config) throws XPathException {
        SerializerFactory sf = config.getSerializerFactory();
        String clarkName = lname;
        if (!uri.isEmpty()) {
            clarkName = "{" + uri + "}" + lname;
        }
        if (uri.isEmpty() || "http://saxon.sf.net/".equals(uri)) {
            switch (clarkName) {
                case "method": {
                    value = Whitespace.trim(value);
                    if (value.startsWith("Q{}") && value.length() > 3) {
                        value = value.substring(3);
                    }
                    if (value.equals("xml") || value.equals("html") || value.equals("text") || value.equals("xhtml") || value.equals("json") || value.equals("adaptive") || prevalidated || value.startsWith("{")) {
                        details.setProperty("method", value);
                        break;
                    }
                    if (value.startsWith("Q{")) {
                        details.setProperty("method", value.substring(1));
                        break;
                    }
                    try {
                        String[] parts = NameChecker.getQNameParts(value);
                        String prefix = parts[0];
                        if (prefix.isEmpty()) {
                            XPathException err = new XPathException("method must be xml, html, xhtml, text, json, adaptive, or a prefixed name");
                            err.setErrorCode("SEPM0016");
                            err.setIsStaticError(true);
                            throw err;
                        }
                        if (nsResolver != null) {
                            String muri = nsResolver.getURIForPrefix(prefix, false);
                            if (muri == null) {
                                XPathException err = new XPathException("Namespace prefix '" + prefix + "' has not been declared");
                                err.setErrorCode("SEPM0016");
                                err.setIsStaticError(true);
                                throw err;
                            }
                            details.setProperty("method", '{' + muri + '}' + parts[1]);
                            break;
                        }
                        details.setProperty("method", value);
                        break;
                    } catch (QNameException e) {
                        XPathException err = new XPathException("Invalid method name. " + e.getMessage());
                        err.setErrorCode("SEPM0016");
                        err.setIsStaticError(true);
                        throw err;
                    }
                }
                case "use-character-maps": {
                    String existing = details.getProperty("use-character-maps");
                    if (existing == null) {
                        existing = "";
                    }
                    details.setProperty("use-character-maps", existing + value);
                    break;
                }
                case "cdata-section-elements": {
                    ResultDocument.processListOfNodeNames(details, clarkName, value, nsResolver, true, prevalidated, false);
                    break;
                }
                case "suppress-indentation": {
                    ResultDocument.processListOfNodeNames(details, clarkName, value, nsResolver, true, prevalidated, false);
                    break;
                }
                case "{http://saxon.sf.net/}double-space": {
                    ResultDocument.processListOfNodeNames(details, clarkName, value, nsResolver, true, prevalidated, false);
                    break;
                }
                case "{http://saxon.sf.net/}attribute-order": {
                    ResultDocument.processListOfNodeNames(details, clarkName, value, nsResolver, false, prevalidated, true);
                    break;
                }
                case "{http://saxon.sf.net/}next-in-chain": {
                    break;
                }
                default: {
                    if (clarkName.equals("output-version")) {
                        clarkName = "version";
                    }
                    if (!prevalidated) {
                        try {
                            if (!SaxonOutputKeys.isUnstrippedProperty(clarkName)) {
                                value = Whitespace.trim(value);
                            }
                            value = sf.checkOutputProperty(clarkName, value);
                        } catch (XPathException err) {
                            err.maybeSetErrorCode("SEPM0016");
                            throw err;
                        }
                    }
                    details.setProperty(clarkName, value);
                }
            }
        } else {
            details.setProperty('{' + uri + '}' + lname, value);
        }
    }

    private static void processListOfNodeNames(Properties details, String key, String value, NamespaceResolver nsResolver, boolean useDefaultNS, boolean prevalidated, boolean allowStar) throws XPathException {
        String existing = details.getProperty(key);
        if (existing == null) {
            existing = "";
        }
        String s = SaxonOutputKeys.parseListOfNodeNames(value, nsResolver, useDefaultNS, prevalidated, allowStar, "SEPM0016");
        details.setProperty(key, existing + s);
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        SchemaType schemaType;
        out.startElement("resultDoc", this);
        out.emitAttribute("global", this.exportProperties(this.globalProperties));
        out.emitAttribute("local", this.exportProperties(this.localProperties));
        if (this.getValidationAction() != 4 && this.getValidationAction() != 8) {
            out.emitAttribute("validation", Validation.toString(this.getValidationAction()));
        }
        if (this.async) {
            out.emitAttribute("flags", "a");
        }
        if ((schemaType = this.getSchemaType()) != null) {
            out.emitAttribute("type", schemaType.getStructuredQName());
        }
        if (this.getHref() != null) {
            out.setChildRole("href");
            this.getHref().export(out);
        }
        if (this.getFormatExpression() != null) {
            out.setChildRole("format");
            this.getFormatExpression().export(out);
        }
        for (Map.Entry<StructuredQName, Operand> p : this.serializationAttributes.entrySet()) {
            StructuredQName name = p.getKey();
            Expression value = p.getValue().getChildExpression();
            out.setChildRole(name.getEQName());
            value.export(out);
        }
        out.setChildRole("content");
        this.getContentExpression().export(out);
        out.endElement();
    }

    private String exportProperties(Properties props) {
        StringBuilder writer = new StringBuilder();
        for (String key : props.stringPropertyNames()) {
            String val = props.getProperty(key);
            if (key.equals("item-separator") || key.equals("{http://saxon.sf.net/}newline")) {
                val = ExpressionPresenter.jsEscape(val);
            }
            if (key.equals("use-character-maps") || key.equals("method")) {
                val = val.replace("{", "Q{");
            }
            if (key.startsWith("{")) {
                key = "Q" + key;
            }
            writer.append(key).append("=").append(val).append("\n");
        }
        return writer.toString();
    }

    public static void processXslOutputElement(NodeInfo element, Properties props, XPathContext c) throws XPathException {
        NamespaceMap resolver = element.getAllNamespaces();
        for (AttributeInfo att : element.attributes()) {
            String uri = att.getNodeName().getURI();
            String local = att.getNodeName().getLocalPart();
            String val = Whitespace.trim(att.getValue());
            ResultDocument.setSerializationProperty(props, uri, local, val, resolver, false, c.getConfiguration());
        }
    }

    @Override
    public String getStreamerName() {
        return "ResultDocument";
    }

    public Expression getHref() {
        return this.hrefOp == null ? null : this.hrefOp.getChildExpression();
    }

    public void setHref(Expression href) {
        this.hrefOp.setChildExpression(href);
    }

    public void setFormatExpression(Expression formatExpression) {
        this.formatOp.setChildExpression(formatExpression);
    }

    @Override
    public Expression getContentExpression() {
        return this.contentOp.getChildExpression();
    }
}

