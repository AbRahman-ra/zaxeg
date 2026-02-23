/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.function.BiConsumer;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.ProxyOutputter;
import net.sf.saxon.event.ReceiverOption;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.SimpleNodeConstructor;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.CharSequenceConsumer;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ComplexType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.Cardinality;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public final class ValueOf
extends SimpleNodeConstructor {
    private int options;
    private boolean isNumberingInstruction = false;
    private boolean noNodeIfEmpty;

    public ValueOf(Expression select, boolean disable, boolean noNodeIfEmpty) {
        this.setSelect(select);
        this.options = disable ? 1 : 0;
        this.noNodeIfEmpty = noNodeIfEmpty;
        this.adoptChildExpression(select);
        if (select instanceof StringLiteral) {
            boolean special = false;
            String val = ((StringLiteral)select).getStringValue();
            for (int k = 0; k < val.length(); ++k) {
                char c = val.charAt(k);
                if (c >= '!' && c <= '~' && c != '<' && c != '>' && c != '&') continue;
                special = true;
                break;
            }
            if (!special) {
                this.options |= 4;
            }
        }
    }

    public void setIsNumberingInstruction() {
        this.isNumberingInstruction = true;
    }

    public boolean isNumberingInstruction() {
        return this.isNumberingInstruction;
    }

    public boolean isNoNodeIfEmpty() {
        return this.noNodeIfEmpty;
    }

    @Override
    public String toShortString() {
        if (this.getSelect() instanceof StringLiteral) {
            return "text{" + Err.depict(((StringLiteral)this.getSelect()).getValue()) + "}";
        }
        return super.toShortString();
    }

    @Override
    public void gatherProperties(BiConsumer<String, Object> consumer) {
        if (this.getSelect() instanceof StringLiteral) {
            consumer.accept("text", ((StringLiteral)this.getSelect()).getValue().getStringValue());
        }
    }

    @Override
    public int getInstructionNameCode() {
        if (this.isNumberingInstruction) {
            return 180;
        }
        if (this.getSelect() instanceof StringLiteral) {
            return 201;
        }
        return 205;
    }

    public int getOptions() {
        return this.options;
    }

    public boolean isDisableOutputEscaping() {
        return ReceiverOption.contains(this.options, 1);
    }

    @Override
    public ItemType getItemType() {
        return NodeKindTest.TEXT;
    }

    @Override
    public int computeCardinality() {
        if (this.noNodeIfEmpty) {
            return 24576;
        }
        return 16384;
    }

    @Override
    public void localTypeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) {
    }

    @Override
    public int getIntrinsicDependencies() {
        int d = super.getIntrinsicDependencies();
        if (this.isDisableOutputEscaping()) {
            d |= 0x200;
        }
        return d;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        ValueOf exp = new ValueOf(this.getSelect().copy(rebindings), ReceiverOption.contains(this.options, 1), this.noNodeIfEmpty);
        ExpressionTool.copyLocationInfo(this, exp);
        if (this.isNumberingInstruction) {
            exp.setIsNumberingInstruction();
        }
        return exp;
    }

    @Override
    public void checkPermittedContents(SchemaType parentType, boolean whole) throws XPathException {
        if (this.getSelect() instanceof Literal) {
            GroundedValue selectValue = ((Literal)this.getSelect()).getValue();
            SimpleType stype = null;
            if (parentType instanceof SimpleType && whole) {
                stype = (SimpleType)parentType;
            } else if (parentType instanceof ComplexType && ((ComplexType)parentType).isSimpleContent()) {
                stype = ((ComplexType)parentType).getSimpleContentType();
            }
            if (whole && stype != null && !stype.isNamespaceSensitive()) {
                ValidationFailure err = stype.validateContent(selectValue.getStringValue(), null, this.getConfiguration().getConversionRules());
                if (err != null) {
                    err.setLocator(this.getLocation());
                    err.setErrorCode(this.isXSLT() ? "XTTE1540" : "XQDY0027");
                    throw err.makeException();
                }
                return;
            }
            if (parentType instanceof ComplexType && !((ComplexType)parentType).isSimpleContent() && !((ComplexType)parentType).isMixedContent() && !Whitespace.isWhite(selectValue.getStringValue())) {
                XPathException err = new XPathException("The containing element must be of type " + parentType.getDescription() + ", which does not allow text content " + Err.wrap(selectValue.getStringValue()));
                err.setLocation(this.getLocation());
                err.setIsTypeError(true);
                throw err;
            }
        }
    }

    public Expression convertToCastAsString() {
        if (this.noNodeIfEmpty || !Cardinality.allowsZero(this.getSelect().getCardinality())) {
            return new CastExpression(this.getSelect(), BuiltInAtomicType.UNTYPED_ATOMIC, true);
        }
        Expression sf = SystemFunction.makeCall("string", this.getRetainedStaticContext(), this.getSelect());
        return new CastExpression(sf, BuiltInAtomicType.UNTYPED_ATOMIC, false);
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        if (this.noNodeIfEmpty) {
            StringValue value = (StringValue)this.getSelect().evaluateItem(context);
            if (value != null) {
                this.processValue(value.getStringValueCS(), output, context);
            }
            return null;
        }
        if (this.getSelect().getItemType() == BuiltInAtomicType.STRING && !this.isDisableOutputEscaping() && !Cardinality.allowsZero(this.getCardinality())) {
            ProxyOutputter toText = new ProxyOutputter(output){

                @Override
                public void append(Item item) throws XPathException {
                    this.getNextOutputter().characters(item.getStringValueCS(), Loc.NONE, ValueOf.this.options);
                }

                @Override
                public void append(Item item, Location locationId, int properties) throws XPathException {
                    this.getNextOutputter().characters(item.getStringValueCS(), locationId, properties | ValueOf.this.options);
                }

                @Override
                public CharSequenceConsumer getStringReceiver(boolean asTextNode) {
                    return this.getNextOutputter().getStringReceiver(true);
                }
            };
            this.getSelect().process(toText, context);
            return null;
        }
        return super.processLeavingTail(output, context);
    }

    @Override
    public void processValue(CharSequence value, Outputter output, XPathContext context) throws XPathException {
        output.characters(value, this.getLocation(), this.options);
    }

    @Override
    public NodeInfo evaluateItem(XPathContext context) throws XPathException {
        try {
            CharSequence val;
            Item item = this.getSelect().evaluateItem(context);
            if (item == null) {
                if (this.noNodeIfEmpty) {
                    return null;
                }
                val = "";
            } else {
                val = item.getStringValueCS();
            }
            Controller controller = context.getController();
            assert (controller != null);
            Orphan o = new Orphan(controller.getConfiguration());
            o.setNodeKind((short)3);
            o.setStringValue(val);
            if (this.isDisableOutputEscaping()) {
                o.setDisableOutputEscaping(true);
            }
            return o;
        } catch (XPathException err) {
            err.maybeSetLocation(this.getLocation());
            throw err;
        }
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("valueOf", this);
        String flags = "";
        if (this.isDisableOutputEscaping()) {
            flags = flags + "d";
        }
        if (ReceiverOption.contains(this.options, 4)) {
            flags = flags + "S";
        }
        if (this.noNodeIfEmpty) {
            flags = flags + "e";
        }
        if (this.isLocal()) {
            flags = flags + "l";
        }
        if (!flags.isEmpty()) {
            out.emitAttribute("flags", flags);
        }
        this.getSelect().export(out);
        out.endElement();
    }
}

