/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.util;

import java.util.function.Predicate;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.SteppingNode;

public abstract class SteppingNavigator {
    static <N extends SteppingNode<N>> N getFollowingNode(N start, N anchor) {
        Object nodei = start.getFirstChild();
        if (nodei != null) {
            return nodei;
        }
        if (start.isSameNodeInfo(anchor)) {
            return null;
        }
        nodei = start;
        NodeInfo parenti = start.getParent();
        do {
            if ((nodei = nodei.getNextSibling()) != null) {
                return nodei;
            }
            if (parenti.isSameNodeInfo(anchor)) {
                return null;
            }
            nodei = parenti;
        } while ((parenti = parenti.getParent()) != null);
        return null;
    }

    public static class DescendantAxisIterator<N extends SteppingNode<N>>
    implements AxisIterator {
        private N start;
        private N current;
        private Stepper<N> stepper;

        public DescendantAxisIterator(N start, boolean includeSelf, Predicate<? super NodeInfo> test) {
            this.start = start;
            if (!includeSelf || !test.test((NodeInfo)start)) {
                this.current = start;
            }
            if (test == null || test == AnyNodeTest.getInstance()) {
                this.stepper = new FollowingNodeStepper<N>(start);
            } else if (test instanceof NameTest) {
                if (((NameTest)test).getPrimitiveType() == 1) {
                    NameTest nt = (NameTest)test;
                    this.stepper = start.hasFingerprint() ? new FollowingFingerprintedElementStepper<N>(start, nt.getFingerprint()) : new FollowingElementStepper<N>(start, nt.getNamespaceURI(), nt.getLocalPart());
                } else {
                    this.stepper = new FollowingFilteredNodeStepper<N>(start, test);
                }
            } else if (test instanceof NodeKindTest) {
                this.stepper = ((NodeKindTest)test).getPrimitiveType() == 1 ? new FollowingElementStepper<N>(start, null, null) : new FollowingFilteredNodeStepper<N>(start, test);
            } else if (test instanceof LocalNameTest) {
                if (((LocalNameTest)test).getPrimitiveType() == 1) {
                    LocalNameTest nt = (LocalNameTest)test;
                    this.stepper = new FollowingElementStepper<N>(start, null, nt.getLocalName());
                } else {
                    this.stepper = new FollowingFilteredNodeStepper<N>(start, test);
                }
            } else if (test instanceof NamespaceTest) {
                if (((NamespaceTest)test).getPrimitiveType() == 1) {
                    NamespaceTest nt = (NamespaceTest)test;
                    this.stepper = new FollowingElementStepper<N>(start, nt.getNamespaceURI(), null);
                } else {
                    this.stepper = new FollowingFilteredNodeStepper<N>(start, test);
                }
            } else {
                this.stepper = new FollowingFilteredNodeStepper<N>(start, test);
            }
        }

        public N next() {
            if (this.current == null) {
                this.current = this.start;
                return this.start;
            }
            N curr = this.stepper.step(this.current);
            this.current = curr;
            return this.current;
        }
    }

    private static class FollowingFingerprintedElementStepper<N extends SteppingNode<N>>
    implements Stepper<N> {
        N anchor;
        int fingerprint;

        FollowingFingerprintedElementStepper(N anchor, int fingerprint) {
            this.anchor = anchor;
            this.fingerprint = fingerprint;
        }

        @Override
        public N step(N node) {
            while ((node = SteppingNavigator.getFollowingNode(node, this.anchor)) != null && node.getFingerprint() != this.fingerprint) {
            }
            return node;
        }
    }

    private static class FollowingElementStepper<N extends SteppingNode<N>>
    implements Stepper<N> {
        N anchor;
        String uri;
        String local;

        FollowingElementStepper(N anchor, String uri, String local) {
            this.anchor = anchor;
            this.uri = uri;
            this.local = local;
        }

        @Override
        public N step(N node) {
            return node.getSuccessorElement(this.anchor, this.uri, this.local);
        }
    }

    private static class FollowingFilteredNodeStepper<N extends SteppingNode<N>>
    implements Stepper<N> {
        N anchor;
        Predicate<? super NodeInfo> test;

        FollowingFilteredNodeStepper(N anchor, Predicate<? super NodeInfo> test) {
            this.anchor = anchor;
            this.test = test;
        }

        @Override
        public N step(N node) {
            while ((node = SteppingNavigator.getFollowingNode(node, this.anchor)) != null && !this.test.test((NodeInfo)node)) {
            }
            return node;
        }
    }

    private static class FollowingNodeStepper<N extends SteppingNode<N>>
    implements Stepper<N> {
        N anchor;

        FollowingNodeStepper(N anchor) {
            this.anchor = anchor;
        }

        @Override
        public N step(N node) {
            return SteppingNavigator.getFollowingNode(node, this.anchor);
        }
    }

    private static interface Stepper<N extends SteppingNode<N>> {
        public N step(N var1);
    }
}

