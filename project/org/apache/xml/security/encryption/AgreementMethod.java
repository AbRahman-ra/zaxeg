/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.encryption;

import java.util.Iterator;
import org.apache.xml.security.keys.KeyInfo;
import org.w3c.dom.Element;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public interface AgreementMethod {
    public byte[] getKANonce();

    public void setKANonce(byte[] var1);

    public Iterator<Element> getAgreementMethodInformation();

    public void addAgreementMethodInformation(Element var1);

    public void revoveAgreementMethodInformation(Element var1);

    public KeyInfo getOriginatorKeyInfo();

    public void setOriginatorKeyInfo(KeyInfo var1);

    public KeyInfo getRecipientKeyInfo();

    public void setRecipientKeyInfo(KeyInfo var1);

    public String getAlgorithm();
}

