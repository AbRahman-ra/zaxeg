/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.Arrays;
import java.util.EnumSet;
import net.sf.saxon.expr.LastPositionFinder;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.GroundedIterator;
import net.sf.saxon.tree.iter.SingletonIterator;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceExtent;

public class MemoSequence
implements Sequence {
    SequenceIterator inputIterator;
    private Item[] reservoir = null;
    private int used;
    protected State state = State.UNREAD;

    public MemoSequence(SequenceIterator iterator) {
        this.inputIterator = iterator;
    }

    @Override
    public Item head() throws XPathException {
        return this.iterate().next();
    }

    @Override
    public synchronized SequenceIterator iterate() throws XPathException {
        switch (this.state) {
            case UNREAD: {
                this.state = State.BUSY;
                if (this.inputIterator instanceof EmptyIterator) {
                    this.state = State.EMPTY;
                    return this.inputIterator;
                }
                this.reservoir = new Item[50];
                this.used = 0;
                this.state = State.MAYBE_MORE;
                return new ProgressiveIterator();
            }
            case MAYBE_MORE: {
                return new ProgressiveIterator();
            }
            case ALL_READ: {
                switch (this.used) {
                    case 0: {
                        this.state = State.EMPTY;
                        return EmptyIterator.emptyIterator();
                    }
                    case 1: {
                        assert (this.reservoir != null);
                        return SingletonIterator.makeIterator(this.reservoir[0]);
                    }
                }
                return new ArrayIterator(this.reservoir, 0, this.used);
            }
            case BUSY: {
                XPathException de = new XPathException("Attempt to access a variable while it is being evaluated");
                de.setErrorCode("XTDE0640");
                throw de;
            }
            case EMPTY: {
                return EmptyIterator.emptyIterator();
            }
        }
        throw new IllegalStateException("Unknown iterator state");
    }

    public synchronized Item itemAt(int n) throws XPathException {
        if (n < 0) {
            return null;
        }
        if (this.reservoir != null && n < this.used) {
            return this.reservoir[n];
        }
        if (this.state == State.ALL_READ || this.state == State.EMPTY) {
            return null;
        }
        if (this.state == State.UNREAD) {
            Item item = this.inputIterator.next();
            if (item == null) {
                this.state = State.EMPTY;
                return null;
            }
            this.state = State.MAYBE_MORE;
            this.reservoir = new Item[50];
            this.append(item);
            if (n == 0) {
                return item;
            }
        }
        int diff = n - this.used + 1;
        while (diff-- > 0) {
            Item i = this.inputIterator.next();
            if (i == null) {
                this.state = State.ALL_READ;
                this.condense();
                return null;
            }
            this.append(i);
            this.state = State.MAYBE_MORE;
        }
        return this.reservoir[n];
    }

    private void append(Item item) {
        assert (this.reservoir != null);
        if (this.used >= this.reservoir.length) {
            this.reservoir = Arrays.copyOf(this.reservoir, this.used * 2);
        }
        this.reservoir[this.used++] = item;
    }

    private void condense() {
        if (this.reservoir != null && this.reservoir.length - this.used > 30) {
            this.reservoir = Arrays.copyOf(this.reservoir, this.used);
        }
    }

    public final class ProgressiveIterator
    implements SequenceIterator,
    LastPositionFinder,
    GroundedIterator {
        int position = -1;

        public MemoSequence getMemoSequence() {
            return MemoSequence.this;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public Item next() throws XPathException {
            MemoSequence memoSequence = MemoSequence.this;
            synchronized (memoSequence) {
                if (this.position == -2) {
                    return null;
                }
                if (++this.position < MemoSequence.this.used) {
                    assert (MemoSequence.this.reservoir != null);
                    return MemoSequence.this.reservoir[this.position];
                }
                if (MemoSequence.this.state == State.ALL_READ) {
                    this.position = -2;
                    return null;
                }
                assert (MemoSequence.this.inputIterator != null);
                Item i = MemoSequence.this.inputIterator.next();
                if (i == null) {
                    MemoSequence.this.state = State.ALL_READ;
                    MemoSequence.this.condense();
                    this.position = -2;
                    return null;
                }
                this.position = MemoSequence.this.used;
                MemoSequence.this.append(i);
                MemoSequence.this.state = State.MAYBE_MORE;
                return i;
            }
        }

        @Override
        public void close() {
        }

        @Override
        public int getLength() throws XPathException {
            if (MemoSequence.this.state == State.ALL_READ) {
                return MemoSequence.this.used;
            }
            if (MemoSequence.this.state == State.EMPTY) {
                return 0;
            }
            int savePos = this.position;
            while (this.next() != null) {
            }
            this.position = savePos;
            return MemoSequence.this.used;
        }

        @Override
        public GroundedValue materialize() throws XPathException {
            if (MemoSequence.this.state == State.ALL_READ) {
                return this.makeExtent();
            }
            if (MemoSequence.this.state == State.EMPTY) {
                return EmptySequence.getInstance();
            }
            int savePos = this.position;
            while (this.next() != null) {
            }
            this.position = savePos;
            return this.makeExtent();
        }

        private GroundedValue makeExtent() {
            if (MemoSequence.this.used == MemoSequence.this.reservoir.length) {
                if (MemoSequence.this.used == 0) {
                    return EmptySequence.getInstance();
                }
                if (MemoSequence.this.used == 1) {
                    return MemoSequence.this.reservoir[0];
                }
                return new SequenceExtent(MemoSequence.this.reservoir);
            }
            return SequenceExtent.makeSequenceExtent(Arrays.asList(MemoSequence.this.reservoir).subList(0, MemoSequence.this.used));
        }

        @Override
        public GroundedValue getResidue() throws XPathException {
            if (MemoSequence.this.state == State.EMPTY || this.position >= MemoSequence.this.used || this.position == -2) {
                return EmptySequence.getInstance();
            }
            if (MemoSequence.this.state == State.ALL_READ) {
                return SequenceExtent.makeSequenceExtent(Arrays.asList(MemoSequence.this.reservoir).subList(this.position + 1, MemoSequence.this.used));
            }
            int savePos = this.position;
            while (this.next() != null) {
            }
            this.position = savePos;
            return SequenceExtent.makeSequenceExtent(Arrays.asList(MemoSequence.this.reservoir).subList(this.position + 1, MemoSequence.this.used));
        }

        @Override
        public EnumSet<SequenceIterator.Property> getProperties() {
            return EnumSet.of(SequenceIterator.Property.GROUNDED, SequenceIterator.Property.LAST_POSITION_FINDER);
        }
    }

    private static enum State {
        UNREAD,
        MAYBE_MORE,
        ALL_READ,
        BUSY,
        EMPTY;

    }
}

