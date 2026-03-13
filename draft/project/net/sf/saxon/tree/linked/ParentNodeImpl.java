/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.tree.linked;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Predicate;
import net.sf.saxon.event.Builder;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.ArrayIterator;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.iter.SingleNodeIterator;
import net.sf.saxon.tree.jiter.MonoIterator;
import net.sf.saxon.tree.linked.ChildEnumeration;
import net.sf.saxon.tree.linked.CommentImpl;
import net.sf.saxon.tree.linked.ElementImpl;
import net.sf.saxon.tree.linked.LinkedTreeBuilder;
import net.sf.saxon.tree.linked.NodeImpl;
import net.sf.saxon.tree.linked.ProcInstImpl;
import net.sf.saxon.tree.linked.TextImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;

public abstract class ParentNodeImpl
extends NodeImpl {
    private Object children = null;
    private int sequence;

    @Override
    protected final long getSequenceNumber() {
        return this.getRawSequenceNumber() == -1 ? -1L : (long)this.getRawSequenceNumber() << 32;
    }

    protected final int getRawSequenceNumber() {
        return this.sequence;
    }

    protected final void setRawSequenceNumber(int seq) {
        this.sequence = seq;
    }

    protected final void setChildren(Object children) {
        this.children = children;
    }

    @Override
    public final boolean hasChildNodes() {
        return this.children != null;
    }

    public Iterable<NodeImpl> children() {
        if (this.children == null) {
            return Collections.emptyList();
        }
        if (this.children instanceof NodeImpl) {
            return () -> new MonoIterator<NodeImpl>((NodeImpl)this.children);
        }
        return Arrays.asList((NodeImpl[])this.children);
    }

    public final int getNumberOfChildren() {
        if (this.children == null) {
            return 0;
        }
        if (this.children instanceof NodeImpl) {
            return 1;
        }
        return ((NodeInfo[])this.children).length;
    }

    protected final AxisIterator iterateChildren(Predicate<? super NodeInfo> test) {
        if (this.children == null) {
            return EmptyIterator.ofNodes();
        }
        if (this.children instanceof NodeImpl) {
            NodeImpl child = (NodeImpl)this.children;
            if (test == null || test == AnyNodeTest.getInstance()) {
                return SingleNodeIterator.makeIterator(child);
            }
            return Navigator.filteredSingleton(child, test);
        }
        if (test == null || test == AnyNodeTest.getInstance()) {
            return new ArrayIterator.OfNodes((NodeImpl[])this.children);
        }
        return new ChildEnumeration(this, test);
    }

    @Override
    public final NodeImpl getFirstChild() {
        if (this.children == null) {
            return null;
        }
        if (this.children instanceof NodeImpl) {
            return (NodeImpl)this.children;
        }
        return ((NodeImpl[])this.children)[0];
    }

    @Override
    public final NodeImpl getLastChild() {
        if (this.children == null) {
            return null;
        }
        if (this.children instanceof NodeImpl) {
            return (NodeImpl)this.children;
        }
        NodeImpl[] n = (NodeImpl[])this.children;
        return n[n.length - 1];
    }

    protected final NodeImpl getNthChild(int n) {
        if (this.children == null) {
            return null;
        }
        if (this.children instanceof NodeImpl) {
            return n == 0 ? (NodeImpl)this.children : null;
        }
        NodeImpl[] nodes = (NodeImpl[])this.children;
        if (n < 0 || n >= nodes.length) {
            return null;
        }
        return nodes[n];
    }

    protected void removeChild(NodeImpl child) {
        if (this.children == null) {
            return;
        }
        if (this.children == child) {
            this.children = null;
            return;
        }
        NodeImpl[] nodes = (NodeImpl[])this.children;
        for (int i = 0; i < nodes.length; ++i) {
            if (nodes[i] != child) continue;
            if (nodes.length == 2) {
                this.children = nodes[1 - i];
                break;
            }
            NodeImpl[] n2 = new NodeImpl[nodes.length - 1];
            if (i > 0) {
                System.arraycopy(nodes, 0, n2, 0, i);
            }
            if (i < nodes.length - 1) {
                System.arraycopy(nodes, i + 1, n2, i, nodes.length - i - 1);
            }
            this.children = this.cleanUpChildren(n2);
            break;
        }
    }

    private NodeImpl[] cleanUpChildren(NodeImpl[] children) {
        boolean prevText = false;
        int j = 0;
        NodeImpl[] c2 = new NodeImpl[children.length];
        for (NodeImpl node : children) {
            if (node instanceof TextImpl) {
                if (prevText) {
                    TextImpl prev = (TextImpl)c2[j - 1];
                    prev.replaceStringValue(prev.getStringValue() + node.getStringValue());
                    continue;
                }
                if (node.getStringValue().isEmpty()) continue;
                prevText = true;
                node.setSiblingPosition(j);
                c2[j++] = node;
                continue;
            }
            node.setSiblingPosition(j);
            c2[j++] = node;
            prevText = false;
        }
        if (j == c2.length) {
            return c2;
        }
        return Arrays.copyOf(c2, j);
    }

    @Override
    public String getStringValue() {
        return this.getStringValueCS().toString();
    }

    @Override
    public CharSequence getStringValueCS() {
        FastStringBuffer sb = null;
        for (NodeImpl next = this.getFirstChild(); next != null; next = next.getNextInDocument(this)) {
            if (!(next instanceof TextImpl)) continue;
            if (sb == null) {
                sb = new FastStringBuffer(64);
            }
            sb.cat(next.getStringValueCS());
        }
        if (sb == null) {
            return "";
        }
        return sb.condense();
    }

    protected synchronized void addChild(NodeImpl node, int index) {
        NodeImpl[] c;
        if (this.children == null) {
            c = new NodeImpl[10];
        } else if (this.children instanceof NodeImpl) {
            c = new NodeImpl[10];
            c[0] = (NodeImpl)this.children;
        } else {
            c = (NodeImpl[])this.children;
        }
        if (index >= c.length) {
            c = Arrays.copyOf(c, c.length * 2);
        }
        c[index] = node;
        node.setRawParent(this);
        node.setSiblingPosition(index);
        this.children = c;
    }

    @Override
    public void insertChildren(NodeInfo[] source, boolean atStart, boolean inherit) {
        if (atStart) {
            this.insertChildrenAt(source, 0, inherit);
        } else {
            this.insertChildrenAt(source, this.getNumberOfChildren(), inherit);
        }
    }

    synchronized void insertChildrenAt(NodeInfo[] source, int index, boolean inherit) {
        if (source.length == 0) {
            return;
        }
        NodeImpl[] source2 = this.adjustSuppliedNodeArray(source, inherit);
        if (this.children == null) {
            if (source2.length == 1) {
                this.children = source2[0];
                ((NodeImpl)this.children).setSiblingPosition(0);
            } else {
                this.children = this.cleanUpChildren(source2);
            }
        } else if (this.children instanceof NodeImpl) {
            int adjacent;
            int n = adjacent = index == 0 ? source2.length - 1 : 0;
            if (this.children instanceof TextImpl && source2[adjacent] instanceof TextImpl) {
                if (index == 0) {
                    source2[adjacent].replaceStringValue(source2[adjacent].getStringValue() + ((TextImpl)this.children).getStringValue());
                } else {
                    source2[adjacent].replaceStringValue(((TextImpl)this.children).getStringValue() + source2[adjacent].getStringValue());
                }
                this.children = this.cleanUpChildren(source2);
            } else {
                NodeImpl[] n2 = new NodeImpl[source2.length + 1];
                if (index == 0) {
                    System.arraycopy(source2, 0, n2, 0, source2.length);
                    n2[source2.length] = (NodeImpl)this.children;
                } else {
                    n2[0] = (NodeImpl)this.children;
                    System.arraycopy(source2, 0, n2, 1, source2.length);
                }
                this.children = this.cleanUpChildren(n2);
            }
        } else {
            NodeImpl[] n0 = (NodeImpl[])this.children;
            NodeImpl[] n2 = new NodeImpl[n0.length + source2.length];
            System.arraycopy(n0, 0, n2, 0, index);
            System.arraycopy(source2, 0, n2, index, source2.length);
            System.arraycopy(n0, index, n2, index + source2.length, n0.length - index);
            this.children = this.cleanUpChildren(n2);
        }
    }

    private NodeImpl convertForeignNode(NodeInfo source) {
        if (!(source instanceof NodeImpl)) {
            int kind = source.getNodeKind();
            switch (kind) {
                case 3: {
                    return new TextImpl(source.getStringValue());
                }
                case 8: {
                    return new CommentImpl(source.getStringValue());
                }
                case 7: {
                    return new ProcInstImpl(source.getLocalPart(), source.getStringValue());
                }
                case 1: {
                    LinkedTreeBuilder builder = null;
                    try {
                        builder = new LinkedTreeBuilder(this.getConfiguration().makePipelineConfiguration());
                        ((Builder)builder).open();
                        source.copy(builder, 2, Loc.NONE);
                        ((Builder)builder).close();
                    } catch (XPathException e) {
                        throw new IllegalArgumentException("Failed to convert inserted element node to an instance of net.sf.saxon.om.tree.ElementImpl");
                    }
                    return (NodeImpl)((Builder)builder).getCurrentRoot();
                }
            }
            throw new IllegalArgumentException("Cannot insert a node unless it is an element, comment, text node, or processing instruction");
        }
        return (NodeImpl)source;
    }

    protected synchronized void replaceChildrenAt(NodeInfo[] source, int index, boolean inherit) {
        if (this.children == null) {
            return;
        }
        NodeImpl[] source2 = this.adjustSuppliedNodeArray(source, inherit);
        if (this.children instanceof NodeImpl) {
            if (source2.length == 0) {
                this.children = null;
            } else if (source2.length == 1) {
                this.children = source2[0];
            } else {
                NodeImpl[] n2 = new NodeImpl[source2.length];
                System.arraycopy(source2, 0, n2, 0, source.length);
                this.children = this.cleanUpChildren(n2);
            }
        } else {
            NodeImpl[] n0 = (NodeImpl[])this.children;
            NodeImpl[] n2 = new NodeImpl[n0.length + source2.length - 1];
            System.arraycopy(n0, 0, n2, 0, index);
            System.arraycopy(source2, 0, n2, index, source2.length);
            System.arraycopy(n0, index + 1, n2, index + source2.length, n0.length - index - 1);
            this.children = this.cleanUpChildren(n2);
        }
    }

    private NodeImpl[] adjustSuppliedNodeArray(NodeInfo[] source, boolean inherit) {
        NodeImpl[] source2 = new NodeImpl[source.length];
        for (int i = 0; i < source.length; ++i) {
            source2[i] = this.convertForeignNode(source[i]);
            NodeImpl child = source2[i];
            child.setRawParent(this);
            if (!(child instanceof ElementImpl)) continue;
            ((ElementImpl)child).fixupInsertedNamespaces(inherit);
        }
        return source2;
    }

    public synchronized void compact(int size) {
        if (size == 0) {
            this.children = null;
        } else if (size == 1) {
            if (this.children instanceof NodeImpl[]) {
                this.children = ((NodeImpl[])this.children)[0];
            }
        } else {
            this.children = Arrays.copyOf((NodeImpl[])this.children, size);
        }
    }
}

