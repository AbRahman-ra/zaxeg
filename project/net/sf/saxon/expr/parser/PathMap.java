/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.parser;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.functions.Doc;
import net.sf.saxon.functions.DocumentFn;
import net.sf.saxon.functions.ResolveURI;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.trans.XPathException;

public class PathMap {
    private List<PathMapRoot> pathMapRoots = new ArrayList<PathMapRoot>();
    private HashMap<Binding, PathMapNodeSet> pathsForVariables = new HashMap();

    public PathMap(Expression exp) {
        PathMapNodeSet finalNodes = exp.addToPathMap(this, null);
        if (finalNodes != null) {
            for (PathMapNode node : finalNodes) {
                node.setReturnable(true);
            }
        }
    }

    public PathMapRoot makeNewRoot(Expression exp) {
        for (PathMapRoot r : this.pathMapRoots) {
            if (!exp.isEqual(r.getRootExpression())) continue;
            return r;
        }
        PathMapRoot root = new PathMapRoot(exp);
        this.pathMapRoots.add(root);
        return root;
    }

    public PathMapRoot[] getPathMapRoots() {
        return this.pathMapRoots.toArray(new PathMapRoot[this.pathMapRoots.size()]);
    }

    public void registerPathForVariable(Binding binding, PathMapNodeSet nodeset) {
        this.pathsForVariables.put(binding, nodeset);
    }

    public PathMapNodeSet getPathForVariable(Binding binding) {
        return this.pathsForVariables.get(binding);
    }

    public PathMapRoot getContextDocumentRoot() {
        PathMapRoot[] roots = this.getPathMapRoots();
        PathMapRoot contextRoot = null;
        for (PathMapRoot root : roots) {
            PathMapRoot newRoot = this.reduceToDownwardsAxes(root);
            if (!(newRoot.getRootExpression() instanceof RootExpression)) continue;
            if (contextRoot != null) {
                throw new IllegalStateException("More than one context document root found in path map");
            }
            contextRoot = newRoot;
        }
        return contextRoot;
    }

    public PathMapRoot getContextItemRoot() {
        PathMapRoot[] roots = this.getPathMapRoots();
        PathMapRoot contextRoot = null;
        for (PathMapRoot root : roots) {
            if (!(root.getRootExpression() instanceof ContextItemExpression)) continue;
            if (contextRoot != null) {
                throw new IllegalStateException("More than one context document root found in path map");
            }
            contextRoot = root;
        }
        return contextRoot;
    }

    public PathMapRoot getRootForDocument(String requiredUri) {
        PathMapRoot[] roots = this.getPathMapRoots();
        PathMapRoot requiredRoot = null;
        for (PathMapRoot root : roots) {
            String baseUri;
            PathMapRoot newRoot = this.reduceToDownwardsAxes(root);
            Expression exp = newRoot.getRootExpression();
            if (exp.isCallOn(Doc.class)) {
                baseUri = exp.getStaticBaseURIString();
            } else {
                if (!exp.isCallOn(DocumentFn.class)) continue;
                baseUri = exp.getStaticBaseURIString();
            }
            Expression arg = ((SystemFunctionCall)exp).getArg(0);
            String suppliedUri = null;
            if (arg instanceof Literal) {
                try {
                    String argValue = ((Literal)arg).getValue().getStringValue();
                    suppliedUri = baseUri == null ? (new URI(argValue).isAbsolute() ? argValue : null) : ResolveURI.makeAbsolute(argValue, baseUri).toString();
                } catch (URISyntaxException err) {
                    suppliedUri = null;
                } catch (XPathException err) {
                    suppliedUri = null;
                }
            }
            if (!requiredUri.equals(suppliedUri)) continue;
            if (requiredRoot != null) {
                throw new IllegalStateException("More than one document root found in path map for " + requiredUri);
            }
            requiredRoot = newRoot;
        }
        return requiredRoot;
    }

    public PathMapRoot reduceToDownwardsAxes(PathMapRoot root) {
        if (root.isDownwardsOnly) {
            return root;
        }
        PathMapRoot newRoot = root;
        if (root.getRootExpression() instanceof ContextItemExpression) {
            int i;
            RootExpression slash = new RootExpression();
            newRoot = this.makeNewRoot(slash);
            block3: for (i = root.arcs.size() - 1; i >= 0; --i) {
                PathMapArc arc = (PathMapArc)root.arcs.get(i);
                int axis = arc.getAxis();
                switch (axis) {
                    case 2: 
                    case 8: {
                        PathMapNode newTarget = new PathMapNode();
                        newTarget.arcs.add(arc);
                        newRoot.createArc(4, NodeKindTest.ELEMENT, newTarget);
                        continue block3;
                    }
                    default: {
                        newRoot.createArc(5, arc.getNodeTest(), arc.getTarget());
                    }
                }
            }
            for (i = 0; i < this.pathMapRoots.size(); ++i) {
                if (this.pathMapRoots.get(i) != root) continue;
                this.pathMapRoots.remove(i);
                break;
            }
        }
        Stack<PathMapNode> nodeStack = new Stack<PathMapNode>();
        nodeStack.push(newRoot);
        this.reduceToDownwardsAxes(newRoot, nodeStack);
        newRoot.isDownwardsOnly = true;
        return newRoot;
    }

