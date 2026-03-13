/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class RegularSequenceChecker
extends ProxyReceiver {
    private Stack<Short> stack = new Stack();
    private State state = State.Initial;
    private boolean fullChecking = false;
    private static Map<State, Map<Transition, State>> machine = new HashMap<State, Map<Transition, State>>();

    private static void edge(State from, Transition event, State to) {
        Map edges = machine.computeIfAbsent(from, s -> new HashMap());
        edges.put(event, to);
    }

    private void transition(Transition event) {
        State newState;
        Map<Transition, State> map = machine.get((Object)this.state);
        State state = newState = map == null ? null : map.get((Object)event);
        if (newState == null) {
            throw new IllegalStateException("Event " + (Object)((Object)event) + " is not permitted in state " + (Object)((Object)this.state));
        }
        this.state = newState;
    }

    public RegularSequenceChecker(Receiver nextReceiver, boolean fullChecking) {
        super(nextReceiver);
        this.fullChecking = fullChecking;
    }

    @Override
    public void append(Item item, Location locationId, int copyNamespaces) throws XPathException {
        try {
            this.transition(Transition.APPEND);
            this.nextReceiver.append(item, locationId, copyNamespaces);
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    @Override
    public void characters(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.transition(Transition.TEXT);
        if (chars.length() == 0 && !this.stack.isEmpty()) {
            throw new IllegalStateException("Zero-length text nodes not allowed within document/element content");
        }
        try {
            this.nextReceiver.characters(chars, locationId, properties);
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    @Override
    public void close() throws XPathException {
        if (this.state != State.Final && this.state != State.Failed) {
            if (!this.stack.isEmpty()) {
                throw new IllegalStateException("Unclosed element or document nodes at end of stream");
            }
            this.nextReceiver.close();
            this.state = State.Final;
        }
    }

    @Override
    public void comment(CharSequence chars, Location locationId, int properties) throws XPathException {
        this.transition(Transition.COMMENT);
        try {
            this.nextReceiver.comment(chars, locationId, properties);
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    @Override
    public void endDocument() throws XPathException {
        this.transition(Transition.END_DOCUMENT);
        if (this.stack.isEmpty() || this.stack.pop() != 9) {
            throw new IllegalStateException("Unmatched endDocument() call");
        }
        try {
            this.nextReceiver.endDocument();
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    @Override
    public void endElement() throws XPathException {
        this.transition(Transition.END_ELEMENT);
        if (this.stack.isEmpty() || this.stack.pop() != 1) {
            throw new IllegalStateException("Unmatched endElement() call");
        }
        if (this.stack.isEmpty()) {
            this.state = State.Open;
        }
        try {
            this.nextReceiver.endElement();
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    @Override
    public void open() throws XPathException {
        this.transition(Transition.OPEN);
        try {
            this.nextReceiver.open();
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    @Override
    public void processingInstruction(String target, CharSequence data, Location locationId, int properties) throws XPathException {
        this.transition(Transition.PI);
        try {
            this.nextReceiver.processingInstruction(target, data, locationId, properties);
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        this.transition(Transition.START_DOCUMENT);
        this.stack.push((short)9);
        try {
            this.nextReceiver.startDocument(properties);
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        this.transition(Transition.START_ELEMENT);
        this.stack.push((short)1);
        if (this.fullChecking) {
            attributes.verify();
            String prefix = elemName.getPrefix();
            if (prefix.isEmpty()) {
                String declaredDefaultUri = namespaces.getDefaultNamespace();
                if (!declaredDefaultUri.equals(elemName.getURI())) {
                    throw new IllegalStateException("URI of element Q{" + elemName.getURI() + "}" + elemName.getLocalPart() + " does not match declared default namespace {" + declaredDefaultUri + "}");
                }
            } else {
                String declaredUri = namespaces.getURI(prefix);
                if (declaredUri == null) {
                    throw new IllegalStateException("Prefix " + prefix + " has not been declared");
                }
                if (!declaredUri.equals(elemName.getURI())) {
                    throw new IllegalStateException("Prefix " + prefix + " is bound to the wrong namespace");
                }
            }
            for (AttributeInfo att : attributes) {
                NodeName name = att.getNodeName();
                if (name.getURI().isEmpty()) continue;
                String attPrefix = name.getPrefix();
                String declaredUri = namespaces.getURI(attPrefix);
                if (declaredUri == null) {
                    throw new IllegalStateException("Prefix " + attPrefix + " has not been declared for attribute " + att.getNodeName().getDisplayName());
                }
                if (declaredUri.equals(name.getURI())) continue;
                throw new IllegalStateException("Prefix " + prefix + " is bound to the wrong namespace {" + declaredUri + "}");
            }
        }
        try {
            this.nextReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        } catch (XPathException e) {
            this.state = State.Failed;
            throw e;
        }
    }

    static {
        RegularSequenceChecker.edge(State.Initial, Transition.OPEN, State.Open);
        RegularSequenceChecker.edge(State.Open, Transition.APPEND, State.Open);
        RegularSequenceChecker.edge(State.Open, Transition.TEXT, State.Open);
        RegularSequenceChecker.edge(State.Open, Transition.COMMENT, State.Open);
        RegularSequenceChecker.edge(State.Open, Transition.PI, State.Open);
        RegularSequenceChecker.edge(State.Open, Transition.START_DOCUMENT, State.Content);
        RegularSequenceChecker.edge(State.Open, Transition.START_ELEMENT, State.Content);
        RegularSequenceChecker.edge(State.Content, Transition.TEXT, State.Content);
        RegularSequenceChecker.edge(State.Content, Transition.COMMENT, State.Content);
        RegularSequenceChecker.edge(State.Content, Transition.PI, State.Content);
        RegularSequenceChecker.edge(State.Content, Transition.START_ELEMENT, State.Content);
        RegularSequenceChecker.edge(State.Content, Transition.END_ELEMENT, State.Content);
        RegularSequenceChecker.edge(State.Content, Transition.END_DOCUMENT, State.Open);
        RegularSequenceChecker.edge(State.Open, Transition.CLOSE, State.Final);
        RegularSequenceChecker.edge(State.Failed, Transition.CLOSE, State.Failed);
    }

    private static enum Transition {
        OPEN,
        APPEND,
        TEXT,
        COMMENT,
        PI,
        START_DOCUMENT,
        START_ELEMENT,
        END_ELEMENT,
        END_DOCUMENT,
        CLOSE;

    }

    public static enum State {
        Initial,
        Open,
        StartTag,
        Content,
        Final,
        Failed;

    }
}

