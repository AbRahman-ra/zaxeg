/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.expr;

import org.jaxen.JaxenException;
import org.jaxen.expr.BinaryExpr;
import org.jaxen.expr.Expr;
import org.jaxen.expr.FilterExpr;
import org.jaxen.expr.FunctionCallExpr;
import org.jaxen.expr.LiteralExpr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.PathExpr;
import org.jaxen.expr.Predicate;
import org.jaxen.expr.PredicateSet;
import org.jaxen.expr.Step;
import org.jaxen.expr.UnionExpr;
import org.jaxen.expr.VariableReferenceExpr;
import org.jaxen.expr.XPathExpr;

public interface XPathFactory {
    public XPathExpr createXPath(Expr var1) throws JaxenException;

    public PathExpr createPathExpr(FilterExpr var1, LocationPath var2) throws JaxenException;

    public LocationPath createRelativeLocationPath() throws JaxenException;

    public LocationPath createAbsoluteLocationPath() throws JaxenException;

    public BinaryExpr createOrExpr(Expr var1, Expr var2) throws JaxenException;

    public BinaryExpr createAndExpr(Expr var1, Expr var2) throws JaxenException;

    public BinaryExpr createEqualityExpr(Expr var1, Expr var2, int var3) throws JaxenException;

    public BinaryExpr createRelationalExpr(Expr var1, Expr var2, int var3) throws JaxenException;

    public BinaryExpr createAdditiveExpr(Expr var1, Expr var2, int var3) throws JaxenException;

    public BinaryExpr createMultiplicativeExpr(Expr var1, Expr var2, int var3) throws JaxenException;

    public Expr createUnaryExpr(Expr var1, int var2) throws JaxenException;

    public UnionExpr createUnionExpr(Expr var1, Expr var2) throws JaxenException;

    public FilterExpr createFilterExpr(Expr var1) throws JaxenException;

    public FunctionCallExpr createFunctionCallExpr(String var1, String var2) throws JaxenException;

    public NumberExpr createNumberExpr(int var1) throws JaxenException;

    public NumberExpr createNumberExpr(double var1) throws JaxenException;

    public LiteralExpr createLiteralExpr(String var1) throws JaxenException;

    public VariableReferenceExpr createVariableReferenceExpr(String var1, String var2) throws JaxenException;

    public Step createNameStep(int var1, String var2, String var3) throws JaxenException;

    public Step createAllNodeStep(int var1) throws JaxenException;

    public Step createCommentNodeStep(int var1) throws JaxenException;

    public Step createTextNodeStep(int var1) throws JaxenException;

    public Step createProcessingInstructionNodeStep(int var1, String var2) throws JaxenException;

    public Predicate createPredicate(Expr var1) throws JaxenException;

    public PredicateSet createPredicateSet() throws JaxenException;
}

