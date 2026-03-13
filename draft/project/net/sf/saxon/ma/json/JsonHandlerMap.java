/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.json;

import java.util.ArrayList;
import java.util.Stack;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.json.JsonHandler;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.StringValue;

public class JsonHandlerMap
extends JsonHandler {
    Stack<Sequence> stack;
    protected Stack<String> keys;

    public JsonHandlerMap(XPathContext context, int flags) {
        this.setContext(context);
        this.stack = new Stack();
        this.keys = new Stack();
        this.escape = (flags & 1) != 0;
        this.charChecker = context.getConfiguration().getValidCharacterChecker();
    }

    @Override
    public Sequence getResult() {
        return this.stack.peek();
    }

    @Override
    public boolean setKey(String unEscaped, String reEscaped) {
        this.keys.push(reEscaped);
        MapItem map = (MapItem)this.stack.peek();
        return map.get(new StringValue(reEscaped)) != null;
    }

    @Override
    public void startArray() {
        SimpleArrayItem map = new SimpleArrayItem(new ArrayList<GroundedValue>());
        this.stack.push(map);
    }

    @Override
    public void endArray() {
        ArrayItem map = (ArrayItem)this.stack.pop();
        if (this.stack.empty()) {
            this.stack.push(map);
        } else {
            this.writeItem(map);
        }
    }

    @Override
    public void startMap() {
        DictionaryMap map = new DictionaryMap();
        this.stack.push(map);
    }

    @Override
    public void endMap() {
        DictionaryMap map = (DictionaryMap)this.stack.pop();
        if (this.stack.empty()) {
            this.stack.push(map);
        } else {
            this.writeItem(map);
        }
    }

    private void writeItem(GroundedValue val) {
        if (this.stack.empty()) {
            this.stack.push(val);
        } else if (this.stack.peek() instanceof ArrayItem) {
            SimpleArrayItem array = (SimpleArrayItem)this.stack.peek();
            array.getMembers().add(val.materialize());
        } else {
            DictionaryMap map = (DictionaryMap)this.stack.peek();
            map.initialPut(this.keys.pop(), val);
        }
    }

    @Override
    public void writeNumeric(String asString, double asDouble) {
        this.writeItem(new DoubleValue(asDouble));
    }

    @Override
    public void writeString(String val) throws XPathException {
        this.writeItem(new StringValue(this.reEscape(val)));
    }

    @Override
    public void writeBoolean(boolean value) {
        this.writeItem(BooleanValue.get(value));
    }

    @Override
    public void writeNull() {
        this.writeItem(EmptySequence.getInstance());
    }
}

