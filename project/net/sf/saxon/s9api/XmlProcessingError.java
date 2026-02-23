/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.StaticError;

public interface XmlProcessingError
extends StaticError {
    public HostLanguage getHostLanguage();

    public boolean isStaticError();

    @Override
    public boolean isTypeError();

    @Override
    public QName getErrorCode();

    @Override
    public String getMessage();

    @Override
    public Location getLocation();

    @Override
    default public String getModuleUri() {
        return this.getLocation().getSystemId();
    }

    @Override
    public boolean isWarning();

    @Override
    public String getPath();

    public Throwable getCause();

    public XmlProcessingError asWarning();

    public boolean isAlreadyReported();

    public void setAlreadyReported(boolean var1);
}