    private void reduceToDownwardsAxes(PathMapRoot root, Stack<PathMapNode> nodeStack) {
        int i;
        PathMapNode node = nodeStack.peek();
        if (node.hasUnknownDependencies()) {
            root.setHasUnknownDependencies();
        }
        for (i = 0; i < node.arcs.size(); ++i) {
            nodeStack.push(node.arcs.get(i).getTarget());
            this.reduceToDownwardsAxes(root, nodeStack);
            nodeStack.pop();
        }
        block9: for (i = node.arcs.size() - 1; i >= 0; --i) {
            PathMapArc thisArc = node.arcs.get(i);
            PathMapNode grandParent = nodeStack.size() < 2 ? null : (PathMapNode)nodeStack.get(nodeStack.size() - 2);
            int lastAxis = -1;
            if (grandParent != null) {
                for (PathMapArc arc1 : grandParent.arcs) {
                    PathMapArc arc = arc1;
                    if (arc.getTarget() != node) continue;
                    lastAxis = arc.getAxis();
                }
            }
            switch (thisArc.getAxis()) {
                case 1: 
                case 5: {
                    if (thisArc.getNodeTest() == NodeKindTest.DOCUMENT) {
                        node.arcs.remove(i);
                        for (PathMapArc arc : thisArc.getTarget().arcs) {
                            root.arcs.add(arc);
                        }
                        continue block9;
                    }
                }
                case 0: 
                case 6: 
                case 10: {
                    if (thisArc.getAxis() == 5) continue block9;
                    root.createArc(5, thisArc.getNodeTest(), thisArc.getTarget());
                    node.arcs.remove(i);
                    continue block9;
                }
                case 2: 
                case 3: 
                case 4: 
                case 8: {
                    continue block9;
                }
                case 7: 
                case 11: {
                    if (grandParent != null) {
                        grandParent.createArc(lastAxis, thisArc.getNodeTest(), thisArc.getTarget());
                        node.arcs.remove(i);
                        continue block9;
                    }
                    root.createArc(3, thisArc.getNodeTest(), thisArc.getTarget());
                    node.arcs.remove(i);
                    continue block9;
                }
                case 9: {
                    if (lastAxis == 3 || lastAxis == 2 || lastAxis == 8) {
                        if (node.isReturnable()) {
                            grandParent.setReturnable(true);
                        }
                        PathMapNode target = thisArc.getTarget();
                        for (int a = 0; a < target.arcs.size(); ++a) {
                            grandParent.arcs.add(target.arcs.get(a));
                        }
                        node.arcs.remove(i);
                        continue block9;
                    }
                    if (lastAxis == 4) {
                        if (thisArc.getTarget().arcs.isEmpty()) {
                            grandParent.createArc(5, thisArc.getNodeTest());
                        } else {
                            grandParent.createArc(5, thisArc.getNodeTest(), thisArc.getTarget());
                        }
                        node.arcs.remove(i);
                        continue block9;
                    }
                    if (thisArc.getTarget().arcs.isEmpty()) {
                        root.createArc(5, thisArc.getNodeTest());
                    } else {
                        root.createArc(5, thisArc.getNodeTest(), thisArc.getTarget());
                    }
                    node.arcs.remove(i);
                    continue block9;
                }
                case 12: {
                    node.arcs.remove(i);
                }
            }
        }
    }

    public void diagnosticDump(Logger out) {
        for (int i = 0; i < this.pathMapRoots.size(); ++i) {
            out.info("\nROOT EXPRESSION " + i);
            PathMapRoot mapRoot = this.pathMapRoots.get(i);
            if (mapRoot.hasUnknownDependencies()) {
                out.info("  -- has unknown dependencies --");
            }
            Expression exp = mapRoot.rootExpression;
            exp.explain(out);
            out.info("\nTREE FOR EXPRESSION " + i);
            this.showArcs(out, mapRoot, 2);
        }
    }

    private void showArcs(Logger out, PathMapNode node, int indent) {
        String pad = "                                           ".substring(0, indent);
        List<PathMapArc> arcs = node.arcs;
        for (PathMapArc arc : arcs) {
            out.info(pad + AxisInfo.axisName[arc.axis] + "::" + arc.test.toString() + (arc.target.isAtomized() ? " @" : "") + (arc.target.isReturnable() ? " #" : "") + (arc.target.hasUnknownDependencies() ? " ...??" : ""));
            this.showArcs(out, arc.target, indent + 2);
        }
    }

