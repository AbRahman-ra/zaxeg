/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import javax.xml.transform.TransformerFactoryConfigurationError;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.style.AbsentExtensionElement;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.DataElement;
import net.sf.saxon.style.LiteralResultElement;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.TextValueTemplateNode;
import net.sf.saxon.style.XSLAccept;
import net.sf.saxon.style.XSLAccumulator;
import net.sf.saxon.style.XSLAccumulatorRule;
import net.sf.saxon.style.XSLAnalyzeString;
import net.sf.saxon.style.XSLApplyImports;
import net.sf.saxon.style.XSLApplyTemplates;
import net.sf.saxon.style.XSLAssert;
import net.sf.saxon.style.XSLAttribute;
import net.sf.saxon.style.XSLAttributeSet;
import net.sf.saxon.style.XSLBreak;
import net.sf.saxon.style.XSLCallTemplate;
import net.sf.saxon.style.XSLCatch;
import net.sf.saxon.style.XSLCharacterMap;
import net.sf.saxon.style.XSLChoose;
import net.sf.saxon.style.XSLComment;
import net.sf.saxon.style.XSLContextItem;
import net.sf.saxon.style.XSLCopy;
import net.sf.saxon.style.XSLCopyOf;
import net.sf.saxon.style.XSLDecimalFormat;
import net.sf.saxon.style.XSLDocument;
import net.sf.saxon.style.XSLElement;
import net.sf.saxon.style.XSLEvaluate;
import net.sf.saxon.style.XSLExpose;
import net.sf.saxon.style.XSLFallback;
import net.sf.saxon.style.XSLForEach;
import net.sf.saxon.style.XSLForEachGroup;
import net.sf.saxon.style.XSLFork;
import net.sf.saxon.style.XSLFunction;
import net.sf.saxon.style.XSLGlobalContextItem;
import net.sf.saxon.style.XSLGlobalParam;
import net.sf.saxon.style.XSLGlobalVariable;
import net.sf.saxon.style.XSLIf;
import net.sf.saxon.style.XSLImport;
import net.sf.saxon.style.XSLImportSchema;
import net.sf.saxon.style.XSLInclude;
import net.sf.saxon.style.XSLIterate;
import net.sf.saxon.style.XSLKey;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.style.XSLLocalVariable;
import net.sf.saxon.style.XSLMap;
import net.sf.saxon.style.XSLMapEntry;
import net.sf.saxon.style.XSLMatchingSubstring;
import net.sf.saxon.style.XSLMerge;
import net.sf.saxon.style.XSLMergeAction;
import net.sf.saxon.style.XSLMergeKey;
import net.sf.saxon.style.XSLMergeSource;
import net.sf.saxon.style.XSLMessage;
import net.sf.saxon.style.XSLMode;
import net.sf.saxon.style.XSLModuleRoot;
import net.sf.saxon.style.XSLNamespace;
import net.sf.saxon.style.XSLNamespaceAlias;
import net.sf.saxon.style.XSLNextIteration;
import net.sf.saxon.style.XSLNextMatch;
import net.sf.saxon.style.XSLNumber;
import net.sf.saxon.style.XSLOnCompletion;
import net.sf.saxon.style.XSLOnEmpty;
import net.sf.saxon.style.XSLOnNonEmpty;
import net.sf.saxon.style.XSLOtherwise;
import net.sf.saxon.style.XSLOutput;
import net.sf.saxon.style.XSLOutputCharacter;
import net.sf.saxon.style.XSLOverride;
import net.sf.saxon.style.XSLPackage;
import net.sf.saxon.style.XSLPerformSort;
import net.sf.saxon.style.XSLPreserveSpace;
import net.sf.saxon.style.XSLProcessingInstruction;
import net.sf.saxon.style.XSLResultDocument;
import net.sf.saxon.style.XSLSequence;
import net.sf.saxon.style.XSLSort;
import net.sf.saxon.style.XSLSourceDocument;
import net.sf.saxon.style.XSLStylesheet;
import net.sf.saxon.style.XSLTemplate;
import net.sf.saxon.style.XSLText;
import net.sf.saxon.style.XSLTry;
import net.sf.saxon.style.XSLUsePackage;
import net.sf.saxon.style.XSLValueOf;
import net.sf.saxon.style.XSLWhen;
import net.sf.saxon.style.XSLWherePopulated;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingIncident;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.NodeFactory;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.TextImpl;
import net.sf.saxon.type.SchemaType;

