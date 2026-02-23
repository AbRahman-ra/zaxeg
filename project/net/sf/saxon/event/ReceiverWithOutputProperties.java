/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.Properties;
import net.sf.saxon.event.Receiver;

public interface ReceiverWithOutputProperties
extends Receiver {
    public Properties getOutputProperties();
}

