/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.HashMap;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.ma.arrays.ArrayItem;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.SequenceTool;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.ObjectValue;

public class Trace
extends SystemFunction
implements Traceable {
    Location location = Loc.NONE;

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        return arguments[0].getSpecialProperties();
    }

    @Override
    public int getCardinality(Expression[] arguments) {
        return arguments[0].getCardinality();
    }

    public void notifyListener(String label, Sequence val, XPathContext context) {
        HashMap<String, Object> info = new HashMap<String, Object>();
        info.put("label", label);
        info.put("value", val);
        TraceListener listener = context.getController().getTraceListener();
        listener.enter(this, info, context);
        listener.leave(this);
    }

    @Override
    public Expression makeFunctionCall(Expression ... arguments) {
        Expression e = super.makeFunctionCall(arguments);
        this.location = e.getLocation();
        return e;
    }

    public static void traceItem(Item val, String label, Logger out) {
        if (val == null) {
            out.info(label);
        } else if (val instanceof NodeInfo) {
            out.info(label + ": " + Type.displayTypeName(val) + ": " + Navigator.getPath((NodeInfo)val));
        } else if (val instanceof AtomicValue) {
            out.info(label + ": " + Type.displayTypeName(val) + ": " + val.getStringValue());
        } else if (val instanceof ArrayItem || val instanceof MapItem) {
            out.info(label + ": " + val.toShortString());
        } else if (val instanceof Function) {
            StructuredQName name = ((Function)val).getFunctionName();
            out.info(label + ": function " + (name == null ? "(anon)" : name.getDisplayName()) + "#" + ((Function)val).getArity());
        } else if (val instanceof ObjectValue) {
            Object obj = ((ObjectValue)val).getObject();
            out.info(label + ": " + obj.getClass().getName() + " = " + Err.truncate30(obj.toString()));
        } else {
            out.info(label + ": " + val.toShortString());
        }
    }

    @Override
    public Location getLocation() {
        return this.location;
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        String label;
        Controller controller = context.getController();
        String string = label = arguments.length == 1 ? "*" : arguments[1].head().getStringValue();
        if (controller.isTracing()) {
            GroundedValue value = arguments[0].iterate().materialize();
            this.notifyListener(label, value, context);
            return value;
        }
        Logger out = controller.getTraceFunctionDestination();
        if (out == null) {
            return arguments[0];
        }
        return SequenceTool.toLazySequence(new TracingIterator(arguments[0].iterate(), label, out));
    }

    @Override
    public StructuredQName getObjectName() {
        return null;
    }

    @Override
    public String getStreamerName() {
        return "Trace";
    }

    private class TracingIterator
    implements SequenceIterator {
        private SequenceIterator base;
        private String label;
        private Logger out;
        private boolean empty = true;
        private int position = 0;

        public TracingIterator(SequenceIterator base, String label, Logger out) {
            this.base = base;
            this.label = label;
            this.out = out;
        }

        @Override
        public Item next() throws XPathException {
            Item n = this.base.next();
            ++this.position;
            if (n == null) {
                if (this.empty) {
                    Trace.traceItem(null, this.label + ": empty sequence", this.out);
                }
            } else {
                Trace.traceItem(n, this.label + " [" + this.position + ']', this.out);
                this.empty = false;
            }
            return n;
        }

        @Override
        public void close() {
            this.base.close();
        }
    }
}

