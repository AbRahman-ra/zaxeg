/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.swing;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.swing.tree.TreeNode;
import org.dom4j.Branch;
import org.dom4j.CharacterData;
import org.dom4j.Node;
import org.dom4j.swing.LeafTreeNode;

public class BranchTreeNode
extends LeafTreeNode {
    protected List children;

    public BranchTreeNode() {
    }

    public BranchTreeNode(Branch xmlNode) {
        super(xmlNode);
    }

    public BranchTreeNode(TreeNode parent, Branch xmlNode) {
        super(parent, xmlNode);
    }

    public Enumeration children() {
        return new Enumeration(){
            private int index = -1;

            public boolean hasMoreElements() {
                return this.index + 1 < BranchTreeNode.this.getChildCount();
            }

            public Object nextElement() {
                return BranchTreeNode.this.getChildAt(++this.index);
            }
        };
    }

    public boolean getAllowsChildren() {
        return true;
    }

    public TreeNode getChildAt(int childIndex) {
        return (TreeNode)this.getChildList().get(childIndex);
    }

    public int getChildCount() {
        return this.getChildList().size();
    }

    public int getIndex(TreeNode node) {
        return this.getChildList().indexOf(node);
    }

    public boolean isLeaf() {
        return this.getXmlBranch().nodeCount() <= 0;
    }

    public String toString() {
        return this.xmlNode.getName();
    }

    protected List getChildList() {
        if (this.children == null) {
            this.children = this.createChildList();
        }
        return this.children;
    }

    protected List createChildList() {
        Branch branch = this.getXmlBranch();
        int size = branch.nodeCount();
        ArrayList<TreeNode> childList = new ArrayList<TreeNode>(size);
        for (int i = 0; i < size; ++i) {
            String text;
            Node node = branch.node(i);
            if (node instanceof CharacterData && ((text = node.getText()) == null || (text = text.trim()).length() <= 0)) continue;
            childList.add(this.createChildTreeNode(node));
        }
        return childList;
    }

    protected TreeNode createChildTreeNode(Node xmlNode) {
        if (xmlNode instanceof Branch) {
            return new BranchTreeNode((TreeNode)this, (Branch)xmlNode);
        }
        return new LeafTreeNode(this, xmlNode);
    }

    protected Branch getXmlBranch() {
        return (Branch)this.xmlNode;
    }
}

