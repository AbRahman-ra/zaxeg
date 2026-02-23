/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import javax.xml.transform.SourceLocator;
import net.sf.saxon.om.AbsolutePath;
import net.sf.saxon.om.NodeInfo;

public interface Invalidity
extends SourceLocator {
    public int getSchemaPart();

    public String getConstraintName();

    public String getConstraintClauseNumber();

    public String getConstraintReference();

    public NodeInfo getInvalidNode();

    public AbsolutePath getPath();

    public AbsolutePath getContextPath();

    public String getMessage();

    public String getErrorCode();
}

