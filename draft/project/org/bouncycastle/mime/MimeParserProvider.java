/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.mime.Headers;
import org.bouncycastle.mime.MimeParser;

public interface MimeParserProvider {
    public MimeParser createParser(InputStream var1) throws IOException;

    public MimeParser createParser(Headers var1, InputStream var2) throws IOException;
}

