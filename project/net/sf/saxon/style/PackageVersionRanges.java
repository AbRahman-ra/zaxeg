/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import net.sf.saxon.style.PackageVersion;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.util.FastStringBuffer;

public class PackageVersionRanges {
    ArrayList<PackageVersionRange> ranges = new ArrayList();

    public PackageVersionRanges(String s) throws XPathException {
        for (String p : s.trim().split("\\s*,\\s*")) {
            this.ranges.add(new PackageVersionRange(p));
        }
    }

    public boolean contains(PackageVersion version) {
        for (PackageVersionRange r : this.ranges) {
            if (!r.contains(version)) continue;
            return true;
        }
        return false;
    }

    public String toString() {
        if (this.ranges.size() == 1) {
            return this.ranges.get((int)0).display;
        }
        FastStringBuffer buffer = new FastStringBuffer(256);
        for (PackageVersionRange r : this.ranges) {
            buffer.append(r.display);
            buffer.append(",");
        }
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }

    private class PackageVersionRange {
        String display;
        PackageVersion low;
        PackageVersion high;
        boolean all = false;
        boolean prefix = false;

        PackageVersionRange(String s) throws XPathException {
            this.display = s;
            if ("*".equals(s)) {
                this.all = true;
            } else if (s.endsWith("+")) {
                this.low = new PackageVersion(s.replace("+", ""));
                this.high = PackageVersion.MAX_VALUE;
            } else if (s.endsWith(".*")) {
                this.prefix = true;
                this.low = new PackageVersion(s.replace(".*", ""));
            } else if (s.matches(".*\\s*to\\s+.*")) {
                String[] range = s.split("\\s*to\\s+");
                if (range.length > 2) {
                    throw new XPathException("Invalid version range:" + s, "XTSE0020");
                }
                this.low = range[0].equals("") ? PackageVersion.ZERO : new PackageVersion(range[0]);
                this.high = new PackageVersion(range[1]);
            } else {
                this.high = this.low = new PackageVersion(s);
            }
        }

        boolean contains(PackageVersion v) {
            if (this.all) {
                return true;
            }
            if (this.prefix) {
                return this.low.isPrefix(v);
            }
            return this.low.compareTo(v) <= 0 && v.compareTo(this.high) <= 0;
        }

        public String toString() {
            return this.display;
        }
    }
}

