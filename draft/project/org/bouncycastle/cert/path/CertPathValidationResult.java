/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cert.path;

import java.util.Collections;
import java.util.Set;
import org.bouncycastle.cert.path.CertPathValidationContext;
import org.bouncycastle.cert.path.CertPathValidationException;
import org.bouncycastle.util.Arrays;

public class CertPathValidationResult {
    private final boolean isValid;
    private final CertPathValidationException cause;
    private final Set unhandledCriticalExtensionOIDs;
    private final int certIndex;
    private final int ruleIndex;
    private CertPathValidationException[] causes;
    private int[] certIndexes;
    private int[] ruleIndexes;

    public CertPathValidationResult(CertPathValidationContext certPathValidationContext) {
        this.unhandledCriticalExtensionOIDs = Collections.unmodifiableSet(certPathValidationContext.getUnhandledCriticalExtensionOIDs());
        this.isValid = this.unhandledCriticalExtensionOIDs.isEmpty();
        this.certIndex = -1;
        this.ruleIndex = -1;
        this.cause = null;
    }

    public CertPathValidationResult(CertPathValidationContext certPathValidationContext, int n, int n2, CertPathValidationException certPathValidationException) {
        this.unhandledCriticalExtensionOIDs = Collections.unmodifiableSet(certPathValidationContext.getUnhandledCriticalExtensionOIDs());
        this.isValid = false;
        this.certIndex = n;
        this.ruleIndex = n2;
        this.cause = certPathValidationException;
    }

    public CertPathValidationResult(CertPathValidationContext certPathValidationContext, int[] nArray, int[] nArray2, CertPathValidationException[] certPathValidationExceptionArray) {
        this.unhandledCriticalExtensionOIDs = Collections.unmodifiableSet(certPathValidationContext.getUnhandledCriticalExtensionOIDs());
        this.isValid = false;
        this.cause = certPathValidationExceptionArray[0];
        this.certIndex = nArray[0];
        this.ruleIndex = nArray2[0];
        this.causes = certPathValidationExceptionArray;
        this.certIndexes = nArray;
        this.ruleIndexes = nArray2;
    }

    public boolean isValid() {
        return this.isValid;
    }

    public CertPathValidationException getCause() {
        if (this.cause != null) {
            return this.cause;
        }
        if (!this.unhandledCriticalExtensionOIDs.isEmpty()) {
            return new CertPathValidationException("Unhandled Critical Extensions");
        }
        return null;
    }

    public int getFailingCertIndex() {
        return this.certIndex;
    }

    public int getFailingRuleIndex() {
        return this.ruleIndex;
    }

    public Set getUnhandledCriticalExtensionOIDs() {
        return this.unhandledCriticalExtensionOIDs;
    }

    public boolean isDetailed() {
        return this.certIndexes != null;
    }

    public CertPathValidationException[] getCauses() {
        if (this.causes != null) {
            CertPathValidationException[] certPathValidationExceptionArray = new CertPathValidationException[this.causes.length];
            System.arraycopy(this.causes, 0, certPathValidationExceptionArray, 0, this.causes.length);
            return certPathValidationExceptionArray;
        }
        if (!this.unhandledCriticalExtensionOIDs.isEmpty()) {
            return new CertPathValidationException[]{new CertPathValidationException("Unhandled Critical Extensions")};
        }
        return null;
    }

    public int[] getFailingCertIndexes() {
        return Arrays.clone(this.certIndexes);
    }

    public int[] getFailingRuleIndexes() {
        return Arrays.clone(this.ruleIndexes);
    }
}

