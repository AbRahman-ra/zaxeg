/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.net.URI;
import java.net.URISyntaxException;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.CopyInformee;
import net.sf.saxon.event.LocationCopier;
import net.sf.saxon.event.NoOpenStartTagException;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceCollector;
import net.sf.saxon.event.Sink;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.DummyNamespaceResolver;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.ValidatingInstruction;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.ContentTypeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.tree.tiny.TinyNodeImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.wrapper.VirtualCopy;
import net.sf.saxon.tree.wrapper.VirtualUntypedCopy;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AnySimpleType;
import net.sf.saxon.type.AnyType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.MissingComponentException;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.SequenceType;

public class CopyOf
extends Instruction
implements ValidatingInstruction {
    private Operand selectOp;
    private boolean copyNamespaces;
    private boolean copyAccumulators;
    private int validation;
    private SchemaType schemaType;
    private boolean requireDocumentOrElement = false;
    private boolean rejectDuplicateAttributes;
    private boolean validating;
    private boolean copyLineNumbers = false;
    private boolean copyForUpdate = false;
    private boolean isSchemaAware = true;

    public CopyOf(Expression select, boolean copyNamespaces, int validation, SchemaType schemaType, boolean rejectDuplicateAttributes) {
        this.selectOp = new Operand(this, select, OperandRole.SINGLE_ATOMIC);
        this.copyNamespaces = copyNamespaces;
        this.validation = validation;
        this.schemaType = schemaType;
        this.validating = schemaType != null || validation == 1 || validation == 2;
        this.rejectDuplicateAttributes = rejectDuplicateAttributes;
    }

    public Expression getSelect() {
        return this.selectOp.getChildExpression();
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.selectOp;
    }

    @Override
    public int getValidationAction() {
        return this.validation;
    }

    public boolean isValidating() {
        return this.validating;
    }

    @Override
    public SchemaType getSchemaType() {
        return this.schemaType;
    }

    public void setSchemaAware(boolean schemaAware) {
        this.isSchemaAware = schemaAware;
    }

    public void setCopyLineNumbers(boolean copy) {
        this.copyLineNumbers = copy;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return !this.getSelect().getItemType().isPlainType();
    }

    @Override
    public int getInstructionNameCode() {
        return 146;
    }

    public void setRequireDocumentOrElement(boolean requireDocumentOrElement) {
        this.requireDocumentOrElement = requireDocumentOrElement;
    }

    public boolean isDocumentOrElementRequired() {
        return this.requireDocumentOrElement;
    }

    public void setCopyForUpdate(boolean forUpdate) {
        this.copyForUpdate = forUpdate;
    }

    public boolean isCopyForUpdate() {
        return this.copyForUpdate;
    }

    @Override
    public int getImplementationMethod() {
        return 14;
    }

    public boolean isCopyNamespaces() {
        return this.copyNamespaces;
    }

    public void setCopyAccumulators(boolean copy) {
        this.copyAccumulators = copy;
    }

    public boolean isCopyAccumulators() {
        return this.copyAccumulators;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        CopyOf c = new CopyOf(this.getSelect().copy(rebindings), this.copyNamespaces, this.validation, this.schemaType, this.rejectDuplicateAttributes);
        ExpressionTool.copyLocationInfo(this, c);
        c.setCopyForUpdate(this.copyForUpdate);
        c.setCopyLineNumbers(this.copyLineNumbers);
        c.isSchemaAware = this.isSchemaAware;
        c.setCopyAccumulators(this.copyAccumulators);
        return c;
    }

    @Override
    public ItemType getItemType() {
        ItemType in = this.getSelect().getItemType();
        if (!this.isSchemaAware) {
            return in;
        }
        Configuration config = this.getConfiguration();
        if (this.schemaType != null) {
            TypeHierarchy th = config.getTypeHierarchy();
            Affinity e = th.relationship(in, NodeKindTest.ELEMENT);
            if (e == Affinity.SAME_TYPE || e == Affinity.SUBSUMED_BY) {
                return new ContentTypeTest(1, this.schemaType, config, false);
            }
            Affinity a = th.relationship(in, NodeKindTest.ATTRIBUTE);
            if (a == Affinity.SAME_TYPE || a == Affinity.SUBSUMED_BY) {
                return new ContentTypeTest(2, this.schemaType, config, false);
            }
        } else {
            switch (this.validation) {
                case 3: {
                    return in;
                }
                case 4: {
                    TypeHierarchy th = config.getTypeHierarchy();
                    Affinity e = th.relationship(in, NodeKindTest.ELEMENT);
                    if (e == Affinity.SAME_TYPE || e == Affinity.SUBSUMED_BY) {
                        return new ContentTypeTest(1, Untyped.getInstance(), config, false);
                    }
                    Affinity a = th.relationship(in, NodeKindTest.ATTRIBUTE);
                    if (a == Affinity.SAME_TYPE || a == Affinity.SUBSUMED_BY) {
                        return new ContentTypeTest(2, BuiltInAtomicType.UNTYPED_ATOMIC, config, false);
                    }
                    if (e != Affinity.DISJOINT || a != Affinity.DISJOINT) {
                        return in instanceof NodeTest ? AnyNodeTest.getInstance() : AnyItemType.getInstance();
                    }
                    return in;
                }
                case 1: 
                case 2: {
                    if (in instanceof NodeTest) {
                        TypeHierarchy th = config.getTypeHierarchy();
                        int fp = ((NodeTest)in).getFingerprint();
                        if (fp != -1) {
                            Affinity e = th.relationship(in, NodeKindTest.ELEMENT);
                            if (e == Affinity.SAME_TYPE || e == Affinity.SUBSUMED_BY) {
                                SchemaDeclaration elem = config.getElementDeclaration(fp);
                                if (elem != null) {
                                    try {
                                        return new ContentTypeTest(1, elem.getType(), config, false);
                                    } catch (MissingComponentException e1) {
                                        return new ContentTypeTest(1, AnyType.getInstance(), config, false);
                                    }
                                }
                                return new ContentTypeTest(1, AnyType.getInstance(), config, false);
                            }
                            Affinity a = th.relationship(in, NodeKindTest.ATTRIBUTE);
                            if (a == Affinity.SAME_TYPE || a == Affinity.SUBSUMED_BY) {
                                SchemaDeclaration attr = config.getElementDeclaration(fp);
                                if (attr != null) {
                                    try {
                                        return new ContentTypeTest(2, attr.getType(), config, false);
                                    } catch (MissingComponentException e1) {
                                        return new ContentTypeTest(2, AnySimpleType.getInstance(), config, false);
                                    }
                                }
                                return new ContentTypeTest(2, AnySimpleType.getInstance(), config, false);
                            }
                        } else {
                            Affinity e = th.relationship(in, NodeKindTest.ELEMENT);
                            if (e == Affinity.SAME_TYPE || e == Affinity.SUBSUMED_BY) {
                                return NodeKindTest.ELEMENT;
                            }
                            Affinity a = th.relationship(in, NodeKindTest.ATTRIBUTE);
                            if (a == Affinity.SAME_TYPE || a == Affinity.SUBSUMED_BY) {
                                return NodeKindTest.ATTRIBUTE;
                            }
                        }
                        return AnyNodeTest.getInstance();
                    }
                    if (in instanceof AtomicType) {
                        return in;
                    }
                    return AnyItemType.getInstance();
                }
            }
        }
        return this.getSelect().getItemType();
    }

    @Override
    public int getCardinality() {
        return this.getSelect().getCardinality();
    }

    @Override
    public int getDependencies() {
        return this.getSelect().getDependencies();
    }

    @Override
    public Expression typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        this.typeCheckChildren(visitor, contextInfo);
        if (this.isDocumentOrElementRequired()) {
            RoleDiagnostic role = new RoleDiagnostic(2, "validate", 0);
            role.setErrorCode("XQTY0030");
            Configuration config = visitor.getConfiguration();
            this.setSelect(config.getTypeChecker(false).staticTypeCheck(this.getSelect(), SequenceType.SINGLE_NODE, role, visitor));
            TypeHierarchy th = config.getTypeHierarchy();
            ItemType t = this.getSelect().getItemType();
            if (th.isSubType(t, NodeKindTest.ATTRIBUTE)) {
                throw new XPathException("validate{} expression cannot be applied to an attribute", "XQTY0030");
            }
            if (th.isSubType(t, NodeKindTest.TEXT)) {
                throw new XPathException("validate{} expression cannot be applied to a text node", "XQTY0030");
            }
            if (th.isSubType(t, NodeKindTest.COMMENT)) {
                throw new XPathException("validate{} expression cannot be applied to a comment node", "XQTY0030");
            }
            if (th.isSubType(t, NodeKindTest.PROCESSING_INSTRUCTION)) {
                throw new XPathException("validate{} expression cannot be applied to a processing instruction node", "XQTY0030");
            }
            if (th.isSubType(t, NodeKindTest.NAMESPACE)) {
                throw new XPathException("validate{} expression cannot be applied to a namespace node", "XQTY0030");
            }
        }
        return this;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        this.selectOp.optimize(visitor, contextItemType);
        if (Literal.isEmptySequence(this.getSelect())) {
            return this.getSelect();
        }
        this.adoptChildExpression(this.getSelect());
        if (this.getSelect().getItemType().isPlainType()) {
            return this.getSelect();
        }
        return this;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("copyOf", this);
        if (this.validation != 4) {
            out.emitAttribute("validation", Validation.toString(this.validation));
        }
        if (this.schemaType != null) {
            out.emitAttribute("type", this.schemaType.getStructuredQName());
        }
        FastStringBuffer fsb = new FastStringBuffer(16);
        if (this.requireDocumentOrElement) {
            fsb.cat('p');
        }
        if (this.rejectDuplicateAttributes) {
            fsb.cat('a');
        }
        if (this.validating) {
            fsb.cat('v');
        }
        if (this.copyLineNumbers) {
            fsb.cat('l');
        }
        if (this.copyForUpdate) {
            fsb.cat('u');
        }
        if (this.isSchemaAware) {
            fsb.cat('s');
        }
        if (this.copyNamespaces) {
            fsb.cat('c');
        }
        if (this.copyAccumulators) {
            fsb.cat('m');
        }
        if (!fsb.isEmpty()) {
            out.emitAttribute("flags", fsb.toString());
        }
        this.getSelect().export(out);
        out.endElement();
    }

    @Override
    public String getStreamerName() {
        return "CopyOf";
    }

    @Override
    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        PathMap.PathMapNodeSet result = super.addToPathMap(pathMap, pathMapNodeSet);
        result.setReturnable(false);
        TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
        ItemType type = this.getItemType();
        if (th.relationship(type, NodeKindTest.ELEMENT) != Affinity.DISJOINT || th.relationship(type, NodeKindTest.DOCUMENT) != Affinity.DISJOINT) {
            result.addDescendants();
        }
        return new PathMap.PathMapNodeSet(pathMap.makeNewRoot(this));
    }

    @Override
    public TailCall processLeavingTail(Outputter out, XPathContext context) throws XPathException {
        if (this.copyAccumulators) {
            if (this.mustPush()) {
                this.getSelect().iterate(context).forEachOrFail(item -> {
                    if (item instanceof NodeInfo) {
                        TinyBuilder builder = new TinyBuilder(out.getPipelineConfiguration());
                        ComplexContentOutputter cco = new ComplexContentOutputter(builder);
                        cco.open();
                        this.copyOneNode(context, cco, (NodeInfo)item, 2);
                        cco.close();
                        TinyNodeImpl copy = (TinyNodeImpl)builder.getCurrentRoot();
                        copy.getTree().setCopiedFrom((NodeInfo)item);
                        out.append(copy);
                    } else {
                        out.append(item);
                    }
                });
            } else {
                this.iterate(context).forEachOrFail(out::append);
            }
        } else {
            int copyOptions = (this.validation == 4 ? 0 : 4) | (this.copyNamespaces ? 2 : 0) | (this.copyForUpdate ? 8 : 0);
            this.getSelect().iterate(context).forEachOrFail(item -> {
                if (item instanceof NodeInfo) {
                    this.copyOneNode(context, out, (NodeInfo)item, copyOptions);
                } else {
                    out.append(item, this.getLocation(), 524288);
                }
            });
        }
        return null;
    }

    private void copyOneNode(XPathContext context, Outputter out, NodeInfo item, int copyOptions) throws XPathException {
        Controller controller = context.getController();
        boolean copyBaseURI = out.getSystemId() == null;
        int kind = item.getNodeKind();
        if (this.requireDocumentOrElement && kind != 1 && kind != 9) {
            XPathException e = new XPathException("Operand of validate expression must be a document or element node");
            e.setXPathContext(context);
            e.setErrorCode("XQTY0030");
            throw e;
        }
        Configuration config = controller.getConfiguration();
        switch (kind) {
            case 1: {
                Outputter eval = out;
                if (this.validating) {
                    String xsitype;
                    ParseOptions options = new ParseOptions();
                    options.setSchemaValidationMode(this.validation);
                    SchemaType type = this.schemaType;
                    if (type == null && (this.validation == 1 || this.validation == 2) && (xsitype = item.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type")) != null) {
                        StructuredQName typeName;
                        try {
                            typeName = StructuredQName.fromLexicalQName(xsitype, true, false, item.getAllNamespaces());
                        } catch (XPathException e) {
                            throw new XPathException("Invalid QName in xsi:type attribute of element being validated: " + xsitype + ". " + e.getMessage(), "XTTE1510");
                        }
                        type = config.getSchemaType(typeName);
                        if (type == null) {
                            throw new XPathException("Unknown xsi:type in element being validated: " + xsitype, "XTTE1510");
                        }
                    }
                    options.setTopLevelType(type);
                    options.setTopLevelElement(NameOfNode.makeName(item).getStructuredQName());
                    options.setErrorReporter(context.getErrorReporter());
                    config.prepareValidationReporting(context, options);
                    Receiver validator = config.getElementValidator(out, options, this.getLocation());
                    eval = new ComplexContentOutputter(validator);
                }
                if (copyBaseURI) {
                    eval.setSystemId(CopyOf.computeNewBaseUri(item, this.getStaticBaseURIString()));
                }
                PipelineConfiguration pipe = out.getPipelineConfiguration();
                if (this.copyLineNumbers) {
                    LocationCopier copier = new LocationCopier(false);
                    pipe.setComponent(CopyInformee.class.getName(), copier);
                }
                item.copy(eval, copyOptions, this.getLocation());
                if (!this.copyLineNumbers) break;
                pipe.setComponent(CopyInformee.class.getName(), null);
                break;
            }
            case 2: {
                if (this.schemaType != null && this.schemaType.isComplexType()) {
                    XPathException e = new XPathException("When copying an attribute with schema validation, the requested type must not be a complex type");
                    e.setLocation(this.getLocation());
                    e.setXPathContext(context);
                    e.setErrorCode("XTTE1535");
                    throw CopyOf.dynamicError(this.getLocation(), e, context);
                }
                try {
                    CopyOf.copyAttribute(item, (SimpleType)this.schemaType, this.validation, this, out, context, this.rejectDuplicateAttributes);
                    break;
                } catch (NoOpenStartTagException err) {
                    XPathException e = new XPathException(err.getMessage());
                    e.setLocation(this.getLocation());
                    e.setXPathContext(context);
                    e.setErrorCodeQName(err.getErrorCodeQName());
                    throw CopyOf.dynamicError(this.getLocation(), e, context);
                }
            }
            case 3: {
                out.characters(item.getStringValueCS(), this.getLocation(), 0);
                break;
            }
            case 7: {
                if (copyBaseURI) {
                    out.setSystemId(item.getBaseURI());
                }
                out.processingInstruction(item.getDisplayName(), item.getStringValueCS(), this.getLocation(), 0);
                break;
            }
            case 8: {
                out.comment(item.getStringValueCS(), this.getLocation(), 0);
                break;
            }
            case 13: {
                try {
                    out.namespace(item.getLocalPart(), item.getStringValue(), 0);
                    break;
                } catch (NoOpenStartTagException err) {
                    XPathException e = new XPathException(err.getMessage());
                    e.setXPathContext(context);
                    e.setErrorCodeQName(err.getErrorCodeQName());
                    throw CopyOf.dynamicError(this.getLocation(), e, context);
                }
            }
            case 9: {
                ParseOptions options = new ParseOptions();
                options.setSchemaValidationMode(this.validation);
                options.setSpaceStrippingRule(NoElementsSpaceStrippingRule.getInstance());
                options.setTopLevelType(this.schemaType);
                options.setErrorReporter(context.getErrorReporter());
                config.prepareValidationReporting(context, options);
                Receiver val = config.getDocumentValidator(out, item.getBaseURI(), options, this.getLocation());
                if (copyBaseURI) {
                    val.setSystemId(item.getBaseURI());
                }
                PipelineConfiguration savedPipe = null;
                if (this.copyLineNumbers) {
                    savedPipe = new PipelineConfiguration(val.getPipelineConfiguration());
                    LocationCopier copier = new LocationCopier(true);
                    val.getPipelineConfiguration().setComponent(CopyInformee.class.getName(), copier);
                }
                item.copy(val, copyOptions, this.getLocation());
                if (!this.copyLineNumbers) break;
                val.setPipelineConfiguration(savedPipe);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown node kind " + item.getNodeKind());
            }
        }
    }

    public static String computeNewBaseUri(NodeInfo source, String staticBaseURI) {
        String newBaseUri;
        block6: {
            String xmlBase = source.getAttributeValue("http://www.w3.org/XML/1998/namespace", "base");
            if (xmlBase != null) {
                try {
                    URI xmlBaseUri = new URI(xmlBase);
                    if (xmlBaseUri.isAbsolute()) {
                        newBaseUri = xmlBase;
                        break block6;
                    }
                    if (staticBaseURI != null) {
                        URI sbu = new URI(staticBaseURI);
                        URI abs = sbu.resolve(xmlBaseUri);
                        newBaseUri = abs.toString();
                        break block6;
                    }
                    newBaseUri = source.getBaseURI();
                } catch (URISyntaxException err) {
                    newBaseUri = source.getBaseURI();
                }
            } else {
                newBaseUri = source.getBaseURI();
            }
        }
        return newBaseUri;
    }

    static void copyAttribute(NodeInfo source, SimpleType schemaType, int validation, Instruction instruction, Outputter output, XPathContext context, boolean rejectDuplicates) throws XPathException {
        int opt = rejectDuplicates ? 32 : 0;
        CharSequence value = source.getStringValueCS();
        SimpleType annotation = CopyOf.validateAttribute(source, schemaType, validation, context);
        try {
            output.attribute(NameOfNode.makeName(source), annotation, value, instruction.getLocation(), opt);
        } catch (XPathException e) {
            e.maybeSetContext(context);
            e.maybeSetLocation(instruction.getLocation());
            if (instruction.getPackageData().getHostLanguage() == HostLanguage.XQUERY && e.getErrorCodeLocalPart().equals("XTTE0950")) {
                e.setErrorCode("XQTY0086");
            }
            throw e;
        }
    }

    public static SimpleType validateAttribute(NodeInfo source, SimpleType schemaType, int validation, XPathContext context) throws XPathException {
        CharSequence value = source.getStringValueCS();
        SimpleType annotation = BuiltInAtomicType.UNTYPED_ATOMIC;
        if (schemaType != null) {
            if (schemaType.isNamespaceSensitive()) {
                XPathException err = new XPathException("Cannot create a parentless attribute whose type is namespace-sensitive (such as xs:QName)");
                err.setErrorCode("XTTE1545");
                throw err;
            }
            ValidationFailure err = schemaType.validateContent(value, DummyNamespaceResolver.getInstance(), context.getConfiguration().getConversionRules());
            if (err != null) {
                err.setMessage("Attribute being copied does not match the required type. " + err.getMessage());
                err.setErrorCode("XTTE1510");
                throw err.makeException();
            }
            annotation = schemaType;
        } else if (validation == 1 || validation == 2) {
            try {
                annotation = context.getConfiguration().validateAttribute(NameOfNode.makeName(source).getStructuredQName(), value, validation);
            } catch (ValidationException e) {
                XPathException err = XPathException.makeXPathException(e);
                err.setErrorCodeQName(e.getErrorCodeQName());
                err.setIsTypeError(true);
                throw err;
            }
        } else if (validation == 3 && !(annotation = (SimpleType)source.getSchemaType()).equals(BuiltInAtomicType.UNTYPED_ATOMIC) && annotation.isNamespaceSensitive()) {
            XPathException err = new XPathException("Cannot preserve type annotation when copying an attribute with namespace-sensitive content");
            err.setErrorCode(context.getController().getExecutable().getHostLanguage() == HostLanguage.XSLT ? "XTTE0950" : "XQTY0086");
            err.setIsTypeError(true);
            throw err;
        }
        return annotation;
    }

    private boolean mustPush() {
        return this.schemaType != null || this.validation == 2 || this.validation == 1 || this.copyForUpdate;
    }

    @Override
    public SequenceIterator iterate(XPathContext context) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        if (this.schemaType == null && !this.copyForUpdate) {
            if (this.validation == 3) {
                ItemMappingFunction copier = item -> {
                    if (item instanceof NodeInfo) {
                        if (((NodeInfo)item).getTreeInfo().isTyped()) {
                            if (!this.copyNamespaces && ((NodeInfo)item).getNodeKind() == 1) {
                                Sink sink = new Sink(controller.makePipelineConfiguration());
                                ((NodeInfo)item).copy(sink, 4, this.getLocation());
                            }
                            if (((NodeInfo)item).getNodeKind() == 2 && ((SimpleType)((NodeInfo)item).getSchemaType()).isNamespaceSensitive()) {
                                throw new XPathException("Cannot copy an attribute with namespace-sensitive content except as part of its containing element", "XTTE0950");
                            }
                        }
                        VirtualCopy vc = VirtualCopy.makeVirtualCopy((NodeInfo)item);
                        vc.setDropNamespaces(!this.copyNamespaces);
                        vc.getTreeInfo().setCopyAccumulators(this.copyAccumulators);
                        if (((NodeInfo)item).getNodeKind() == 1) {
                            vc.setSystemId(CopyOf.computeNewBaseUri((NodeInfo)item, this.getStaticBaseURIString()));
                        }
                        return vc;
                    }
                    return item;
                };
                return new ItemMappingIterator(this.getSelect().iterate(context), copier, true);
            }
            if (this.validation == 4) {
                ItemMappingFunction copier = item -> {
                    if (!(item instanceof NodeInfo)) {
                        return item;
                    }
                    VirtualCopy vc = VirtualUntypedCopy.makeVirtualUntypedTree((NodeInfo)item, (NodeInfo)item);
                    vc.getTreeInfo().setCopyAccumulators(this.copyAccumulators);
                    vc.setDropNamespaces(!this.copyNamespaces);
                    if (((NodeInfo)item).getNodeKind() == 1) {
                        vc.setSystemId(CopyOf.computeNewBaseUri((NodeInfo)item, this.getStaticBaseURIString()));
                    }
                    return vc;
                };
                return new ItemMappingIterator(this.getSelect().iterate(context), copier, true);
            }
        }
        PipelineConfiguration pipe = controller.makePipelineConfiguration();
        pipe.setXPathContext(context);
        SequenceCollector out = new SequenceCollector(pipe);
        if (this.copyForUpdate) {
            out.setTreeModel(TreeModel.LINKED_TREE);
        }
        pipe.setHostLanguage(this.getPackageData().getHostLanguage());
        try {
            this.process(new ComplexContentOutputter(out), context);
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            err.maybeSetContext(context);
            throw err;
        }
        return out.getSequence().iterate();
    }
}

