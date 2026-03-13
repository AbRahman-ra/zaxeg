/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.BindingReference;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.instruct.DocumentInstr;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLGlobalParam;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class SourceBinding {
    private StyleElement sourceElement;
    private StructuredQName name;
    private Expression select = null;
    private SequenceType declaredType = null;
    private SequenceType inferredType = null;
    protected SlotManager slotManager = null;
    private Visibility visibility;
    private GroundedValue constantValue = null;
    private EnumSet<BindingProperty> properties = EnumSet.noneOf(BindingProperty.class);
    private List<BindingReference> references = new ArrayList<BindingReference>(10);

    public SourceBinding(StyleElement sourceElement) {
        this.sourceElement = sourceElement;
    }

    public void prepareAttributes(EnumSet<BindingProperty> permittedAttributes) {
        AttributeMap atts = this.sourceElement.attributes();
        AttributeInfo selectAtt = null;
        String asAtt = null;
        String extraAsAtt = null;
        String requiredAtt = null;
        String tunnelAtt = null;
        String assignableAtt = null;
        String staticAtt = null;
        String visibilityAtt = null;
        for (AttributeInfo att : atts) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            if (f.equals("name")) {
                if (this.name != null && !this.name.equals(this.errorName())) continue;
                this.processVariableName(att.getValue());
                continue;
            }
            if (f.equals("select")) {
                if (permittedAttributes.contains((Object)BindingProperty.SELECT)) {
                    selectAtt = att;
                    continue;
                }
                this.sourceElement.compileErrorInAttribute("The select attribute is not permitted on a function parameter", "XTSE0760", "select");
                continue;
            }
            if (f.equals("as") && permittedAttributes.contains((Object)BindingProperty.AS)) {
                asAtt = att.getValue();
                continue;
            }
            if (f.equals("required") && permittedAttributes.contains((Object)BindingProperty.REQUIRED)) {
                requiredAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if (f.equals("tunnel")) {
                tunnelAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if (f.equals("static") && permittedAttributes.contains((Object)BindingProperty.STATIC)) {
                staticAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if (f.equals("visibility") && permittedAttributes.contains((Object)BindingProperty.VISIBILITY)) {
                visibilityAtt = Whitespace.trim(att.getValue());
                continue;
            }
            if ("http://saxon.sf.net/".equals(attName.getURI())) {
                if (!this.sourceElement.isExtensionAttributeAllowed(attName.getDisplayName())) continue;
                if (attName.getLocalPart().equals("assignable") && permittedAttributes.contains((Object)BindingProperty.ASSIGNABLE)) {
                    assignableAtt = Whitespace.trim(att.getValue());
                    continue;
                }
                if (attName.getLocalPart().equals("as")) {
                    extraAsAtt = att.getValue();
                    continue;
                }
                this.sourceElement.checkUnknownAttribute(att.getNodeName());
                continue;
            }
            this.sourceElement.checkUnknownAttribute(att.getNodeName());
        }
        if (this.name == null) {
            this.sourceElement.reportAbsence("name");
            this.name = this.errorName();
        }
        if (selectAtt != null) {
            this.select = this.sourceElement.makeExpression(selectAtt.getValue(), selectAtt);
        }
        if (requiredAtt != null) {
            boolean required = this.sourceElement.processBooleanAttribute("required", requiredAtt);
            this.setProperty(BindingProperty.REQUIRED, required);
            if (required && this.select != null) {
                this.sourceElement.compileError("xsl:param: cannot supply a default value when required='yes'");
            }
        }
        if (tunnelAtt != null) {
            boolean tunnel = this.sourceElement.processBooleanAttribute("tunnel", tunnelAtt);
            if (tunnel && !permittedAttributes.contains((Object)BindingProperty.TUNNEL)) {
                this.sourceElement.compileErrorInAttribute("The only permitted value of the 'tunnel' attribute is 'no'", "XTSE0020", "tunnel");
            }
            this.setProperty(BindingProperty.TUNNEL, tunnel);
        }
        if (assignableAtt != null) {
            boolean assignable = this.sourceElement.processBooleanAttribute("saxon:assignable", assignableAtt);
            this.setProperty(BindingProperty.ASSIGNABLE, assignable);
        }
        if (staticAtt != null) {
            boolean statick = this.sourceElement.processBooleanAttribute("static", staticAtt);
            this.setProperty(BindingProperty.STATIC, statick);
            if (statick) {
                this.setProperty(BindingProperty.DISALLOWS_CONTENT, true);
            }
            if (statick && !this.hasProperty(BindingProperty.GLOBAL)) {
                this.sourceElement.compileErrorInAttribute("Only global declarations can be static", "XTSE0020", "static");
            }
        }
        this.declaredType = this.combineTypeDeclarations(asAtt, extraAsAtt);
        if (visibilityAtt != null) {
            if (this.hasProperty(BindingProperty.PARAM)) {
                this.sourceElement.compileErrorInAttribute("The visibility attribute is not allowed on xsl:param", "XTSE0020", "visibility");
            } else {
                this.visibility = this.sourceElement.interpretVisibilityValue(visibilityAtt, "");
            }
            if (!this.hasProperty(BindingProperty.GLOBAL)) {
                this.sourceElement.compileErrorInAttribute("The visibility attribute is allowed only on global declarations", "XTSE0020", "visibility");
            }
        }
        if (this.hasProperty(BindingProperty.STATIC) && this.visibility != Visibility.PRIVATE && visibilityAtt != null) {
            this.sourceElement.compileErrorInAttribute("A static variable or parameter must be private", "XTSE0020", "static");
        }
    }

    public void prepareTemplateSignatureAttributes() {
        AttributeMap atts = this.sourceElement.attributes();
        String asAtt = null;
        String extraAsAtt = null;
        for (AttributeInfo att : atts) {
            NodeName attName = att.getNodeName();
            String f = attName.getDisplayName();
            if (f.equals("name")) {
                if (this.name != null && !this.name.equals(this.errorName())) continue;
                this.processVariableName(att.getValue());
                continue;
            }
            if (f.equals("as")) {
                asAtt = att.getValue();
                continue;
            }
            if (f.equals("required")) {
                String requiredAtt = Whitespace.trim(att.getValue());
                boolean required = this.sourceElement.processBooleanAttribute("required", requiredAtt);
                this.setProperty(BindingProperty.REQUIRED, required);
                continue;
            }
            if (f.equals("tunnel")) {
                String tunnelAtt = Whitespace.trim(att.getValue());
                boolean tunnel = this.sourceElement.processBooleanAttribute("tunnel", tunnelAtt);
                this.setProperty(BindingProperty.TUNNEL, tunnel);
                continue;
            }
            if (!"http://saxon.sf.net/".equals(attName.getURI()) || !attName.getLocalPart().equals("as")) continue;
            extraAsAtt = att.getValue();
        }
        if (this.name == null) {
            this.sourceElement.reportAbsence("name");
            this.name = this.errorName();
        }
        this.declaredType = this.combineTypeDeclarations(asAtt, extraAsAtt);
    }

    private SequenceType combineTypeDeclarations(String asAtt, String extraAsAtt) {
        SequenceType declaredType = null;
        if (asAtt != null) {
            try {
                declaredType = this.sourceElement.makeSequenceType(asAtt);
            } catch (XPathException e) {
                this.sourceElement.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "as");
            }
        }
        if (extraAsAtt != null) {
            SequenceType extraResultType = null;
            try {
                extraResultType = this.sourceElement.makeExtendedSequenceType(extraAsAtt);
            } catch (XPathException e) {
                this.sourceElement.compileErrorInAttribute(e.getMessage(), e.getErrorCodeLocalPart(), "saxon:as");
                extraResultType = SequenceType.ANY_SEQUENCE;
            }
            if (asAtt != null) {
                Affinity rel = this.sourceElement.getConfiguration().getTypeHierarchy().sequenceTypeRelationship(extraResultType, declaredType);
                if (rel == Affinity.SAME_TYPE || rel == Affinity.SUBSUMED_BY) {
                    declaredType = extraResultType;
                } else {
                    this.sourceElement.compileErrorInAttribute("When both are present, @saxon:as must be a subtype of @as", "SXER7TBA", "as");
                }
            } else {
                declaredType = extraResultType;
            }
        }
        return declaredType;
    }

    public StyleElement getSourceElement() {
        return this.sourceElement;
    }

    public void setVariableQName(StructuredQName name) {
        this.name = name;
    }

    public void setDeclaredType(SequenceType declaredType) {
        this.declaredType = declaredType;
    }

    private void processVariableName(String nameAttribute) {
        if (nameAttribute != null) {
            if (nameAttribute.startsWith("$")) {
                this.sourceElement.compileErrorInAttribute("Invalid variable name (no '$' sign needed)", "XTSE0020", "name");
                nameAttribute = nameAttribute.substring(1);
            }
            this.name = this.sourceElement.makeQName(nameAttribute, null, "name");
        }
    }

    private StructuredQName errorName() {
        return new StructuredQName("saxon", "http://saxon.sf.net/", "error-variable-name");
    }

    public void validate() throws XPathException {
        if (this.select != null && this.sourceElement.hasChildNodes()) {
            this.sourceElement.compileError("An " + this.sourceElement.getDisplayName() + " element with a select attribute must be empty", "XTSE0620");
        }
        if (this.hasProperty(BindingProperty.DISALLOWS_CONTENT) && this.sourceElement.hasChildNodes()) {
            if (this.isStatic()) {
                this.sourceElement.compileError("A static variable or parameter must have no content", "XTSE0010");
            } else {
                this.sourceElement.compileError("Within xsl:function, an xsl:param element must have no content", "XTSE0620");
            }
        }
        if (this.visibility == Visibility.ABSTRACT && (this.select != null || this.sourceElement.hasChildNodes())) {
            this.sourceElement.compileError("An abstract variable must have no select attribute and no content", "XTSE0620");
        }
    }

    public void postValidate() throws XPathException {
        AxisIterator kids;
        NodeInfo first;
        this.checkAgainstRequiredType(this.declaredType);
        if (this.select == null && !this.hasProperty(BindingProperty.DISALLOWS_CONTENT) && this.visibility != Visibility.ABSTRACT && (first = (kids = this.sourceElement.iterateAxis(3)).next()) == null) {
            if (this.declaredType == null) {
                this.select = new StringLiteral(StringValue.EMPTY_STRING);
                this.select.setRetainedStaticContext(this.sourceElement.makeRetainedStaticContext());
            } else if (this.sourceElement instanceof XSLLocalParam || this.sourceElement instanceof XSLGlobalParam) {
                if (!this.hasProperty(BindingProperty.REQUIRED)) {
                    if (Cardinality.allowsZero(this.declaredType.getCardinality())) {
                        this.select = Literal.makeEmptySequence();
                        this.select.setRetainedStaticContext(this.sourceElement.makeRetainedStaticContext());
                    } else {
                        this.setProperty(BindingProperty.IMPLICITLY_REQUIRED, true);
                    }
                }
            } else if (Cardinality.allowsZero(this.declaredType.getCardinality())) {
                this.select = Literal.makeEmptySequence();
                this.select.setRetainedStaticContext(this.sourceElement.makeRetainedStaticContext());
            } else {
                this.sourceElement.compileError("The implicit value () is not valid for the declared type", "XTTE0570");
            }
        }
        this.select = this.sourceElement.typeCheck("select", this.select);
    }

    public boolean isStatic() {
        return this.hasProperty(BindingProperty.STATIC);
    }

    public void checkAgainstRequiredType(SequenceType required) {
        if (this.visibility != Visibility.ABSTRACT) {
            try {
                if (required != null && this.select != null) {
                    int category = 3;
                    String errorCode = "XTTE0570";
                    if (this.sourceElement instanceof XSLLocalParam) {
                        category = 8;
                        errorCode = "XTTE0600";
                    } else if (this.sourceElement instanceof XSLWithParam || this.sourceElement instanceof XSLGlobalParam) {
                        category = 8;
                        errorCode = "XTTE0590";
                    }
                    RoleDiagnostic role = new RoleDiagnostic(category, this.name.getDisplayName(), 0);
                    role.setErrorCode(errorCode);
                    this.select = this.sourceElement.getConfiguration().getTypeChecker(false).staticTypeCheck(this.select, required, role, this.sourceElement.makeExpressionVisitor());
                }
            } catch (XPathException err) {
                err.setLocator(this.sourceElement);
                this.sourceElement.compileError(err);
                this.select = new ErrorExpression(new XmlProcessingException(err));
            }
        }
    }

    public StructuredQName getVariableQName() {
        if (this.name == null) {
            this.processVariableName(this.sourceElement.getAttributeValue("", "name"));
        }
        return this.name;
    }

    public void setProperty(BindingProperty prop, boolean flag) {
        if (flag) {
            this.properties.add(prop);
        } else {
            this.properties.remove((Object)prop);
        }
    }

    public boolean hasProperty(BindingProperty prop) {
        return this.properties.contains((Object)prop);
    }

    public List<BindingReference> getReferences() {
        return this.references;
    }

    public SlotManager getSlotManager() {
        return this.slotManager;
    }

    public void handleSequenceConstructor(Compilation compilation, ComponentDeclaration decl) throws XPathException {
        if (this.sourceElement.hasChildNodes()) {
            if (this.declaredType == null) {
                Expression b = this.sourceElement.compileSequenceConstructor(compilation, decl, true);
                if (b == null) {
                    b = Literal.makeEmptySequence();
                }
                boolean textonly = UType.TEXT.subsumes(b.getItemType().getUType());
                String constant = null;
                if (textonly && b instanceof ValueOf && ((ValueOf)b).getSelect() instanceof StringLiteral) {
                    constant = ((StringLiteral)((ValueOf)b).getSelect()).getStringValue();
                }
                DocumentInstr doc = new DocumentInstr(textonly, constant);
                doc.setContentExpression(b);
                doc.setRetainedStaticContext(this.sourceElement.makeRetainedStaticContext());
                this.select = doc;
            } else {
                this.select = this.sourceElement.compileSequenceConstructor(compilation, decl, true);
                if (this.select == null) {
                    this.select = Literal.makeEmptySequence();
                }
                try {
                    RoleDiagnostic role = new RoleDiagnostic(3, this.name.getDisplayName(), 0);
                    role.setErrorCode("XTTE0570");
                    this.select = this.select.simplify();
                    this.select = this.sourceElement.getConfiguration().getTypeChecker(false).staticTypeCheck(this.select, this.declaredType, role, this.sourceElement.makeExpressionVisitor());
                } catch (XPathException err) {
                    err.setLocator(this.sourceElement);
                    XmlProcessingException error = new XmlProcessingException(err);
                    this.sourceElement.compileError(error);
                    this.select = new ErrorExpression(error);
                }
            }
        }
    }

    public SequenceType getDeclaredType() {
        if (this.declaredType == null) {
            String asAtt = this.sourceElement.getAttributeValue("", "as");
            if (asAtt == null) {
                return null;
            }
            try {
                this.declaredType = this.sourceElement.makeSequenceType(asAtt);
            } catch (XPathException xPathException) {
                // empty catch block
            }
        }
        return this.declaredType;
    }

    public Expression getSelectExpression() {
        return this.select;
    }

    public SequenceType getInferredType(boolean useContentRules) {
        if (this.inferredType != null) {
            return this.inferredType;
        }
        Visibility visibility = this.sourceElement.getVisibility();
        if (this.hasProperty(BindingProperty.PARAM) || this.hasProperty(BindingProperty.ASSIGNABLE) || visibility != Visibility.PRIVATE && visibility != Visibility.FINAL) {
            SequenceType declared = this.getDeclaredType();
            this.inferredType = declared == null ? SequenceType.ANY_SEQUENCE : declared;
            return this.inferredType;
        }
        if (this.select != null) {
            TypeHierarchy th = this.sourceElement.getConfiguration().getTypeHierarchy();
            if (Literal.isEmptySequence(this.select)) {
                this.inferredType = this.declaredType == null ? SequenceType.ANY_SEQUENCE : this.declaredType;
                return this.inferredType;
            }
            ItemType actual = this.select.getItemType();
            int card = this.select.getCardinality();
            if (this.declaredType != null) {
                if (!th.isSubType(actual, this.declaredType.getPrimaryType())) {
                    actual = this.declaredType.getPrimaryType();
                }
                if (!Cardinality.subsumes(this.declaredType.getCardinality(), card)) {
                    card = this.declaredType.getCardinality();
                }
            }
            this.inferredType = SequenceType.makeSequenceType(actual, card);
            return this.inferredType;
        }
        if (useContentRules) {
            if (this.sourceElement.hasChildNodes()) {
                if (this.declaredType == null) {
                    return SequenceType.makeSequenceType(NodeKindTest.DOCUMENT, 16384);
                }
                return this.declaredType;
            }
            if (this.declaredType == null) {
                return SequenceType.SINGLE_STRING;
            }
            return this.declaredType;
        }
        return this.declaredType;
    }

    public void registerReference(BindingReference ref) {
        this.references.add(ref);
    }

    public GroundedValue getConstantValue() {
        if (this.constantValue == null) {
            Affinity relation;
            SequenceType type = this.getInferredType(true);
            TypeHierarchy th = this.sourceElement.getConfiguration().getTypeHierarchy();
            if (!(this.hasProperty(BindingProperty.ASSIGNABLE) || this.hasProperty(BindingProperty.PARAM) || this.visibility == Visibility.PUBLIC || this.visibility == Visibility.ABSTRACT || !(this.select instanceof Literal) || (relation = th.relationship(this.select.getItemType(), type.getPrimaryType())) != Affinity.SAME_TYPE && relation != Affinity.SUBSUMED_BY)) {
                this.constantValue = ((Literal)this.select).getValue();
            }
        }
        return this.constantValue;
    }

    public void fixupReferences(GlobalVariable compiledGlobalVariable) {
        SequenceType type = this.getInferredType(true);
        TypeHierarchy th = this.sourceElement.getConfiguration().getTypeHierarchy();
        Object constantValue = null;
        int properties = 0;
        if (!this.hasProperty(BindingProperty.ASSIGNABLE) && !this.hasProperty(BindingProperty.PARAM) && this.visibility != Visibility.PUBLIC && this.visibility != Visibility.ABSTRACT && this.select != null) {
            properties = this.select.getSpecialProperties();
        }
        for (BindingReference reference : this.references) {
            if (compiledGlobalVariable != null) {
                reference.fixup(compiledGlobalVariable);
            }
            reference.setStaticType(type, this.getConstantValue(), properties);
        }
    }

    protected void fixupBinding(Binding binding) {
        for (BindingReference reference : this.references) {
            reference.fixup(binding);
        }
    }

    public static enum BindingProperty {
        PRIVATE,
        GLOBAL,
        PARAM,
        TUNNEL,
        REQUIRED,
        IMPLICITLY_REQUIRED,
        ASSIGNABLE,
        SELECT,
        AS,
        DISALLOWS_CONTENT,
        STATIC,
        VISIBILITY,
        IMPLICITLY_DECLARED;

    }
}

