/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMObjectModel;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.value.SequenceExtent;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMEnvelope
implements ExternalObjectModel {
    private static final DOMEnvelope THE_INSTANCE = new DOMEnvelope();

    public static DOMEnvelope getInstance() {
        return THE_INSTANCE;
    }

    @Override
    public String getDocumentClassName() {
        return "org.w3c.dom.Document";
    }

    @Override
    public String getIdentifyingURI() {
        return "http://java.sun.com/jaxp/xpath/dom";
    }

    @Override
    public PJConverter getPJConverter(Class<?> targetClass) {
        if (NodeOverNodeInfo.class.isAssignableFrom(targetClass)) {
            return new PJConverter(){

                @Override
                public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
                    return DOMObjectModel.convertXPathValueToObject(value, targetClass);
                }
            };
        }
        if (NodeList.class.isAssignableFrom(targetClass)) {
            return new PJConverter(){

                @Override
                public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
                    return DOMObjectModel.convertXPathValueToObject(value, targetClass);
                }
            };
        }
        return null;
    }

    @Override
    public JPConverter getJPConverter(Class sourceClass, Configuration config) {
        if (NodeOverNodeInfo.class.isAssignableFrom(sourceClass)) {
            return new JPConverter(){

                @Override
                public Sequence convert(Object object, XPathContext context) {
                    return DOMEnvelope.this.convertObjectToXPathValue(object);
                }

                @Override
                public ItemType getItemType() {
                    return AnyNodeTest.getInstance();
                }
            };
        }
        return null;
    }

    @Override
    public PJConverter getNodeListCreator(Object node) {
        return null;
    }

    public boolean isRecognizedNode(Object object) {
        return object instanceof NodeOverNodeInfo;
    }

    public boolean isRecognizedNodeClass(Class nodeClass) {
        return NodeOverNodeInfo.class.isAssignableFrom(nodeClass);
    }

    @Override
    public Receiver getDocumentBuilder(Result result) {
        return null;
    }

    @Override
    public boolean sendSource(Source source, Receiver receiver) throws XPathException {
        Node startNode;
        if (source instanceof DOMSource && (startNode = ((DOMSource)source).getNode()) instanceof NodeOverNodeInfo) {
            NodeInfo base = ((NodeOverNodeInfo)startNode).getUnderlyingNodeInfo();
            Sender.send(base, receiver, null);
            return true;
        }
        return false;
    }

    @Override
    public NodeInfo unravel(Source source, Configuration config) {
        Node dsnode;
        if (source instanceof DOMSource && (dsnode = ((DOMSource)source).getNode()) instanceof NodeOverNodeInfo) {
            return ((NodeOverNodeInfo)dsnode).getUnderlyingNodeInfo();
        }
        return null;
    }

    private Sequence convertObjectToXPathValue(Object object) {
        if (object instanceof NodeList) {
            NodeList list = (NodeList)object;
            int len = list.getLength();
            if (len == 0) {
                return null;
            }
            Item[] nodes = new NodeInfo[len];
            for (int i = 0; i < len; ++i) {
                if (!(list.item(i) instanceof NodeOverNodeInfo)) {
                    return null;
                }
                nodes[i] = ((NodeOverNodeInfo)list.item(i)).getUnderlyingNodeInfo();
            }
            return new SequenceExtent(nodes);
        }
        if (object instanceof NodeOverNodeInfo) {
            return ((NodeOverNodeInfo)object).getUnderlyingNodeInfo();
        }
        return null;
    }
}

