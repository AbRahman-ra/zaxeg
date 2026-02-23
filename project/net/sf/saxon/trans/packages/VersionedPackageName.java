/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans.packages;

import net.sf.saxon.style.PackageVersion;
import net.sf.saxon.trans.XPathException;

public class VersionedPackageName {
    public String packageName;
    public PackageVersion packageVersion;

    public VersionedPackageName(String packageName, PackageVersion version) {
        this.packageName = packageName;
        this.packageVersion = version;
    }

    public VersionedPackageName(String packageName, String version) throws XPathException {
        this.packageName = packageName;
        this.packageVersion = new PackageVersion(version);
    }

    public String toString() {
        return this.packageName + " (" + this.packageVersion.toString() + ")";
    }

    public boolean equalsIgnoringSuffix(VersionedPackageName other) {
        return this.packageName.equals(other.packageName) && this.packageVersion.equalsIgnoringSuffix(other.packageVersion);
    }

    public boolean equals(Object obj) {
        return obj instanceof VersionedPackageName && this.packageName.equals(((VersionedPackageName)obj).packageName) && this.packageVersion.equals(((VersionedPackageName)obj).packageVersion);
    }

    public int hashCode() {
        return this.packageName.hashCode() ^ this.packageVersion.hashCode();
    }
}

