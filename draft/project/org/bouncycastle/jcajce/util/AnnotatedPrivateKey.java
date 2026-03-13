/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.util;

import java.security.PrivateKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class AnnotatedPrivateKey
implements PrivateKey {
    public static final String LABEL = "label";
    private final PrivateKey key;
    private final Map<String, Object> annotations;

    AnnotatedPrivateKey(PrivateKey privateKey, String string) {
        this.key = privateKey;
        this.annotations = Collections.singletonMap(LABEL, string);
    }

    AnnotatedPrivateKey(PrivateKey privateKey, Map<String, Object> map) {
        this.key = privateKey;
        this.annotations = map;
    }

    public PrivateKey getKey() {
        return this.key;
    }

    public Map<String, Object> getAnnotations() {
        return this.annotations;
    }

    @Override
    public String getAlgorithm() {
        return this.key.getAlgorithm();
    }

    public Object getAnnotation(String string) {
        return this.annotations.get(string);
    }

    public AnnotatedPrivateKey addAnnotation(String string, Object object) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>(this.annotations);
        hashMap.put(string, object);
        return new AnnotatedPrivateKey(this.key, Collections.unmodifiableMap(hashMap));
    }

    public AnnotatedPrivateKey removeAnnotation(String string) {
        HashMap<String, Object> hashMap = new HashMap<String, Object>(this.annotations);
        hashMap.remove(string);
        return new AnnotatedPrivateKey(this.key, Collections.unmodifiableMap(hashMap));
    }

    @Override
    public String getFormat() {
        return this.key.getFormat();
    }

    @Override
    public byte[] getEncoded() {
        return this.key.getEncoded();
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public boolean equals(Object object) {
        if (object instanceof AnnotatedPrivateKey) {
            return this.key.equals(((AnnotatedPrivateKey)object).key);
        }
        return this.key.equals(object);
    }

    public String toString() {
        if (this.annotations.containsKey(LABEL)) {
            return this.annotations.get(LABEL).toString();
        }
        return this.key.toString();
    }
}

