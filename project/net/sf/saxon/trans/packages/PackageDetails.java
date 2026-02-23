/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.packages;

import java.util.Map;
import javax.xml.transform.Source;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.packages.VersionedPackageName;

public class PackageDetails {
    public VersionedPackageName nameAndVersion;
    public String baseName;
    public String shortName;
    public StylesheetPackage loadedPackage;
    public Source sourceLocation;
    public Source exportLocation;
    public Integer priority;
    public Map<StructuredQName, GroundedValue> staticParams;
    public Thread beingProcessed;
}

