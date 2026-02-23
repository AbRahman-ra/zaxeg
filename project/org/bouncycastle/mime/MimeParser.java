/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.mime;

import java.io.IOException;
import org.bouncycastle.mime.MimeParserListener;

public interface MimeParser {
    public void parse(MimeParserListener var1) throws IOException;
}

