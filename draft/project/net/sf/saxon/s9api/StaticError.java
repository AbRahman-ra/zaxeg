/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.s9api;

import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.QName;

public interface StaticError {
    public QName getErrorCode();

    public String getMessage();

    public Location getLocation();

    default public String getModuleUri() {
        return this.getLocation().getSystemId();
    }

    default public int getColumnNumber() {
        return this.getLocation().getColumnNumber();
    }

    default public int getLineNumber() {
        return this.getLocation().getLineNumber();
    }

    default public String getInstructionName() {
        return null;
    }

    public boolean isWarning();

    public boolean isTypeError();

    default public String getPath() {
        return null;
    }

    public void setFatal(String var1);

    public String getFatalErrorMessage();
}

