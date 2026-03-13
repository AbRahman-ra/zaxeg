/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.ContextSupport;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.UnresolvableException;
import org.jaxen.expr.DefaultStep;
import org.jaxen.expr.IdentitySet;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.iter.IterableAxis;

public class DefaultNameStep
extends DefaultStep
implements NameStep {
    private static final long serialVersionUID = 428414912247718390L;
    private String prefix;
    private String localName;
    private boolean matchesAnyName;
    private boolean hasPrefix;

    public DefaultNameStep(IterableAxis axis, String prefix, String localName, PredicateSet predicateSet) {
        super(axis, predicateSet);
        this.prefix = prefix;
        this.localName = localName;
        this.matchesAnyName = "*".equals(localName);
        this.hasPrefix = this.prefix != null && this.prefix.length() > 0;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getLocalName() {
        return this.localName;
    }

    public boolean isMatchesAnyName() {
        return this.matchesAnyName;
    }

    public String getText() {
        StringBuffer buf = new StringBuffer(64);
        buf.append(this.getAxisName()).append("::");
        if (this.getPrefix() != null && this.getPrefix().length() > 0) {
            buf.append(this.getPrefix()).append(':');
        }
        return buf.append(this.getLocalName()).append(super.getText()).toString();
    }

    public List evaluate(Context context) throws JaxenException {
        boolean namedAccess;
        List contextNodeSet = context.getNodeSet();
        int contextSize = contextNodeSet.size();
        if (contextSize == 0) {
            return Collections.EMPTY_LIST;
        }
        ContextSupport support = context.getContextSupport();
        IterableAxis iterableAxis = this.getIterableAxis();
        boolean bl = namedAccess = !this.matchesAnyName && iterableAxis.supportsNamedAccess(support);
        if (contextSize == 1) {
            Object contextNode = contextNodeSet.get(0);
            if (namedAccess) {
                String uri = null;
                if (this.hasPrefix && (uri = support.translateNamespacePrefixToUri(this.prefix)) == null) {
                    throw new UnresolvableException("XPath expression uses unbound namespace prefix " + this.prefix);
                }
                Iterator axisNodeIter = iterableAxis.namedAccessIterator(contextNode, support, this.localName, this.prefix, uri);
                if (axisNodeIter == null || !axisNodeIter.hasNext()) {
                    return Collections.EMPTY_LIST;
                }
                ArrayList newNodeSet = new ArrayList();
                while (axisNodeIter.hasNext()) {
                    newNodeSet.add(axisNodeIter.next());
                }
                return this.getPredicateSet().evaluatePredicates(newNodeSet, support);
            }
            Iterator axisNodeIter = iterableAxis.iterator(contextNode, support);
            if (axisNodeIter == null || !axisNodeIter.hasNext()) {
                return Collections.EMPTY_LIST;
            }
            ArrayList newNodeSet = new ArrayList(contextSize);
            while (axisNodeIter.hasNext()) {
                Object eachAxisNode = axisNodeIter.next();
                if (!this.matches(eachAxisNode, support)) continue;
                newNodeSet.add(eachAxisNode);
            }
            return this.getPredicateSet().evaluatePredicates(newNodeSet, support);
        }
        IdentitySet unique = new IdentitySet();
        ArrayList interimSet = new ArrayList(contextSize);
        ArrayList newNodeSet = new ArrayList(contextSize);
        if (namedAccess) {
            String uri = null;
            if (this.hasPrefix && (uri = support.translateNamespacePrefixToUri(this.prefix)) == null) {
                throw new UnresolvableException("XPath expression uses unbound namespace prefix " + this.prefix);
            }
            for (int i = 0; i < contextSize; ++i) {
                Object eachContextNode = contextNodeSet.get(i);
                Iterator axisNodeIter = iterableAxis.namedAccessIterator(eachContextNode, support, this.localName, this.prefix, uri);
                if (axisNodeIter == null || !axisNodeIter.hasNext()) continue;
                while (axisNodeIter.hasNext()) {
                    Object eachAxisNode = axisNodeIter.next();
                    interimSet.add(eachAxisNode);
                }
                List predicateNodes = this.getPredicateSet().evaluatePredicates(interimSet, support);
                Iterator predicateNodeIter = predicateNodes.iterator();
                while (predicateNodeIter.hasNext()) {
                    Object eachPredicateNode = predicateNodeIter.next();
                    if (unique.contains(eachPredicateNode)) continue;
                    unique.add(eachPredicateNode);
                    newNodeSet.add(eachPredicateNode);
                }
                interimSet.clear();
            }
        } else {
            for (int i = 0; i < contextSize; ++i) {
                Object eachContextNode = contextNodeSet.get(i);
                Iterator axisNodeIter = this.axisIterator(eachContextNode, support);
                if (axisNodeIter == null || !axisNodeIter.hasNext()) continue;
                while (axisNodeIter.hasNext()) {
                    Object eachAxisNode = axisNodeIter.next();
                    if (!this.matches(eachAxisNode, support)) continue;
                    interimSet.add(eachAxisNode);
                }
                List predicateNodes = this.getPredicateSet().evaluatePredicates(interimSet, support);
                Iterator predicateNodeIter = predicateNodes.iterator();
                while (predicateNodeIter.hasNext()) {
                    Object eachPredicateNode = predicateNodeIter.next();
                    if (unique.contains(eachPredicateNode)) continue;
                    unique.add(eachPredicateNode);
                    newNodeSet.add(eachPredicateNode);
                }
                interimSet.clear();
            }
        }
        return newNodeSet;
    }

    public boolean matches(Object node, ContextSupport contextSupport) throws JaxenException {
        Navigator nav = contextSupport.getNavigator();
        String myUri = null;
        String nodeName = null;
        String nodeUri = null;
        if (nav.isElement(node)) {
            nodeName = nav.getElementName(node);
            nodeUri = nav.getElementNamespaceUri(node);
        } else {
            if (nav.isText(node)) {
                return false;
            }
            if (nav.isAttribute(node)) {
                if (this.getAxis() != 9) {
                    return false;
                }
                nodeName = nav.getAttributeName(node);
                nodeUri = nav.getAttributeNamespaceUri(node);
            } else {
                if (nav.isDocument(node)) {
                    return false;
                }
                if (nav.isNamespace(node)) {
                    if (this.getAxis() != 10) {
                        return false;
                    }
                    nodeName = nav.getNamespacePrefix(node);
                } else {
                    return false;
                }
            }
        }
        if (this.hasPrefix) {
            myUri = contextSupport.translateNamespacePrefixToUri(this.prefix);
            if (myUri == null) {
                throw new UnresolvableException("Cannot resolve namespace prefix '" + this.prefix + "'");
            }
        } else if (this.matchesAnyName) {
            return true;
        }
        if (this.hasNamespace(myUri) != this.hasNamespace(nodeUri)) {
            return false;
        }
        if (this.matchesAnyName || nodeName.equals(this.getLocalName())) {
            return this.matchesNamespaceURIs(myUri, nodeUri);
        }
        return false;
    }

    private boolean hasNamespace(String uri) {
        return uri != null && uri.length() > 0;
    }

    protected boolean matchesNamespaceURIs(String uri1, String uri2) {
        if (uri1 == uri2) {
            return true;
        }
        if (uri1 == null) {
            return uri2.length() == 0;
        }
        if (uri2 == null) {
            return uri1.length() == 0;
        }
        return uri1.equals(uri2);
    }

    public String toString() {
        String prefix = this.getPrefix();
        String qName = "".equals(prefix) ? this.getLocalName() : this.getPrefix() + ":" + this.getLocalName();
        return "[(DefaultNameStep): " + qName + "]";
    }
}

