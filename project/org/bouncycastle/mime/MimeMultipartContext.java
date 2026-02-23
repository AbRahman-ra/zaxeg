/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import org.bouncycastle.mime.MimeContext;

public interface MimeMultipartContext
extends MimeContext {
    public MimeContext createContext(int var1) throws IOException;
}

