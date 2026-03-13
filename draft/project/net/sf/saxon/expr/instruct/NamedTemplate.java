/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.TailCall;
import net.sf.saxon.expr.instruct.TailCallReturner;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public class NamedTemplate
extends Actor
implements TraceableComponent {
    private StructuredQName templateName;
    private boolean hasRequiredParams;
    private boolean bodyIsTailCallReturner;
    private SequenceType requiredType;
    private ItemType requiredContextItemType = AnyItemType.getInstance();
    private boolean mayOmitContextItem = true;
    private boolean absentFocus = false;
    private List<LocalParamInfo> localParamDetails = new ArrayList<LocalParamInfo>(4);

    public NamedTemplate(StructuredQName templateName) {
        this.setTemplateName(templateName);
    }

    public void setTemplateName(StructuredQName templateName) {
        this.templateName = templateName;
    }

    public void setContextItemRequirements(ItemType type, boolean mayBeOmitted, boolean absentFocus) {
        this.requiredContextItemType = type;
        this.mayOmitContextItem = mayBeOmitted;
        this.absentFocus = absentFocus;
    }

    @Override
    public SymbolicName getSymbolicName() {
        if (this.getTemplateName() == null) {
            return null;
        }
        return new SymbolicName(200, this.getTemplateName());
    }

    @Override
    public String getTracingTag() {
        return "xsl:template";
    }

    @Override
    public void gatherProperties(BiConsumer<String, Object> consumer) {
        consumer.accept("name", this.getTemplateName());
    }

    @Override
    public void setBody(Expression body) {
        super.setBody(body);
        this.bodyIsTailCallReturner = body instanceof TailCallReturner;
    }

    public StructuredQName getTemplateName() {
        return this.templateName;
    }

    @Override
    public StructuredQName getObjectName() {
        return this.templateName;
    }

    public void setHasRequiredParams(boolean has) {
        this.hasRequiredParams = has;
    }

    public boolean hasRequiredParams() {
        return this.hasRequiredParams;
    }

    public void setRequiredType(SequenceType type) {
        this.requiredType = type;
    }

    public SequenceType getRequiredType() {
        if (this.requiredType == null) {
            return SequenceType.ANY_SEQUENCE;
        }
        return this.requiredType;
    }

    public ItemType getRequiredContextItemType() {
        return this.requiredContextItemType;
    }

    public boolean isMayOmitContextItem() {
        return this.mayOmitContextItem;
    }

    public boolean isAbsentFocus() {
        return this.absentFocus;
    }

    public LocalParamInfo getLocalParamInfo(StructuredQName id) {
        List<LocalParamInfo> params = this.getLocalParamDetails();
        for (LocalParamInfo lp : params) {
            if (!lp.name.equals(id)) continue;
            return lp;
        }
        return null;
    }

    public TailCall expand(Outputter output, XPathContext context) throws XPathException {
        Item contextItem = context.getContextItem();
        if (contextItem == null) {
            if (!this.mayOmitContextItem) {
                XPathException err = new XPathException("The template requires a context item, but none has been supplied", "XTTE3090");
                err.setLocation(this.getLocation());
                err.setIsTypeError(true);
                throw err;
            }
        } else {
            TypeHierarchy th = context.getConfiguration().getTypeHierarchy();
            if (this.requiredContextItemType != AnyItemType.getInstance() && !this.requiredContextItemType.matches(contextItem, th)) {
                RoleDiagnostic role = new RoleDiagnostic(20, "context item for the named template", 0);
                String message = role.composeErrorMessage(this.requiredContextItemType, contextItem, th);
                XPathException err = new XPathException(message, "XTTE0590");
                err.setLocation(this.getLocation());
                err.setIsTypeError(true);
                throw err;
            }
            if (this.absentFocus) {
                context = context.newMinorContext();
                context.setCurrentIterator(null);
            }
        }
        if (this.bodyIsTailCallReturner) {
            return ((TailCallReturner)((Object)this.body)).processLeavingTail(output, context);
        }
        if (this.body != null) {
            this.body.process(output, context);
        }
        return null;
    }

    @Override
    public void export(ExpressionPresenter presenter) throws XPathException {
        presenter.startElement("template");
        presenter.emitAttribute("name", this.getTemplateName());
        this.explainProperties(presenter);
        presenter.emitAttribute("slots", "" + this.getStackFrameMap().getNumberOfVariables());
        if (this.getBody() != null) {
            presenter.setChildRole("body");
            this.getBody().export(presenter);
        }
        presenter.endElement();
    }

    public void explainProperties(ExpressionPresenter presenter) throws XPathException {
        if (this.getRequiredContextItemType() != AnyItemType.getInstance()) {
            SequenceType st = SequenceType.makeSequenceType(this.getRequiredContextItemType(), 16384);
            presenter.emitAttribute("cxt", st.toAlphaCode());
        }
        String flags = "";
        if (this.mayOmitContextItem) {
            flags = "o";
        }
        if (!this.absentFocus) {
            flags = flags + "s";
        }
        presenter.emitAttribute("flags", flags);
        if (this.getRequiredType() != SequenceType.ANY_SEQUENCE) {
            presenter.emitAttribute("as", this.getRequiredType().toAlphaCode());
        }
        presenter.emitAttribute("line", this.getLineNumber() + "");
        presenter.emitAttribute("module", this.getSystemId());
    }

    public void setLocalParamDetails(List<LocalParamInfo> details) {
        this.localParamDetails = details;
    }

    public List<LocalParamInfo> getLocalParamDetails() {
        return this.localParamDetails;
    }

    public static class LocalParamInfo {
        public StructuredQName name;
        public SequenceType requiredType;
        public boolean isRequired;
        public boolean isTunnel;
    }
}

