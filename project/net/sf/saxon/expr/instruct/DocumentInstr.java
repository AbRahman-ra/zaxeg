/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.FixedElement;
import net.sf.saxon.expr.instruct.ParentNodeConstructor;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyBuilder;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaDeclaration;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TextFragmentValue;
import net.sf.saxon.value.UntypedAtomicValue;

public class DocumentInstr
extends ParentNodeConstructor {
    private boolean textOnly;
    private String constantText;

    public DocumentInstr(boolean textOnly, String constantText) {
        this.textOnly = textOnly;
        this.constantText = constantText;
    }

    @Override
    public Iterable<Operand> operands() {
        return this.contentOp;
    }

    @Override
    public int getImplementationMethod() {
        return 1;
    }

    public boolean isTextOnly() {
        return this.textOnly;
    }

    public CharSequence getConstantText() {
        return this.constantText;
    }

    @Override
    protected void checkContentSequence(StaticContext env) throws XPathException {
        DocumentInstr.checkContentSequence(env, this.getContentOperand(), this.getValidationOptions());
    }

    protected static void checkContentSequence(StaticContext env, Operand content, ParseOptions validationOptions) throws XPathException {
        Operand[] components = content.getChildExpression() instanceof Block ? ((Block)content.getChildExpression()).getOperanda() : new Operand[]{content};
        int validation = validationOptions == null ? 3 : validationOptions.getSchemaValidationMode();
        SchemaType type = validationOptions == null ? null : validationOptions.getTopLevelType();
        int elementCount = 0;
        boolean isXSLT = content.getChildExpression().getPackageData().isXSLT();
        for (Operand o : components) {
            SchemaDeclaration decl;
            XPathException de;
            Expression component = o.getChildExpression();
            ItemType it = component.getItemType();
            if (!(it instanceof NodeTest)) continue;
            UType possibleNodeKinds = it.getUType();
            if (possibleNodeKinds.equals(UType.ATTRIBUTE)) {
                de = new XPathException("Cannot create an attribute node whose parent is a document node");
                de.setErrorCode(isXSLT ? "XTDE0420" : "XPTY0004");
                de.setLocator(component.getLocation());
                throw de;
            }
            if (possibleNodeKinds.equals(UType.NAMESPACE)) {
                de = new XPathException("Cannot create a namespace node whose parent is a document node");
                de.setErrorCode(isXSLT ? "XTDE0420" : "XQTY0024");
                de.setLocator(component.getLocation());
                throw de;
            }
            if (!possibleNodeKinds.equals(UType.ELEMENT)) continue;
            if (++elementCount > 1 && (validation == 1 || validation == 2 || type != null)) {
                de = new XPathException("A valid document must have only one child element");
                if (isXSLT) {
                    de.setErrorCode("XTTE1550");
                } else {
                    de.setErrorCode("XQDY0061");
                }
                de.setLocator(component.getLocation());
                throw de;
            }
            if (validation != 1 || !(component instanceof FixedElement) || (decl = env.getConfiguration().getElementDeclaration(((FixedElement)component).getElementName().getFingerprint())) == null) continue;
            ((FixedElement)component).getContentExpression().checkPermittedContents(decl.getType(), true);
        }
    }

    @Override
    public int computeSpecialProperties() {
        int p = super.computeSpecialProperties();
        p |= 0x1000000;
        if (this.getValidationAction() == 4) {
            p |= 0x8000000;
        }
        return p;
    }

    public Expression getStringValueExpression() {
        if (this.textOnly) {
            if (this.constantText != null) {
                return new StringLiteral(new UntypedAtomicValue(this.constantText));
            }
            if (this.getContentExpression() instanceof ValueOf) {
                return ((ValueOf)this.getContentExpression()).convertToCastAsString();
            }
            Expression fn = SystemFunction.makeCall("string-join", this.getRetainedStaticContext(), this.getContentExpression(), new StringLiteral(StringValue.EMPTY_STRING));
            CastExpression cast = new CastExpression(fn, BuiltInAtomicType.UNTYPED_ATOMIC, false);
            ExpressionTool.copyLocationInfo(this, cast);
            return cast;
        }
        throw new AssertionError((Object)"getStringValueExpression() called on non-text-only document instruction");
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        DocumentInstr doc = new DocumentInstr(this.textOnly, this.constantText);
        ExpressionTool.copyLocationInfo(this, doc);
        doc.setContentExpression(this.getContentExpression().copy(rebindings));
        doc.setValidationAction(this.getValidationAction(), this.getSchemaType());
        return doc;
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.DOCUMENT;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        if (this.preservingTypes && !this.textOnly) {
            output.startDocument(0);
            this.getContentExpression().process(output, context);
            output.endDocument();
            return null;
        }
        NodeInfo item = this.evaluateItem(context);
        if (item != null) {
            output.append(item, this.getLocation(), 524288);
        }
        return null;
    }

    @Override
    public NodeInfo evaluateItem(XPathContext context) throws XPathException {
        NodeInfo root;
        Controller controller = context.getController();
        Configuration config = controller.getConfiguration();
        if (this.textOnly) {
            CharSequence textValue;
            if (this.constantText != null) {
                textValue = this.constantText;
            } else {
                Item item;
                FastStringBuffer sb = new FastStringBuffer(64);
                SequenceIterator iter = this.getContentExpression().iterate(context);
                while ((item = iter.next()) != null) {
                    sb.cat(item.getStringValueCS());
                }
                textValue = sb.condense();
            }
            root = TextFragmentValue.makeTextFragment(config, textValue, this.getStaticBaseURIString());
        } else {
            try {
                PipelineConfiguration pipe = controller.makePipelineConfiguration();
                pipe.setXPathContext(context);
                Builder builder = controller.makeBuilder();
                builder.setUseEventLocation(false);
                if (builder instanceof TinyBuilder) {
                    ((TinyBuilder)builder).setStatistics(config.getTreeStatistics().SOURCE_DOCUMENT_STATISTICS);
                }
                builder.setBaseURI(this.getStaticBaseURIString());
                builder.setTiming(false);
                pipe.setHostLanguage(this.getPackageData().getHostLanguage());
                builder.setPipelineConfiguration(pipe);
                ComplexContentOutputter out = ComplexContentOutputter.makeComplexContentReceiver(builder, this.getValidationOptions());
                out.open();
                out.startDocument(0);
                this.getContentExpression().process(out, context);
                out.endDocument();
                out.close();
                root = builder.getCurrentRoot();
            } catch (XPathException e) {
                e.maybeSetLocation(this.getLocation());
                e.maybeSetContext(context);
                throw e;
            }
        }
        return root;
    }

    @Override
    public int getInstructionNameCode() {
        return 150;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        SchemaType schemaType;
        out.startElement("doc", this);
        if (!out.isRelocatable()) {
            out.emitAttribute("base", this.getStaticBaseURIString());
        }
        String flags = "";
        if (this.textOnly) {
            flags = flags + "t";
        }
        if (this.isLocal()) {
            flags = flags + "l";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        if (this.constantText != null) {
            out.emitAttribute("text", this.constantText);
        }
        if (this.getValidationAction() != 4 && this.getValidationAction() != 8) {
            out.emitAttribute("validation", Validation.toString(this.getValidationAction()));
        }
        if ((schemaType = this.getSchemaType()) != null) {
            out.emitAttribute("type", schemaType.getStructuredQName());
        }
        this.getContentExpression().export(out);
        out.endElement();
    }

    @Override
    public String getStreamerName() {
        return "DocumentInstr";
    }
}

