/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.asn1.x509;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.asn1.x500.style.RFC4519Style;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.NameConstraintValidator;
import org.bouncycastle.asn1.x509.NameConstraintValidatorException;
import org.bouncycastle.asn1.x509.OtherName;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Strings;
import org.bouncycastle.util.encoders.Hex;

public class PKIXNameConstraintValidator
implements NameConstraintValidator {
    private Set excludedSubtreesDN = new HashSet();
    private Set excludedSubtreesDNS = new HashSet();
    private Set excludedSubtreesEmail = new HashSet();
    private Set excludedSubtreesURI = new HashSet();
    private Set excludedSubtreesIP = new HashSet();
    private Set excludedSubtreesOtherName = new HashSet();
    private Set permittedSubtreesDN;
    private Set permittedSubtreesDNS;
    private Set permittedSubtreesEmail;
    private Set permittedSubtreesURI;
    private Set permittedSubtreesIP;
    private Set permittedSubtreesOtherName;

    public void checkPermitted(GeneralName generalName) throws NameConstraintValidatorException {
        switch (generalName.getTagNo()) {
            case 0: {
                this.checkPermittedOtherName(this.permittedSubtreesOtherName, OtherName.getInstance(generalName.getName()));
                break;
            }
            case 1: {
                this.checkPermittedEmail(this.permittedSubtreesEmail, this.extractNameAsString(generalName));
                break;
            }
            case 2: {
                this.checkPermittedDNS(this.permittedSubtreesDNS, this.extractNameAsString(generalName));
                break;
            }
            case 4: {
                this.checkPermittedDN(X500Name.getInstance(generalName.getName()));
                break;
            }
            case 6: {
                this.checkPermittedURI(this.permittedSubtreesURI, this.extractNameAsString(generalName));
                break;
            }
            case 7: {
                this.checkPermittedIP(this.permittedSubtreesIP, ASN1OctetString.getInstance(generalName.getName()).getOctets());
                break;
            }
        }
    }

    public void checkExcluded(GeneralName generalName) throws NameConstraintValidatorException {
        switch (generalName.getTagNo()) {
            case 0: {
                this.checkExcludedOtherName(this.excludedSubtreesOtherName, OtherName.getInstance(generalName.getName()));
                break;
            }
            case 1: {
                this.checkExcludedEmail(this.excludedSubtreesEmail, this.extractNameAsString(generalName));
                break;
            }
            case 2: {
                this.checkExcludedDNS(this.excludedSubtreesDNS, this.extractNameAsString(generalName));
                break;
            }
            case 4: {
                this.checkExcludedDN(X500Name.getInstance(generalName.getName()));
                break;
            }
            case 6: {
                this.checkExcludedURI(this.excludedSubtreesURI, this.extractNameAsString(generalName));
                break;
            }
            case 7: {
                this.checkExcludedIP(this.excludedSubtreesIP, ASN1OctetString.getInstance(generalName.getName()).getOctets());
                break;
            }
        }
    }

    public void intersectPermittedSubtree(GeneralSubtree generalSubtree) {
        this.intersectPermittedSubtree(new GeneralSubtree[]{generalSubtree});
    }

    public void intersectPermittedSubtree(GeneralSubtree[] generalSubtreeArray) {
        HashMap hashMap = new HashMap();
        for (int i = 0; i != generalSubtreeArray.length; ++i) {
            GeneralSubtree object = generalSubtreeArray[i];
            Integer n = Integers.valueOf(object.getBase().getTagNo());
            if (hashMap.get(n) == null) {
                hashMap.put(n, new HashSet());
            }
            ((Set)hashMap.get(n)).add(object);
        }
        block9: for (Map.Entry entry : hashMap.entrySet()) {
            int n = (Integer)entry.getKey();
            switch (n) {
                case 0: {
                    this.permittedSubtreesOtherName = this.intersectOtherName(this.permittedSubtreesOtherName, (Set)entry.getValue());
                    continue block9;
                }
                case 1: {
                    this.permittedSubtreesEmail = this.intersectEmail(this.permittedSubtreesEmail, (Set)entry.getValue());
                    continue block9;
                }
                case 2: {
                    this.permittedSubtreesDNS = this.intersectDNS(this.permittedSubtreesDNS, (Set)entry.getValue());
                    continue block9;
                }
                case 4: {
                    this.permittedSubtreesDN = this.intersectDN(this.permittedSubtreesDN, (Set)entry.getValue());
                    continue block9;
                }
                case 6: {
                    this.permittedSubtreesURI = this.intersectURI(this.permittedSubtreesURI, (Set)entry.getValue());
                    continue block9;
                }
                case 7: {
                    this.permittedSubtreesIP = this.intersectIP(this.permittedSubtreesIP, (Set)entry.getValue());
                    continue block9;
                }
            }
            throw new IllegalStateException("Unknown tag encountered: " + n);
        }
    }

    public void intersectEmptyPermittedSubtree(int n) {
        switch (n) {
            case 0: {
                this.permittedSubtreesOtherName = new HashSet();
                break;
            }
            case 1: {
                this.permittedSubtreesEmail = new HashSet();
                break;
            }
            case 2: {
                this.permittedSubtreesDNS = new HashSet();
                break;
            }
            case 4: {
                this.permittedSubtreesDN = new HashSet();
                break;
            }
            case 6: {
                this.permittedSubtreesURI = new HashSet();
                break;
            }
            case 7: {
                this.permittedSubtreesIP = new HashSet();
                break;
            }
            default: {
                throw new IllegalStateException("Unknown tag encountered: " + n);
            }
        }
    }

    public void addExcludedSubtree(GeneralSubtree generalSubtree) {
        GeneralName generalName = generalSubtree.getBase();
        switch (generalName.getTagNo()) {
            case 0: {
                this.excludedSubtreesOtherName = this.unionOtherName(this.excludedSubtreesOtherName, OtherName.getInstance(generalName.getName()));
                break;
            }
            case 1: {
                this.excludedSubtreesEmail = this.unionEmail(this.excludedSubtreesEmail, this.extractNameAsString(generalName));
                break;
            }
            case 2: {
                this.excludedSubtreesDNS = this.unionDNS(this.excludedSubtreesDNS, this.extractNameAsString(generalName));
                break;
            }
            case 4: {
                this.excludedSubtreesDN = this.unionDN(this.excludedSubtreesDN, (ASN1Sequence)generalName.getName().toASN1Primitive());
                break;
            }
            case 6: {
                this.excludedSubtreesURI = this.unionURI(this.excludedSubtreesURI, this.extractNameAsString(generalName));
                break;
            }
            case 7: {
                this.excludedSubtreesIP = this.unionIP(this.excludedSubtreesIP, ASN1OctetString.getInstance(generalName.getName()).getOctets());
                break;
            }
            default: {
                throw new IllegalStateException("Unknown tag encountered: " + generalName.getTagNo());
            }
        }
    }

    public int hashCode() {
        return this.hashCollection(this.excludedSubtreesDN) + this.hashCollection(this.excludedSubtreesDNS) + this.hashCollection(this.excludedSubtreesEmail) + this.hashCollection(this.excludedSubtreesIP) + this.hashCollection(this.excludedSubtreesURI) + this.hashCollection(this.excludedSubtreesOtherName) + this.hashCollection(this.permittedSubtreesDN) + this.hashCollection(this.permittedSubtreesDNS) + this.hashCollection(this.permittedSubtreesEmail) + this.hashCollection(this.permittedSubtreesIP) + this.hashCollection(this.permittedSubtreesURI) + this.hashCollection(this.permittedSubtreesOtherName);
    }

    public boolean equals(Object object) {
        if (!(object instanceof PKIXNameConstraintValidator)) {
            return false;
        }
        PKIXNameConstraintValidator pKIXNameConstraintValidator = (PKIXNameConstraintValidator)object;
        return this.collectionsAreEqual(pKIXNameConstraintValidator.excludedSubtreesDN, this.excludedSubtreesDN) && this.collectionsAreEqual(pKIXNameConstraintValidator.excludedSubtreesDNS, this.excludedSubtreesDNS) && this.collectionsAreEqual(pKIXNameConstraintValidator.excludedSubtreesEmail, this.excludedSubtreesEmail) && this.collectionsAreEqual(pKIXNameConstraintValidator.excludedSubtreesIP, this.excludedSubtreesIP) && this.collectionsAreEqual(pKIXNameConstraintValidator.excludedSubtreesURI, this.excludedSubtreesURI) && this.collectionsAreEqual(pKIXNameConstraintValidator.excludedSubtreesOtherName, this.excludedSubtreesOtherName) && this.collectionsAreEqual(pKIXNameConstraintValidator.permittedSubtreesDN, this.permittedSubtreesDN) && this.collectionsAreEqual(pKIXNameConstraintValidator.permittedSubtreesDNS, this.permittedSubtreesDNS) && this.collectionsAreEqual(pKIXNameConstraintValidator.permittedSubtreesEmail, this.permittedSubtreesEmail) && this.collectionsAreEqual(pKIXNameConstraintValidator.permittedSubtreesIP, this.permittedSubtreesIP) && this.collectionsAreEqual(pKIXNameConstraintValidator.permittedSubtreesURI, this.permittedSubtreesURI) && this.collectionsAreEqual(pKIXNameConstraintValidator.permittedSubtreesOtherName, this.permittedSubtreesOtherName);
    }

    public void checkPermittedDN(X500Name x500Name) throws NameConstraintValidatorException {
        this.checkPermittedDN(this.permittedSubtreesDN, ASN1Sequence.getInstance(x500Name.toASN1Primitive()));
    }

    public void checkExcludedDN(X500Name x500Name) throws NameConstraintValidatorException {
        this.checkExcludedDN(this.excludedSubtreesDN, ASN1Sequence.getInstance(x500Name));
    }

    private static boolean withinDNSubtree(ASN1Sequence aSN1Sequence, ASN1Sequence aSN1Sequence2) {
        RDN rDN;
        int n;
        if (aSN1Sequence2.size() < 1) {
            return false;
        }
        if (aSN1Sequence2.size() > aSN1Sequence.size()) {
            return false;
        }
        int n2 = 0;
        RDN rDN2 = RDN.getInstance(aSN1Sequence2.getObjectAt(0));
        for (n = 0; n < aSN1Sequence.size(); ++n) {
            n2 = n;
            rDN = RDN.getInstance(aSN1Sequence.getObjectAt(n));
            if (IETFUtils.rDNAreEqual(rDN2, rDN)) break;
        }
        if (aSN1Sequence2.size() > aSN1Sequence.size() - n2) {
            return false;
        }
        for (n = 0; n < aSN1Sequence2.size(); ++n) {
            rDN = RDN.getInstance(aSN1Sequence2.getObjectAt(n));
            RDN rDN3 = RDN.getInstance(aSN1Sequence.getObjectAt(n2 + n));
            if (rDN.size() == rDN3.size()) {
                if (!rDN.getFirst().getType().equals(rDN3.getFirst().getType())) {
                    return false;
                }
                if (!(rDN.size() == 1 && rDN.getFirst().getType().equals(RFC4519Style.serialNumber) ? !rDN3.getFirst().getValue().toString().startsWith(rDN.getFirst().getValue().toString()) : !IETFUtils.rDNAreEqual(rDN, rDN3))) continue;
                return false;
            }
            return false;
        }
        return true;
    }

    private void checkPermittedDN(Set set, ASN1Sequence aSN1Sequence) throws NameConstraintValidatorException {
        if (set == null) {
            return;
        }
        if (set.isEmpty() && aSN1Sequence.size() == 0) {
            return;
        }
        for (ASN1Sequence aSN1Sequence2 : set) {
            if (!PKIXNameConstraintValidator.withinDNSubtree(aSN1Sequence, aSN1Sequence2)) continue;
            return;
        }
        throw new NameConstraintValidatorException("Subject distinguished name is not from a permitted subtree");
    }

    private void checkExcludedDN(Set set, ASN1Sequence aSN1Sequence) throws NameConstraintValidatorException {
        if (set.isEmpty()) {
            return;
        }
        for (ASN1Sequence aSN1Sequence2 : set) {
            if (!PKIXNameConstraintValidator.withinDNSubtree(aSN1Sequence, aSN1Sequence2)) continue;
            throw new NameConstraintValidatorException("Subject distinguished name is from an excluded subtree");
        }
    }

    private Set intersectDN(Set set, Set set2) {
        HashSet<ASN1Sequence> hashSet = new HashSet<ASN1Sequence>();
        Iterator iterator = set2.iterator();
        while (iterator.hasNext()) {
            ASN1Sequence aSN1Sequence = ASN1Sequence.getInstance(((GeneralSubtree)iterator.next()).getBase().getName().toASN1Primitive());
            if (set == null) {
                if (aSN1Sequence == null) continue;
                hashSet.add(aSN1Sequence);
                continue;
            }
            for (ASN1Sequence aSN1Sequence2 : set) {
                if (PKIXNameConstraintValidator.withinDNSubtree(aSN1Sequence, aSN1Sequence2)) {
                    hashSet.add(aSN1Sequence);
                    continue;
                }
                if (!PKIXNameConstraintValidator.withinDNSubtree(aSN1Sequence2, aSN1Sequence)) continue;
                hashSet.add(aSN1Sequence2);
            }
        }
        return hashSet;
    }

    private Set unionDN(Set set, ASN1Sequence aSN1Sequence) {
        if (set.isEmpty()) {
            if (aSN1Sequence == null) {
                return set;
            }
            set.add(aSN1Sequence);
            return set;
        }
        HashSet<ASN1Sequence> hashSet = new HashSet<ASN1Sequence>();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            ASN1Sequence aSN1Sequence2 = ASN1Sequence.getInstance(iterator.next());
            if (PKIXNameConstraintValidator.withinDNSubtree(aSN1Sequence, aSN1Sequence2)) {
                hashSet.add(aSN1Sequence2);
                continue;
            }
            if (PKIXNameConstraintValidator.withinDNSubtree(aSN1Sequence2, aSN1Sequence)) {
                hashSet.add(aSN1Sequence);
                continue;
            }
            hashSet.add(aSN1Sequence2);
            hashSet.add(aSN1Sequence);
        }
        return hashSet;
    }

    private Set intersectOtherName(Set set, Set set2) {
        HashSet<OtherName> hashSet = new HashSet<OtherName>();
        Iterator iterator = set2.iterator();
        while (iterator.hasNext()) {
            OtherName otherName = OtherName.getInstance(((GeneralSubtree)iterator.next()).getBase().getName());
            if (set == null) {
                if (otherName == null) continue;
                hashSet.add(otherName);
                continue;
            }
            Iterator iterator2 = set.iterator();
            while (iterator2.hasNext()) {
                OtherName otherName2 = OtherName.getInstance(iterator2.next());
                this.intersectOtherName(otherName, otherName2, hashSet);
            }
        }
        return hashSet;
    }

    private void intersectOtherName(OtherName otherName, OtherName otherName2, Set set) {
        if (otherName.equals(otherName2)) {
            set.add(otherName);
        }
    }

    private Set unionOtherName(Set set, OtherName otherName) {
        HashSet<OtherName> hashSet = set != null ? new HashSet<OtherName>(set) : new HashSet();
        hashSet.add(otherName);
        return hashSet;
    }

    private Set intersectEmail(Set set, Set set2) {
        HashSet<String> hashSet = new HashSet<String>();
        Iterator iterator = set2.iterator();
        while (iterator.hasNext()) {
            String string = this.extractNameAsString(((GeneralSubtree)iterator.next()).getBase());
            if (set == null) {
                if (string == null) continue;
                hashSet.add(string);
                continue;
            }
            for (String string2 : set) {
                this.intersectEmail(string, string2, hashSet);
            }
        }
        return hashSet;
    }

    private Set unionEmail(Set set, String string) {
        if (set.isEmpty()) {
            if (string == null) {
                return set;
            }
            set.add(string);
            return set;
        }
        HashSet hashSet = new HashSet();
        for (String string2 : set) {
            this.unionEmail(string2, string, hashSet);
        }
        return hashSet;
    }

    private Set intersectIP(Set set, Set set2) {
        HashSet<byte[]> hashSet = new HashSet<byte[]>();
        Iterator iterator = set2.iterator();
        while (iterator.hasNext()) {
            byte[] byArray = ASN1OctetString.getInstance(((GeneralSubtree)iterator.next()).getBase().getName()).getOctets();
            if (set == null) {
                if (byArray == null) continue;
                hashSet.add(byArray);
                continue;
            }
            for (byte[] byArray2 : set) {
                hashSet.addAll(this.intersectIPRange(byArray2, byArray));
            }
        }
        return hashSet;
    }

    private Set unionIP(Set set, byte[] byArray) {
        if (set.isEmpty()) {
            if (byArray == null) {
                return set;
            }
            set.add(byArray);
            return set;
        }
        HashSet hashSet = new HashSet();
        for (byte[] byArray2 : set) {
            hashSet.addAll(this.unionIPRange(byArray2, byArray));
        }
        return hashSet;
    }

    private Set unionIPRange(byte[] byArray, byte[] byArray2) {
        HashSet<byte[]> hashSet = new HashSet<byte[]>();
        if (Arrays.areEqual(byArray, byArray2)) {
            hashSet.add(byArray);
        } else {
            hashSet.add(byArray);
            hashSet.add(byArray2);
        }
        return hashSet;
    }

    private Set intersectIPRange(byte[] byArray, byte[] byArray2) {
        if (byArray.length != byArray2.length) {
            return Collections.EMPTY_SET;
        }
        byte[][] byArray3 = this.extractIPsAndSubnetMasks(byArray, byArray2);
        byte[] byArray4 = byArray3[0];
        byte[] byArray5 = byArray3[1];
        byte[] byArray6 = byArray3[2];
        byte[] byArray7 = byArray3[3];
        byte[][] byArray8 = this.minMaxIPs(byArray4, byArray5, byArray6, byArray7);
        byte[] byArray9 = PKIXNameConstraintValidator.min(byArray8[1], byArray8[3]);
        byte[] byArray10 = PKIXNameConstraintValidator.max(byArray8[0], byArray8[2]);
        if (PKIXNameConstraintValidator.compareTo(byArray10, byArray9) == 1) {
            return Collections.EMPTY_SET;
        }
        byte[] byArray11 = PKIXNameConstraintValidator.or(byArray8[0], byArray8[2]);
        byte[] byArray12 = PKIXNameConstraintValidator.or(byArray5, byArray7);
        return Collections.singleton(this.ipWithSubnetMask(byArray11, byArray12));
    }

    private byte[] ipWithSubnetMask(byte[] byArray, byte[] byArray2) {
        int n = byArray.length;
        byte[] byArray3 = new byte[n * 2];
        System.arraycopy(byArray, 0, byArray3, 0, n);
        System.arraycopy(byArray2, 0, byArray3, n, n);
        return byArray3;
    }

    private byte[][] extractIPsAndSubnetMasks(byte[] byArray, byte[] byArray2) {
        int n = byArray.length / 2;
        byte[] byArray3 = new byte[n];
        byte[] byArray4 = new byte[n];
        System.arraycopy(byArray, 0, byArray3, 0, n);
        System.arraycopy(byArray, n, byArray4, 0, n);
        byte[] byArray5 = new byte[n];
        byte[] byArray6 = new byte[n];
        System.arraycopy(byArray2, 0, byArray5, 0, n);
        System.arraycopy(byArray2, n, byArray6, 0, n);
        return new byte[][]{byArray3, byArray4, byArray5, byArray6};
    }

    private byte[][] minMaxIPs(byte[] byArray, byte[] byArray2, byte[] byArray3, byte[] byArray4) {
        int n = byArray.length;
        byte[] byArray5 = new byte[n];
        byte[] byArray6 = new byte[n];
        byte[] byArray7 = new byte[n];
        byte[] byArray8 = new byte[n];
        for (int i = 0; i < n; ++i) {
            byArray5[i] = (byte)(byArray[i] & byArray2[i]);
            byArray6[i] = (byte)(byArray[i] & byArray2[i] | ~byArray2[i]);
            byArray7[i] = (byte)(byArray3[i] & byArray4[i]);
            byArray8[i] = (byte)(byArray3[i] & byArray4[i] | ~byArray4[i]);
        }
        return new byte[][]{byArray5, byArray6, byArray7, byArray8};
    }

    private void checkPermittedEmail(Set set, String string) throws NameConstraintValidatorException {
        if (set == null) {
            return;
        }
        for (String string2 : set) {
            if (!this.emailIsConstrained(string, string2)) continue;
            return;
        }
        if (string.length() == 0 && set.size() == 0) {
            return;
        }
        throw new NameConstraintValidatorException("Subject email address is not from a permitted subtree.");
    }

    private void checkPermittedOtherName(Set set, OtherName otherName) throws NameConstraintValidatorException {
        if (set == null) {
            return;
        }
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            OtherName otherName2 = OtherName.getInstance(iterator.next());
            if (!this.otherNameIsConstrained(otherName, otherName2)) continue;
            return;
        }
        throw new NameConstraintValidatorException("Subject OtherName is not from a permitted subtree.");
    }

    private void checkExcludedOtherName(Set set, OtherName otherName) throws NameConstraintValidatorException {
        if (set.isEmpty()) {
            return;
        }
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            OtherName otherName2 = OtherName.getInstance(iterator.next());
            if (!this.otherNameIsConstrained(otherName, otherName2)) continue;
            throw new NameConstraintValidatorException("OtherName is from an excluded subtree.");
        }
    }

    private void checkExcludedEmail(Set set, String string) throws NameConstraintValidatorException {
        if (set.isEmpty()) {
            return;
        }
        for (String string2 : set) {
            if (!this.emailIsConstrained(string, string2)) continue;
            throw new NameConstraintValidatorException("Email address is from an excluded subtree.");
        }
    }

    private void checkPermittedIP(Set set, byte[] byArray) throws NameConstraintValidatorException {
        if (set == null) {
            return;
        }
        for (byte[] byArray2 : set) {
            if (!this.isIPConstrained(byArray, byArray2)) continue;
            return;
        }
        if (byArray.length == 0 && set.size() == 0) {
            return;
        }
        throw new NameConstraintValidatorException("IP is not from a permitted subtree.");
    }

    private void checkExcludedIP(Set set, byte[] byArray) throws NameConstraintValidatorException {
        if (set.isEmpty()) {
            return;
        }
        for (byte[] byArray2 : set) {
            if (!this.isIPConstrained(byArray, byArray2)) continue;
            throw new NameConstraintValidatorException("IP is from an excluded subtree.");
        }
    }

    private boolean isIPConstrained(byte[] byArray, byte[] byArray2) {
        int n = byArray.length;
        if (n != byArray2.length / 2) {
            return false;
        }
        byte[] byArray3 = new byte[n];
        System.arraycopy(byArray2, n, byArray3, 0, n);
        byte[] byArray4 = new byte[n];
        byte[] byArray5 = new byte[n];
        for (int i = 0; i < n; ++i) {
            byArray4[i] = (byte)(byArray2[i] & byArray3[i]);
            byArray5[i] = (byte)(byArray[i] & byArray3[i]);
        }
        return Arrays.areEqual(byArray4, byArray5);
    }

    private boolean otherNameIsConstrained(OtherName otherName, OtherName otherName2) {
        return otherName2.equals(otherName);
    }

    private boolean emailIsConstrained(String string, String string2) {
        String string3 = string.substring(string.indexOf(64) + 1);
        if (string2.indexOf(64) != -1) {
            if (string.equalsIgnoreCase(string2)) {
                return true;
            }
            if (string3.equalsIgnoreCase(string2.substring(1))) {
                return true;
            }
        } else if (string2.charAt(0) != '.' ? string3.equalsIgnoreCase(string2) : this.withinDomain(string3, string2)) {
            return true;
        }
        return false;
    }

    private boolean withinDomain(String string, String string2) {
        String string3 = string2;
        if (string3.startsWith(".")) {
            string3 = string3.substring(1);
        }
        String[] stringArray = Strings.split(string3, '.');
        String[] stringArray2 = Strings.split(string, '.');
        if (stringArray2.length <= stringArray.length) {
            return false;
        }
        int n = stringArray2.length - stringArray.length;
        for (int i = -1; i < stringArray.length; ++i) {
            if (!(i == -1 ? stringArray2[i + n].equals("") : !stringArray[i].equalsIgnoreCase(stringArray2[i + n]))) continue;
            return false;
        }
        return true;
    }

    private void checkPermittedDNS(Set set, String string) throws NameConstraintValidatorException {
        if (set == null) {
            return;
        }
        for (String string2 : set) {
            if (!this.withinDomain(string, string2) && !string.equalsIgnoreCase(string2)) continue;
            return;
        }
        if (string.length() == 0 && set.size() == 0) {
            return;
        }
        throw new NameConstraintValidatorException("DNS is not from a permitted subtree.");
    }

    private void checkExcludedDNS(Set set, String string) throws NameConstraintValidatorException {
        if (set.isEmpty()) {
            return;
        }
        for (String string2 : set) {
            if (!this.withinDomain(string, string2) && !string.equalsIgnoreCase(string2)) continue;
            throw new NameConstraintValidatorException("DNS is from an excluded subtree.");
        }
    }

    private void unionEmail(String string, String string2, Set set) {
        if (string.indexOf(64) != -1) {
            String string3 = string.substring(string.indexOf(64) + 1);
            if (string2.indexOf(64) != -1) {
                if (string.equalsIgnoreCase(string2)) {
                    set.add(string);
                } else {
                    set.add(string);
                    set.add(string2);
                }
            } else if (string2.startsWith(".")) {
                if (this.withinDomain(string3, string2)) {
                    set.add(string2);
                } else {
                    set.add(string);
                    set.add(string2);
                }
            } else if (string3.equalsIgnoreCase(string2)) {
                set.add(string2);
            } else {
                set.add(string);
                set.add(string2);
            }
        } else if (string.startsWith(".")) {
            if (string2.indexOf(64) != -1) {
                String string4 = string2.substring(string.indexOf(64) + 1);
                if (this.withinDomain(string4, string)) {
                    set.add(string);
                } else {
                    set.add(string);
                    set.add(string2);
                }
            } else if (string2.startsWith(".")) {
                if (this.withinDomain(string, string2) || string.equalsIgnoreCase(string2)) {
                    set.add(string2);
                } else if (this.withinDomain(string2, string)) {
                    set.add(string);
                } else {
                    set.add(string);
                    set.add(string2);
                }
            } else if (this.withinDomain(string2, string)) {
                set.add(string);
            } else {
                set.add(string);
                set.add(string2);
            }
        } else if (string2.indexOf(64) != -1) {
            String string5 = string2.substring(string.indexOf(64) + 1);
            if (string5.equalsIgnoreCase(string)) {
                set.add(string);
            } else {
                set.add(string);
                set.add(string2);
            }
        } else if (string2.startsWith(".")) {
            if (this.withinDomain(string, string2)) {
                set.add(string2);
            } else {
                set.add(string);
                set.add(string2);
            }
        } else if (string.equalsIgnoreCase(string2)) {
            set.add(string);
        } else {
            set.add(string);
            set.add(string2);
        }
    }

    private void unionURI(String string, String string2, Set set) {
        if (string.indexOf(64) != -1) {
            String string3 = string.substring(string.indexOf(64) + 1);
            if (string2.indexOf(64) != -1) {
                if (string.equalsIgnoreCase(string2)) {
                    set.add(string);
                } else {
                    set.add(string);
                    set.add(string2);
                }
            } else if (string2.startsWith(".")) {
                if (this.withinDomain(string3, string2)) {
                    set.add(string2);
                } else {
                    set.add(string);
                    set.add(string2);
                }
            } else if (string3.equalsIgnoreCase(string2)) {
                set.add(string2);
            } else {
                set.add(string);
                set.add(string2);
            }
        } else if (string.startsWith(".")) {
            if (string2.indexOf(64) != -1) {
                String string4 = string2.substring(string.indexOf(64) + 1);
                if (this.withinDomain(string4, string)) {
                    set.add(string);
                } else {
                    set.add(string);
                    set.add(string2);
                }
            } else if (string2.startsWith(".")) {
                if (this.withinDomain(string, string2) || string.equalsIgnoreCase(string2)) {
                    set.add(string2);
                } else if (this.withinDomain(string2, string)) {
                    set.add(string);
                } else {
                    set.add(string);
                    set.add(string2);
                }
            } else if (this.withinDomain(string2, string)) {
                set.add(string);
            } else {
                set.add(string);
                set.add(string2);
            }
        } else if (string2.indexOf(64) != -1) {
            String string5 = string2.substring(string.indexOf(64) + 1);
            if (string5.equalsIgnoreCase(string)) {
                set.add(string);
            } else {
                set.add(string);
                set.add(string2);
            }
        } else if (string2.startsWith(".")) {
            if (this.withinDomain(string, string2)) {
                set.add(string2);
            } else {
                set.add(string);
                set.add(string2);
            }
        } else if (string.equalsIgnoreCase(string2)) {
            set.add(string);
        } else {
            set.add(string);
            set.add(string2);
        }
    }

    private Set intersectDNS(Set set, Set set2) {
        HashSet<String> hashSet = new HashSet<String>();
        Iterator iterator = set2.iterator();
        while (iterator.hasNext()) {
            String string = this.extractNameAsString(((GeneralSubtree)iterator.next()).getBase());
            if (set == null) {
                if (string == null) continue;
                hashSet.add(string);
                continue;
            }
            for (String string2 : set) {
                if (this.withinDomain(string2, string)) {
                    hashSet.add(string2);
                    continue;
                }
                if (!this.withinDomain(string, string2)) continue;
                hashSet.add(string);
            }
        }
        return hashSet;
    }

    private Set unionDNS(Set set, String string) {
        if (set.isEmpty()) {
            if (string == null) {
                return set;
            }
            set.add(string);
            return set;
        }
        HashSet<String> hashSet = new HashSet<String>();
        for (String string2 : set) {
            if (this.withinDomain(string2, string)) {
                hashSet.add(string);
                continue;
            }
            if (this.withinDomain(string, string2)) {
                hashSet.add(string2);
                continue;
            }
            hashSet.add(string2);
            hashSet.add(string);
        }
        return hashSet;
    }

    private void intersectEmail(String string, String string2, Set set) {
        if (string.indexOf(64) != -1) {
            String string3 = string.substring(string.indexOf(64) + 1);
            if (string2.indexOf(64) != -1) {
                if (string.equalsIgnoreCase(string2)) {
                    set.add(string);
                }
            } else if (string2.startsWith(".")) {
                if (this.withinDomain(string3, string2)) {
                    set.add(string);
                }
            } else if (string3.equalsIgnoreCase(string2)) {
                set.add(string);
            }
        } else if (string.startsWith(".")) {
            if (string2.indexOf(64) != -1) {
                String string4 = string2.substring(string.indexOf(64) + 1);
                if (this.withinDomain(string4, string)) {
                    set.add(string2);
                }
            } else if (string2.startsWith(".")) {
                if (this.withinDomain(string, string2) || string.equalsIgnoreCase(string2)) {
                    set.add(string);
                } else if (this.withinDomain(string2, string)) {
                    set.add(string2);
                }
            } else if (this.withinDomain(string2, string)) {
                set.add(string2);
            }
        } else if (string2.indexOf(64) != -1) {
            String string5 = string2.substring(string2.indexOf(64) + 1);
            if (string5.equalsIgnoreCase(string)) {
                set.add(string2);
            }
        } else if (string2.startsWith(".")) {
            if (this.withinDomain(string, string2)) {
                set.add(string);
            }
        } else if (string.equalsIgnoreCase(string2)) {
            set.add(string);
        }
    }

    private void checkExcludedURI(Set set, String string) throws NameConstraintValidatorException {
        if (set.isEmpty()) {
            return;
        }
        for (String string2 : set) {
            if (!this.isUriConstrained(string, string2)) continue;
            throw new NameConstraintValidatorException("URI is from an excluded subtree.");
        }
    }

    private Set intersectURI(Set set, Set set2) {
        HashSet<String> hashSet = new HashSet<String>();
        Iterator iterator = set2.iterator();
        while (iterator.hasNext()) {
            String string = this.extractNameAsString(((GeneralSubtree)iterator.next()).getBase());
            if (set == null) {
                if (string == null) continue;
                hashSet.add(string);
                continue;
            }
            for (String string2 : set) {
                this.intersectURI(string2, string, hashSet);
            }
        }
        return hashSet;
    }

    private Set unionURI(Set set, String string) {
        if (set.isEmpty()) {
            if (string == null) {
                return set;
            }
            set.add(string);
            return set;
        }
        HashSet hashSet = new HashSet();
        for (String string2 : set) {
            this.unionURI(string2, string, hashSet);
        }
        return hashSet;
    }

    private void intersectURI(String string, String string2, Set set) {
        if (string.indexOf(64) != -1) {
            String string3 = string.substring(string.indexOf(64) + 1);
            if (string2.indexOf(64) != -1) {
                if (string.equalsIgnoreCase(string2)) {
                    set.add(string);
                }
            } else if (string2.startsWith(".")) {
                if (this.withinDomain(string3, string2)) {
                    set.add(string);
                }
            } else if (string3.equalsIgnoreCase(string2)) {
                set.add(string);
            }
        } else if (string.startsWith(".")) {
            if (string2.indexOf(64) != -1) {
                String string4 = string2.substring(string.indexOf(64) + 1);
                if (this.withinDomain(string4, string)) {
                    set.add(string2);
                }
            } else if (string2.startsWith(".")) {
                if (this.withinDomain(string, string2) || string.equalsIgnoreCase(string2)) {
                    set.add(string);
                } else if (this.withinDomain(string2, string)) {
                    set.add(string2);
                }
            } else if (this.withinDomain(string2, string)) {
                set.add(string2);
            }
        } else if (string2.indexOf(64) != -1) {
            String string5 = string2.substring(string2.indexOf(64) + 1);
            if (string5.equalsIgnoreCase(string)) {
                set.add(string2);
            }
        } else if (string2.startsWith(".")) {
            if (this.withinDomain(string, string2)) {
                set.add(string);
            }
        } else if (string.equalsIgnoreCase(string2)) {
            set.add(string);
        }
    }

    private void checkPermittedURI(Set set, String string) throws NameConstraintValidatorException {
        if (set == null) {
            return;
        }
        for (String string2 : set) {
            if (!this.isUriConstrained(string, string2)) continue;
            return;
        }
        if (string.length() == 0 && set.size() == 0) {
            return;
        }
        throw new NameConstraintValidatorException("URI is not from a permitted subtree.");
    }

    private boolean isUriConstrained(String string, String string2) {
        String string3 = PKIXNameConstraintValidator.extractHostFromURL(string);
        return !string2.startsWith(".") ? string3.equalsIgnoreCase(string2) : this.withinDomain(string3, string2);
    }

    private static String extractHostFromURL(String string) {
        String string2 = string.substring(string.indexOf(58) + 1);
        if (string2.indexOf("//") != -1) {
            string2 = string2.substring(string2.indexOf("//") + 2);
        }
        if (string2.lastIndexOf(58) != -1) {
            string2 = string2.substring(0, string2.lastIndexOf(58));
        }
        string2 = string2.substring(string2.indexOf(58) + 1);
        if ((string2 = string2.substring(string2.indexOf(64) + 1)).indexOf(47) != -1) {
            string2 = string2.substring(0, string2.indexOf(47));
        }
        return string2;
    }

    private String extractNameAsString(GeneralName generalName) {
        return DERIA5String.getInstance(generalName.getName()).getString();
    }

    private static byte[] max(byte[] byArray, byte[] byArray2) {
        for (int i = 0; i < byArray.length; ++i) {
            if ((byArray[i] & 0xFFFF) <= (byArray2[i] & 0xFFFF)) continue;
            return byArray;
        }
        return byArray2;
    }

    private static byte[] min(byte[] byArray, byte[] byArray2) {
        for (int i = 0; i < byArray.length; ++i) {
            if ((byArray[i] & 0xFFFF) >= (byArray2[i] & 0xFFFF)) continue;
            return byArray;
        }
        return byArray2;
    }

    private static int compareTo(byte[] byArray, byte[] byArray2) {
        if (Arrays.areEqual(byArray, byArray2)) {
            return 0;
        }
        if (Arrays.areEqual(PKIXNameConstraintValidator.max(byArray, byArray2), byArray)) {
            return 1;
        }
        return -1;
    }

    private static byte[] or(byte[] byArray, byte[] byArray2) {
        byte[] byArray3 = new byte[byArray.length];
        for (int i = 0; i < byArray.length; ++i) {
            byArray3[i] = (byte)(byArray[i] | byArray2[i]);
        }
        return byArray3;
    }

    private int hashCollection(Collection collection) {
        if (collection == null) {
            return 0;
        }
        int n = 0;
        for (Object e : collection) {
            if (e instanceof byte[]) {
                n += Arrays.hashCode((byte[])e);
                continue;
            }
            n += e.hashCode();
        }
        return n;
    }

    private boolean collectionsAreEqual(Collection collection, Collection collection2) {
        if (collection == collection2) {
            return true;
        }
        if (collection == null || collection2 == null) {
            return false;
        }
        if (collection.size() != collection2.size()) {
            return false;
        }
        for (Object e : collection) {
            Iterator iterator = collection2.iterator();
            boolean bl = false;
            while (iterator.hasNext()) {
                Object e2 = iterator.next();
                if (!this.equals(e, e2)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            return false;
        }
        return true;
    }

    private boolean equals(Object object, Object object2) {
        if (object == object2) {
            return true;
        }
        if (object == null || object2 == null) {
            return false;
        }
        if (object instanceof byte[] && object2 instanceof byte[]) {
            return Arrays.areEqual((byte[])object, (byte[])object2);
        }
        return object.equals(object2);
    }

    private String stringifyIP(byte[] byArray) {
        int n;
        StringBuilder stringBuilder = new StringBuilder();
        for (n = 0; n < byArray.length / 2; ++n) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append(".");
            }
            stringBuilder.append(Integer.toString(byArray[n] & 0xFF));
        }
        stringBuilder.append("/");
        n = 1;
        for (int i = byArray.length / 2; i < byArray.length; ++i) {
            if (n != 0) {
                n = 0;
            } else {
                stringBuilder.append(".");
            }
            stringBuilder.append(Integer.toString(byArray[i] & 0xFF));
        }
        return stringBuilder.toString();
    }

    private String stringifyIPCollection(Set set) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            if (stringBuilder.length() > 1) {
                stringBuilder.append(",");
            }
            stringBuilder.append(this.stringifyIP((byte[])iterator.next()));
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private String stringifyOtherNameCollection(Set set) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            if (stringBuilder.length() > 1) {
                stringBuilder.append(",");
            }
            OtherName otherName = OtherName.getInstance(iterator.next());
            stringBuilder.append(otherName.getTypeID().getId());
            stringBuilder.append(":");
            try {
                stringBuilder.append(Hex.toHexString(otherName.getValue().toASN1Primitive().getEncoded()));
            } catch (IOException iOException) {
                stringBuilder.append(iOException.toString());
            }
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private final void addLine(StringBuilder stringBuilder, String string) {
        stringBuilder.append(string).append(Strings.lineSeparator());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        this.addLine(stringBuilder, "permitted:");
        if (this.permittedSubtreesDN != null) {
            this.addLine(stringBuilder, "DN:");
            this.addLine(stringBuilder, this.permittedSubtreesDN.toString());
        }
        if (this.permittedSubtreesDNS != null) {
            this.addLine(stringBuilder, "DNS:");
            this.addLine(stringBuilder, this.permittedSubtreesDNS.toString());
        }
        if (this.permittedSubtreesEmail != null) {
            this.addLine(stringBuilder, "Email:");
            this.addLine(stringBuilder, this.permittedSubtreesEmail.toString());
        }
        if (this.permittedSubtreesURI != null) {
            this.addLine(stringBuilder, "URI:");
            this.addLine(stringBuilder, this.permittedSubtreesURI.toString());
        }
        if (this.permittedSubtreesIP != null) {
            this.addLine(stringBuilder, "IP:");
            this.addLine(stringBuilder, this.stringifyIPCollection(this.permittedSubtreesIP));
        }
        if (this.permittedSubtreesOtherName != null) {
            this.addLine(stringBuilder, "OtherName:");
            this.addLine(stringBuilder, this.stringifyOtherNameCollection(this.permittedSubtreesOtherName));
        }
        this.addLine(stringBuilder, "excluded:");
        if (!this.excludedSubtreesDN.isEmpty()) {
            this.addLine(stringBuilder, "DN:");
            this.addLine(stringBuilder, this.excludedSubtreesDN.toString());
        }
        if (!this.excludedSubtreesDNS.isEmpty()) {
            this.addLine(stringBuilder, "DNS:");
            this.addLine(stringBuilder, this.excludedSubtreesDNS.toString());
        }
        if (!this.excludedSubtreesEmail.isEmpty()) {
            this.addLine(stringBuilder, "Email:");
            this.addLine(stringBuilder, this.excludedSubtreesEmail.toString());
        }
        if (!this.excludedSubtreesURI.isEmpty()) {
            this.addLine(stringBuilder, "URI:");
            this.addLine(stringBuilder, this.excludedSubtreesURI.toString());
        }
        if (!this.excludedSubtreesIP.isEmpty()) {
            this.addLine(stringBuilder, "IP:");
            this.addLine(stringBuilder, this.stringifyIPCollection(this.excludedSubtreesIP));
        }
        if (!this.excludedSubtreesOtherName.isEmpty()) {
            this.addLine(stringBuilder, "OtherName:");
            this.addLine(stringBuilder, this.stringifyOtherNameCollection(this.excludedSubtreesOtherName));
        }
        return stringBuilder.toString();
    }
}

