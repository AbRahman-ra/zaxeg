/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.DummyNamespaceResolver;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.instruct.ValidatingInstruction;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.ValidationException;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.Whitespace;

public abstract class AttributeCreator
extends SimpleNodeConstructor
implements ValidatingInstruction {
    SimpleType schemaType = null;
    private int validationAction;
    private int options = 0;
    private boolean isInstruction;

    public void setInstruction(boolean inst) {
        this.isInstruction = inst;
    }

    @Override
    public boolean isInstruction() {
        return this.isInstruction;
    }

    public void setSchemaType(SimpleType type) {
        this.schemaType = type;
    }

    @Override
    public SimpleType getSchemaType() {
        return this.schemaType;
    }

    public void setValidationAction(int action) {
        this.validationAction = action;
    }

    @Override
    public int getValidationAction() {
        return this.validationAction;
    }

    public void setOptions(int options) {
        this.options = options;
    }

    public void setRejectDuplicates() {
        this.options |= 0x20;
    }

    public void setNoSpecialChars() {
        this.options |= 4;
    }

    public int getOptions() {
        return this.options;
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        if (this.getValidationAction() == 4) {
            p |= 0x8000000;
        }
        return p;
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.ATTRIBUTE;
    }

    @Override
    public void processValue(CharSequence value, Outputter output, XPathContext context) throws XPathException {
        SimpleType ann;
        NodeName attName = this.evaluateNodeName(context);
        int opt = this.getOptions();
        SimpleType schemaType = this.getSchemaType();
        int validationAction = this.getValidationAction();
        if (schemaType != null) {
            ann = schemaType;
            ValidationFailure err = schemaType.validateContent(value, DummyNamespaceResolver.getInstance(), context.getConfiguration().getConversionRules());
            if (err != null) {
                ValidationFailure ve = new ValidationFailure("Attribute value " + Err.wrap(value, 4) + " does not match the required type " + schemaType.getDescription() + ". " + err.getMessage());
                ve.setSchemaType(schemaType);
                ve.setErrorCode("XTTE1540");
                throw ve.makeException();
            }
        } else if (validationAction == 1 || validationAction == 2) {
            try {
                Configuration config = context.getConfiguration();
                ann = config.validateAttribute(attName.getStructuredQName(), value, validationAction);
            } catch (ValidationException e) {
                XPathException err = XPathException.makeXPathException(e);
                err.maybeSetErrorCode(validationAction == 1 ? "XTTE1510" : "XTTE1515");
                err.setXPathContext(context);
                err.maybeSetLocation(this.getLocation());
                err.setIsTypeError(true);
                throw err;
            }
        } else {
            ann = BuiltInAtomicType.UNTYPED_ATOMIC;
        }
        if (attName.equals(StandardNames.XML_ID_NAME)) {
            value = Whitespace.collapseWhitespace(value);
        }
        try {
            output.attribute(attName, ann, value, this.getLocation(), opt);
        } catch (XPathException err) {
            throw AttributeCreator.dynamicError(this.getLocation(), err, context);
        }
    }

    protected void validateOrphanAttribute(Orphan orphan, XPathContext context) throws XPathException {
        ConversionRules rules = context.getConfiguration().getConversionRules();
        SimpleType schemaType = this.getSchemaType();
        int validationAction = this.getValidationAction();
        if (schemaType != null) {
            ValidationFailure err = schemaType.validateContent(orphan.getStringValueCS(), DummyNamespaceResolver.getInstance(), rules);
            if (err != null) {
                err.setMessage("Attribute value " + Err.wrap(orphan.getStringValueCS(), 4) + " does not the match the required type " + schemaType.getDescription() + ". " + err.getMessage());
                err.setErrorCode("XTTE1555");
                err.setLocator(this.getLocation());
                throw err.makeException();
            }
            orphan.setTypeAnnotation(schemaType);
            if (schemaType.isNamespaceSensitive()) {
                throw new XPathException("Cannot validate a parentless attribute whose content is namespace-sensitive", "XTTE1545");
            }
        } else if (validationAction == 1 || validationAction == 2) {
            try {
                Controller controller = context.getController();
                assert (controller != null);
                SimpleType ann = controller.getConfiguration().validateAttribute(NameOfNode.makeName(orphan).getStructuredQName(), orphan.getStringValueCS(), validationAction);
                orphan.setTypeAnnotation(ann);
            } catch (ValidationException e) {
                XPathException err = XPathException.makeXPathException(e);
                err.setErrorCodeQName(e.getErrorCodeQName());
                err.setXPathContext(context);
                err.setLocation(this.getLocation());
                err.setIsTypeError(true);
                throw err;
            }
        }
    }
}

