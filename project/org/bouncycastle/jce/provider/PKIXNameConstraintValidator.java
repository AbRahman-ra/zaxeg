/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.jce.provider;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralSubtree;
import org.bouncycastle.asn1.x509.NameConstraintValidatorException;
import org.bouncycastle.jce.provider.PKIXNameConstraintValidatorException;

public class PKIXNameConstraintValidator {
    org.bouncycastle.asn1.x509.PKIXNameConstraintValidator validator = new org.bouncycastle.asn1.x509.PKIXNameConstraintValidator();

    public int hashCode() {
        return this.validator.hashCode();
    }

    public boolean equals(Object object) {
        if (!(object instanceof PKIXNameConstraintValidator)) {
            return false;
        }
        PKIXNameConstraintValidator pKIXNameConstraintValidator = (PKIXNameConstraintValidator)object;
        return this.validator.equals(pKIXNameConstraintValidator.validator);
    }

    public void checkPermittedDN(ASN1Sequence aSN1Sequence) throws PKIXNameConstraintValidatorException {
        try {
            this.validator.checkPermittedDN(X500Name.getInstance(aSN1Sequence));
        } catch (NameConstraintValidatorException nameConstraintValidatorException) {
            throw new PKIXNameConstraintValidatorException(nameConstraintValidatorException.getMessage(), nameConstraintValidatorException);
        }
    }

    public void checkExcludedDN(ASN1Sequence aSN1Sequence) throws PKIXNameConstraintValidatorException {
        try {
            this.validator.checkExcludedDN(X500Name.getInstance(aSN1Sequence));
        } catch (NameConstraintValidatorException nameConstraintValidatorException) {
            throw new PKIXNameConstraintValidatorException(nameConstraintValidatorException.getMessage(), nameConstraintValidatorException);
        }
    }

    public void checkPermitted(GeneralName generalName) throws PKIXNameConstraintValidatorException {
        try {
            this.validator.checkPermitted(generalName);
        } catch (NameConstraintValidatorException nameConstraintValidatorException) {
            throw new PKIXNameConstraintValidatorException(nameConstraintValidatorException.getMessage(), nameConstraintValidatorException);
        }
    }

    public void checkExcluded(GeneralName generalName) throws PKIXNameConstraintValidatorException {
        try {
            this.validator.checkExcluded(generalName);
        } catch (NameConstraintValidatorException nameConstraintValidatorException) {
            throw new PKIXNameConstraintValidatorException(nameConstraintValidatorException.getMessage(), nameConstraintValidatorException);
        }
    }

    public void intersectPermittedSubtree(GeneralSubtree generalSubtree) {
        this.validator.intersectPermittedSubtree(generalSubtree);
    }

    public void intersectPermittedSubtree(GeneralSubtree[] generalSubtreeArray) {
        this.validator.intersectPermittedSubtree(generalSubtreeArray);
    }

    public void intersectEmptyPermittedSubtree(int n) {
        this.validator.intersectEmptyPermittedSubtree(n);
    }

    public void addExcludedSubtree(GeneralSubtree generalSubtree) {
        this.validator.addExcludedSubtree(generalSubtree);
    }

    public String toString() {
        return this.validator.toString();
    }
}

