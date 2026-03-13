/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.flwor;

import java.util.List;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.Outputter;
import net.sf.saxon.expr.Binding;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.VariableReference;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.flwor.FLWORExpression;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.flwor.OperandProcessor;
import net.sf.saxon.expr.flwor.TuplePull;
import net.sf.saxon.expr.flwor.TuplePush;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;

public abstract class Clause {
    private Location location;
    private PackageData packageData;
    private boolean repeated;

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location locationId) {
        this.location = locationId;
    }

    public void setPackageData(PackageData pd) {
        this.packageData = pd;
    }

    public PackageData getPackageData() {
        return this.packageData;
    }

    public Configuration getConfiguration() {
        return this.packageData.getConfiguration();
    }

    public void setRepeated(boolean repeated) {
        this.repeated = repeated;
    }

    public boolean isRepeated() {
        return this.repeated;
    }

    public abstract Clause copy(FLWORExpression var1, RebindingMap var2);

    public void optimize(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
    }

    public void typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextInfo) throws XPathException {
    }

    public abstract TuplePull getPullStream(TuplePull var1, XPathContext var2);

    public abstract TuplePush getPushStream(TuplePush var1, Outputter var2, XPathContext var3);

    public abstract void processOperands(OperandProcessor var1) throws XPathException;

    public abstract void explain(ExpressionPresenter var1) throws XPathException;

    public LocalVariableBinding[] getRangeVariables() {
        return new LocalVariableBinding[0];
    }

    public void gatherVariableReferences(ExpressionVisitor visitor, Binding binding, List<VariableReference> refs) {
    }

    public boolean containsNonInlineableVariableReference(Binding binding) {
        return false;
    }

    public void refineVariableType(ExpressionVisitor visitor, List<VariableReference> references, Expression returnExpr) {
    }

    public abstract void addToPathMap(PathMap var1, PathMap.PathMapNodeSet var2);

    public abstract ClauseName getClauseKey();

    public String toShortString() {
        return this.toString();
    }

    public static enum ClauseName {
        FOR,
        LET,
        WINDOW,
        GROUP_BY,
        COUNT,
        ORDER_BY,
        WHERE,
        TRACE,
        FOR_MEMBER;

    }
}

