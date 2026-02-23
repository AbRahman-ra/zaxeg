/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.CardinalityCheckingIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;

public abstract class TreatFn
extends SystemFunction
implements Callable {
    @Override
    public abstract String getErrorCodeForTypeErrors();

    public abstract int getRequiredCardinality();

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        SequenceIterator iterator = arguments[0].iterate();
        int card = this.getRequiredCardinality();
        RoleDiagnostic role = this.makeRoleDiagnostic();
        iterator = new CardinalityCheckingIterator(iterator, card, role, null);
        return new LazySequence(iterator);
    }

    public RoleDiagnostic makeRoleDiagnostic() {
        RoleDiagnostic role = new RoleDiagnostic(0, this.getFunctionName().getDisplayName(), 0);
        role.setErrorCode(this.getErrorCodeForTypeErrors());
        return role;
    }

    @Override
    public String getStreamerName() {
        return "TreatFn";
    }

    public static class ZeroOrOne
    extends TreatFn {
        @Override
        public int getRequiredCardinality() {
            return 24576;
        }

        @Override
        public String getErrorCodeForTypeErrors() {
            return "FORG0003";
        }
    }

    public static class OneOrMore
    extends TreatFn {
        @Override
        public int getRequiredCardinality() {
            return 49152;
        }

        @Override
        public String getErrorCodeForTypeErrors() {
            return "FORG0004";
        }
    }

    public static class ExactlyOne
    extends TreatFn {
        @Override
        public int getRequiredCardinality() {
            return 16384;
        }

        @Override
        public String getErrorCodeForTypeErrors() {
            return "FORG0005";
        }
    }
}

