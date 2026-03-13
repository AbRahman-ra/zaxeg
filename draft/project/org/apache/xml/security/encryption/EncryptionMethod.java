/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.util.Iterator;
import org.w3c.dom.Element;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface EncryptionMethod {
    public String getAlgorithm();

    public int getKeySize();

    public void setKeySize(int var1);

    public byte[] getOAEPparams();

    public void setOAEPparams(byte[] var1);

    public void setDigestAlgorithm(String var1);

    public String getDigestAlgorithm();

    public void setMGFAlgorithm(String var1);

    public String getMGFAlgorithm();

    public Iterator<Element> getEncryptionMethodInformation();

    public void addEncryptionMethodInformation(Element var1);

    public void removeEncryptionMethodInformation(Element var1);
}

