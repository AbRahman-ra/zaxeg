/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Genre;
import net.sf.saxon.query.AnnotationList;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.value.SequenceType;

public interface FunctionItemType
extends ItemType {
    @Override
    default public Genre getGenre() {
        return Genre.FUNCTION;
    }

    public SequenceType[] getArgumentTypes();

    public SequenceType getResultType();

    public Affinity relationship(FunctionItemType var1, TypeHierarchy var2);

    public AnnotationList getAnnotationAssertions();

    public Expression makeFunctionSequenceCoercer(Expression var1, RoleDiagnostic var2) throws XPathException;

    public boolean isMapType();

    public boolean isArrayType();
}

