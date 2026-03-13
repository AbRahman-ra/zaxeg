/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.MimeContext;
import org.bouncycastle.mime.MimeParserContext;

public interface MimeParserListener {
    public MimeContext createContext(MimeParserContext var1, Headers var2);

    public void object(MimeParserContext var1, Headers var2, InputStream var3) throws IOException;
}

