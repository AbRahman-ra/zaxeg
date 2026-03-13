/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cms;

import java.io.InputStream;
import java.io.OutputStream;
import org.bouncycastle.operator.InputAEADDecryptor;
import org.bouncycastle.operator.InputDecryptor;
import org.bouncycastle.operator.MacCalculator;
import org.bouncycastle.util.io.TeeInputStream;

public class RecipientOperator {
    private final Object operator;

    public RecipientOperator(InputDecryptor inputDecryptor) {
        this.operator = inputDecryptor;
    }

    public RecipientOperator(MacCalculator macCalculator) {
        this.operator = macCalculator;
    }

    public InputStream getInputStream(InputStream inputStream) {
        if (this.operator instanceof InputDecryptor) {
            return ((InputDecryptor)this.operator).getInputStream(inputStream);
        }
        return new TeeInputStream(inputStream, ((MacCalculator)this.operator).getOutputStream());
    }

    public boolean isAEADBased() {
        return this.operator instanceof InputAEADDecryptor;
    }

    public OutputStream getAADStream() {
        return ((InputAEADDecryptor)this.operator).getAADStream();
    }

    public boolean isMacBased() {
        return this.operator instanceof MacCalculator;
    }

    public byte[] getMac() {
        return ((MacCalculator)this.operator).getMac();
    }
}

