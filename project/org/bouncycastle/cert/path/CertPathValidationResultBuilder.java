/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.bouncycastle.cert.path;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.cert.path.CertPathValidationContext;
import org.bouncycastle.cert.path.CertPathValidationException;
import org.bouncycastle.cert.path.CertPathValidationResult;
import org.bouncycastle.util.Integers;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
class CertPathValidationResultBuilder {
    private final CertPathValidationContext context;
    private final List<Integer> certIndexes = new ArrayList<Integer>();
    private final List<Integer> ruleIndexes = new ArrayList<Integer>();
    private final List<CertPathValidationException> exceptions = new ArrayList<CertPathValidationException>();

    CertPathValidationResultBuilder(CertPathValidationContext certPathValidationContext) {
        this.context = certPathValidationContext;
    }

    public CertPathValidationResult build() {
        if (this.exceptions.isEmpty()) {
            return new CertPathValidationResult(this.context);
        }
        return new CertPathValidationResult(this.context, this.toInts(this.certIndexes), this.toInts(this.ruleIndexes), this.exceptions.toArray(new CertPathValidationException[this.exceptions.size()]));
    }

    public void addException(int n, int n2, CertPathValidationException certPathValidationException) {
        this.certIndexes.add(Integers.valueOf(n));
        this.ruleIndexes.add(Integers.valueOf(n2));
        this.exceptions.add(certPathValidationException);
    }

    private int[] toInts(List<Integer> list) {
        int[] nArray = new int[list.size()];
        for (int i = 0; i != nArray.length; ++i) {
            nArray[i] = list.get(i);
        }
        return nArray;
    }
}

