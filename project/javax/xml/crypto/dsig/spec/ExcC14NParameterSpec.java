/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package javax.xml.crypto.dsig.spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;

public final class ExcC14NParameterSpec
implements C14NMethodParameterSpec {
    private List preList;
    public static final String DEFAULT = "#default";

    public ExcC14NParameterSpec() {
        this.preList = Collections.EMPTY_LIST;
    }

    public ExcC14NParameterSpec(List prefixList) {
        if (prefixList == null) {
            throw new NullPointerException("prefixList cannot be null");
        }
        this.preList = ExcC14NParameterSpec.unmodifiableCopyOfList(prefixList);
        int size = this.preList.size();
        for (int i = 0; i < size; ++i) {
            if (this.preList.get(i) instanceof String) continue;
            throw new ClassCastException("not a String");
        }
    }

    private static List unmodifiableCopyOfList(List list) {
        return Collections.unmodifiableList(new ArrayList(list));
    }

    public List getPrefixList() {
        return this.preList;
    }
}

