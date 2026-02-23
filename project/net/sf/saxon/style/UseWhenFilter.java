/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.XPathContextMajor;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.ElementAvailable;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.lib.EmptySource;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.DocumentKey;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.style.AttributeValueTemplate;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetModule;
import net.sf.saxon.style.UseWhenStaticContext;
import net.sf.saxon.style.XSLGeneralIncorporate;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.trans.packages.UsePack;
import net.sf.saxon.tree.AttributeLocation;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Type;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.NestedIntegerValue;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class UseWhenFilter
extends ProxyReceiver {
    private int depthOfHole = 0;
    private boolean emptyStylesheetElement = false;
    private Stack<String> defaultNamespaceStack = new Stack();
    private Stack<Integer> versionStack = new Stack();
    private DateTimeValue currentDateTime = DateTimeValue.getCurrentDateTime(null);
    private Compilation compilation;
    private Stack<String> systemIdStack = new Stack();
    private Stack<URI> baseUriStack = new Stack();
    private NestedIntegerValue precedence;
    private int importCount = 0;
    private boolean dropUnderscoredAttributes;
    private LinkedTreeBuilder treeBuilder;

    public UseWhenFilter(Compilation compilation, Receiver next, NestedIntegerValue precedence) {
        super(next);
        this.compilation = compilation;
        this.precedence = precedence;
        assert (next instanceof LinkedTreeBuilder);
        this.treeBuilder = (LinkedTreeBuilder)next;
    }

    @Override
    public void open() throws XPathException {
        this.nextReceiver.open();
        String sysId = this.getSystemId();
        if (sysId == null) {
            sysId = "";
        }
        this.systemIdStack.push(sysId);
        try {
            this.baseUriStack.push(new URI(sysId));
        } catch (URISyntaxException e) {
            try {
                this.baseUriStack.push(new File(sysId).toURI());
            } catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        int fp = elemName.obtainFingerprint(this.getNamePool());
        boolean inXsltNamespace = elemName.hasURI("http://www.w3.org/1999/XSL/Transform");
        boolean inSaxonNamespace = elemName.hasURI("http://saxon.sf.net/");
        String stdAttUri = inXsltNamespace ? "" : "http://www.w3.org/1999/XSL/Transform";
        String xpathDefaultNamespaceAtt = null;
        String versionAtt = null;
        String xmlBaseAtt = null;
        String useWhenAtt = null;
        String staticAtt = null;
        boolean hasShadowAttributes = false;
        DocumentImpl includedDoc = null;
        for (AttributeInfo att : attributes) {
            NodeName attName = att.getNodeName();
            attName.obtainFingerprint(this.getNamePool());
            String uri = attName.getURI();
            if (uri.equals(stdAttUri)) {
                String local;
                switch (local = attName.getLocalPart()) {
                    case "xpath-default-namespace": {
                        xpathDefaultNamespaceAtt = att.getValue();
                        break;
                    }
                    case "version": {
                        versionAtt = att.getValue();
                        break;
                    }
                    case "use-when": {
                        useWhenAtt = att.getValue();
                        break;
                    }
                    case "static": {
                        staticAtt = att.getValue();
                    }
                }
                if (!local.startsWith("_") || !uri.equals("") || !inXsltNamespace && !inSaxonNamespace) continue;
                hasShadowAttributes = true;
                continue;
            }
            if (inSaxonNamespace || uri.equals("http://saxon.sf.net/")) {
                if (!attName.getLocalPart().startsWith("_")) continue;
                hasShadowAttributes = true;
                continue;
            }
            if (!uri.equals("http://www.w3.org/XML/1998/namespace") || !attName.getLocalPart().equals("base")) continue;
            xmlBaseAtt = att.getValue();
        }
        this.defaultNamespaceStack.push(xpathDefaultNamespaceAtt);
        if (this.emptyStylesheetElement) {
            ++this.depthOfHole;
            return;
        }
        if (this.depthOfHole == 0) {
            String uw;
            URI baseUri = this.processBaseUri(location, xmlBaseAtt);
            boolean ignore = false;
            int version = Integer.MIN_VALUE;
            if (versionAtt != null && fp != 185) {
                version = this.processVersionAttribute(versionAtt);
            }
            if (version == Integer.MIN_VALUE) {
                version = this.versionStack.isEmpty() ? 30 : this.versionStack.peek();
            }
            this.versionStack.push(version);
            if (inXsltNamespace && this.defaultNamespaceStack.size() == 2 && version > 30 && !ElementAvailable.isXslt30Element(fp)) {
                ignore = true;
            }
            if (hasShadowAttributes && !ignore && (uw = (attributes = this.processShadowAttributes(elemName, attributes, namespaces, location, baseUri)).getValue(stdAttUri, "use-when")) != null) {
                useWhenAtt = uw;
            }
            if (!ignore) {
                if (useWhenAtt != null) {
                    AttributeLocation attLoc = new AttributeLocation(elemName.getStructuredQName(), new StructuredQName("", stdAttUri, "use-when"), location);
                    boolean use = this.evaluateUseWhen(useWhenAtt, attLoc, baseUri.toString(), namespaces);
                    boolean bl = ignore = !use;
                }
                if (ignore) {
                    if (fp == 199 || fp == 202 || fp == 188) {
                        this.emptyStylesheetElement = true;
                    } else {
                        this.depthOfHole = 1;
                        return;
                    }
                }
            }
            if (inXsltNamespace && this.defaultNamespaceStack.size() == 2) {
                switch (fp) {
                    case 189: 
                    case 206: {
                        String staticStr;
                        if (hasShadowAttributes) {
                            staticAtt = attributes.getValue("", "static");
                        }
                        if (staticAtt == null || !StyleElement.isYes(staticStr = Whitespace.trim(staticAtt))) break;
                        this.processStaticVariable(elemName, attributes, namespaces, location, baseUri, this.precedence);
                        break;
                    }
                    case 161: 
                    case 163: {
                        String href = attributes.getValue("", "href");
                        includedDoc = this.processIncludeImport(elemName, location, baseUri, href, fp == 161);
                        break;
                    }
                    case 162: {
                        this.compilation.setSchemaAware(true);
                        break;
                    }
                    case 204: {
                        if (this.precedence.getDepth() > 1) {
                            throw new XPathException("xsl:use-package cannot appear in an imported stylesheet", "XTSE3008");
                        }
                        String name = attributes.getValue("", "name");
                        String pversion = attributes.getValue("", "package-version");
                        if (name == null) break;
                        try {
                            UsePack use = new UsePack(name, pversion, location.saveLocation());
                            this.compilation.registerPackageDependency(use);
                            break;
                        } catch (XPathException xPathException) {
                            // empty catch block
                        }
                    }
                }
            }
            this.dropUnderscoredAttributes = inXsltNamespace;
            this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
            if (includedDoc != null) {
                XSLGeneralIncorporate node = (XSLGeneralIncorporate)this.treeBuilder.getCurrentParentNode();
                node.setTargetDocument(includedDoc);
            }
        } else {
            ++this.depthOfHole;
        }
    }

    private DocumentImpl processIncludeImport(NodeName elemName, Location location, URI baseUri, String href, boolean isImport) throws XPathException {
        Source source;
        if (href == null) {
            throw new XPathException("Missing href attribute on " + elemName.getDisplayName(), "XTSE0010");
        }
        URIResolver resolver = this.compilation.getCompilerInfo().getURIResolver();
        String baseUriStr = baseUri.toString();
        DocumentKey key = DocumentFn.computeDocumentKey(href, baseUriStr, this.compilation.getPackageData(), resolver, false);
        Map<DocumentKey, TreeInfo> map = this.compilation.getStylesheetModules();
        if (map.containsKey(key)) {
            return (DocumentImpl)map.get(key);
        }
        try {
            source = resolver.resolve(href, baseUriStr);
        } catch (TransformerException e) {
            throw XPathException.makeXPathException(e);
        }
        if (source == null) {
            source = this.getConfiguration().getSystemURIResolver().resolve(href, baseUriStr);
        }
        if (source instanceof EmptySource) {
            source = new StreamSource(new StringReader("<xsl:transform version='3.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'/>"));
        }
        NestedIntegerValue newPrecedence = this.precedence;
        if (isImport) {
            newPrecedence = this.precedence.getStem().append(this.precedence.getLeaf() - 1).append(2 * ++this.importCount);
        }
        try {
            DocumentImpl includedDoc = StylesheetModule.loadStylesheetModule(source, false, this.compilation, newPrecedence);
            map.put(key, includedDoc);
            return includedDoc;
        } catch (XPathException e) {
            e.maybeSetLocation(location);
            e.maybeSetErrorCode("XTSE0165");
            if ("XTSE0180".equals(e.getErrorCodeLocalPart()) && isImport) {
                e.setErrorCode("XTSE0210");
            }
            if (!e.hasBeenReported()) {
                this.compilation.reportError(e);
            }
            throw e;
        }
    }

    private void processStaticVariable(NodeName elemName, AttributeMap attributes, NamespaceResolver nsResolver, Location location, URI baseUri, NestedIntegerValue precedence) throws XPathException {
        StructuredQName varName;
        String nameStr = attributes.getValue("", "name");
        String asStr = attributes.getValue("", "as");
        String requiredStr = Whitespace.trim(attributes.getValue("", "required"));
        boolean isRequired = StyleElement.isYes(requiredStr);
        UseWhenStaticContext staticContext = new UseWhenStaticContext(this.compilation, nsResolver);
        staticContext.setBaseURI(baseUri.toString());
        staticContext.setContainingLocation(new AttributeLocation(elemName.getStructuredQName(), new StructuredQName("", "", "as"), location));
        SequenceType requiredType = SequenceType.ANY_SEQUENCE;
        int languageLevel = this.compilation.getConfiguration().getConfigurationProperty(Feature.XPATH_VERSION_FOR_XSLT);
        if (languageLevel == 30) {
            languageLevel = 305;
        }
        if (asStr != null) {
            XPathParser parser = this.compilation.getConfiguration().newExpressionParser("XP", false, languageLevel);
            requiredType = parser.parseSequenceType(asStr, staticContext);
        }
        try {
            varName = StructuredQName.fromLexicalQName(nameStr, false, true, nsResolver);
        } catch (XPathException err) {
            throw this.createXPathException("Invalid variable name:" + nameStr + ". " + err.getMessage(), err.getErrorCodeLocalPart(), location);
        }
        boolean isVariable = elemName.getLocalPart().equals("variable");
        boolean isParam = elemName.getLocalPart().equals("param");
        boolean isSupplied = isParam && this.compilation.getParameters().containsKey(varName);
        AttributeLocation attLoc = new AttributeLocation(elemName.getStructuredQName(), new StructuredQName("", "", "select"), location);
        if (isParam) {
            if (isRequired && !isSupplied) {
                String selectStr = attributes.getValue("", "select");
                if (selectStr != null) {
                    throw this.createXPathException("Cannot supply a default value when required='yes'", "XTSE0010", attLoc);
                }
                throw this.createXPathException("No value was supplied for the required static parameter $" + varName.getDisplayName(), "XTDE0050", location);
            }
            if (isSupplied) {
                GroundedValue suppliedValue = this.compilation.getParameters().convertParameterValue(varName, requiredType, true, staticContext.makeEarlyEvaluationContext());
                this.compilation.declareStaticVariable(varName, suppliedValue.materialize(), precedence, isParam);
            }
        }
        if (isVariable || !isSupplied) {
            GroundedValue value;
            String selectStr = attributes.getValue("", "select");
            if (selectStr == null) {
                if (isVariable) {
                    throw this.createXPathException("The select attribute is required for a static global variable", "XTSE0010", location);
                }
                if (!Cardinality.allowsZero(requiredType.getCardinality())) {
                    throw this.createXPathException("The parameter is implicitly required because it does not accept an empty sequence, but no value has been supplied", "XTDE0700", location);
                }
                value = asStr == null ? StringValue.EMPTY_STRING : EmptySequence.getInstance();
                this.compilation.declareStaticVariable(varName, value, precedence, isParam);
            } else {
                try {
                    staticContext.setContainingLocation(attLoc);
                    Sequence seq = this.evaluateStatic(selectStr, location, staticContext);
                    value = seq.materialize();
                } catch (XPathException e) {
                    throw this.createXPathException("Error in " + elemName.getLocalPart() + " expression. " + e.getMessage(), e.getErrorCodeLocalPart(), attLoc);
                }
            }
            RoleDiagnostic role = new RoleDiagnostic(3, varName.getDisplayName(), 0);
            role.setErrorCode("XTDE0050");
            TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
            Sequence seq = th.applyFunctionConversionRules(value, requiredType, role, attLoc);
            value = seq.materialize();
            try {
                this.compilation.declareStaticVariable(varName, value, precedence, isParam);
            } catch (XPathException e) {
                throw this.createXPathException(e.getMessage(), e.getErrorCodeLocalPart(), attLoc);
            }
        }
    }

    private AttributeMap processShadowAttributes(NodeName elemName, AttributeMap attributes, NamespaceResolver nsResolver, Location location, URI baseUri) throws XPathException {
        NodeName attName;
        HashMap<NodeName, AttributeInfo> attMap = new HashMap<NodeName, AttributeInfo>();
        for (AttributeInfo att : attributes) {
            attName = att.getNodeName();
            attMap.put(attName, att);
        }
        for (AttributeInfo att : attributes) {
            attName = att.getNodeName();
            String local = attName.getLocalPart();
            String uri = attName.getURI();
            if (!local.startsWith("_") || !uri.isEmpty() && !uri.equals("http://saxon.sf.net/") || local.length() < 2) continue;
            String value = att.getValue();
            AttributeLocation attLocation = new AttributeLocation(elemName.getStructuredQName(), attName.getStructuredQName(), location);
            String newValue = this.processShadowAttribute(value, baseUri.toString(), nsResolver, attLocation);
            String plainName = local.substring(1);
            NodeName newName = uri.isEmpty() ? new NoNamespaceName(plainName) : new FingerprintedQName(attName.getPrefix(), "http://saxon.sf.net/", plainName);
            AttributeInfo newAtt = new AttributeInfo(newName, att.getType(), newValue, att.getLocation(), 0);
            attMap.put(newName, newAtt);
            attMap.remove(attName);
        }
        AttributeMap resultAtts = EmptyAttributeMap.getInstance();
        for (AttributeInfo att : attMap.values()) {
            resultAtts = resultAtts.put(new AttributeInfo(att.getNodeName(), att.getType(), att.getValue(), att.getLocation(), att.getProperties()));
        }
        return resultAtts;
    }

    private URI processBaseUri(Location location, String xmlBaseAtt) throws XPathException {
        URI baseUri;
        String systemId = location.getSystemId();
        if (systemId == null) {
            systemId = this.getSystemId();
        }
        if (systemId == null || systemId.equals(this.systemIdStack.peek())) {
            baseUri = this.baseUriStack.peek();
        } else {
            try {
                baseUri = new URI(systemId);
            } catch (URISyntaxException e) {
                throw new XPathException("Invalid URI for stylesheet entity: " + systemId);
            }
        }
        if (xmlBaseAtt != null) {
            try {
                baseUri = baseUri.resolve(xmlBaseAtt);
            } catch (IllegalArgumentException iae) {
                throw new XPathException("Invalid URI in xml:base attribute: " + xmlBaseAtt + ". " + iae.getMessage());
            }
        }
        this.baseUriStack.push(baseUri);
        this.systemIdStack.push(systemId);
        return baseUri;
    }

    private int processVersionAttribute(String version) throws XPathException {
        if (version != null) {
            ConversionResult cr = BigDecimalValue.makeDecimalValue(version, true);
            if (cr instanceof ValidationFailure) {
                throw new XPathException("Invalid version number: " + version, "XTSE0110");
            }
            BigDecimalValue d = (BigDecimalValue)cr.asAtomic();
            return d.getDecimalValue().multiply(BigDecimal.TEN).intValue();
        }
        return Integer.MIN_VALUE;
    }

    private String processShadowAttribute(String expression, String baseUri, NamespaceResolver nsResolver, AttributeLocation loc) throws XPathException {
        UseWhenStaticContext staticContext = new UseWhenStaticContext(this.compilation, nsResolver);
        staticContext.setBaseURI(baseUri);
        staticContext.setContainingLocation(loc);
        this.setNamespaceBindings(staticContext);
        Expression expr = AttributeValueTemplate.make(expression, staticContext);
        expr = this.typeCheck(expr, staticContext);
        SlotManager stackFrameMap = this.allocateSlots(expression, expr);
        XPathContext dynamicContext = this.makeDynamicContext(staticContext);
        ((XPathContextMajor)dynamicContext).openStackFrame(stackFrameMap);
        return expr.evaluateAsString(dynamicContext).toString();
    }

    private XPathException createXPathException(String message, String errorCode, Location location) {
        XPathException err = new XPathException(message);
        err.setErrorCode(errorCode);
        err.setIsStaticError(true);
        err.setLocator(location.saveLocation());
        this.getPipelineConfiguration().getErrorReporter().report(new XmlProcessingException(err));
        err.setHasBeenReported(true);
        return err;
    }

    @Override
    public void endElement() throws XPathException {
        this.defaultNamespaceStack.pop();
        if (this.depthOfHole > 0) {
            --this.depthOfHole;
        } else {
            this.systemIdStack.pop();
            this.baseUriStack.pop();
            this.versionStack.pop();
            this.nextReceiver.endElement();
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        if (this.depthOfHole == 0) {
            this.nextReceiver.characters(chars, locationId, properties);
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) {
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
    }

    private boolean evaluateUseWhen(String expression, AttributeLocation location, String baseUri, NamespaceResolver nsResolver) throws XPathException {
        UseWhenStaticContext staticContext = new UseWhenStaticContext(this.compilation, nsResolver);
        staticContext.setBaseURI(baseUri);
        staticContext.setContainingLocation(location);
        this.setNamespaceBindings(staticContext);
        Expression expr = ExpressionTool.make(expression, staticContext, 0, 0, null);
        expr.setRetainedStaticContext(staticContext.makeRetainedStaticContext());
        expr = this.typeCheck(expr, staticContext);
        SlotManager stackFrameMap = this.allocateSlots(expression, expr);
        XPathContext dynamicContext = this.makeDynamicContext(staticContext);
        ((XPathContextMajor)dynamicContext).openStackFrame(stackFrameMap);
        return expr.effectiveBooleanValue(dynamicContext);
    }

    private SlotManager allocateSlots(String expression, Expression expr) {
        SlotManager stackFrameMap = this.getPipelineConfiguration().getConfiguration().makeSlotManager();
        if (expression.indexOf(36) >= 0) {
            ExpressionTool.allocateSlots(expr, stackFrameMap.getNumberOfVariables(), stackFrameMap);
        }
        return stackFrameMap;
    }

    private void setNamespaceBindings(UseWhenStaticContext staticContext) {
        staticContext.setDefaultElementNamespace("");
        for (int i = this.defaultNamespaceStack.size() - 1; i >= 0; --i) {
            String uri = (String)this.defaultNamespaceStack.get(i);
            if (uri == null) continue;
            staticContext.setDefaultElementNamespace(uri);
            break;
        }
    }

    private Expression typeCheck(Expression expr, UseWhenStaticContext staticContext) throws XPathException {
        ItemType contextItemType = Type.ITEM_TYPE;
        ContextItemStaticInfo cit = this.getConfiguration().makeContextItemStaticInfo(contextItemType, true);
        ExpressionVisitor visitor = ExpressionVisitor.make(staticContext);
        return expr.typeCheck(visitor, cit);
    }

    private XPathContext makeDynamicContext(UseWhenStaticContext staticContext) throws XPathException {
        Controller controller = new Controller(this.getConfiguration());
        controller.getExecutable().setFunctionLibrary((FunctionLibraryList)staticContext.getFunctionLibrary());
        if (staticContext.getXPathVersion() < 30) {
            controller.setURIResolver(new URIPreventer());
        }
        controller.setCurrentDateTime(this.currentDateTime);
        XPathContextMajor dynamicContext = controller.newXPathContext();
        dynamicContext = dynamicContext.newCleanContext();
        return dynamicContext;
    }

    public Sequence evaluateStatic(String expression, Location locationId, UseWhenStaticContext staticContext) throws XPathException {
        this.setNamespaceBindings(staticContext);
        Expression expr = ExpressionTool.make(expression, staticContext, 0, 0, null);
        expr = this.typeCheck(expr, staticContext);
        SlotManager stackFrameMap = this.getPipelineConfiguration().getConfiguration().makeSlotManager();
        ExpressionTool.allocateSlots(expr, stackFrameMap.getNumberOfVariables(), stackFrameMap);
        XPathContext dynamicContext = this.makeDynamicContext(staticContext);
        ((XPathContextMajor)dynamicContext).openStackFrame(stackFrameMap);
        return expr.iterate(dynamicContext).materialize();
    }

    private static class URIPreventer
    implements URIResolver {
        private URIPreventer() {
        }

        @Override
        public Source resolve(String href, String base) throws XPathException {
            throw new XPathException("No external documents are available within an [xsl]use-when expression");
        }
    }
}

