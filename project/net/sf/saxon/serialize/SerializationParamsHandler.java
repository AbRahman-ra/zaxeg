/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.serialize;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import net.sf.saxon.expr.instruct.ResultDocument;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.z.IntHashMap;

public class SerializationParamsHandler {
    public static final String NAMESPACE = "http://www.w3.org/2010/xslt-xquery-serialization";
    Properties properties;
    CharacterMap characterMap;
    Location locator;

    public SerializationParamsHandler() {
    }

    public SerializationParamsHandler(Properties props) {
        this.properties = props;
    }

    public void setLocator(Location locator) {
        this.locator = locator;
    }

    public void setSerializationParams(NodeInfo node) throws XPathException {
        NodeInfo child;
        if (this.properties == null) {
            this.properties = new Properties();
        }
        if (node.getNodeKind() == 9) {
            node = Navigator.getOutermostElement(node.getTreeInfo());
        }
        if (node.getNodeKind() != 1) {
            throw new XPathException("Serialization params: node must be a document or element node");
        }
        if (!node.getLocalPart().equals("serialization-parameters")) {
            throw new XPathException("Serialization params: element name must be 'serialization-parameters");
        }
        if (!node.getURI().equals(NAMESPACE)) {
            throw new XPathException("Serialization params: element namespace must be http://www.w3.org/2010/xslt-xquery-serialization");
        }
        SerializationParamsHandler.restrictAttributes(node, new String[0]);
        HashSet<NodeName> nodeNames = new HashSet<NodeName>();
        AxisIterator kids = node.iterateAxis(3, NodeKindTest.ELEMENT);
        while ((child = kids.next()) != null) {
            if (!nodeNames.add(NameOfNode.makeName(child))) {
                throw new XPathException("Duplicated serialization parameter " + child.getDisplayName(), "SEPM0019");
            }
            String lname = child.getLocalPart();
            String uri = child.getURI();
            if (uri.isEmpty()) {
                throw new XPathException("Serialization parameter " + lname + " is in no namespace", "SEPM0017");
            }
            if (NAMESPACE.equals(uri)) {
                uri = "";
            }
            if ("".equals(uri) && lname.equals("use-character-maps")) {
                NodeInfo gChild;
                SerializationParamsHandler.restrictAttributes(child, new String[0]);
                AxisIterator gKids = child.iterateAxis(3, NodeKindTest.ELEMENT);
                IntHashMap<String> map = new IntHashMap<String>();
                while ((gChild = gKids.next()) != null) {
                    SerializationParamsHandler.restrictAttributes(gChild, "character", "map-string");
                    if (!(gChild.getURI().equals(NAMESPACE) && gChild.getLocalPart().equals("character-map") || !gChild.getURI().equals(NAMESPACE) && !gChild.getURI().isEmpty())) {
                        throw new XPathException("Invalid child of use-character-maps: " + gChild.getDisplayName(), "SEPM0017");
                    }
                    String ch = SerializationParamsHandler.getAttribute(gChild, "character");
                    String str = SerializationParamsHandler.getAttribute(gChild, "map-string");
                    UnicodeString chValue = UnicodeString.makeUnicodeString(ch);
                    if (chValue.uLength() != 1) {
                        throw new XPathException("In the serialization parameters, the value of @character in the character map must be a single Unicode character", "SEPM0017");
                    }
                    int code = chValue.uCharAt(0);
                    String prev = map.put(code, str);
                    if (prev == null) continue;
                    throw new XPathException("In the serialization parameters, the character map contains two entries for the character \\u" + Integer.toHexString(65536 + code).substring(1), "SEPM0018");
                }
                this.characterMap = new CharacterMap(NameOfNode.makeName(node).getStructuredQName(), map);
                continue;
            }
            SerializationParamsHandler.restrictAttributes(child, "value");
            String value = SerializationParamsHandler.getAttribute(child, "value");
            try {
                ResultDocument.setSerializationProperty(this.properties, uri, lname, value, child.getAllNamespaces(), false, node.getConfiguration());
            } catch (XPathException err) {
                if ("XQST0109".equals(err.getErrorCodeLocalPart()) || "SEPM0016".equals(err.getErrorCodeLocalPart())) {
                    if (!"".equals(uri)) continue;
                    XPathException e2 = new XPathException("Unknown serialization parameter " + Err.depict(child), "SEPM0017");
                    e2.setLocator(this.locator);
                    throw e2;
                }
                throw err;
            }
        }
    }

    private static void restrictAttributes(NodeInfo element, String ... allowedNames) throws XPathException {
        for (AttributeInfo att : element.attributes()) {
            NodeName name = att.getNodeName();
            if (!"".equals(name.getURI()) || Arrays.binarySearch(allowedNames, name.getLocalPart()) >= 0) continue;
            throw new XPathException("In serialization parameters, attribute @" + name.getLocalPart() + " must not appear on element " + element.getDisplayName(), "SEPM0017");
        }
    }

    private static String getAttribute(NodeInfo element, String localName) throws XPathException {
        String value = element.getAttributeValue("", localName);
        if (value == null) {
            throw new XPathException("In serialization parameters, attribute @" + localName + " is missing on element " + element.getDisplayName());
        }
        return value;
    }

    public SerializationProperties getSerializationProperties() {
        CharacterMapIndex index = new CharacterMapIndex();
        if (this.characterMap != null) {
            index.putCharacterMap(new StructuredQName("", "", "charMap"), this.characterMap);
            this.properties.put("use-character-maps", "charMap");
        }
        return new SerializationProperties(this.properties, index);
    }

    public CharacterMap getCharacterMap() {
        return this.characterMap;
    }
}

