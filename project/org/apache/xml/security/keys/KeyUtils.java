/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.xml.security.keys;

import java.io.PrintStream;
import java.security.PublicKey;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.keys.content.KeyName;
import org.apache.xml.security.keys.content.KeyValue;
import org.apache.xml.security.keys.content.MgmtData;
import org.apache.xml.security.keys.content.X509Data;
import org.apache.xml.security.utils.SignatureElementProxy;

public class KeyUtils {
    private KeyUtils() {
    }

    public static void prinoutKeyInfo(KeyInfo ki, PrintStream os) throws XMLSecurityException {
        SignatureElementProxy x;
        int i;
        for (i = 0; i < ki.lengthKeyName(); ++i) {
            x = ki.itemKeyName(i);
            os.println("KeyName(" + i + ")=\"" + ((KeyName)x).getKeyName() + "\"");
        }
        for (i = 0; i < ki.lengthKeyValue(); ++i) {
            x = ki.itemKeyValue(i);
            PublicKey pk = ((KeyValue)x).getPublicKey();
            os.println("KeyValue Nr. " + i);
            os.println(pk);
        }
        for (i = 0; i < ki.lengthMgmtData(); ++i) {
            x = ki.itemMgmtData(i);
            os.println("MgmtData(" + i + ")=\"" + ((MgmtData)x).getMgmtData() + "\"");
        }
        for (i = 0; i < ki.lengthX509Data(); ++i) {
            x = ki.itemX509Data(i);
            os.println("X509Data(" + i + ")=\"" + (((X509Data)x).containsCertificate() ? "Certificate " : "") + (((X509Data)x).containsIssuerSerial() ? "IssuerSerial " : "") + "\"");
        }
    }
}

