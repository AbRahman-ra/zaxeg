/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import net.sf.saxon.style.PackageVersion;

public class DocumentKey {
    public static final boolean CASE_BLIND_FILES = new File("a").equals(new File("A"));
    private String displayValue;
    private String normalizedValue;
    private String packageName = "";
    private PackageVersion packageVersion = PackageVersion.ONE;

    public DocumentKey(String uri) {
        Objects.requireNonNull(uri);
        this.displayValue = uri;
        this.normalizedValue = DocumentKey.normalizeURI(uri);
    }

    public DocumentKey(String uri, String packageName, PackageVersion version) {
        Objects.requireNonNull(uri);
        this.displayValue = uri;
        this.normalizedValue = DocumentKey.normalizeURI(uri);
        this.packageName = packageName;
        this.packageVersion = version;
    }

    public String getAbsoluteURI() {
        return this.displayValue;
    }

    public String toString() {
        return this.displayValue;
    }

    public boolean equals(Object obj) {
        return obj instanceof DocumentKey && this.normalizedValue.equals(((DocumentKey)obj).normalizedValue) && this.packageName.equals(((DocumentKey)obj).packageName) && this.packageVersion.equals(((DocumentKey)obj).packageVersion);
    }

    public int hashCode() {
        return this.normalizedValue.hashCode();
    }

    public static String normalizeURI(String uri) {
        if (uri == null) {
            return null;
        }
        if (uri.startsWith("FILE:")) {
            uri = "file:" + uri.substring(5);
        }
        if (uri.startsWith("file:")) {
            if (uri.startsWith("file:///")) {
                uri = "file:/" + uri.substring(8);
            }
            if (uri.startsWith("file:/")) {
                try {
                    String cpath = new File(uri.substring(5)).getCanonicalPath();
                    uri = "file:" + cpath;
                } catch (IOException iOException) {
                    // empty catch block
                }
            }
            if (CASE_BLIND_FILES) {
                uri = uri.toLowerCase();
            }
        }
        return uri;
    }
}

