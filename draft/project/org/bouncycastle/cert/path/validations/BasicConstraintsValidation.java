/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cert.path.validations;

import java.math.BigInteger;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.path.CertPathValidation;
import org.bouncycastle.cert.path.CertPathValidationContext;
import org.bouncycastle.cert.path.CertPathValidationException;
import org.bouncycastle.util.BigIntegers;
import org.bouncycastle.util.Integers;
import org.bouncycastle.util.Memoable;

public class BasicConstraintsValidation
implements CertPathValidation {
    private boolean previousCertWasCA = true;
    private Integer maxPathLength = null;
    private boolean isMandatory = true;

    public BasicConstraintsValidation() {
        this(true);
    }

    public BasicConstraintsValidation(boolean bl) {
        this.isMandatory = bl;
    }

    public void validate(CertPathValidationContext certPathValidationContext, X509CertificateHolder x509CertificateHolder) throws CertPathValidationException {
        BigInteger bigInteger;
        certPathValidationContext.addHandledExtension(Extension.basicConstraints);
        if (!this.previousCertWasCA) {
            throw new CertPathValidationException("Basic constraints violated: issuer is not a CA");
        }
        BasicConstraints basicConstraints = BasicConstraints.fromExtensions(x509CertificateHolder.getExtensions());
        boolean bl = this.previousCertWasCA = basicConstraints != null && basicConstraints.isCA() || basicConstraints == null && !this.isMandatory;
        if (this.maxPathLength != null && !x509CertificateHolder.getSubject().equals(x509CertificateHolder.getIssuer())) {
            if (this.maxPathLength < 0) {
                throw new CertPathValidationException("Basic constraints violated: path length exceeded");
            }
            this.maxPathLength = Integers.valueOf(this.maxPathLength - 1);
        }
        if (basicConstraints != null && (bigInteger = basicConstraints.getPathLenConstraint()) != null) {
            int n = BigIntegers.intValueExact(bigInteger);
            this.maxPathLength = this.maxPathLength == null ? Integers.valueOf(n) : Integers.valueOf(Math.min(n, this.maxPathLength));
        }
    }

    public Memoable copy() {
        BasicConstraintsValidation basicConstraintsValidation = new BasicConstraintsValidation();
        basicConstraintsValidation.isMandatory = this.isMandatory;
        basicConstraintsValidation.previousCertWasCA = this.previousCertWasCA;
        basicConstraintsValidation.maxPathLength = this.maxPathLength;
        return basicConstraintsValidation;
    }

    public void reset(Memoable memoable) {
        BasicConstraintsValidation basicConstraintsValidation = (BasicConstraintsValidation)memoable;
        this.isMandatory = basicConstraintsValidation.isMandatory;
        this.previousCertWasCA = basicConstraintsValidation.previousCertWasCA;
        this.maxPathLength = basicConstraintsValidation.maxPathLength;
    }
}

