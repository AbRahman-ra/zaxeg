/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.tree.util.FastStringBuffer;

public class UnionQNameTest
implements QNameTest {
    List<QNameTest> tests;

    public UnionQNameTest(List<QNameTest> tests) {
        this.tests = new ArrayList<QNameTest>(tests);
    }

    @Override
    public boolean matches(StructuredQName qname) {
        for (QNameTest test : this.tests) {
            if (!test.matches(qname)) continue;
            return true;
        }
        return false;
    }

    public String toString() {
        boolean started = false;
        FastStringBuffer fsb = new FastStringBuffer(256);
        for (QNameTest qt : this.tests) {
            if (started) {
                fsb.append("|");
            } else {
                started = true;
            }
            fsb.append(qt.toString());
        }
        return fsb.toString();
    }

    @Override
    public String exportQNameTest() {
        return String.join((CharSequence)" ", this.tests.stream().map(QNameTest::exportQNameTest).collect(Collectors.toList()));
    }

    @Override
    public String generateJavaScriptNameTest(int targetVersion) {
        FastStringBuffer fsb = new FastStringBuffer(256);
        boolean started = false;
        for (QNameTest qt : this.tests) {
            if (started) {
                fsb.append("||");
            } else {
                started = true;
            }
            String test = qt.generateJavaScriptNameTest(targetVersion);
            fsb.append("(" + test + ")");
        }
        return fsb.toString();
    }
}

