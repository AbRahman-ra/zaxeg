/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.stax;

import java.util.Properties;
import javax.xml.transform.Result;
import javax.xml.transform.stax.StAXResult;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.lib.StAXResultHandler;
import net.sf.saxon.stax.ReceiverToXMLStreamWriter;

public class StAXResultHandlerImpl
implements StAXResultHandler {
    @Override
    public Receiver getReceiver(Result result, Properties properties) {
        if (((StAXResult)result).getXMLStreamWriter() != null) {
            return new ReceiverToXMLStreamWriter(((StAXResult)result).getXMLStreamWriter());
        }
        if (((StAXResult)result).getXMLEventWriter() != null) {
            throw new UnsupportedOperationException("XMLEventWriter is currently not supported as a Saxon output destination");
        }
        throw new IllegalStateException("StAXResult contains neither an XMLStreamWriter nor XMLEventWriter");
    }
}

