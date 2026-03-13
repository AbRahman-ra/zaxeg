/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.trie;

public final class Tuple2<T1, T2> {
    public final T1 _1;
    public final T2 _2;

    public Tuple2(T1 v1, T2 v2) {
        this._1 = v1;
        this._2 = v2;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Tuple2 tuple = (Tuple2)o;
        if (this._1 != null ? !this._1.equals(tuple._1) : tuple._1 != null) {
            return false;
        }
        return !(this._2 != null ? !this._2.equals(tuple._2) : tuple._2 != null);
    }

    public int hashCode() {
        int result = this._1 != null ? this._1.hashCode() : 0;
        result = 31 * result + (this._2 != null ? this._2.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "(" + this._1 + ',' + this._2 + ')';
    }
}

