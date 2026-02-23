/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.jaxp;

import java.util.Properties;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import net.sf.saxon.jaxp.StreamingTransformerImpl;
import net.sf.saxon.jaxp.TransformerImpl;
import net.sf.saxon.s9api.XsltExecutable;

public class TemplatesImpl
implements Templates {
    private XsltExecutable executable;
    private boolean forceStreaming;

    public TemplatesImpl(XsltExecutable executable) {
        this.executable = executable;
    }

    @Override
    public Transformer newTransformer() {
        if (this.forceStreaming) {
            return new StreamingTransformerImpl(this.executable, this.executable.load30());
        }
        return new TransformerImpl(this.executable, this.executable.load());
    }

    @Override
    public Properties getOutputProperties() {
        Properties details = this.executable.getUnderlyingCompiledStylesheet().getPrimarySerializationProperties().getProperties();
        return new Properties(details);
    }

    public boolean isForceStreaming() {
        return this.forceStreaming;
    }

    public void setForceStreaming(boolean forceStreaming) {
        this.forceStreaming = forceStreaming;
    }

    public XsltExecutable getImplementation() {
        return this.executable;
    }
}

