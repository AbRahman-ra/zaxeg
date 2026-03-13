/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.StringValue;

public class QNameFn
extends SystemFunction {
    public static QNameValue expandedQName(StringValue namespace, StringValue lexical) throws XPathException {
        String uri = namespace == null ? null : namespace.getStringValue();
        try {
            String lex = lexical.getStringValue();
            String[] parts = NameChecker.getQNameParts(lex);
            if (!parts[0].isEmpty() && !NameChecker.isValidNCName(parts[0])) {
                XPathException err = new XPathException("Malformed prefix in QName: '" + parts[0] + '\'');
                err.setErrorCode("FOCA0002");
                throw err;
            }
            return new QNameValue(parts[0], uri, parts[1], BuiltInAtomicType.QNAME, true);
        } catch (QNameException e) {
            throw new XPathException(e.getMessage(), "FOCA0002");
        } catch (XPathException err) {
            if (err.getErrorCodeLocalPart().equals("FORG0001")) {
                err.setErrorCode("FOCA0002");
            }
            throw err;
        }
    }

    @Override
    public QNameValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return QNameFn.expandedQName((StringValue)arguments[0].head(), (StringValue)arguments[1].head());
    }

    @Override
    public String getCompilerName() {
        return "QNameFnCompiler";
    }
}

