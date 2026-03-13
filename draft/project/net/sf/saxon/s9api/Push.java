/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

public interface Push {
    public Document document(boolean var1) throws SaxonApiException;

    public static interface Element
    extends Container {
        public Element attribute(QName var1, String var2) throws SaxonApiException;

        public Element attribute(String var1, String var2) throws SaxonApiException;

        public Element namespace(String var1, String var2) throws SaxonApiException;

        @Override
        public Element text(CharSequence var1) throws SaxonApiException;

        @Override
        public Element comment(CharSequence var1) throws SaxonApiException;

        @Override
        public Element processingInstruction(String var1, CharSequence var2) throws SaxonApiException;
    }

    public static interface Document
    extends Container {
        @Override
        public Document text(CharSequence var1) throws SaxonApiException;

        @Override
        public Document comment(CharSequence var1) throws SaxonApiException;

        @Override
        public Document processingInstruction(String var1, CharSequence var2) throws SaxonApiException;
    }

    public static interface Container {
        public void setDefaultNamespace(String var1);

        public Element element(QName var1) throws SaxonApiException;

        public Element element(String var1) throws SaxonApiException;

        public Container text(CharSequence var1) throws SaxonApiException;

        public Container comment(CharSequence var1) throws SaxonApiException;

        public Container processingInstruction(String var1, CharSequence var2) throws SaxonApiException;

        public void close() throws SaxonApiException;
    }
}

