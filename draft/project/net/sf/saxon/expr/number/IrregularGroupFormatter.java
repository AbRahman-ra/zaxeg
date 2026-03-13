/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.number;

import java.util.List;
import net.sf.saxon.expr.number.NumericGroupFormatter;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.z.IntSet;

public class IrregularGroupFormatter
extends NumericGroupFormatter {
    private IntSet groupingPositions = null;
    private List<Integer> separators = null;

    public IrregularGroupFormatter(IntSet groupingPositions, List<Integer> sep, UnicodeString adjustedPicture) {
        this.groupingPositions = groupingPositions;
        this.separators = sep;
        this.adjustedPicture = adjustedPicture;
    }

    @Override
    public String format(FastStringBuffer value) {
        UnicodeString in = UnicodeString.makeUnicodeString(value);
        int m = 0;
        for (int l = 0; l < in.uLength(); ++l) {
            if (!this.groupingPositions.contains(l)) continue;
            ++m;
        }
        int[] out = new int[in.uLength() + m];
        int j = 0;
        int k = out.length - 1;
        for (int i = in.uLength() - 1; i >= 0; --i) {
            out[k--] = in.uCharAt(i);
            if (i <= 0 || !this.groupingPositions.contains(in.uLength() - i)) continue;
            out[k--] = this.separators.get(j++);
        }
        return UnicodeString.makeUnicodeString(out).toString();
    }

    @Override
    public String getSeparator() {
        if (this.separators.size() == 0) {
            return null;
        }
        int sep = this.separators.get(this.separators.size() - 1);
        FastStringBuffer fsb = new FastStringBuffer(16);
        fsb.appendWideChar(sep);
        return fsb.toString();
    }
}

