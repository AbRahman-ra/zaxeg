/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.instruct;

import java.util.HashMap;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ExpressionTool;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.QNameValue;

public class MemoFunction
extends UserFunction {
    @Override
    public void computeEvaluationMode() {
        this.evaluator = ExpressionTool.eagerEvaluator(this.getBody());
    }

    @Override
    public boolean isMemoFunction() {
        return true;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] actualArgs) throws XPathException {
        Sequence value;
        String key = MemoFunction.getCombinedKey(actualArgs);
        Controller controller = context.getController();
        HashMap<String, Sequence> map = (HashMap<String, Sequence>)controller.getUserData(this, "memo-function-cache");
        Sequence sequence = value = map == null ? null : (Sequence)map.get(key);
        if (value != null) {
            return value;
        }
        value = super.call(context, actualArgs);
        if (map == null) {
            map = new HashMap<String, Sequence>(32);
            controller.setUserData(this, "memo-function-cache", map);
        }
        map.put(key, value);
        return value;
    }

    private static String getCombinedKey(Sequence[] params) throws XPathException {
        FastStringBuffer sb = new FastStringBuffer(256);
        for (Sequence val : params) {
            Item item;
            SequenceIterator iter = val.iterate();
            while ((item = iter.next()) != null) {
                if (item instanceof NodeInfo) {
                    NodeInfo node = (NodeInfo)item;
                    node.generateId(sb);
                } else if (item instanceof QNameValue) {
                    sb.cat(Type.displayTypeName(item)).cat('/').cat(((QNameValue)item).getClarkName());
                } else if (item instanceof AtomicValue) {
                    sb.cat(Type.displayTypeName(item)).cat('/').cat(item.getStringValueCS());
                } else if (item instanceof Function) {
                    sb.cat(item.getClass().getName()).cat("@").cat("" + System.identityHashCode(item));
                }
                sb.cat('\u0001');
            }
            sb.cat('\u0002');
        }
        return sb.toString();
    }
}

