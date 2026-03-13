/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UseAttributeSet;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.StylesheetComponent;
import net.sf.saxon.style.XSLAttribute;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.value.Whitespace;

public class XSLAttributeSet
extends StyleElement
implements StylesheetComponent {
    private String nameAtt;
    private String useAtt;
    private String visibilityAtt;
    private SlotManager stackFrameMap;
    private List<ComponentDeclaration> attributeSetElements = new ArrayList<ComponentDeclaration>();
    private StructuredQName[] useAttributeSetNames;
    private List<Expression> containedInstructions = new ArrayList<Expression>();
    private boolean validated = false;
    private Visibility visibility;
    private boolean streamable = false;

    @Override
    public AttributeSet getActor() {
        return (AttributeSet)this.getPrincipalStylesheetModule().getStylesheetPackage().getComponent(new SymbolicName(136, this.getObjectName())).getActor();
    }

    @Override
    public SymbolicName getSymbolicName() {
        return new SymbolicName(136, this.getObjectName());
    }

    @Override
    public void checkCompatibility(Component component) throws XPathException {
        if (((AttributeSet)component.getActor()).isDeclaredStreamable() && !this.isDeclaredStreamable()) {
            this.compileError("The overridden attribute set is declared streamable, so the overriding attribute set must also be declared streamable");
        }
    }

    @Override
    public boolean isDeclaration() {
        return true;
    }

    public StructuredQName getAttributeSetName() {
        return this.getObjectName();
    }

    public boolean isDeclaredStreamable() {
        return this.streamable;
    }

    @Override
    public void prepareAttributes() {
        this.useAtt = null;
        String streamableAtt = null;
        block12: for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            String value = att.getValue();
            switch (f) {
                case "name": {
                    this.nameAtt = Whitespace.trim(value);
                    continue block12;
                }
                case "use-attribute-sets": {
                    this.useAtt = value;
                    continue block12;
                }
                case "streamable": {
                    streamableAtt = value;
                    continue block12;
                }
                case "visibility": {
                    this.visibilityAtt = Whitespace.trim(value);
                    continue block12;
                }
            }
            this.checkUnknownAttribute(attName);
        }
        if (this.nameAtt == null) {
            this.reportAbsence("name");
            this.setObjectName(new StructuredQName("", "", "attribute-set-error-name"));
            return;
        }
        this.visibility = this.visibilityAtt == null ? Visibility.PRIVATE : this.interpretVisibilityValue(this.visibilityAtt, "");
        if (streamableAtt != null) {
            this.streamable = this.processStreamableAtt(streamableAtt);
        }
        this.setObjectName(this.makeQName(this.nameAtt, null, "name"));
    }

    @Override
    public StructuredQName getObjectName() {
        StructuredQName o = super.getObjectName();
        if (o == null) {
            this.prepareAttributes();
            o = this.getObjectName();
        }
        return o;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.validated) {
            return;
        }
        this.checkTopLevel("XTSE0010", true);
        this.stackFrameMap = this.getConfiguration().makeSlotManager();
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLAttribute) {
                if (this.visibility != Visibility.ABSTRACT) continue;
                this.compileError("An abstract attribute-set must contain no xsl:attribute instructions");
                continue;
            }
            this.compileError("Only xsl:attribute is allowed within xsl:attribute-set", "XTSE0010");
        }
        if (this.useAtt != null) {
            if (this.visibility == Visibility.ABSTRACT) {
                this.compileError("An abstract attribute-set must have no @use-attribute-sets attribute");
            }
            this.useAttributeSetNames = this.getUsedAttributeSets(this.useAtt);
        }
        this.validated = true;
    }

    public StructuredQName[] getUseAttributeSetNames() {
        return this.useAttributeSetNames;
    }

    @Override
    public void index(ComponentDeclaration decl, PrincipalStylesheetModule top) throws XPathException {
        top.indexAttributeSet(decl);
    }

    public void checkCircularity(XSLAttributeSet origin) throws XPathException {
        if (this == origin) {
            this.compileError("The definition of the attribute set is circular", "XTSE0720");
        } else {
            if (!this.validated) {
                return;
            }
            if (this.attributeSetElements != null) {
                for (ComponentDeclaration attributeSetElement : this.attributeSetElements) {
                    XSLAttributeSet element = (XSLAttributeSet)attributeSetElement.getSourceElement();
                    element.checkCircularity(origin);
                    if (!this.streamable || element.streamable) continue;
                    this.compileError("Attribute-set is declared streamable but references a non-streamable attribute set " + element.getAttributeSetName().getDisplayName(), "XTSE3430");
                }
            }
        }
    }

    public List<Expression> getContainedInstructions() {
        return this.containedInstructions;
    }

    @Override
    public SlotManager getSlotManager() {
        return this.stackFrameMap;
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        XSLAttribute node;
        if (this.isActionCompleted(2)) {
            return;
        }
        if (this.useAtt != null) {
            List<UseAttributeSet> invocations = UseAttributeSet.makeUseAttributeSetInstructions(this.useAttributeSetNames, this);
            if (!invocations.isEmpty()) {
                this.containedInstructions.add(UseAttributeSet.makeCompositeExpression(invocations));
            }
            for (StructuredQName name : this.useAttributeSetNames) {
                this.getPrincipalStylesheetModule().getAttributeSets(name, this.attributeSetElements);
            }
            for (ComponentDeclaration attributeSetElement : this.attributeSetElements) {
                ((XSLAttributeSet)attributeSetElement.getSourceElement()).checkCircularity(this);
            }
            if (this.streamable) {
                for (ComponentDeclaration attributeSetElement : this.attributeSetElements) {
                    if (((XSLAttributeSet)attributeSetElement.getSourceElement()).streamable) continue;
                    this.compileError("Attribute set is declared streamable, but references an attribute set that is not declared streamable", "XTSE0730");
                }
            }
        }
        AxisIterator iter = this.iterateAxis(3, NodeKindTest.ELEMENT);
        while ((node = (XSLAttribute)iter.next()) != null) {
            Expression inst = node.compile(compilation, decl);
            inst.setRetainedStaticContext(this.makeRetainedStaticContext());
            inst = inst.simplify();
            if (compilation.getCompilerInfo().isCompileWithTracing()) {
                inst = XSLAttributeSet.makeTraceInstruction(this, inst);
            }
            this.containedInstructions.add(inst);
        }
        this.setActionCompleted(2);
    }

    @Override
    public void optimize(ComponentDeclaration declaration) throws XPathException {
    }

    @Override
    public void generateByteCode(Optimizer opt) {
    }
}

