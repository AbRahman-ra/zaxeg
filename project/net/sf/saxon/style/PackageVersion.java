/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.trans.XPathException;

public class PackageVersion
implements Comparable<PackageVersion> {
    public List<Integer> parts;
    public String suffix;
    public static PackageVersion ZERO = new PackageVersion(new int[]{0});
    public static PackageVersion ONE = new PackageVersion(new int[]{1});
    public static PackageVersion MAX_VALUE = new PackageVersion(new int[]{Integer.MAX_VALUE});

    public PackageVersion(int[] values) {
        this.parts = new ArrayList<Integer>(values.length);
        for (int value : values) {
            this.parts.add(value);
        }
        this.trimTrailingZeroes();
    }

    private void trimTrailingZeroes() {
        for (int i = this.parts.size() - 1; i > 0; --i) {
            if (this.parts.get(i) != 0) {
                return;
            }
            this.parts.remove(i);
        }
    }

    public PackageVersion(String s) throws XPathException {
        this.parts = new ArrayList<Integer>();
        String original = s;
        if (s.contains("-")) {
            int i = s.indexOf(45);
            this.suffix = s.substring(i + 1);
            if (!NameChecker.isValidNCName(this.suffix)) {
                throw new XPathException("Illegal NCName as package-version NamePart: " + original, "XTSE0020");
            }
            s = s.substring(0, i);
        }
        if (s.equals("")) {
            throw new XPathException("No numeric component of package-version: " + original, "XTSE0020");
        }
        if (s.startsWith(".")) {
            throw new XPathException("The package-version cannot start with '.'", "XTSE0020");
        }
        if (s.endsWith(".")) {
            throw new XPathException("The package-version cannot end with '.'", "XTSE0020");
        }
        for (String p : s.trim().split("\\.")) {
            try {
                this.parts.add(Integer.valueOf(p));
            } catch (NumberFormatException e) {
                throw new XPathException("Error in package-version: " + e.getMessage(), "XTSE0020");
            }
        }
        this.trimTrailingZeroes();
    }

    public boolean equals(Object o) {
        if (o instanceof PackageVersion) {
            PackageVersion p = (PackageVersion)o;
            if (this.parts.equals(p.parts)) {
                if (this.suffix != null) {
                    return this.suffix.equals(p.suffix);
                }
                return p.suffix == null;
            }
        }
        return false;
    }

    public boolean equalsIgnoringSuffix(PackageVersion other) {
        return this.parts.equals(other.parts);
    }

    @Override
    public int compareTo(PackageVersion o) {
        PackageVersion pv = o;
        List<Integer> p = pv.parts;
        int extent = this.parts.size() - p.size();
        int len = Math.min(this.parts.size(), p.size());
        for (int i = 0; i < len; ++i) {
            int comp = this.parts.get(i).compareTo(p.get(i));
            if (comp == 0) continue;
            return comp;
        }
        if (extent == 0) {
            if (this.suffix != null) {
                if (pv.suffix == null) {
                    return -1;
                }
                return this.suffix.compareTo(pv.suffix);
            }
            if (pv.suffix != null) {
                return 1;
            }
        }
        return extent;
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        for (Integer i : this.parts) {
            result.append(".").append(i);
        }
        if (!this.parts.isEmpty()) {
            result = new StringBuilder(result.substring(1));
        }
        if (this.suffix != null) {
            result.append("-").append(this.suffix);
        }
        return result.toString();
    }

    public boolean isPrefix(PackageVersion v) {
        if (v.parts.size() >= this.parts.size()) {
            for (int i = 0; i < this.parts.size(); ++i) {
                if (this.parts.get(i).equals(v.parts.get(i))) continue;
                return false;
            }
            return true;
        }
        return false;
    }
}

