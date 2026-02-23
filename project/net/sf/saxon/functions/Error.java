/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.StringValue;

public class Error
extends SystemFunction
implements Callable {
    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return super.getSpecialProperties(arguments) & 0xFF7FFFFF;
    }

    public boolean isVacuousExpression() {
        return true;
    }

    public Item error(XPathContext context, QNameValue errorCode, StringValue desc, SequenceIterator errObject) throws XPathException {
        QNameValue qname = null;
        if (this.getArity() > 0) {
            qname = errorCode;
        }
        if (qname == null) {
            qname = new QNameValue("err", "http://www.w3.org/2005/xqt-errors", this.getArity() == 1 ? "FOTY0004" : "FOER0000", BuiltInAtomicType.QNAME, false);
        }
        String description = this.getArity() > 1 ? (desc == null ? "" : desc.getStringValue()) : "Error signalled by application call on error()";
        UserDefinedXPathException e = new UserDefinedXPathException(description);
        e.setErrorCodeQName(qname.getStructuredQName());
        e.setXPathContext(context);
        if (this.getArity() > 2 && errObject != null) {
            AxisIterator iter;
            NodeInfo errorElement;
            Object root;
            GroundedValue errorObject = errObject.materialize();
            if (errorObject instanceof ZeroOrOne && (root = ((ZeroOrOne)errorObject).head()) instanceof NodeInfo && ((NodeInfo)root).getNodeKind() == 9 && (errorElement = (iter = ((NodeInfo)root).iterateAxis(3, new NameTest(1, "", "error", context.getConfiguration().getNamePool()))).next()) != null) {
                String module = errorElement.getAttributeValue("", "module");
                String lineVal = errorElement.getAttributeValue("", "line");
                int line = lineVal == null ? -1 : Integer.parseInt(lineVal);
                String columnVal = errorElement.getAttributeValue("", "column");
                int col = columnVal == null ? -1 : Integer.parseInt(columnVal);
                Loc locator = new Loc(module, line, col);
                e.setLocator(locator);
            }
            e.setErrorObject(errorObject);
        }
        throw e;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        int len = arguments.length;
        switch (len) {
            case 0: {
                return this.error(context, null, null, null);
            }
            case 1: {
                QNameValue arg0 = (QNameValue)arguments[0].head();
                if (arg0 == null) {
                    arg0 = new QNameValue("err", "http://www.w3.org/2005/xqt-errors", "FOER0000");
                }
                return this.error(context, arg0, null, null);
            }
            case 2: {
                return this.error(context, (QNameValue)arguments[0].head(), (StringValue)arguments[1].head(), null);
            }
            case 3: {
                return this.error(context, (QNameValue)arguments[0].head(), (StringValue)arguments[1].head(), arguments[2].iterate());
            }
        }
        return null;
    }

    public static class UserDefinedXPathException
    extends XPathException {
        public UserDefinedXPathException(String message) {
            super(message);
        }
    }
}

