/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.mime.Headers;

public interface MimeContext {
    public InputStream applyContext(Headers var1, InputStream var2) throws IOException;
}

