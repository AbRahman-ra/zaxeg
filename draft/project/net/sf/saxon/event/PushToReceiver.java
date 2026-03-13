/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.event;

import net.sf.saxon.Configuration;
import net.sf.saxon.event.ComplexContentOutputter;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.RegularSequenceChecker;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NoNamespaceName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Push;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Untyped;

public class PushToReceiver
implements Push {
    private ComplexContentOutputter out;
    private Configuration config;

    public PushToReceiver(Receiver out) {
        this.out = new ComplexContentOutputter(new RegularSequenceChecker(out, false));
        this.config = out.getPipelineConfiguration().getConfiguration();
    }

    @Override
    public Push.Document document(boolean wellFormed) throws SaxonApiException {
        try {
            this.out.open();
        } catch (XPathException e) {
            throw new SaxonApiException(e);
        }
        return new DocImpl(wellFormed);
    }

    private class ElemImpl
    extends ContainerImpl
    implements Push.Element {
        private boolean foundChild;

        ElemImpl(String defaultNamespace) {
            super(defaultNamespace);
        }

        @Override
        public Push.Element attribute(QName name, String value) throws SaxonApiException {
            this.checkChildNotFound();
            try {
                if (value != null) {
                    FingerprintedQName fp = new FingerprintedQName(name.getStructuredQName(), PushToReceiver.this.config.getNamePool());
                    PushToReceiver.this.out.attribute(fp, BuiltInAtomicType.UNTYPED_ATOMIC, value, Loc.NONE, 0);
                }
                return this;
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }

        @Override
        public Push.Element attribute(String name, String value) throws SaxonApiException {
            this.checkChildNotFound();
            try {
                if (value != null) {
                    NoNamespaceName fp = new NoNamespaceName(name);
                    PushToReceiver.this.out.attribute(fp, BuiltInAtomicType.UNTYPED_ATOMIC, value, Loc.NONE, 0);
                }
                return this;
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }

        @Override
        public Push.Element namespace(String prefix, String uri) throws SaxonApiException {
            this.checkChildNotFound();
            try {
                PushToReceiver.this.out.namespace(prefix, uri, 0);
                return this;
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }

        private void checkChildNotFound() throws SaxonApiException {
            if (this.foundChild) {
                throw new SaxonApiException("Attribute nodes must be attached to an element before any children");
            }
        }

        @Override
        public Push.Element element(QName name) throws SaxonApiException {
            this.foundChild = true;
            return super.element(name);
        }

        @Override
        public Push.Element element(String name) throws SaxonApiException {
            this.foundChild = true;
            return super.element(name);
        }

        @Override
        public Push.Element text(CharSequence value) throws SaxonApiException {
            this.foundChild = true;
            return (Push.Element)super.text(value);
        }

        @Override
        public Push.Element comment(CharSequence value) throws SaxonApiException {
            this.foundChild = true;
            return (Push.Element)super.comment(value);
        }

        @Override
        public Push.Element processingInstruction(String name, CharSequence value) throws SaxonApiException {
            this.foundChild = true;
            return (Push.Element)super.processingInstruction(name, value);
        }

        @Override
        void sendEndEvent() throws SaxonApiException {
            try {
                PushToReceiver.this.out.endElement();
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }
    }

    private class DocImpl
    extends ContainerImpl
    implements Push.Document {
        private final boolean wellFormed;
        private boolean foundElement;

        DocImpl(boolean wellFormed) throws SaxonApiException {
            super("");
            this.foundElement = false;
            try {
                this.wellFormed = wellFormed;
                PushToReceiver.this.out.startDocument(0);
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }

        @Override
        public Push.Element element(QName name) throws SaxonApiException {
            if (this.wellFormed && this.foundElement) {
                throw new SaxonApiException("A well-formed document cannot have more than one element child");
            }
            this.foundElement = true;
            return super.element(name);
        }

        @Override
        public Push.Element element(String name) throws SaxonApiException {
            if (this.wellFormed && this.foundElement) {
                throw new SaxonApiException("A well-formed document cannot have more than one element child");
            }
            this.foundElement = true;
            return super.element(name);
        }

        @Override
        public DocImpl text(CharSequence value) throws SaxonApiException {
            if (this.wellFormed && value != null && value.length() > 0) {
                throw new SaxonApiException("A well-formed document cannot contain text outside any element");
            }
            return (DocImpl)super.text(value);
        }

        @Override
        public Push.Document comment(CharSequence value) throws SaxonApiException {
            return (Push.Document)super.comment(value);
        }

        @Override
        public Push.Document processingInstruction(String name, CharSequence value) throws SaxonApiException {
            return (Push.Document)super.processingInstruction(name, value);
        }

        @Override
        void sendEndEvent() throws SaxonApiException {
            try {
                if (this.wellFormed && !this.foundElement) {
                    throw new SaxonApiException("A well-formed document must contain an element node");
                }
                PushToReceiver.this.out.endDocument();
                PushToReceiver.this.out.close();
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }
    }

    private abstract class ContainerImpl
    implements Push.Container {
        private String defaultNamespace;
        private ElemImpl elementAwaitingClosure;
        private boolean closed;

        public ContainerImpl(String defaultNamespace) {
            this.defaultNamespace = defaultNamespace;
        }

        @Override
        public void setDefaultNamespace(String uri) {
            this.defaultNamespace = uri;
        }

        @Override
        public Push.Element element(QName name) throws SaxonApiException {
            try {
                this.implicitClose();
                FingerprintedQName fp = new FingerprintedQName(name.getStructuredQName(), PushToReceiver.this.config.getNamePool());
                PushToReceiver.this.out.startElement(fp, Untyped.getInstance(), Loc.NONE, 0);
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
            this.elementAwaitingClosure = new ElemImpl(this.defaultNamespace);
            return this.elementAwaitingClosure;
        }

        @Override
        public Push.Element element(String name) throws SaxonApiException {
            try {
                this.implicitClose();
                NodeName fp = this.defaultNamespace.isEmpty() ? new NoNamespaceName(name) : new FingerprintedQName("", this.defaultNamespace, name);
                PushToReceiver.this.out.startElement(fp, Untyped.getInstance(), Loc.NONE, 0);
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
            this.elementAwaitingClosure = new ElemImpl(this.defaultNamespace);
            return this.elementAwaitingClosure;
        }

        @Override
        public Push.Container text(CharSequence value) throws SaxonApiException {
            try {
                this.implicitClose();
                if (value != null && value.length() > 0) {
                    PushToReceiver.this.out.characters(value, Loc.NONE, 0);
                }
                return this;
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }

        @Override
        public Push.Container comment(CharSequence value) throws SaxonApiException {
            try {
                this.implicitClose();
                if (value != null) {
                    PushToReceiver.this.out.comment(value, Loc.NONE, 0);
                }
                return this;
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }

        @Override
        public Push.Container processingInstruction(String name, CharSequence value) throws SaxonApiException {
            try {
                this.implicitClose();
                if (value != null) {
                    PushToReceiver.this.out.processingInstruction(name, value, Loc.NONE, 0);
                }
                return this;
            } catch (XPathException e) {
                throw new SaxonApiException(e);
            }
        }

        @Override
        public void close() throws SaxonApiException {
            if (!this.closed) {
                this.implicitClose();
                this.sendEndEvent();
                this.closed = true;
            }
        }

        private void implicitClose() throws SaxonApiException {
            if (this.closed) {
                throw new SaxonApiException("The container has been closed");
            }
            if (this.elementAwaitingClosure != null) {
                this.elementAwaitingClosure.close();
                this.elementAwaitingClosure = null;
            }
        }

        abstract void sendEndEvent() throws SaxonApiException;
    }
}

