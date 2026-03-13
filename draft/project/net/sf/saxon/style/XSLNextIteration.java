/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.NextIteration;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.XSLBreakOrContinue;
import net.sf.saxon.style.XSLLocalParam;
import net.sf.saxon.style.XSLWithParam;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.Whitespace;

public class XSLNextIteration
extends XSLBreakOrContinue {
    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        this.validatePosition();
        if (this.xslIterate == null) {
            this.compileError("xsl:next-iteration must be a descendant of an xsl:iterate instruction");
        }
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLWithParam) {
                if (!((XSLWithParam)nodeInfo).isTunnelParam()) continue;
                this.compileError("An xsl:with-param element within xsl:iterate must not specify tunnel='yes'", "XTSE0020");
                continue;
            }
            if (nodeInfo.getNodeKind() == 3) {
                if (Whitespace.isWhite(nodeInfo.getStringValueCS())) continue;
                this.compileError("No character data is allowed within xsl:next-iteration", "XTSE0010");
                continue;
            }
            this.compileError("Child element " + Err.wrap(nodeInfo.getDisplayName(), 1) + " is not allowed as a child of xsl:next-iteration", "XTSE0010");
        }
    }

    @Override
    public void postValidate() throws XPathException {
        if (this.xslIterate == null) {
            return;
        }
        for (NodeInfo nodeInfo : this.children(XSLWithParam.class::isInstance)) {
            NodeInfo param;
            XSLWithParam withParam = (XSLWithParam)nodeInfo;
            AxisIterator formalParams = this.xslIterate.iterateAxis(3);
            boolean ok = false;
            while ((param = formalParams.next()) != null) {
                if (!(param instanceof XSLLocalParam) || !((XSLLocalParam)param).getVariableQName().equals(withParam.getVariableQName())) continue;
                ok = true;
                SequenceType required = ((XSLLocalParam)param).getRequiredType();
                withParam.checkAgainstRequiredType(required);
                break;
            }
            if (ok) continue;
            this.compileError("Parameter " + withParam.getVariableQName().getDisplayName() + " is not declared in the containing xsl:iterate instruction", "XTSE3130");
        }
    }

    public SequenceType getDeclaredParamType(StructuredQName name) {
        for (NodeInfo nodeInfo : this.xslIterate.children(XSLLocalParam.class::isInstance)) {
            if (!((XSLLocalParam)nodeInfo).getVariableQName().equals(name)) continue;
            return ((XSLLocalParam)nodeInfo).getRequiredType();
        }
        return null;
    }

    @Override
    public Expression compile(Compilation exec, ComponentDeclaration decl) throws XPathException {
        NextIteration call = new NextIteration();
        call.setRetainedStaticContext(this.makeRetainedStaticContext());
        WithParam[] actualParams = this.getWithParamInstructions(call, exec, decl, false);
        call.setParameters(actualParams);
        if (this.xslIterate != null) {
            NodeInfo param;
            AxisIterator declaredParams = this.xslIterate.iterateAxis(3);
            while ((param = declaredParams.next()) != null) {
                if (!(param instanceof XSLLocalParam)) continue;
                XSLLocalParam pdecl = (XSLLocalParam)param;
                StructuredQName paramName = pdecl.getVariableQName();
                LocalParam lp = pdecl.getCompiledParam();
                boolean found = false;
                for (WithParam actualParam : actualParams) {
                    if (!paramName.equals(actualParam.getVariableQName())) continue;
                    actualParam.setSlotNumber(lp.getSlotNumber());
                    found = true;
                    break;
                }
                if (found) continue;
                WithParam wp = new WithParam();
                wp.setVariableQName(paramName);
                LocalVariableReference ref = new LocalVariableReference(lp);
                wp.setSelectExpression(call, ref);
                wp.setSlotNumber(lp.getSlotNumber());
                ref.setStaticType(pdecl.getRequiredType(), null, 0);
                WithParam[] p2 = new WithParam[actualParams.length + 1];
                p2[0] = wp;
                System.arraycopy(actualParams, 0, p2, 1, actualParams.length);
                actualParams = p2;
            }
        }
        return call;
    }
}

