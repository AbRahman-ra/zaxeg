/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.event.Builder;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.event.TreeReceiver;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XsltController;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class Message
extends Instruction {
    private Operand selectOp;
    private Operand terminateOp;
    private Operand errorCodeOp;
    private boolean isAssert;

    public Message(Expression select, Expression terminate, Expression errorCode) {
        if (errorCode == null) {
            errorCode = new StringLiteral("Q{http://www.w3.org/2005/xqt-errors}XTMM9000");
        }
        this.selectOp = new Operand(this, select, OperandRole.SINGLE_ATOMIC);
        this.terminateOp = new Operand(this, terminate, OperandRole.SINGLE_ATOMIC);
        this.errorCodeOp = new Operand(this, errorCode, OperandRole.SINGLE_ATOMIC);
    }

    public Expression getSelect() {
        return this.selectOp.getChildExpression();
    }

    public void setSelect(Expression select) {
        this.selectOp.setChildExpression(select);
    }

    public Expression getTerminate() {
        return this.terminateOp.getChildExpression();
    }

    public void setTerminate(Expression terminate) {
        this.terminateOp.setChildExpression(terminate);
    }

    public Expression getErrorCode() {
        return this.errorCodeOp.getChildExpression();
    }

    public void setErrorCode(Expression errorCode) {
        this.errorCodeOp.setChildExpression(errorCode);
    }

    @Override
    public Iterable<Operand> operands() {
        return this.operandList(this.selectOp, this.terminateOp, this.errorCodeOp);
    }

    public void setIsAssert(boolean isAssert) {
        this.isAssert = isAssert;
    }

    @Override
    public Expression copy(RebindingMap rebindings) {
        Message exp = new Message(this.getSelect().copy(rebindings), this.getTerminate().copy(rebindings), this.getErrorCode().copy(rebindings));
        ExpressionTool.copyLocationInfo(this, exp);
        return exp;
    }

    @Override
    public int getInstructionNameCode() {
        return this.isAssert ? 134 : 173;
    }

    @Override
    public ItemType getItemType() {
        return AnyItemType.getInstance();
    }

    @Override
    public int getCardinality() {
        return 24576;
    }

    @Override
    public final boolean mayCreateNewNodes() {
        return true;
    }

    @Override
    public Expression optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
        Expression e = super.optimize(visitor, contextInfo);
        if (e != this) {
            return e;
        }
        return this;
    }

    @Override
    public TailCall processLeavingTail(Outputter output, XPathContext context) throws XPathException {
        MessageAdapter rec;
        String code;
        String term;
        XsltController controller = (XsltController)context.getController();
        if (this.isAssert && !controller.isAssertionsEnabled()) {
            return null;
        }
        boolean abort = false;
        switch (term = Whitespace.trim(this.getTerminate().evaluateAsString(context))) {
            case "no": 
            case "false": 
            case "0": {
                break;
            }
            case "yes": 
            case "true": 
            case "1": {
                abort = true;
                break;
            }
            default: {
                XPathException e = new XPathException("The terminate attribute of xsl:message must be yes|true|1 or no|false|0");
                e.setXPathContext(context);
                e.setErrorCode("XTDE0030");
                throw e;
            }
        }
        try {
            code = this.getErrorCode().evaluateAsString(context).toString();
        } catch (XPathException err) {
            code = err.getErrorCodeQName().getEQName();
        }
        StructuredQName errorCode = null;
        try {
            errorCode = StructuredQName.fromLexicalQName(code, false, true, this.getRetainedStaticContext());
        } catch (XPathException err) {
            errorCode = new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "XTMM9000");
        }
        controller.incrementMessageCounter(errorCode);
        Receiver emitter = controller.makeMessageReceiver();
        PipelineConfiguration pipe = controller.makePipelineConfiguration();
        pipe.setHostLanguage(HostLanguage.XSLT);
        pipe.setXPathContext(context);
        emitter.setPipelineConfiguration(pipe);
        Builder builder = null;
        if (abort) {
            builder = controller.makeBuilder();
            rec = new MessageAdapter(new TreeReceiver(builder), errorCode.getEQName(), this.getLocation());
            rec.open();
        } else {
            rec = new MessageAdapter(new TreeReceiver(emitter), errorCode.getEQName(), this.getLocation());
        }
        ComplexContentOutputter cco = new ComplexContentOutputter(rec);
        cco.startDocument(abort ? 16384 : 0);
        try {
            this.getSelect().process(cco, context);
        } catch (XPathException e) {
            cco.append(new StringValue("Error " + e.getErrorCodeLocalPart() + " while evaluating xsl:message at line " + this.getLocation().getLineNumber() + " of " + this.getLocation().getSystemId() + ": " + e.getMessage()));
        }
        cco.endDocument();
        cco.close();
        if (abort) {
            builder.close();
            NodeInfo content = builder.getCurrentRoot();
            emitter = new ProxyReceiver(emitter){

                @Override
                public void startDocument(int properties) throws XPathException {
                    super.startDocument(16384);
                }
            };
            content.copy(emitter, 2, this.getLocation());
            TerminationException te = new TerminationException("Processing terminated by " + StandardErrorListener.getInstructionNameDefault(this) + " at line " + this.getLocation().getLineNumber() + " in " + StandardDiagnostics.abbreviateLocationURIDefault(this.getLocation().getSystemId()));
            te.setLocation(this.getLocation());
            te.setErrorCodeQName(errorCode);
            te.setErrorObject(content);
            throw te;
        }
        return null;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("message", this);
        out.setChildRole("select");
        this.getSelect().export(out);
        out.setChildRole("terminate");
        this.getTerminate().export(out);
        out.setChildRole("error");
        this.getErrorCode().export(out);
        out.endElement();
    }

    private static class MessageAdapter
    extends ProxyReceiver {
        private String errorCode;
        private Location location;

        public MessageAdapter(SequenceReceiver next, String errorCode, Location location) {
            super(next);
            this.errorCode = errorCode;
            this.location = location;
        }

        @Override
        public void startDocument(int properties) throws XPathException {
            super.startDocument(properties);
            this.processingInstruction("error-code", this.errorCode, this.location, 0);
        }

        @Override
        public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
            super.startElement(elemName, type, attributes, namespaces, location, properties);
        }

        @Override
        public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
            if (item instanceof NodeInfo) {
                int kind = ((NodeInfo)item).getNodeKind();
                if (kind == 2) {
                    String attName = ((NodeInfo)item).getDisplayName();
                    this.processingInstruction("attribute", "name=\"" + attName + "\" value=\"" + item.getStringValue() + "\"", locationId, 0);
                    return;
                }
                if (kind == 13) {
                    String prefix = ((NodeInfo)item).getLocalPart();
                    this.processingInstruction("namespace", "prefix=\"" + prefix + "\" uri=\"" + item.getStringValue() + "\"", Loc.NONE, 0);
                    return;
                }
            } else if (item instanceof Function && !((Function)item).isArray()) {
                CharSequence representation = ((Function)item).isMap() ? Err.depict(item) : "Function " + Err.depict(item);
                this.nextReceiver.characters(representation, locationId, 0);
                return;
            }
            this.nextReceiver.append(item, locationId, copyNamespaces);
        }
    }
}

