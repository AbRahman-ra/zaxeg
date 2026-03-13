/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.event.Outputter;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;

public abstract class OutputterEvent {
    public void replay(Outputter out) throws XPathException {
    }

    public static class Append
    extends OutputterEvent {
        Item item;
        Location location;
        int properties;

        public Append(Item item, Location location, int properties) {
            this.item = item;
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Outputter out) throws XPathException {
            out.append(this.item, this.location, this.properties);
        }
    }

    public static class ProcessingInstruction
    extends OutputterEvent {
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
        public void replay(Outputter out) throws XPathException {
            out.processingInstruction(this.target, this.content, this.location, this.properties);
        }
    }

    public static class Comment
    extends OutputterEvent {
        String content;
        Location location;
        int properties;

        public Comment(CharSequence content, Location location, int properties) {
            this.content = content.toString();
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Outputter out) throws XPathException {
            out.comment(this.content, this.location, this.properties);
        }
    }

    public static class Text
    extends OutputterEvent {
        String content;
        Location location;
        int properties;

        public Text(CharSequence content, Location location, int properties) {
            this.content = content.toString();
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Outputter out) throws XPathException {
            out.characters(this.content, this.location, this.properties);
        }
    }

    public static class EndElement
    extends OutputterEvent {
        @Override
        public void replay(Outputter out) throws XPathException {
            out.endElement();
        }
    }

    public static class StartContent
    extends OutputterEvent {
        @Override
        public void replay(Outputter out) throws XPathException {
            out.startContent();
        }
    }

    public static class Namespace
    extends OutputterEvent {
        String prefix;
        String uri;
        int properties;

        public Namespace(String prefix, String uri, int properties) {
            this.prefix = prefix;
            this.uri = uri;
            this.properties = properties;
        }

        @Override
        public void replay(Outputter out) throws XPathException {
            out.namespace(this.prefix, this.uri, this.properties);
        }
    }

    public static class Attribute
    extends OutputterEvent {
        NodeName name;
        SimpleType type;
        String value;
        Location location;
        int properties;

        public Attribute(NodeName name, SimpleType type, String value, Location location, int properties) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Outputter out) throws XPathException {
            out.attribute(this.name, this.type, this.value, this.location, this.properties);
        }
    }

    public static class StartElement
    extends OutputterEvent {
        NodeName name;
        SchemaType type;
        Location location;
        int properties;

        public StartElement(NodeName name, SchemaType type, Location location, int properties) {
            this.name = name;
            this.type = type;
            this.location = location;
            this.properties = properties;
        }

        @Override
        public void replay(Outputter out) throws XPathException {
            out.startElement(this.name, this.type, this.location, this.properties);
        }
    }

    public static class EndDocument
    extends OutputterEvent {
        @Override
        public void replay(Outputter out) throws XPathException {
            out.endDocument();
        }
    }

    public static class StartDocument
    extends OutputterEvent {
        int properties;

        public StartDocument(int properties) {
            this.properties = properties;
        }

        @Override
        public void replay(Outputter out) throws XPathException {
            out.startDocument(this.properties);
        }
    }
}