public class StyleNodeFactory
implements NodeFactory {
    protected Configuration config;
    protected NamePool namePool;
    private Compilation compilation;
    private boolean topLevelModule;

    public StyleNodeFactory(Configuration config, Compilation compilation) {
        this.config = config;
        this.compilation = compilation;
        this.namePool = config.getNamePool();
    }

    public void setTopLevelModule(boolean topLevelModule) {
        this.topLevelModule = topLevelModule;
    }

    public boolean isTopLevelModule() {
        return this.topLevelModule;
    }

    public Compilation getCompilation() {
        return this.compilation;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    @Override
    public ElementImpl makeElementNode(NodeInfo parent, NodeName elemName, SchemaType elemType, boolean isNilled, AttributeMap attlist, NamespaceMap namespaces, PipelineConfiguration pipe, Location location, int sequence) {
        StyleElement node;
        Class actualClass;
        XmlProcessingIncident reason;
        int f = elemName.obtainFingerprint(pipe.getConfiguration().getNamePool());
        boolean toplevel = parent instanceof XSLModuleRoot;
        String baseURI = null;
        int lineNumber = -1;
        int columnNumber = -1;
        baseURI = location.getSystemId();
        lineNumber = location.getLineNumber();
        columnNumber = location.getColumnNumber();
        if (parent instanceof DataElement) {
            DataElement d = new DataElement();
            d.setNamespaceMap(namespaces);
            d.initialise(elemName, elemType, attlist, parent, sequence);
            d.setLocation(baseURI, lineNumber, columnNumber);
            return d;
        }
        StyleElement e = this.makeXSLElement(f, (NodeImpl)parent);
        if ((e instanceof XSLStylesheet || e instanceof XSLPackage) && parent.getNodeKind() != 9) {
            e = new AbsentExtensionElement();
            XmlProcessingIncident reason2 = new XmlProcessingIncident(elemName.getDisplayName() + " can only appear at the outermost level", "XTSE0010");
            e.setValidationError(reason2, StyleElement.OnFailure.REPORT_ALWAYS);
        }
        if (e != null) {
            e.setCompilation(this.compilation);
            e.setNamespaceMap(namespaces);
            e.initialise(elemName, elemType, attlist, parent, sequence);
            e.setLocation(baseURI, lineNumber, columnNumber);
            e.processExtensionElementAttribute("");
            e.processExcludedNamespaces("");
            e.processVersionAttribute("");
            e.processDefaultXPathNamespaceAttribute("");
            e.processExpandTextAttribute("");
            e.processDefaultValidationAttribute("");
            if (toplevel && !e.isDeclaration() && !(e instanceof XSLExpose) && e.forwardsCompatibleModeIsEnabled()) {
                DataElement d = new DataElement();
                d.setNamespaceMap(namespaces);
                d.initialise(elemName, elemType, attlist, parent, sequence);
                d.setLocation(baseURI, lineNumber, columnNumber);
                return d;
            }
            if (parent instanceof AbsentExtensionElement && ((AbsentExtensionElement)parent).forwardsCompatibleModeIsEnabled() && parent.getURI().equals("http://www.w3.org/1999/XSL/Transform") && !(e instanceof XSLFallback)) {
                AbsentExtensionElement temp = new AbsentExtensionElement();
                temp.initialise(elemName, elemType, attlist, parent, sequence);
                temp.setLocation(baseURI, lineNumber, columnNumber);
                temp.setCompilation(this.compilation);
                temp.setIgnoreInstruction();
                return temp;
            }
            return e;
        }
        String uri = elemName.getURI();
        if (toplevel && !uri.equals("http://www.w3.org/1999/XSL/Transform")) {
            DataElement d = new DataElement();
            d.setNamespaceMap(namespaces);
            d.initialise(elemName, elemType, attlist, parent, sequence);
            d.setLocation(baseURI, lineNumber, columnNumber);
            return d;
        }
        String localname = elemName.getLocalPart();
        StyleElement temp = null;
        if (uri.equals("http://www.w3.org/1999/XSL/Transform")) {
            if (parent instanceof XSLStylesheet) {
                if (((XSLStylesheet)parent).getEffectiveVersion() <= 20) {
                    temp = new AbsentExtensionElement();
                    temp.setCompilation(this.compilation);
                    temp.setValidationError(new XmlProcessingIncident("Unknown top-level XSLT declaration"), StyleElement.OnFailure.REPORT_UNLESS_FORWARDS_COMPATIBLE);
                }
            } else {
                temp = new AbsentExtensionElement();
                temp.initialise(elemName, elemType, attlist, parent, sequence);
                temp.setLocation(baseURI, lineNumber, columnNumber);
                temp.setCompilation(this.compilation);
                temp.processStandardAttributes("");
                if (temp.getEffectiveVersion() > 20) {
                    temp.setValidationError(new XmlProcessingIncident("Unknown XSLT instruction"), StyleElement.OnFailure.REPORT_STATICALLY_UNLESS_FALLBACK_AVAILABLE);
                } else {
                    temp.setValidationError(new XmlProcessingIncident("Unknown XSLT instruction"), StyleElement.OnFailure.REPORT_IF_INSTANTIATED);
                }
            }
        }
        if (uri.equals("http://saxon.sf.net/")) {
            String message = elemName.getDisplayName() + " is not recognized as a Saxon instruction";
            if (this.config.getEditionCode().equals("HE")) {
                message = message + ". Saxon extensions require Saxon-PE or higher";
            } else if (!this.config.isLicensedFeature(8)) {
                message = message + ". No Saxon-PE or -EE license was found";
            }
            XmlProcessingIncident err = new XmlProcessingIncident(message, "SXWN9008", location.saveLocation()).asWarning();
            pipe.getErrorReporter().report(err);
        }
        Class<LiteralResultElement> assumedClass = LiteralResultElement.class;
        if (temp == null) {
            temp = new LiteralResultElement();
        }
        temp.setNamespaceMap(namespaces);
        temp.setCompilation(this.compilation);
        temp.initialise(elemName, elemType, attlist, parent, sequence);
        temp.setLocation(baseURI, lineNumber, columnNumber);
        temp.processStandardAttributes("http://www.w3.org/1999/XSL/Transform");
        if (uri.equals("http://www.w3.org/1999/XSL/Transform")) {
            reason = new XmlProcessingIncident("Unknown XSLT element: " + Err.wrap(localname, 1), "XTSE0010");
            actualClass = AbsentExtensionElement.class;
            temp.setValidationError(reason, StyleElement.OnFailure.REPORT_STATICALLY_UNLESS_FALLBACK_AVAILABLE);
        } else if (temp.isExtensionNamespace(uri) && !toplevel) {
            actualClass = AbsentExtensionElement.class;
            if (NamespaceConstant.isReserved(uri)) {
                reason = new XmlProcessingIncident("Cannot use a reserved namespace for extension instructions", "XTSE0800");
                temp.setValidationError(reason, StyleElement.OnFailure.REPORT_ALWAYS);
            } else {
                reason = new XmlProcessingIncident("Unknown extension instruction " + Err.wrap(elemName.getDisplayName(), 1), "XTDE1450");
                temp.setValidationError(reason, StyleElement.OnFailure.REPORT_DYNAMICALLY_UNLESS_FALLBACK_AVAILABLE);
            }
        } else {
            actualClass = LiteralResultElement.class;
        }
        if (actualClass.equals(assumedClass)) {
            node = temp;
        } else {
            try {
                node = (StyleElement)actualClass.newInstance();
            } catch (InstantiationException err1) {
                throw new TransformerFactoryConfigurationError(err1, "Failed to create instance of " + actualClass.getName());
            } catch (IllegalAccessException err2) {
                throw new TransformerFactoryConfigurationError(err2, "Failed to access class " + actualClass.getName());
            }
            node.substituteFor(temp);
        }
        return node;
    }

    protected StyleElement makeXSLElement(int f, NodeImpl parent) {
        switch (f) {
            case 128: {
                return new XSLAccept();
            }
            case 129: {
                return new XSLAccumulator();
            }
            case 130: {
                return new XSLAccumulatorRule();
            }
            case 131: {
                return new XSLAnalyzeString();
            }
            case 132: {
                return new XSLApplyImports();
            }
            case 133: {
                return new XSLApplyTemplates();
            }
            case 134: {
                return new XSLAssert();
            }
            case 135: {
                return new XSLAttribute();
            }
            case 136: {
                return new XSLAttributeSet();
            }
            case 137: {
                return new XSLBreak();
            }
            case 138: {
                return new XSLCallTemplate();
            }
            case 139: {
                return new XSLCatch();
            }
            case 144: {
                return new XSLContextItem();
            }
            case 141: {
                return new XSLCharacterMap();
            }
            case 142: {
                return new XSLChoose();
            }
            case 143: {
                return new XSLComment();
            }
            case 145: {
                return new XSLCopy();
            }
            case 146: {
                return new XSLCopyOf();
            }
            case 147: {
                return new XSLDecimalFormat();
            }
            case 150: {
                return new XSLDocument();
            }
            case 151: {
                return new XSLElement();
            }
            case 153: {
                return new XSLEvaluate();
            }
            case 152: {
                return new XSLExpose();
            }
            case 154: {
                return new XSLFallback();
            }
            case 155: {
                return new XSLForEach();
            }
            case 157: {
                return new XSLForEachGroup();
            }
            case 156: {
                return new XSLFork();
            }
            case 158: {
                return new XSLFunction();
            }
            case 159: {
                return new XSLGlobalContextItem();
            }
            case 160: {
                return new XSLIf();
            }
            case 161: {
                return new XSLImport();
            }
            case 162: {
                return new XSLImportSchema();
            }
            case 163: {
                return new XSLInclude();
            }
            case 164: {
                return new XSLIterate();
            }
            case 165: {
                return new XSLKey();
            }
            case 166: {
                return new XSLMap();
            }
            case 167: {
                return new XSLMapEntry();
            }
            case 168: {
                return new XSLMatchingSubstring();
            }
            case 169: {
                return new XSLMerge();
            }
            case 170: {
                return new XSLMergeAction();
            }
            case 171: {
                return new XSLMergeKey();
            }
            case 172: {
                return new XSLMergeSource();
            }
            case 173: {
                return new XSLMessage();
            }
            case 174: {
                return new XSLMode();
            }
            case 177: {
                return new XSLNextIteration();
            }
            case 178: {
                return new XSLNextMatch();
            }
            case 179: {
                return new XSLMatchingSubstring();
            }
            case 180: {
                return new XSLNumber();
            }
            case 175: {
                return new XSLNamespace();
            }
            case 176: {
                return new XSLNamespaceAlias();
            }
            case 182: {
                return new XSLOnCompletion();
            }
            case 183: {
                return new XSLOnEmpty();
            }
            case 184: {
                return new XSLOnNonEmpty();
            }
            case 181: {
                return new XSLOtherwise();
            }
            case 185: {
                return new XSLOutput();
            }
            case 187: {
                return new XSLOutputCharacter();
            }
            case 186: {
                return new XSLOverride();
            }
            case 188: {
                return new XSLPackage();
            }
            case 189: {
                return parent instanceof XSLModuleRoot || parent instanceof XSLOverride ? new XSLGlobalParam() : new XSLLocalParam();
            }
            case 190: {
                return new XSLPerformSort();
            }
            case 191: {
                return new XSLPreserveSpace();
            }
            case 192: {
                return new XSLProcessingInstruction();
            }
            case 193: {
                this.compilation.setCreatesSecondaryResultDocuments(true);
                return new XSLResultDocument();
            }
            case 194: {
                return new XSLSequence();
            }
            case 195: {
                return new XSLSort();
            }
            case 196: {
                return new XSLSourceDocument();
            }
            case 198: {
                return new XSLPreserveSpace();
            }
            case 199: {
                return this.topLevelModule ? new XSLPackage() : new XSLStylesheet();
            }
            case 200: {
                return new XSLTemplate();
            }
            case 201: {
                return new XSLText();
            }
            case 202: {
                return this.topLevelModule ? new XSLPackage() : new XSLStylesheet();
            }
            case 203: {
                return new XSLTry();
            }
            case 204: {
                return new XSLUsePackage();
            }
            case 205: {
                return new XSLValueOf();
            }
            case 206: {
                return parent instanceof XSLModuleRoot || parent instanceof XSLOverride ? new XSLGlobalVariable() : new XSLLocalVariable();
            }
            case 209: {
                return new XSLWithParam();
            }
            case 207: {
                return new XSLWhen();
            }
            case 208: {
                return new XSLWherePopulated();
            }
        }
        return null;
    }

    @Override
    public TextImpl makeTextNode(NodeInfo parent, CharSequence content) {
        if (parent instanceof StyleElement && ((StyleElement)parent).isExpandingText()) {
            return new TextValueTemplateNode(content.toString());
        }
        return new TextImpl(content.toString());
    }

    public boolean isElementAvailable(String uri, String localName, boolean instructionsOnly) {
        int fingerprint = this.namePool.getFingerprint(uri, localName);
        if (uri.equals("http://www.w3.org/1999/XSL/Transform")) {
            if (fingerprint == -1) {
                return false;
            }
            StyleElement e = this.makeXSLElement(fingerprint, null);
            if (e != null) {
                return !instructionsOnly || e.isInstruction();
            }
        }
        return false;
    }

    public AccumulatorRegistry makeAccumulatorManager() {
        return new AccumulatorRegistry();
    }

    public PrincipalStylesheetModule newPrincipalModule(XSLPackage node) throws XPathException {
        return new PrincipalStylesheetModule(node);
    }
}

