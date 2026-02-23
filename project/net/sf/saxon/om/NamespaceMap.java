/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NamespaceBindingSet;
import net.sf.saxon.om.NamespaceDeltaMap;
import net.sf.saxon.om.NamespaceResolver;

public class NamespaceMap
implements NamespaceBindingSet,
NamespaceResolver {
    protected String[] prefixes;
    protected String[] uris;
    private static String[] emptyArray = new String[0];
    private static NamespaceMap EMPTY_MAP = new NamespaceMap();

    public static NamespaceMap emptyMap() {
        return EMPTY_MAP;
    }

    public static NamespaceMap of(String prefix, String uri) {
        NamespaceMap map = new NamespaceMap();
        if (map.isPointlessMapping(prefix, uri)) {
            return EMPTY_MAP;
        }
        map.prefixes = new String[]{prefix};
        map.uris = new String[]{uri};
        return map;
    }

    protected NamespaceMap() {
        this.prefixes = emptyArray;
        this.uris = emptyArray;
    }

    protected NamespaceMap newInstance() {
        return new NamespaceMap();
    }

    public NamespaceMap(List<NamespaceBinding> bindings) {
        NamespaceBinding[] bindingArray = bindings.toArray(NamespaceBinding.EMPTY_ARRAY);
        Arrays.sort(bindingArray, Comparator.comparing(NamespaceBinding::getPrefix));
        boolean bindsXmlNamespace = false;
        this.prefixes = new String[bindingArray.length];
        this.uris = new String[bindingArray.length];
        for (int i = 0; i < bindingArray.length; ++i) {
            this.prefixes[i] = bindingArray[i].getPrefix();
            this.uris[i] = bindingArray[i].getURI();
            if (this.prefixes[i].equals("xml")) {
                bindsXmlNamespace = true;
                if (this.uris[i].equals("http://www.w3.org/XML/1998/namespace")) continue;
                throw new IllegalArgumentException("Binds xml prefix to the wrong namespace");
            }
            if (!this.uris[i].equals("http://www.w3.org/XML/1998/namespace")) continue;
            throw new IllegalArgumentException("Binds xml namespace to the wrong prefix");
        }
        if (bindsXmlNamespace) {
            this.remove("xml");
        }
    }

    public static NamespaceMap fromNamespaceResolver(NamespaceResolver resolver) {
        Iterator<String> iter = resolver.iteratePrefixes();
        ArrayList<NamespaceBinding> bindings = new ArrayList<NamespaceBinding>();
        while (iter.hasNext()) {
            String prefix = iter.next();
            String uri = resolver.getURIForPrefix(prefix, true);
            bindings.add(new NamespaceBinding(prefix, uri));
        }
        return new NamespaceMap(bindings);
    }

    public boolean allowsNamespaceUndeclarations() {
        return false;
    }

    public int size() {
        return this.prefixes.length;
    }

    public boolean isEmpty() {
        return this.prefixes.length == 0;
    }

    @Override
    public String getURI(String prefix) {
        if (prefix.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        int position = Arrays.binarySearch(this.prefixes, prefix);
        return position >= 0 ? this.uris[position] : null;
    }

    public String getDefaultNamespace() {
        if (this.prefixes.length > 0 && this.prefixes[0].isEmpty()) {
            return this.uris[0];
        }
        return "";
    }

    public NamespaceMap put(String prefix, String uri) {
        if (this.isPointlessMapping(prefix, uri)) {
            return this;
        }
        int position = Arrays.binarySearch(this.prefixes, prefix);
        if (position >= 0) {
            if (this.uris[position].equals(uri)) {
                return this;
            }
            if (uri.isEmpty()) {
                NamespaceMap n2 = this.newInstance();
                n2.prefixes = new String[this.prefixes.length - 1];
                System.arraycopy(this.prefixes, 0, n2.prefixes, 0, position);
                System.arraycopy(this.prefixes, position + 1, n2.prefixes, position + 1, this.prefixes.length - position);
                n2.uris = new String[this.uris.length - 1];
                System.arraycopy(this.uris, 0, n2.uris, 0, position);
                System.arraycopy(this.uris, position + 1, n2.uris, position + 1, this.uris.length - position);
                return n2;
            }
            NamespaceMap n2 = this.newInstance();
            n2.prefixes = Arrays.copyOf(this.prefixes, this.prefixes.length);
            n2.uris = Arrays.copyOf(this.uris, this.uris.length);
            n2.uris[position] = uri;
            return n2;
        }
        int insertionPoint = -position - 1;
        String[] p2 = new String[this.prefixes.length + 1];
        String[] u2 = new String[this.uris.length + 1];
        System.arraycopy(this.prefixes, 0, p2, 0, insertionPoint);
        System.arraycopy(this.uris, 0, u2, 0, insertionPoint);
        p2[insertionPoint] = prefix;
        u2[insertionPoint] = uri;
        System.arraycopy(this.prefixes, insertionPoint, p2, insertionPoint + 1, this.prefixes.length - insertionPoint);
        System.arraycopy(this.uris, insertionPoint, u2, insertionPoint + 1, this.prefixes.length - insertionPoint);
        NamespaceMap n2 = this.newInstance();
        n2.prefixes = p2;
        n2.uris = u2;
        return n2;
    }

    private boolean isPointlessMapping(String prefix, String uri) {
        if (prefix.equals("xml")) {
            if (!uri.equals("http://www.w3.org/XML/1998/namespace")) {
                throw new IllegalArgumentException("Invalid URI for xml prefix");
            }
            return true;
        }
        if (uri.equals("http://www.w3.org/XML/1998/namespace")) {
            throw new IllegalArgumentException("Invalid prefix for XML namespace");
        }
        return false;
    }

    public NamespaceMap bind(String prefix, String uri) {
        if (uri.isEmpty()) {
            return this.remove(prefix);
        }
        return this.put(prefix, uri);
    }

    public NamespaceMap remove(String prefix) {
        int position = Arrays.binarySearch(this.prefixes, prefix);
        if (position >= 0) {
            String[] p2 = new String[this.prefixes.length - 1];
            String[] u2 = new String[this.uris.length - 1];
            System.arraycopy(this.prefixes, 0, p2, 0, position);
            System.arraycopy(this.uris, 0, u2, 0, position);
            System.arraycopy(this.prefixes, position + 1, p2, position, this.prefixes.length - position - 1);
            System.arraycopy(this.uris, position + 1, u2, position, this.uris.length - position - 1);
            NamespaceMap n2 = this.newInstance();
            n2.prefixes = p2;
            n2.uris = u2;
            return n2;
        }
        return this;
    }

    public NamespaceMap putAll(NamespaceMap delta) {
        if (this == delta) {
            return this;
        }
        if (this.isEmpty()) {
            return delta;
        }
        if (delta.isEmpty()) {
            return this;
        }
        String[] p1 = this.prefixes;
        String[] u1 = this.uris;
        String[] p2 = delta.prefixes;
        String[] u2 = delta.uris;
        ArrayList<String> p3 = new ArrayList<String>(p1.length + p2.length);
        ArrayList<String> u3 = new ArrayList<String>(p1.length + p2.length);
        int i1 = 0;
        int i2 = 0;
        while (true) {
            int c;
            if ((c = p1[i1].compareTo(p2[i2])) < 0) {
                p3.add(p1[i1]);
                u3.add(u1[i1]);
                if (++i1 < p1.length) continue;
                break;
            }
            if (c > 0) {
                p3.add(p2[i2]);
                u3.add(u2[i2]);
                if (++i2 < p2.length) continue;
                break;
            }
            p3.add(p2[i2]);
            u3.add(u2[i2]);
            if (++i1 >= p1.length || ++i2 >= p2.length) break;
        }
        while (i1 < p1.length) {
            p3.add(p1[i1]);
            u3.add(u1[i1]);
            ++i1;
        }
        while (i2 < p2.length) {
            p3.add(p2[i2]);
            u3.add(u2[i2]);
            ++i2;
        }
        NamespaceMap n2 = new NamespaceMap();
        n2.prefixes = p3.toArray(new String[0]);
        n2.uris = u3.toArray(new String[0]);
        return n2;
    }

    public NamespaceMap addAll(NamespaceBindingSet namespaces) {
        if (namespaces instanceof NamespaceMap) {
            return this.putAll((NamespaceMap)namespaces);
        }
        NamespaceMap map = this;
        for (NamespaceBinding nb : namespaces) {
            map = map.put(nb.getPrefix(), nb.getURI());
        }
        return map;
    }

    public NamespaceMap applyDifferences(NamespaceDeltaMap delta) {
        if (delta.isEmpty()) {
            return this;
        }
        String[] p1 = this.prefixes;
        String[] u1 = this.uris;
        String[] p2 = delta.prefixes;
        String[] u2 = delta.uris;
        ArrayList<String> prefixes = new ArrayList<String>(p1.length + p2.length);
        ArrayList<String> uris = new ArrayList<String>(p1.length + p2.length);
        int i1 = 0;
        int i2 = 0;
        while (i1 < p1.length && i2 < p2.length) {
            int c = p1[i1].compareTo(p2[i2]);
            if (c < 0) {
                prefixes.add(p1[i1]);
                uris.add(u1[i1]);
                ++i1;
                continue;
            }
            if (c > 0) {
                if (!u2[i2].isEmpty()) {
                    prefixes.add(p2[i2]);
                    uris.add(u2[i2]);
                }
                ++i2;
                continue;
            }
            if (!u2[i2].isEmpty() || p2[i2].isEmpty()) {
                prefixes.add(p2[i2]);
                uris.add(u2[i2]);
            }
            ++i1;
            ++i2;
        }
        while (i1 < p1.length) {
            prefixes.add(p1[i1]);
            uris.add(u1[i1]);
            ++i1;
        }
        while (i2 < p2.length) {
            if (!u2[i2].isEmpty()) {
                prefixes.add(p2[i2]);
                uris.add(u2[i2]);
            }
            ++i2;
        }
        NamespaceMap n2 = new NamespaceMap();
        n2.prefixes = prefixes.toArray(new String[0]);
        n2.uris = uris.toArray(new String[0]);
        return n2;
    }

    @Override
    public Iterator<NamespaceBinding> iterator() {
        return new Iterator<NamespaceBinding>(){
            int i = 0;

            @Override
            public boolean hasNext() {
                return this.i < NamespaceMap.this.prefixes.length;
            }

            @Override
            public NamespaceBinding next() {
                NamespaceBinding nb = new NamespaceBinding(NamespaceMap.this.prefixes[this.i], NamespaceMap.this.uris[this.i]);
                ++this.i;
                return nb;
            }
        };
    }

    public NamespaceBinding[] getNamespaceBindings() {
        NamespaceBinding[] result = new NamespaceBinding[this.prefixes.length];
        for (int i = 0; i < this.prefixes.length; ++i) {
            result[i] = new NamespaceBinding(this.prefixes[i], this.uris[i]);
        }
        return result;
    }

    public NamespaceBinding[] getDifferences(NamespaceMap other, boolean addUndeclarations) {
        ArrayList<NamespaceBinding> result = new ArrayList<NamespaceBinding>();
        int i = 0;
        int j = 0;
        while (true) {
            if (i < this.prefixes.length && j < other.prefixes.length) {
                int c = this.prefixes[i].compareTo(other.prefixes[j]);
                if (c < 0) {
                    result.add(new NamespaceBinding(this.prefixes[i], this.uris[i]));
                    ++i;
                    continue;
                }
                if (c == 0) {
                    if (!this.uris[i].equals(other.uris[j])) {
                        result.add(new NamespaceBinding(this.prefixes[i], this.uris[i]));
                    }
                    ++i;
                    ++j;
                    continue;
                }
                if (addUndeclarations || this.prefixes[i].isEmpty()) {
                    result.add(new NamespaceBinding(other.prefixes[j], ""));
                }
                ++j;
                continue;
            }
            if (i < this.prefixes.length) {
                result.add(new NamespaceBinding(this.prefixes[i], this.uris[i]));
                ++i;
                continue;
            }
            if (j >= other.prefixes.length) break;
            result.add(new NamespaceBinding(other.prefixes[j], ""));
            ++j;
        }
        return result.toArray(NamespaceBinding.EMPTY_ARRAY);
    }

    @Override
    public String getURIForPrefix(String prefix, boolean useDefault) {
        if (prefix.equals("xml")) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if (prefix.equals("")) {
            if (useDefault) {
                return this.getDefaultNamespace();
            }
            return "";
        }
        return this.getURI(prefix);
    }

    @Override
    public Iterator<String> iteratePrefixes() {
        ArrayList<String> prefixList = new ArrayList<String>(Arrays.asList(this.prefixes));
        prefixList.add("xml");
        return prefixList.iterator();
    }

    public String[] getPrefixArray() {
        return this.prefixes;
    }

    public String[] getURIsAsArray() {
        return this.uris;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (NamespaceBinding nb : this) {
            sb.append(nb.getPrefix()).append("=").append(nb.getURI()).append(" ");
        }
        return sb.toString();
    }

    public int hashCode() {
        return Arrays.hashCode(this.prefixes) ^ Arrays.hashCode(this.uris);
    }

    public boolean equals(Object obj) {
        return this == obj || obj instanceof NamespaceMap && Arrays.equals(this.prefixes, ((NamespaceMap)obj).prefixes) && Arrays.equals(this.uris, ((NamespaceMap)obj).uris);
    }
}

