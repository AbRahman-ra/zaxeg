/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.crypto.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.asn1.sec.SECObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.util.Strings;

public class SSHNamedCurves {
    private static final Map<ASN1ObjectIdentifier, String> oidToName;
    private static final Map<String, ASN1ObjectIdentifier> oidMap;
    private static final Map<String, String> curveNameToSSHName;
    private static HashMap<ECCurve, String> curveMap;

    public static ASN1ObjectIdentifier getByName(String string) {
        return oidMap.get(string);
    }

    public static X9ECParameters getParameters(String string) {
        return NISTNamedCurves.getByOID(oidMap.get(Strings.toLowerCase(string)));
    }

    public static X9ECParameters getParameters(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return NISTNamedCurves.getByOID(aSN1ObjectIdentifier);
    }

    public static String getName(ASN1ObjectIdentifier aSN1ObjectIdentifier) {
        return oidToName.get(aSN1ObjectIdentifier);
    }

    public static String getNameForParameters(ECDomainParameters eCDomainParameters) {
        if (eCDomainParameters instanceof ECNamedDomainParameters) {
            return SSHNamedCurves.getName(((ECNamedDomainParameters)eCDomainParameters).getName());
        }
        return SSHNamedCurves.getNameForParameters(eCDomainParameters.getCurve());
    }

    public static String getNameForParameters(ECCurve eCCurve) {
        return curveNameToSSHName.get(curveMap.get(eCCurve));
    }

    static {
        oidMap = Collections.unmodifiableMap(new HashMap<String, ASN1ObjectIdentifier>(){
            {
                this.put("nistp256", SECObjectIdentifiers.secp256r1);
                this.put("nistp384", SECObjectIdentifiers.secp384r1);
                this.put("nistp521", SECObjectIdentifiers.secp521r1);
                this.put("nistk163", SECObjectIdentifiers.sect163k1);
                this.put("nistp192", SECObjectIdentifiers.secp192r1);
                this.put("nistp224", SECObjectIdentifiers.secp224r1);
                this.put("nistk233", SECObjectIdentifiers.sect233k1);
                this.put("nistb233", SECObjectIdentifiers.sect233r1);
                this.put("nistk283", SECObjectIdentifiers.sect283k1);
                this.put("nistk409", SECObjectIdentifiers.sect409k1);
                this.put("nistb409", SECObjectIdentifiers.sect409r1);
                this.put("nistt571", SECObjectIdentifiers.sect571k1);
            }
        });
        curveNameToSSHName = Collections.unmodifiableMap(new HashMap<String, String>(){
            {
                String[][] stringArrayArray = new String[][]{{"secp256r1", "nistp256"}, {"secp384r1", "nistp384"}, {"secp521r1", "nistp521"}, {"sect163k1", "nistk163"}, {"secp192r1", "nistp192"}, {"secp224r1", "nistp224"}, {"sect233k1", "nistk233"}, {"sect233r1", "nistb233"}, {"sect283k1", "nistk283"}, {"sect409k1", "nistk409"}, {"sect409r1", "nistb409"}, {"sect571k1", "nistt571"}};
                for (int i = 0; i != stringArrayArray.length; ++i) {
                    String[] stringArray = stringArrayArray[i];
                    this.put(stringArray[0], stringArray[1]);
                }
            }
        });
        curveMap = new HashMap<ECCurve, String>(){
            {
                Enumeration enumeration = CustomNamedCurves.getNames();
                while (enumeration.hasMoreElements()) {
                    String string = (String)enumeration.nextElement();
                    X9ECParameters x9ECParameters = CustomNamedCurves.getByName(string);
                    this.put(x9ECParameters.getCurve(), string);
                }
            }
        };
        oidToName = Collections.unmodifiableMap(new HashMap<ASN1ObjectIdentifier, String>(){
            {
                for (String string : oidMap.keySet()) {
                    this.put(oidMap.get(string), string);
                }
            }
        });
    }
}

