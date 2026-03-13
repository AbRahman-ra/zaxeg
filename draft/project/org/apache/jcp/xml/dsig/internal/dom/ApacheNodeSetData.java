/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.jcp.xml.dsig.internal.dom;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.xml.crypto.NodeSetData;
import org.apache.jcp.xml.dsig.internal.dom.ApacheData;
import org.apache.xml.security.signature.NodeFilter;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.XMLUtils;
import org.w3c.dom.Node;

/*
 * This class specifies class file version 49.0 but uses Java 6 signatures.  Assumed Java 6.
 */
public class ApacheNodeSetData
implements ApacheData,
NodeSetData {
    private XMLSignatureInput xi;

    public ApacheNodeSetData(XMLSignatureInput xi) {
        this.xi = xi;
    }

    @Override
    public Iterator iterator() {
        if (this.xi.getNodeFilters() != null && !this.xi.getNodeFilters().isEmpty()) {
            return Collections.unmodifiableSet(this.getNodeSet(this.xi.getNodeFilters())).iterator();
        }
        try {
            return Collections.unmodifiableSet(this.xi.getNodeSet()).iterator();
        } catch (Exception e) {
            throw new RuntimeException("unrecoverable error retrieving nodeset", e);
        }
    }

    @Override
    public XMLSignatureInput getXMLSignatureInput() {
        return this.xi;
    }

    private Set<Node> getNodeSet(List<NodeFilter> nodeFilters) {
        if (this.xi.isNeedsToBeExpanded()) {
            XMLUtils.circumventBug2650(XMLUtils.getOwnerDocument(this.xi.getSubNode()));
        }
        LinkedHashSet<Node> inputSet = new LinkedHashSet<Node>();
        XMLUtils.getSet(this.xi.getSubNode(), inputSet, null, !this.xi.isExcludeComments());
        LinkedHashSet<Node> nodeSet = new LinkedHashSet<Node>();
        for (Node currentNode : inputSet) {
            Iterator<NodeFilter> it = nodeFilters.iterator();
            boolean skipNode = false;
            while (it.hasNext() && !skipNode) {
                NodeFilter nf = it.next();
                if (nf.isNodeInclude(currentNode) == 1) continue;
                skipNode = true;
            }
            if (skipNode) continue;
            nodeSet.add(currentNode);
        }
        return nodeSet;
    }
}