    public static class PathMapNodeSet
    extends HashSet<PathMapNode> {
        public PathMapNodeSet() {
        }

        public PathMapNodeSet(PathMapNode singleton) {
            this.add(singleton);
        }

        public PathMapNodeSet createArc(int axis, NodeTest test) {
            PathMapNodeSet targetSet = new PathMapNodeSet();
            for (PathMapNode node : this) {
                targetSet.add(node.createArc(axis, test));
            }
            return targetSet;
        }

        public void addNodeSet(PathMapNodeSet nodes) {
            if (nodes != null) {
                for (PathMapNode node : nodes) {
                    this.add(node);
                }
            }
        }

        public void setAtomized() {
            for (PathMapNode node : this) {
                node.setAtomized();
            }
        }

        public void setReturnable(boolean isReturned) {
            for (PathMapNode node : this) {
                node.setReturnable(isReturned);
            }
        }

        public boolean hasReachableReturnables() {
            for (PathMapNode node : this) {
                if (!node.hasReachableReturnables()) continue;
                return true;
            }
            return false;
        }

        public boolean allPathsAreWithinStreamableSnapshot() {
            for (PathMapNode node : this) {
                if (node.allPathsAreWithinStreamableSnapshot()) continue;
                return false;
            }
            return true;
        }

        public void addDescendants() {
            for (PathMapNode node : this) {
                node.createArc(4, AnyNodeTest.getInstance());
            }
        }

        public void setHasUnknownDependencies() {
            for (PathMapNode node : this) {
                node.setHasUnknownDependencies();
            }
        }
    }

    public static class PathMapArc {
        private PathMapNode target;
        private int axis;
        private NodeTest test;

        private PathMapArc(int axis, NodeTest test, PathMapNode target) {
            this.axis = axis;
            this.test = test;
            this.target = target;
        }

        public int getAxis() {
            return this.axis;
        }

        public NodeTest getNodeTest() {
            return this.test;
        }

        public PathMapNode getTarget() {
            return this.target;
        }
    }

    public static class PathMapRoot
    extends PathMapNode {
        private Expression rootExpression;
        private boolean isDownwardsOnly;

        private PathMapRoot(Expression root) {
            this.rootExpression = root;
        }

        public Expression getRootExpression() {
            return this.rootExpression;
        }
    }

    public static class PathMapNode {
        List<PathMapArc> arcs = new ArrayList<PathMapArc>();
        private boolean returnable;
        private boolean atomized;
        private boolean hasUnknownDependencies;

        private PathMapNode() {
        }

        public PathMapNode createArc(int axis, NodeTest test) {
            for (PathMapArc a : this.arcs) {
                if (a.getAxis() != axis || !a.getNodeTest().equals(test)) continue;
                return a.getTarget();
            }
            PathMapNode target = new PathMapNode();
            PathMapArc arc = new PathMapArc(axis, test, target);
            this.arcs.add(arc);
            return target;
        }

        public void createArc(int axis, NodeTest test, PathMapNode target) {
            for (PathMapArc a : this.arcs) {
                if (a.getAxis() != axis || !a.getNodeTest().equals(test) || a.getTarget() != target) continue;
                a.getTarget().setReturnable(a.getTarget().isReturnable() || target.isReturnable());
                if (target.isAtomized()) {
                    a.getTarget().setAtomized();
                }
                return;
            }
            PathMapArc arc = new PathMapArc(axis, test, target);
            this.arcs.add(arc);
        }

        public PathMapArc[] getArcs() {
            return this.arcs.toArray(new PathMapArc[this.arcs.size()]);
        }

        public void setReturnable(boolean returnable) {
            this.returnable = returnable;
        }

        public boolean isReturnable() {
            return this.returnable;
        }

        public boolean hasReachableReturnables() {
            if (this.isReturnable()) {
                return true;
            }
            for (PathMapArc arc : this.arcs) {
                if (!arc.getTarget().hasReachableReturnables()) continue;
                return true;
            }
            return false;
        }

        public void setAtomized() {
            this.atomized = true;
        }

        public boolean isAtomized() {
            return this.atomized;
        }

        public void setHasUnknownDependencies() {
            this.hasUnknownDependencies = true;
        }

        public boolean hasUnknownDependencies() {
            return this.hasUnknownDependencies;
        }

        public boolean allPathsAreWithinStreamableSnapshot() {
            if (this.hasUnknownDependencies() || this.isReturnable() || this.isAtomized()) {
                return false;
            }
            for (PathMapArc arc : this.arcs) {
                PathMapNode next;
                int axis = arc.getAxis();
                if (axis == 2) {
                    next = arc.getTarget();
                    if (next.isReturnable()) {
                        return false;
                    }
                    if (next.getArcs().length == 0 || next.allPathsAreWithinStreamableSnapshot()) continue;
                    return false;
                }
                if (axis == 12 || axis == 0 || axis == 1 || axis == 9) {
                    next = arc.getTarget();
                    if (next.isAtomized()) {
                        return false;
                    }
                    if (next.allPathsAreWithinStreamableSnapshot()) continue;
                    return false;
                }
                return false;
            }
            return true;
        }
    }
}

