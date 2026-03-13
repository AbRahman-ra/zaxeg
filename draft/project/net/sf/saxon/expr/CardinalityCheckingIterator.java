/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.value.Cardinality;

public final class CardinalityCheckingIterator
implements SequenceIterator {
    private SequenceIterator base;
    private Location locator;
    private Item first = null;
    private Item second = null;
    private int position = 0;

    public CardinalityCheckingIterator(SequenceIterator base, int requiredCardinality, RoleDiagnostic role, Location locator) throws XPathException {
        this.base = base;
        this.locator = locator;
        this.first = base.next();
        if (this.first == null) {
            if (!Cardinality.allowsZero(requiredCardinality)) {
                this.typeError("An empty sequence is not allowed as the " + role.getMessage(), role.getErrorCode());
            }
        } else {
            if (requiredCardinality == 8192) {
                this.typeError("The only value allowed for the " + role.getMessage() + " is an empty sequence", role.getErrorCode());
            }
            this.second = base.next();
            if (this.second != null && !Cardinality.allowsMany(requiredCardinality)) {
                Item[] leaders = new Item[]{this.first, this.second};
                this.typeError("A sequence of more than one item is not allowed as the " + role.getMessage() + CardinalityChecker.depictSequenceStart(new ArrayIterator(leaders), 2), role.getErrorCode());
            }
        }
    }

    @Override
    public Item next() throws XPathException {
        if (this.position < 2) {
            if (this.position == 0) {
                Item current = this.first;
                this.position = this.first == null ? -1 : 1;
                return current;
            }
            if (this.position == 1) {
                Item current = this.second;
                this.position = this.second == null ? -1 : 2;
                return current;
            }
            return null;
        }
        Item current = this.base.next();
        this.position = current == null ? -1 : ++this.position;
        return current;
    }

    @Override
    public void close() {
        this.base.close();
    }

    private void typeError(String message, String errorCode) throws XPathException {
        XPathException e = new XPathException(message, errorCode, this.locator);
        e.setIsTypeError(!errorCode.startsWith("FORG"));
        throw e;
    }
}

