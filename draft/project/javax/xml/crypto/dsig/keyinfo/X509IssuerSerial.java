/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.keyinfo;

import java.math.BigInteger;
import javax.xml.crypto.XMLStructure;

public interface X509IssuerSerial
extends XMLStructure {
    public String getIssuerName();

    public BigInteger getSerialNumber();
}

