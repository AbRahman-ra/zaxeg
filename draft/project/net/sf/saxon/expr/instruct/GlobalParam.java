/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import net.sf.saxon.Controller;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.Bindery;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.trans.XPathException;

public final class GlobalParam
extends GlobalVariable {
    private boolean implicitlyRequired;

    public void setImplicitlyRequiredParam(boolean requiredParam) {
        this.implicitlyRequired = requiredParam;
    }

    public boolean isImplicitlyRequiredParam() {
        return this.implicitlyRequired;
    }

    @Override
    public String getTracingTag() {
        return "xsl:param";
    }

    @Override
    public GroundedValue evaluateVariable(XPathContext context, Component target) throws XPathException {
        Controller controller = context.getController();
        assert (controller != null);
        Bindery b = controller.getBindery(this.getPackageData());
        GroundedValue val = b.getGlobalVariableValue(this);
        if (val != null) {
            if (val instanceof Bindery.FailureValue) {
                throw (XPathException)((Bindery.FailureValue)val).getObject();
            }
            return val;
        }
        val = controller.getConvertedParameter(this.getVariableQName(), this.getRequiredType(), context);
        if (val != null) {
            return b.saveGlobalVariableValue(this, val);
        }
        if (this.isRequiredParam()) {
            XPathException e = new XPathException("No value supplied for required parameter $" + this.getVariableQName().getDisplayName());
            e.setXPathContext(context);
            e.setLocator(this);
            e.setErrorCode(this.getPackageData().isXSLT() ? "XTDE0050" : "XPDY0002");
            throw e;
        }
        if (this.isImplicitlyRequiredParam()) {
            XPathException e = new XPathException("A value must be supplied for parameter $" + this.getVariableQName().getDisplayName() + " because there is no default value for the required type");
            e.setXPathContext(context);
            e.setLocator(this);
            e.setErrorCode("XTDE0700");
            throw e;
        }
        return this.actuallyEvaluate(context, target);
    }

    @Override
    public GroundedValue evaluateVariable(XPathContext context) throws XPathException {
        return this.evaluateVariable(context, null);
    }

    @Override
    protected String getFlags() {
        String f = super.getFlags();
        if (this.isImplicitlyRequiredParam()) {
            f = f + "i";
        }
        return f;
    }
}

