/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public abstract class Event {
    public void replay(Receiver out) throws XPathException {
    }

    public static class Append
    extends Event {
        Item item;
        Location location;
        int properties;

        public Append(Item item, Location location, int properties) {
            this.item = item;
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Receiver out) throws XPathException {
            out.append(this.item, this.location, this.properties);
        }
    }

    public static class ProcessingInstruction
    extends Event {
        String target;
        String content;
        Location location;
        int properties;

        public ProcessingInstruction(String target, CharSequence content, Location location, int properties) {
            this.target = target;
            this.content = content.toString();
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Receiver out) throws XPathException {
            out.processingInstruction(this.target, this.content, this.location, this.properties);
        }
    }

    public static class Comment
    extends Event {
        String content;
        Location location;
        int properties;

        public Comment(CharSequence content, Location location, int properties) {
            this.content = content.toString();
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Receiver out) throws XPathException {
            out.comment(this.content, this.location, this.properties);
        }
    }

    public static class Text
    extends Event {
        String content;
        Location location;
        int properties;

        public Text(CharSequence content, Location location, int properties) {
            this.content = content.toString();
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Receiver out) throws XPathException {
            out.characters(this.content, this.location, this.properties);
        }
    }

    public static class EndElement
    extends Event {
        @Override
        public void replay(Receiver out) throws XPathException {
            out.endElement();
        }
    }

    public static class StartElement
    extends Event {
        NodeName name;
        SchemaType type;
        AttributeMap attributes;
        NamespaceMap namespaces;
        Location location;
        int properties;

        public StartElement(NodeName name, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) {
            this.name = name;
            this.type = type;
            this.attributes = attributes;
            this.namespaces = namespaces;
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Receiver out) throws XPathException {
            out.startElement(this.name, this.type, this.attributes, this.namespaces, this.location, this.properties);
        }

        public void replay(Receiver out, int newProps) throws XPathException {
            out.startElement(this.name, this.type, this.attributes, this.namespaces, this.location, newProps);
        }

        public int getProperties() {
            return this.properties;
        }
    }

    public static class EndDocument
    extends Event {
        @Override
        public void replay(Receiver out) throws XPathException {
            out.endDocument();
        }
    }

    public static class StartDocument
    extends Event {
        int properties;

        public StartDocument(int properties) {
            this.properties = properties;
        }

        @Override
        public void replay(Receiver out) throws XPathException {
            out.startDocument(this.properties);
        }
    }
}

