/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.tsp.ers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.tsp.ers.ERSCachingData;
import org.bouncycastle.tsp.ers.ERSUtil;

public class ERSFileData
extends ERSCachingData {
    private final File content;

    public ERSFileData(File file) throws FileNotFoundException {
        if (file.isDirectory()) {
            throw new IllegalArgumentException("directory not allowed as ERSFileData");
        }
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath() + " does not exist");
        }
        if (!file.canRead()) {
            throw new FileNotFoundException(file.getAbsolutePath() + " is not readable");
        }
        this.content = file;
    }

    protected byte[] calculateHash(DigestCalculator digestCalculator) {
        try {
            FileInputStream fileInputStream = new FileInputStream(this.content);
            byte[] byArray = ERSUtil.calculateDigest(digestCalculator, fileInputStream);
            ((InputStream)fileInputStream).close();
            return byArray;
        } catch (IOException iOException) {
            throw new IllegalStateException("unable to process " + this.content.getAbsolutePath());
        }
    }
}

