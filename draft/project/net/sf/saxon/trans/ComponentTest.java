/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.trans.SymbolicName;

public class ComponentTest {
    private int componentKind;
    private QNameTest nameTest;
    private int arity;

    public ComponentTest(int componentKind, QNameTest nameTest, int arity) {
        this.componentKind = componentKind;
        this.nameTest = nameTest;
        this.arity = arity;
    }

    public int getComponentKind() {
        return this.componentKind;
    }

    public QNameTest getQNameTest() {
        return this.nameTest;
    }

    public int getArity() {
        return this.arity;
    }

    public boolean isPartialWildcard() {
        return this.nameTest instanceof LocalNameTest || this.nameTest instanceof NamespaceTest;
    }

    public boolean matches(Actor component) {
        return this.matches(component.getSymbolicName());
    }

    public boolean matches(SymbolicName sn) {
        return !(this.componentKind != -1 && sn.getComponentKind() != this.componentKind || !this.nameTest.matches(sn.getComponentName()) || this.componentKind == 158 && this.arity != -1 && this.arity != ((SymbolicName.F)sn).getArity());
    }

    public SymbolicName getSymbolicNameIfExplicit() {
        if (this.nameTest instanceof NameTest) {
            if (this.componentKind == 158) {
                return new SymbolicName.F(((NameTest)this.nameTest).getMatchingNodeName(), this.arity);
            }
            return new SymbolicName(this.componentKind, ((NameTest)this.nameTest).getMatchingNodeName());
        }
        return null;
    }

    public boolean equals(Object other) {
        return other instanceof ComponentTest && ((ComponentTest)other).componentKind == this.componentKind && ((ComponentTest)other).arity == this.arity && ((ComponentTest)other).nameTest.equals(this.nameTest);
    }

    public int hashCode() {
        return this.componentKind ^ this.arity ^ this.nameTest.hashCode();
    }
}

