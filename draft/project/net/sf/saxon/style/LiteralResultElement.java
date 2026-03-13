/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.Arrays;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.instruct.FixedElement;
import net.sf.saxon.expr.instruct.UseAttributeSet;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.CodedName;
import net.sf.saxon.om.EmptyAttributeMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StyleNodeFactory;
import net.sf.saxon.style.XSLStylesheet;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.linked.DocumentImpl;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.Untyped;
import net.sf.saxon.z.IntHashSet;

public class LiteralResultElement
extends StyleElement {
    private NodeName resultNodeName;
    private NodeName[] attributeNames;
    private Expression[] attributeValues;
    private int numberOfAttributes;
    private boolean toplevel;
    private NamespaceMap retainedNamespaces = NamespaceMap.emptyMap();
    private StructuredQName[] attributeSets;
    private SchemaType schemaType = null;
    private int validation = 4;
    private boolean inheritNamespaces = true;
    private static IntHashSet STANDARD_ATTRIBUTES = IntHashSet.of(236, 228, 229, 230, 233, 231, 232, 239, 240, 235, 237, 238);

    @Override
    public boolean mayContainSequenceConstructor() {
        return true;
    }

    @Override
    public boolean isInstruction() {
        return true;
    }

    @Override
    public void prepareAttributes() {
        AttributeMap atts = this.attributes();
        int num = atts.size();
        if (num == 0) {
            this.numberOfAttributes = 0;
        } else {
            this.attributeNames = new NodeName[num];
            this.attributeValues = new Expression[num];
            this.numberOfAttributes = 0;
            for (AttributeInfo att : atts) {
                Expression exp;
                NodeName name = att.getNodeName();
                int fp = name.getFingerprint();
                String attURI = name.getURI();
                if (attURI.equals("http://www.w3.org/1999/XSL/Transform")) {
                    if (STANDARD_ATTRIBUTES.contains(fp)) continue;
                    if (fp == 234) {
                        this.inheritNamespaces = this.processBooleanAttribute("xsl:inherit-namespaces", att.getValue());
                        continue;
                    }
                    if (this.forwardsCompatibleModeIsEnabled()) continue;
                    this.compileError("Unknown XSLT attribute " + Err.wrap(name.getDisplayName(), 2), "XTSE0805");
                    continue;
                }
                this.attributeNames[this.numberOfAttributes] = name;
                this.attributeValues[this.numberOfAttributes] = exp = this.makeAttributeValueTemplate(att.getValue(), att);
                ++this.numberOfAttributes;
            }
            if (this.numberOfAttributes < this.attributeNames.length) {
                this.attributeNames = Arrays.copyOf(this.attributeNames, this.numberOfAttributes);
                this.attributeValues = Arrays.copyOf(this.attributeValues, this.numberOfAttributes);
            }
        }
        this.resultNodeName = this.getNodeName();
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.toplevel = this.getParent() instanceof XSLStylesheet;
        this.resultNodeName = this.getNodeName();
        String elementURI = this.getURI();
        if (this.toplevel) {
            if (elementURI.isEmpty()) {
                this.compileError("Top level elements must have a non-null namespace URI", "XTSE0130");
            }
        } else {
            String validate;
            String useAttSets;
            this.retainedNamespaces = this.getAllNamespaces();
            PrincipalStylesheetModule sheet = this.getPrincipalStylesheetModule();
            if (sheet.hasNamespaceAliases()) {
                NamespaceMap aliasedNamespaces = this.retainedNamespaces;
                for (NamespaceBinding nb : this.retainedNamespaces) {
                    String suri = nb.getURI();
                    NamespaceBinding ncode = sheet.getNamespaceAlias(suri);
                    if (ncode == null || ncode.getURI().equals(suri)) continue;
                    aliasedNamespaces = aliasedNamespaces.remove(nb.getPrefix());
                    if (ncode.getURI().isEmpty()) continue;
                    aliasedNamespaces = aliasedNamespaces.put(ncode.getPrefix(), ncode.getURI());
                }
                this.retainedNamespaces = aliasedNamespaces;
                NamespaceBinding elementAlias = sheet.getNamespaceAlias(elementURI);
                if (elementAlias != null && !elementAlias.getURI().equals(elementURI)) {
                    this.resultNodeName = new FingerprintedQName(elementAlias.getPrefix(), elementAlias.getURI(), this.getLocalPart());
                }
            }
            if ((useAttSets = this.getAttributeValue("http://www.w3.org/1999/XSL/Transform", "use-attribute-sets")) != null) {
                this.attributeSets = this.getUsedAttributeSets(useAttSets);
            }
            this.validation = this.getDefaultValidation();
            String type = this.getAttributeValue("http://www.w3.org/1999/XSL/Transform", "type");
            if (type != null) {
                if (!this.isSchemaAware()) {
                    this.compileError("The xsl:type attribute is available only with a schema-aware XSLT processor", "XTSE1660");
                }
                this.schemaType = this.getSchemaType(type);
                this.validation = 8;
            }
            if ((validate = this.getAttributeValue("http://www.w3.org/1999/XSL/Transform", "validation")) != null) {
                this.validation = this.validateValidationAttribute(validate);
                if (this.schemaType != null) {
                    this.compileError("The attributes xsl:type and xsl:validation are mutually exclusive", "XTSE1505");
                }
            }
            if (this.numberOfAttributes > 0) {
                boolean changed = false;
                for (int i = 0; i < this.numberOfAttributes; ++i) {
                    NamespaceBinding newBinding;
                    NodeName anameCode;
                    NodeName alias = anameCode = this.attributeNames[i];
                    String attURI = anameCode.getURI();
                    if (!attURI.isEmpty() && (newBinding = sheet.getNamespaceAlias(attURI)) != null && !newBinding.getURI().equals(attURI)) {
                        alias = new FingerprintedQName(newBinding.getPrefix(), newBinding.getURI(), anameCode.getLocalPart());
                        changed = true;
                    }
                    this.attributeNames[i] = alias;
                    this.attributeValues[i] = this.typeCheck(alias.getDisplayName(), this.attributeValues[i]);
                }
                if (changed && this.numberOfAttributes > 1) {
                    IntHashSet names = new IntHashSet(this.numberOfAttributes);
                    for (int i = 0; i < this.numberOfAttributes; ++i) {
                        int fp = this.attributeNames[i].obtainFingerprint(this.getNamePool());
                        boolean absent = names.add(fp);
                        if (absent) continue;
                        this.compileError("As a result of namespace aliasing, two attributes have the same expanded name", "XTSE0813");
                    }
                }
            }
            NamespaceMap afterExclusions = this.retainedNamespaces;
            for (NamespaceBinding nb : this.retainedNamespaces) {
                String uri = nb.getURI();
                if (!this.isExcludedNamespace(uri) || sheet.isAliasResultNamespace(uri)) continue;
                afterExclusions = afterExclusions.remove(nb.getPrefix());
            }
            this.retainedNamespaces = afterExclusions;
        }
    }

    @Override
    protected void validateChildren(ComponentDeclaration decl, boolean excludeStylesheet) throws XPathException {
        if (!this.toplevel) {
            super.validateChildren(decl, excludeStylesheet);
        }
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        if (this.toplevel) {
            return null;
        }
        FixedElement inst = new FixedElement(this.resultNodeName, this.retainedNamespaces, this.inheritNamespaces, true, this.schemaType, this.validation);
        Expression content = this.compileSequenceConstructor(exec, decl, true);
        if (this.numberOfAttributes > 0) {
            for (int i = this.attributeNames.length - 1; i >= 0; --i) {
                FixedAttribute att = new FixedAttribute(this.attributeNames[i], 4, null);
                att.setRetainedStaticContext(this.makeRetainedStaticContext());
                att.setSelect(this.attributeValues[i]);
                att.setLocation(this.allocateLocation());
                FixedAttribute exp = att;
                if (content == null) {
                    content = exp;
                    continue;
                }
                content = Block.makeBlock(exp, content);
                content.setLocation(this.allocateLocation());
            }
        }
        if (this.attributeSets != null) {
            Expression use = UseAttributeSet.makeUseAttributeSets(this.attributeSets, this);
            if (content == null) {
                content = use;
            } else {
                content = Block.makeBlock(use, content);
                content.setLocation(this.allocateLocation());
            }
        }
        if (content == null) {
            content = Literal.makeEmptySequence();
        }
        inst.setContentExpression(content);
        inst.setRetainedStaticContext(this.makeRetainedStaticContext());
        inst.setLocation(this.allocateLocation());
        return inst;
    }

    public DocumentImpl makeStylesheet(boolean topLevel) throws XPathException {
        StyleNodeFactory nodeFactory = this.getCompilation().getStyleNodeFactory(topLevel);
        if (!this.isInScopeNamespace("http://www.w3.org/1999/XSL/Transform")) {
            String message = this.getLocalPart().equals("stylesheet") || this.getLocalPart().equals("transform") ? "Namespace for stylesheet element should be http://www.w3.org/1999/XSL/Transform" : "The supplied file does not appear to be a stylesheet";
            XPathException err = new XPathException(message);
            err.setLocation(this.allocateLocation());
            err.setErrorCode("XTSE0150");
            err.setIsStaticError(true);
            this.compileError(err);
            throw err;
        }
        String version = this.getAttributeValue("http://www.w3.org/1999/XSL/Transform", "version");
        if (version == null) {
            XPathException err = new XPathException("Simplified stylesheet: xsl:version attribute is missing");
            err.setErrorCode("XTSE0150");
            err.setIsStaticError(true);
            err.setLocation(this.allocateLocation());
            this.compileError(err);
            throw err;
        }
        try {
            DocumentImpl oldRoot = (DocumentImpl)this.getRoot();
            LinkedTreeBuilder builder = new LinkedTreeBuilder(this.getConfiguration().makePipelineConfiguration());
            builder.setNodeFactory(nodeFactory);
            builder.setSystemId(this.getSystemId());
            builder.open();
            builder.startDocument(0);
            Loc loc = Loc.NONE;
            NamespaceMap map = this.getAllNamespaces().put("xsl", "http://www.w3.org/1999/XSL/Transform");
            AttributeMap atts = EmptyAttributeMap.getInstance();
            atts = atts.put(new AttributeInfo(new NoNamespaceName("version"), BuiltInAtomicType.UNTYPED_ATOMIC, version, loc, 0));
            int st = 199;
            builder.startElement(new CodedName(st, "xsl", this.getNamePool()), Untyped.getInstance(), atts, map, loc, 0);
            atts = EmptyAttributeMap.getInstance();
            atts = atts.put(new AttributeInfo(new NoNamespaceName("match"), BuiltInAtomicType.UNTYPED_ATOMIC, "/", loc, 0));
            int te = 200;
            builder.startElement(new CodedName(te, "xsl", this.getNamePool()), Untyped.getInstance(), atts, map, loc, 0);
            builder.graftElement(this);
            builder.endElement();
            builder.endElement();
            builder.endDocument();
            builder.close();
            DocumentImpl newRoot = (DocumentImpl)builder.getCurrentRoot();
            newRoot.graftLocationMap(oldRoot);
            return newRoot;
        } catch (XPathException err) {
            err.setLocation(this.allocateLocation());
            throw err;
        }
    }

    @Override
    public StructuredQName getObjectName() {
        return new StructuredQName(this.getPrefix(), this.getURI(), this.getLocalPart());
    }
}

