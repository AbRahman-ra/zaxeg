/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Sink;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.QuitParsingException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.value.BooleanValue;

public class StreamAvailable
extends SystemFunction
implements Callable {
    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        boolean result = this.isAvailable(arguments[0].head().getStringValue(), context);
        return BooleanValue.get(result);
    }

    private boolean isAvailable(String uri, XPathContext context) {
        try {
            StreamTester tester = new StreamTester(context.getConfiguration().makePipelineConfiguration());
            DocumentFn.sendDoc(uri, this.getRetainedStaticContext().getStaticBaseUriString(), context, null, tester, new ParseOptions());
        } catch (QuitParsingException e) {
            return true;
        } catch (XPathException e) {
            return false;
        }
        return false;
    }

    private static class StreamTester
    extends ProxyReceiver {
        public StreamTester(PipelineConfiguration pipe) {
            super(new Sink(pipe));
        }

        @Override
        public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
            throw new QuitParsingException(false);
        }
    }
}

