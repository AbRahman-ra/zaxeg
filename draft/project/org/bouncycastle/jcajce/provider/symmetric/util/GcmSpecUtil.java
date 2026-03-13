/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jcajce.provider.symmetric.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.internal.asn1.cms.GCMParameters;
import org.bouncycastle.jcajce.provider.symmetric.util.ClassUtil;
import org.bouncycastle.util.Integers;

public class GcmSpecUtil {
    static final Class gcmSpecClass = ClassUtil.loadClass(GcmSpecUtil.class, "javax.crypto.spec.GCMParameterSpec");
    static final Method tLen;
    static final Method iv;

    private static Method extractMethod(final String string) {
        try {
            return (Method)AccessController.doPrivileged(new PrivilegedExceptionAction(){

                public Object run() throws Exception {
                    return gcmSpecClass.getDeclaredMethod(string, new Class[0]);
                }
            });
        } catch (PrivilegedActionException privilegedActionException) {
            return null;
        }
    }

    public static boolean gcmSpecExists() {
        return gcmSpecClass != null;
    }

    public static boolean isGcmSpec(AlgorithmParameterSpec algorithmParameterSpec) {
        return gcmSpecClass != null && gcmSpecClass.isInstance(algorithmParameterSpec);
    }

    public static boolean isGcmSpec(Class clazz) {
        return gcmSpecClass == clazz;
    }

    public static AlgorithmParameterSpec extractGcmSpec(ASN1Primitive aSN1Primitive) throws InvalidParameterSpecException {
        try {
            GCMParameters gCMParameters = GCMParameters.getInstance(aSN1Primitive);
            Constructor constructor = gcmSpecClass.getConstructor(Integer.TYPE, byte[].class);
            return (AlgorithmParameterSpec)constructor.newInstance(Integers.valueOf(gCMParameters.getIcvLen() * 8), gCMParameters.getNonce());
        } catch (NoSuchMethodException noSuchMethodException) {
            throw new InvalidParameterSpecException("No constructor found!");
        } catch (Exception exception) {
            throw new InvalidParameterSpecException("Construction failed: " + exception.getMessage());
        }
    }

    static AEADParameters extractAeadParameters(final KeyParameter keyParameter, final AlgorithmParameterSpec algorithmParameterSpec) throws InvalidAlgorithmParameterException {
        try {
            return (AEADParameters)AccessController.doPrivileged(new PrivilegedExceptionAction(){

                public Object run() throws Exception {
                    return new AEADParameters(keyParameter, (Integer)tLen.invoke(algorithmParameterSpec, new Object[0]), (byte[])iv.invoke(algorithmParameterSpec, new Object[0]));
                }
            });
        } catch (Exception exception) {
            throw new InvalidAlgorithmParameterException("Cannot process GCMParameterSpec.");
        }
    }

    public static GCMParameters extractGcmParameters(final AlgorithmParameterSpec algorithmParameterSpec) throws InvalidParameterSpecException {
        try {
            return (GCMParameters)AccessController.doPrivileged(new PrivilegedExceptionAction(){

                public Object run() throws Exception {
                    return new GCMParameters((byte[])iv.invoke(algorithmParameterSpec, new Object[0]), (Integer)tLen.invoke(algorithmParameterSpec, new Object[0]) / 8);
                }
            });
        } catch (Exception exception) {
            throw new InvalidParameterSpecException("Cannot process GCMParameterSpec");
        }
    }

    static {
        if (gcmSpecClass != null) {
            tLen = GcmSpecUtil.extractMethod("getTLen");
            iv = GcmSpecUtil.extractMethod("getIV");
        } else {
            tLen = null;
            iv = null;
        }
    }
}

