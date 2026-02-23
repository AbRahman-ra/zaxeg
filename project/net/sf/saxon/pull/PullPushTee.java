/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pull;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.SequenceReceiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.pull.PullFilter;
import net.sf.saxon.pull.PullProvider;
import net.sf.saxon.pull.UnparsedEntity;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.Orphan;

public class PullPushTee
extends PullFilter {
    private Receiver branch;
    boolean previousAtomic = false;
    private Stack<NamespaceMap> nsStack = new Stack();

    public PullPushTee(PullProvider base, Receiver branch) {
        super(base);
        this.branch = branch;
    }

    public Receiver getReceiver() {
        return this.branch;
    }

    @Override
    public PullProvider.Event next() throws XPathException {
        this.currentEvent = super.next();
        this.copyEvent(this.currentEvent);
        return this.currentEvent;
    }

    private void copyEvent(PullProvider.Event event) throws XPathException {
        PullProvider in = this.getUnderlyingProvider();
        Location loc = in.getSourceLocator();
        if (loc == null) {
            loc = Loc.NONE;
        }
        Receiver out = this.branch;
        switch (event) {
            case START_DOCUMENT: {
                out.startDocument(0);
                break;
            }
            case START_ELEMENT: {
                NamespaceBinding[] bindings = in.getNamespaceDeclarations();
                NamespaceMap nsMap = this.nsStack.isEmpty() ? NamespaceMap.emptyMap() : this.nsStack.peek();
                for (NamespaceBinding binding : bindings) {
                    if (binding == null) break;
                    nsMap = nsMap.put(binding.getPrefix(), binding.getURI());
                }
                this.nsStack.push(nsMap);
                out.startElement(in.getNodeName(), in.getSchemaType(), in.getAttributes(), nsMap, loc, 64);
                break;
            }
            case TEXT: {
                out.characters(in.getStringValue(), loc, 1024);
                break;
            }
            case COMMENT: {
                out.comment(in.getStringValue(), loc, 0);
                break;
            }
            case PROCESSING_INSTRUCTION: {
                out.processingInstruction(in.getNodeName().getLocalPart(), in.getStringValue(), loc, 0);
                break;
            }
            case END_ELEMENT: {
                out.endElement();
                this.nsStack.pop();
                break;
            }
            case END_DOCUMENT: {
                List<UnparsedEntity> entities = in.getUnparsedEntities();
                if (entities != null) {
                    Iterator<UnparsedEntity> iterator = entities.iterator();
                    while (iterator.hasNext()) {
                        UnparsedEntity entity;
                        UnparsedEntity ue = entity = iterator.next();
                        out.setUnparsedEntity(ue.getName(), ue.getSystemId(), ue.getPublicId());
                    }
                }
                out.endDocument();
                break;
            }
            case END_OF_INPUT: {
                in.close();
                break;
            }
            case ATOMIC_VALUE: {
                if (out instanceof SequenceReceiver) {
                    out.append(super.getAtomicValue(), loc, 0);
                    break;
                }
                if (this.previousAtomic) {
                    out.characters(" ", loc, 0);
                }
                CharSequence chars = in.getStringValue();
                out.characters(chars, loc, 0);
                break;
            }
            case ATTRIBUTE: {
                if (!(out instanceof SequenceReceiver)) break;
                Orphan o = new Orphan(in.getPipelineConfiguration().getConfiguration());
                o.setNodeName(this.getNodeName());
                o.setNodeKind((short)2);
                o.setStringValue(this.getStringValue());
                out.append(o, loc, 0);
                break;
            }
            case NAMESPACE: {
                if (!(out instanceof SequenceReceiver)) break;
                Orphan o = new Orphan(in.getPipelineConfiguration().getConfiguration());
                o.setNodeName(this.getNodeName());
                o.setNodeKind((short)13);
                o.setStringValue(this.getStringValue());
                out.append(o, loc, 0);
                break;
            }
            default: {
                throw new UnsupportedOperationException("" + (Object)((Object)event));
            }
        }
        this.previousAtomic = event == PullProvider.Event.ATOMIC_VALUE;
    }
}

