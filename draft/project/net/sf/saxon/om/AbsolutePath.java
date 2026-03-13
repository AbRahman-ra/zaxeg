/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.trans.Err;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;

public class AbsolutePath {
    private List<PathElement> path;
    private String systemId;

    public AbsolutePath(List<PathElement> path) {
        this.path = new ArrayList<PathElement>(path);
    }

    public void appendAttributeName(NodeName attributeName) {
        PathElement last;
        if (!this.path.isEmpty() && (last = this.path.get(this.path.size() - 1)).getNodeKind() == 2) {
            this.path.remove(this.path.size() - 1);
        }
        PathElement att = new PathElement(2, attributeName, 1);
        this.path.add(att);
    }

    public static AbsolutePath pathToNode(NodeInfo node) {
        LinkedList<PathElement> list = new LinkedList<PathElement>();
        while (node != null && node.getNodeKind() != 9) {
            PathElement pe = new PathElement(node.getNodeKind(), NameOfNode.makeName(node), Navigator.getNumberSimple(node, null));
            list.addFirst(pe);
            node = node.getParent();
        }
        return new AbsolutePath(list);
    }

    public String getPathUsingPrefixes() {
        FastStringBuffer fsb = new FastStringBuffer(256);
        for (PathElement pe : this.path) {
            fsb.cat('/');
            pe.toString(fsb, 'p');
        }
        return fsb.toString();
    }

    public String getPathUsingUris() {
        FastStringBuffer fsb = new FastStringBuffer(256);
        for (PathElement pe : this.path) {
            fsb.cat('/');
            pe.toString(fsb, 'u');
        }
        return fsb.toString();
    }

    public String getPathUsingAbbreviatedUris() {
        FastStringBuffer fsb = new FastStringBuffer(256);
        for (PathElement pe : this.path) {
            fsb.cat('/');
            pe.toString(fsb, 's');
        }
        return fsb.toString();
    }

    public String toString() {
        return this.getPathUsingUris();
    }

    public boolean equals(Object obj) {
        return obj instanceof AbsolutePath && obj.toString().equals(this.toString());
    }

    public int hashCode() {
        return this.toString().hashCode();
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public static class PathElement {
        int nodeKind;
        NodeName name;
        int index;

        public PathElement(int nodeKind, NodeName name, int index) {
            this.nodeKind = nodeKind;
            this.name = name;
            this.index = index;
        }

        public int getNodeKind() {
            return this.nodeKind;
        }

        public NodeName getName() {
            return this.name;
        }

        public int getIndex() {
            return this.index;
        }

        public void toString(FastStringBuffer fsb, char option) {
            switch (this.nodeKind) {
                case 9: {
                    fsb.append("(/)");
                    break;
                }
                case 2: {
                    fsb.cat('@');
                    if (!this.name.getURI().isEmpty()) {
                        if (option == 'u') {
                            fsb.append("Q{");
                            fsb.append(this.name.getURI());
                            fsb.append("}");
                        } else if (option == 'p') {
                            String prefix = this.name.getPrefix();
                            if (!prefix.isEmpty()) {
                                fsb.append(prefix);
                                fsb.cat(':');
                            }
                        } else if (option == 's') {
                            fsb.append("Q{");
                            fsb.append(Err.abbreviateURI(this.name.getURI()));
                            fsb.append("}");
                        }
                    }
                    fsb.append(this.getName().getLocalPart());
                    break;
                }
                case 1: {
                    if (option == 'u') {
                        fsb.append("Q{");
                        fsb.append(this.name.getURI());
                        fsb.append("}");
                    } else if (option == 'p') {
                        String prefix = this.name.getPrefix();
                        if (!prefix.isEmpty()) {
                            fsb.append(prefix);
                            fsb.cat(':');
                        }
                    } else if (option == 's' && !this.name.getURI().isEmpty()) {
                        fsb.append("Q{");
                        fsb.append(Err.abbreviateURI(this.name.getURI()));
                        fsb.append("}");
                    }
                    fsb.append(this.name.getLocalPart());
                    this.appendPredicate(fsb);
                    break;
                }
                case 3: {
                    fsb.append("text()");
                    break;
                }
                case 8: {
                    fsb.append("comment()");
                    this.appendPredicate(fsb);
                    break;
                }
                case 7: {
                    fsb.append("processing-instruction(");
                    fsb.append(this.name.getLocalPart());
                    fsb.append(")");
                    this.appendPredicate(fsb);
                    break;
                }
                case 13: {
                    fsb.append("namespace::");
                    if (this.name.getLocalPart().isEmpty()) {
                        fsb.append("*[Q{http://www.w3.org/2005/xpath-functions}local-name()=\"\"]");
                        break;
                    }
                    fsb.append(this.name.getLocalPart());
                    break;
                }
            }
        }

        private void appendPredicate(FastStringBuffer fsb) {
            int index = this.getIndex();
            if (index != -1) {
                fsb.cat('[');
                fsb.append(this.getIndex() + "");
                fsb.cat(']');
            }
        }
    }
}

