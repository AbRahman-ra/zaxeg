/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.packages;

import net.sf.saxon.s9api.Location;
import net.sf.saxon.style.PackageVersionRanges;
import net.sf.saxon.trans.XPathException;

public class UsePack {
    public String packageName;
    public PackageVersionRanges ranges;
    public Location location;

    public UsePack(String name, String version, Location location) throws XPathException {
        this.packageName = name;
        this.ranges = new PackageVersionRanges(version == null ? "*" : version);
        this.location = location;
    }
}

