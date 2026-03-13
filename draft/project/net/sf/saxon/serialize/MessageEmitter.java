/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.io.IOException;
import java.util.Properties;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.XMLEmitter;
import net.sf.saxon.trans.XPathException;

public class MessageEmitter
extends XMLEmitter {
    @Override
    public void setPipelineConfiguration(PipelineConfiguration pipelineConfiguration) {
        super.setPipelineConfiguration(pipelineConfiguration);
        if (this.writer == null && this.outputStream == null) {
            try {
                this.setWriter(this.getConfiguration().getLogger().asWriter());
            } catch (XPathException e) {
                throw new AssertionError((Object)e);
            }
        }
        try {
            Properties props = new Properties();
            props.setProperty("method", "xml");
            props.setProperty("indent", "yes");
            props.setProperty("omit-xml-declaration", "yes");
            this.setOutputProperties(props);
        } catch (XPathException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        if (!this.suppressProcessingInstruction(target, data, locationId, properties)) {
            super.processingInstruction(target, data, locationId, properties);
        }
    }

    protected boolean suppressProcessingInstruction(String target, CharSequence data, Location locationId, int properties) {
        return target.equals("error-code");
    }

    @Override
    public void endDocument() throws XPathException {
        try {
            if (this.writer != null) {
                this.writer.write(10);
                this.writer.flush();
            }
        } catch (IOException err) {
            throw new XPathException(err);
        }
        super.endDocument();
    }

    @Override
    public void close() throws XPathException {
        try {
            if (this.writer != null) {
                this.writer.flush();
            }
        } catch (IOException err) {
            throw new XPathException(err);
        }
        super.close();
    }
}

