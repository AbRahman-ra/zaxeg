/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.value.SequenceType;

public abstract class TreatExpression {
    private TreatExpression() {
    }

    public static Expression make(Expression sequence, SequenceType type) {
        return TreatExpression.make(sequence, type, "XPDY0050");
    }

    public static Expression make(Expression sequence, SequenceType type, String errorCode) {
        RoleDiagnostic role = new RoleDiagnostic(2, "treat as", 0);
        role.setErrorCode(errorCode);
        Expression e = CardinalityChecker.makeCardinalityChecker(sequence, type.getCardinality(), role);
        return new ItemChecker(e, type.getPrimaryType(), role);
    }
}

