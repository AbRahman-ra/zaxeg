/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.jaxen.Navigator;
import org.jaxen.function.StringFunction;

public class IdFunction
implements Function {
    public Object call(Context context, List args) throws FunctionCallException {
        if (args.size() == 1) {
            return IdFunction.evaluate(context.getNodeSet(), args.get(0), context.getNavigator());
        }
        throw new FunctionCallException("id() requires one argument");
    }

    public static List evaluate(List contextNodes, Object arg, Navigator nav) {
        if (contextNodes.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        ArrayList<Object> nodes = new ArrayList<Object>();
        Object contextNode = contextNodes.get(0);
        if (arg instanceof List) {
            Iterator iter = ((List)arg).iterator();
            while (iter.hasNext()) {
                String id = StringFunction.evaluate(iter.next(), nav);
                nodes.addAll(IdFunction.evaluate(contextNodes, id, nav));
            }
        } else {
            String ids = StringFunction.evaluate(arg, nav);
            StringTokenizer tok = new StringTokenizer(ids, " \t\n\r");
            while (tok.hasMoreTokens()) {
                String id = tok.nextToken();
                Object node = nav.getElementById(contextNode, id);
                if (node == null) continue;
                nodes.add(node);
            }
        }
        return nodes;
    }
}

