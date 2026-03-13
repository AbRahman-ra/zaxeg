/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.lib.SchemaURIResolver;
import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SchemaValidator;

public abstract class SchemaManager {
    public abstract void setXsdVersion(String var1);

    public abstract String getXsdVersion();

    public abstract void setErrorListener(ErrorListener var1);

    public abstract ErrorListener getErrorListener();

    public abstract void setErrorReporter(ErrorReporter var1);

    public abstract ErrorReporter getErrorReporter();

    public abstract void setSchemaURIResolver(SchemaURIResolver var1);

    public abstract SchemaURIResolver getSchemaURIResolver();

    public abstract void load(Source var1) throws SaxonApiException;

    public abstract void importComponents(Source var1) throws SaxonApiException;

    public abstract void exportComponents(Destination var1) throws SaxonApiException;

    public abstract SchemaValidator newSchemaValidator();
}

