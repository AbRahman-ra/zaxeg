/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.UnsupportedAxisException;
import org.jaxen.function.StringFunction;

public class LangFunction
implements Function {
    private static final String LANG_LOCALNAME = "lang";
    private static final String XMLNS_URI = "http://www.w3.org/XML/1998/namespace";

    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() != 1) {
            throw new FunctionCallException("lang() requires exactly one argument.");
        }
        Object arg = args.get(0);
        try {
            return LangFunction.evaluate(context.getNodeSet(), arg, context.getNavigator());
        } catch (UnsupportedAxisException e) {
            throw new FunctionCallException("Can't evaluate lang()", e);
        }
    }

    private static Boolean evaluate(List contextNodes, Object lang, Navigator nav) throws UnsupportedAxisException {
        return LangFunction.evaluate(contextNodes.get(0), StringFunction.evaluate(lang, nav), nav) ? Boolean.TRUE : Boolean.FALSE;
    }

    private static boolean evaluate(Object node, String lang, Navigator nav) throws UnsupportedAxisException {
        Object element = node;
        if (!nav.isElement(element)) {
            element = nav.getParentNode(node);
        }
        while (element != null && nav.isElement(element)) {
            Iterator attrs = nav.getAttributeAxisIterator(element);
            while (attrs.hasNext()) {
                Object attr = attrs.next();
                if (!LANG_LOCALNAME.equals(nav.getAttributeName(attr)) || !XMLNS_URI.equals(nav.getAttributeNamespaceUri(attr))) continue;
                return LangFunction.isSublang(nav.getAttributeStringValue(attr), lang);
            }
            element = nav.getParentNode(element);
        }
        return false;
    }

    private static boolean isSublang(String sublang, String lang) {
        if (sublang.equalsIgnoreCase(lang)) {
            return true;
        }
        int ll = lang.length();
        return sublang.length() > ll && sublang.charAt(ll) == '-' && sublang.substring(0, ll).equalsIgnoreCase(lang);
    }
}

