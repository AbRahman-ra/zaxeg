/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import javax.xml.crypto.NodeSetData;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class DOMSubTreeData
implements NodeSetData {
    private boolean excludeComments;
    private Node root;

    public DOMSubTreeData(Node root, boolean excludeComments) {
        this.root = root;
        this.excludeComments = excludeComments;
    }

    public Iterator iterator() {
        return new DelayedNodeIterator(this.root, this.excludeComments);
    }

    public Node getRoot() {
        return this.root;
    }

    public boolean excludeComments() {
        return this.excludeComments;
    }

    /*
     * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
     */
    static class DelayedNodeIterator
    implements Iterator<Node> {
        private Node root;
        private List<Node> nodeSet;
        private ListIterator<Node> li;
        private boolean withComments;

        DelayedNodeIterator(Node root, boolean excludeComments) {
            this.root = root;
            this.withComments = !excludeComments;
        }

        @Override
        public boolean hasNext() {
            if (this.nodeSet == null) {
                this.nodeSet = this.dereferenceSameDocumentURI(this.root);
                this.li = this.nodeSet.listIterator();
            }
            return this.li.hasNext();
        }

        @Override
        public Node next() {
            if (this.nodeSet == null) {
                this.nodeSet = this.dereferenceSameDocumentURI(this.root);
                this.li = this.nodeSet.listIterator();
            }
            if (this.li.hasNext()) {
                return this.li.next();
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private List<Node> dereferenceSameDocumentURI(Node node) {
            ArrayList<Node> nodeSet = new ArrayList<Node>();
            if (node != null) {
                this.nodeSetMinusCommentNodes(node, nodeSet, null);
            }
            return nodeSet;
        }

        private void nodeSetMinusCommentNodes(Node node, List<Node> nodeSet, Node prevSibling) {
            switch (node.getNodeType()) {
                case 1: {
                    NamedNodeMap attrs = node.getAttributes();
                    if (attrs != null) {
                        int len = attrs.getLength();
                        for (int i = 0; i < len; ++i) {
                            nodeSet.add(attrs.item(i));
                        }
                    }
                    nodeSet.add(node);
                    Node pSibling = null;
                    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                        this.nodeSetMinusCommentNodes(child, nodeSet, pSibling);
                        pSibling = child;
                    }
                    break;
                }
                case 9: {
                    Node pSibling = null;
                    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
                        this.nodeSetMinusCommentNodes(child, nodeSet, pSibling);
                        pSibling = child;
                    }
                    break;
                }
                case 3: 
                case 4: {
                    if (prevSibling != null && (prevSibling.getNodeType() == 3 || prevSibling.getNodeType() == 4)) {
                        return;
                    }
                    nodeSet.add(node);
                    break;
                }
                case 7: {
                    nodeSet.add(node);
                    break;
                }
                case 8: {
                    if (!this.withComments) break;
                    nodeSet.add(node);
                }
            }
        }
    }
}

