/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import net.sf.saxon.dom.NodeOverNodeInfo;
import org.w3c.dom.DOMException;
import org.w3c.dom.ProcessingInstruction;

public class PIOverNodeInfo
extends NodeOverNodeInfo
implements ProcessingInstruction {
    @Override
    public String getTarget() {
        return this.node.getLocalPart();
    }

    @Override
    public String getData() {
        return this.node.getStringValue();
    }

    @Override
    public void setData(String data) throws DOMException {
        PIOverNodeInfo.disallowUpdate();
    }
}

