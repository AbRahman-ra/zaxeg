/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.dom;

import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.dom.DOMSender;
import net.sf.saxon.dom.DOMWriter;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.JPConverter;
import net.sf.saxon.expr.PJConverter;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExternalObjectModel;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.MutableNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.wrapper.VirtualNode;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.Type;
import net.sf.saxon.value.SequenceExtent;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMObjectModel
extends TreeModel
implements ExternalObjectModel {
    private static final DOMObjectModel THE_INSTANCE = new DOMObjectModel();
    private static DocumentBuilderFactory factory = null;

    public static DOMObjectModel getInstance() {
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
    public String getName() {
        return "DOM";
    }

    @Override
    public PJConverter getPJConverter(Class<?> targetClass) {
        if (Node.class.isAssignableFrom(targetClass) && !NodeOverNodeInfo.class.isAssignableFrom(targetClass)) {
            return new PJConverter(){

                @Override
                public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
                    return DOMObjectModel.convertXPathValueToObject(value, targetClass);
                }
            };
        }
        if (NodeList.class == targetClass) {
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
        if (Node.class.isAssignableFrom(sourceClass) && !NodeOverNodeInfo.class.isAssignableFrom(sourceClass)) {
            return new JPConverter(){

                @Override
                public Sequence convert(Object obj, XPathContext context) {
                    return DOMObjectModel.this.wrapOrUnwrapNode((Node)obj, context.getConfiguration());
                }

                @Override
                public ItemType getItemType() {
                    return AnyNodeTest.getInstance();
                }
            };
        }
        if (NodeList.class.isAssignableFrom(sourceClass)) {
            return new JPConverter(){

                @Override
                public Sequence convert(Object obj, XPathContext context) {
                    Configuration config = context.getConfiguration();
                    NodeList list = (NodeList)obj;
                    int len = list.getLength();
                    Item[] nodes = new NodeInfo[len];
                    for (int i = 0; i < len; ++i) {
                        nodes[i] = DOMObjectModel.this.wrapOrUnwrapNode(list.item(i), config);
                    }
                    return new SequenceExtent(nodes);
                }

                @Override
                public ItemType getItemType() {
                    return AnyNodeTest.getInstance();
                }

                @Override
                public int getCardinality() {
                    return 57344;
                }
            };
        }
        if (DOMSource.class.isAssignableFrom(sourceClass)) {
            return new JPConverter(){

                @Override
                public Sequence convert(Object obj, XPathContext context) {
                    return DOMObjectModel.this.unravel((DOMSource)obj, context.getConfiguration());
                }

                @Override
                public ItemType getItemType() {
                    return AnyNodeTest.getInstance();
                }
            };
        }
        if (DocumentWrapper.class == sourceClass) {
            return new JPConverter(){

                @Override
                public Sequence convert(Object obj, XPathContext context) {
                    return ((DocumentWrapper)obj).getRootNode();
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
        if (node == null || node instanceof Node || node instanceof DOMSource || node instanceof DocumentWrapper || node instanceof VirtualNode && ((VirtualNode)node).getRealNode() instanceof Node) {
            return new PJConverter(){

                @Override
                public Object convert(Sequence value, Class<?> targetClass, XPathContext context) throws XPathException {
                    return DOMObjectModel.convertXPathValueToObject(value, NodeList.class);
                }
            };
        }
        return null;
    }

    @Override
    public Receiver getDocumentBuilder(Result result) throws XPathException {
        if (result instanceof DOMResult) {
            DOMWriter emitter = new DOMWriter();
            Node root = ((DOMResult)result).getNode();
            if (root instanceof NodeOverNodeInfo && !(((NodeOverNodeInfo)root).getUnderlyingNodeInfo() instanceof MutableNodeInfo)) {
                throw new XPathException("Supplied DOMResult is a non-mutable Saxon implementation");
            }
            Node nextSibling = ((DOMResult)result).getNextSibling();
            if (root == null) {
                try {
                    if (factory == null) {
                        factory = DocumentBuilderFactory.newInstance();
                    }
                    DocumentBuilder docBuilder = factory.newDocumentBuilder();
                    Document out = docBuilder.newDocument();
                    ((DOMResult)result).setNode(out);
                    emitter.setNode(out);
                } catch (ParserConfigurationException e) {
                    throw new XPathException(e);
                }
            } else {
                emitter.setNode(root);
                emitter.setNextSibling(nextSibling);
            }
            return emitter;
        }
        return null;
    }

    @Override
    public Builder makeBuilder(PipelineConfiguration pipe) {
        DOMWriter dw = new DOMWriter();
        dw.setPipelineConfiguration(pipe);
        return dw;
    }

    @Override
    public boolean sendSource(Source source, Receiver receiver) throws XPathException {
        if (source instanceof DOMSource) {
            DOMObjectModel.sendDOMSource((DOMSource)source, receiver);
            return true;
        }
        return false;
    }

    public static void sendDOMSource(DOMSource source, Receiver receiver) throws XPathException {
        Node startNode = source.getNode();
        if (startNode == null) {
            receiver.open();
            receiver.startDocument(0);
            receiver.endDocument();
            receiver.close();
        } else {
            DOMSender driver = new DOMSender(startNode, receiver);
            driver.setSystemId(source.getSystemId());
            receiver.open();
            driver.send();
            receiver.close();
        }
    }

    public NodeInfo wrap(Node node, Configuration config) {
        Document dom = node.getNodeType() == 9 ? (Document)node : node.getOwnerDocument();
        DocumentWrapper docWrapper = new DocumentWrapper(dom, node.getBaseURI(), config);
        return docWrapper.wrap(node);
    }

    public NodeInfo copy(Node node, TreeModel model, Configuration config) throws XPathException {
        PipelineConfiguration pipe = config.makePipelineConfiguration();
        Builder builder = model.makeBuilder(pipe);
        builder.open();
        Sender.send(new DOMSource(node), builder, null);
        NodeInfo result = builder.getCurrentRoot();
        builder.close();
        return result;
    }

    @Override
    public NodeInfo unravel(Source source, Configuration config) {
        Node dsnode;
        if (source instanceof DOMSource && !((dsnode = ((DOMSource)source).getNode()) instanceof NodeOverNodeInfo)) {
            Document dom = dsnode.getNodeType() == 9 ? (Document)dsnode : dsnode.getOwnerDocument();
            DocumentWrapper docWrapper = new DocumentWrapper(dom, source.getSystemId(), config);
            return docWrapper.wrap(dsnode);
        }
        return null;
    }

    private NodeInfo wrapOrUnwrapNode(Node node, Configuration config) {
        if (node instanceof NodeOverNodeInfo) {
            return ((NodeOverNodeInfo)node).getUnderlyingNodeInfo();
        }
        TreeInfo doc = this.wrapDocument(node, "", config);
        return this.wrapNode(doc, node);
    }

    public static Object convertXPathValueToObject(Sequence value, Class<?> target) throws XPathException {
        Item item;
        boolean allowDOM;
        boolean requireDOM = Node.class.isAssignableFrom(target) || target == NodeList.class || target.isArray() && Node.class.isAssignableFrom(target.getComponentType());
        boolean bl = allowDOM = target == Object.class || target.isAssignableFrom(ArrayList.class) || target.isAssignableFrom(HashSet.class) || target.isArray() && target.getComponentType() == Object.class;
        if (!requireDOM && !allowDOM) {
            return null;
        }
        ArrayList<Node> nodes = new ArrayList<Node>(20);
        SequenceIterator iter = value.iterate();
        while ((item = iter.next()) != null) {
            Object o;
            if (item instanceof VirtualNode && (o = ((VirtualNode)item).getRealNode()) instanceof Node) {
                nodes.add((Node)o);
                continue;
            }
            if (requireDOM) {
                if (item instanceof NodeInfo) {
                    nodes.add(NodeOverNodeInfo.wrap((NodeInfo)item));
                    continue;
                }
                throw new XPathException("Cannot convert XPath value to Java object: required class is " + target.getName() + "; supplied value has type " + Type.displayTypeName(item));
            }
            return null;
        }
        if (nodes.isEmpty() && !requireDOM) {
            return null;
        }
        if (Node.class.isAssignableFrom(target)) {
            if (nodes.size() != 1) {
                throw new XPathException("Cannot convert XPath value to Java object: requires a single DOM Nodebut supplied value contains " + nodes.size() + " nodes");
            }
            return nodes.get(0);
        }
        if (target == NodeList.class) {
            return new DOMNodeList(nodes);
        }
        if (target.isArray() && target.getComponentType() == Node.class) {
            return nodes.toArray(new Node[0]);
        }
        if (target.isAssignableFrom(ArrayList.class)) {
            return nodes;
        }
        if (target.isAssignableFrom(HashSet.class)) {
            return new HashSet<Node>(nodes);
        }
        return null;
    }

    private TreeInfo wrapDocument(Object node, String baseURI, Configuration config) {
        if (node instanceof DocumentOverNodeInfo) {
            return (TreeInfo)((Object)((DocumentOverNodeInfo)node).getUnderlyingNodeInfo());
        }
        if (node instanceof NodeOverNodeInfo) {
            return ((NodeOverNodeInfo)node).getUnderlyingNodeInfo().getTreeInfo();
        }
        if (node instanceof Node) {
            if (((Node)node).getNodeType() == 9) {
                Document doc = (Document)node;
                return new DocumentWrapper(doc, baseURI, config);
            }
            if (((Node)node).getNodeType() == 11) {
                DocumentFragment doc = (DocumentFragment)node;
                return new DocumentWrapper(doc, baseURI, config);
            }
            Document doc = ((Node)node).getOwnerDocument();
            return new DocumentWrapper(doc, baseURI, config);
        }
        throw new IllegalArgumentException("Unknown node class " + node.getClass());
    }

    private NodeInfo wrapNode(TreeInfo document, Object node) {
        return ((DocumentWrapper)document).wrap((Node)node);
    }
}

