/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.ArrayList;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import net.sf.saxon.event.CloseNotifier;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.ResultDocumentResolver;
import net.sf.saxon.lib.SerializerFactory;
import net.sf.saxon.s9api.Action;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;

public class OutputURIResolverWrapper
implements ResultDocumentResolver {
    private OutputURIResolver outputURIResolver;

    public OutputURIResolverWrapper(OutputURIResolver resolver) {
        this.outputURIResolver = resolver;
    }

    @Override
    public Receiver resolve(XPathContext context, String href, String baseUri, SerializationProperties properties) throws XPathException {
        OutputURIResolver r2 = this.outputURIResolver.newInstance();
        try {
            Receiver out;
            Result result = r2.resolve(href, baseUri);
            Action onClose = () -> {
                try {
                    r2.close(result);
                } catch (TransformerException te) {
                    throw new UncheckedXPathException(XPathException.makeXPathException(te));
                }
            };
            if (result instanceof Receiver) {
                out = (Receiver)result;
            } else {
                SerializerFactory factory = context.getConfiguration().getSerializerFactory();
                PipelineConfiguration pipe = context.getController().makePipelineConfiguration();
                pipe.setXPathContext(context);
                out = factory.getReceiver(result, properties, pipe);
            }
            ArrayList<Action> actions = new ArrayList<Action>();
            actions.add(onClose);
            return new CloseNotifier(out, actions);
        } catch (TransformerException e) {
            throw XPathException.makeXPathException(e);
        }
    }

    public OutputURIResolver getOutputURIResolver() {
        return this.outputURIResolver;
    }
}

