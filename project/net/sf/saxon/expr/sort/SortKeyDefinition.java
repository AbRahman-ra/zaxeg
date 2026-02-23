/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.sort;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;
import net.sf.saxon.Configuration;
import net.sf.saxon.Version;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.Operand;
import net.sf.saxon.expr.OperandRole;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.PseudoExpression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.ExpressionVisitor;
import net.sf.saxon.expr.parser.RebindingMap;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.AtomicSortComparer;
import net.sf.saxon.expr.sort.DescendingComparer;
import net.sf.saxon.expr.sort.EmptyGreatestComparer;
import net.sf.saxon.expr.sort.NumericComparer;
import net.sf.saxon.expr.sort.NumericComparer11;
import net.sf.saxon.expr.sort.TextComparer;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class SortKeyDefinition
extends PseudoExpression {
    protected Operand sortKey;
    protected Operand order = new Operand(this, new StringLiteral("ascending"), OperandRole.SINGLE_ATOMIC);
    protected Operand dataTypeExpression = null;
    protected Operand caseOrder = new Operand(this, new StringLiteral("#default"), OperandRole.SINGLE_ATOMIC);
    protected Operand language = new Operand(this, new StringLiteral(StringValue.EMPTY_STRING), OperandRole.SINGLE_ATOMIC);
    protected Operand collationName = null;
    protected Operand stable = null;
    protected StringCollator collation;
    protected String baseURI;
    protected boolean emptyLeast = true;
    protected boolean backwardsCompatible = false;
    protected boolean setContextForSortKey = false;
    private transient AtomicComparer finalComparator = null;

    @Override
    public boolean isLiftable(boolean forStreaming) {
        return false;
    }

    public void setSortKey(Expression exp, boolean setContext) {
        OperandRole opRole = setContext ? new OperandRole(68, OperandUsage.TRANSMISSION, SequenceType.ANY_SEQUENCE) : OperandRole.ATOMIC_SEQUENCE;
        this.sortKey = new Operand(this, exp, opRole);
        this.setContextForSortKey = setContext;
    }

    public Expression getSortKey() {
        return this.sortKey.getChildExpression();
    }

    public Operand getSortKeyOperand() {
        return this.sortKey;
    }

    public boolean isSetContextForSortKey() {
        return this.setContextForSortKey;
    }

    public void setOrder(Expression exp) {
        this.order.setChildExpression(exp);
    }

    public Expression getOrder() {
        return this.order.getChildExpression();
    }

    public void setDataTypeExpression(Expression exp) {
        if (exp == null) {
            this.dataTypeExpression = null;
        } else {
            if (this.dataTypeExpression == null) {
                this.dataTypeExpression = new Operand(this, exp, OperandRole.SINGLE_ATOMIC);
            }
            this.dataTypeExpression.setChildExpression(exp);
        }
    }

    public Expression getDataTypeExpression() {
        return this.dataTypeExpression == null ? null : this.dataTypeExpression.getChildExpression();
    }

    public void setCaseOrder(Expression exp) {
        this.caseOrder.setChildExpression(exp);
    }

    public Expression getCaseOrder() {
        return this.caseOrder.getChildExpression();
    }

    public void setLanguage(Expression exp) {
        this.language.setChildExpression(exp);
    }

    public Expression getLanguage() {
        return this.language.getChildExpression();
    }

    public void setCollationNameExpression(Expression collationNameExpr) {
        if (collationNameExpr == null) {
            this.collationName = null;
        } else {
            if (this.collationName == null) {
                this.collationName = new Operand(this, collationNameExpr, OperandRole.SINGLE_ATOMIC);
            }
            this.collationName.setChildExpression(collationNameExpr);
        }
    }

    public Expression getCollationNameExpression() {
        return this.collationName == null ? null : this.collationName.getChildExpression();
    }

    public void setCollation(StringCollator collation) {
        this.collation = collation;
    }

    public StringCollator getCollation() {
        return this.collation;
    }

    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public String getBaseURI() {
        return this.baseURI;
    }

    public void setStable(Expression stableExpr) {
        if (stableExpr == null) {
            stableExpr = new StringLiteral("yes");
        }
        if (this.stable == null) {
            this.stable = new Operand(this, stableExpr, OperandRole.SINGLE_ATOMIC);
        }
        this.stable.setChildExpression(stableExpr);
    }

    public Expression getStable() {
        return this.stable.getChildExpression();
    }

    public void setBackwardsCompatible(boolean compatible) {
        this.backwardsCompatible = compatible;
    }

    public boolean isBackwardsCompatible() {
        return this.backwardsCompatible;
    }

    public void setEmptyLeast(boolean emptyLeast) {
        this.emptyLeast = emptyLeast;
    }

    public boolean getEmptyLeast() {
        return this.emptyLeast;
    }

    public boolean isFixed() {
        return !(!(this.order.getChildExpression() instanceof Literal) || this.dataTypeExpression != null && !(this.dataTypeExpression.getChildExpression() instanceof Literal) || !(this.caseOrder.getChildExpression() instanceof Literal) || !(this.language.getChildExpression() instanceof Literal) || this.stable != null && !(this.stable.getChildExpression() instanceof Literal) || this.collationName != null && !(this.collationName.getChildExpression() instanceof Literal));
    }

    @Override
    public SortKeyDefinition copy(RebindingMap rm) {
        SortKeyDefinition sk2 = new SortKeyDefinition();
        sk2.setSortKey(this.copy(this.sortKey.getChildExpression(), rm), true);
        sk2.setOrder(this.copy(this.order.getChildExpression(), rm));
        sk2.setDataTypeExpression(this.dataTypeExpression == null ? null : this.copy(this.dataTypeExpression.getChildExpression(), rm));
        sk2.setCaseOrder(this.copy(this.caseOrder.getChildExpression(), rm));
        sk2.setLanguage(this.copy(this.language.getChildExpression(), rm));
        sk2.setStable(this.copy(this.stable == null ? null : this.stable.getChildExpression(), rm));
        sk2.setCollationNameExpression(this.collationName == null ? null : this.copy(this.collationName.getChildExpression(), rm));
        sk2.collation = this.collation;
        sk2.emptyLeast = this.emptyLeast;
        sk2.baseURI = this.baseURI;
        sk2.backwardsCompatible = this.backwardsCompatible;
        sk2.finalComparator = this.finalComparator;
        sk2.setContextForSortKey = this.setContextForSortKey;
        return sk2;
    }

    private Expression copy(Expression in, RebindingMap rebindings) {
        return in == null ? null : in.copy(rebindings);
    }

    @Override
    public SortKeyDefinition typeCheck(ExpressionVisitor visitor, ContextItemStaticInfo contextItemType) throws XPathException {
        ValidationFailure vf;
        for (Operand o : this.checkedOperands()) {
            if (!o.hasSameFocus()) continue;
            o.typeCheck(visitor, contextItemType);
        }
        Expression lang = this.getLanguage();
        if (lang instanceof StringLiteral && !((StringLiteral)lang).getStringValue().isEmpty() && (vf = StringConverter.StringToLanguage.INSTANCE.validate(((StringLiteral)lang).getStringValue())) != null) {
            throw new XPathException("The lang attribute of xsl:sort must be a valid language code", "XTDE0030");
        }
        return this;
    }

    @Override
    public Iterable<Operand> operands() {
        ArrayList<Operand> list = new ArrayList<Operand>(8);
        list.add(this.sortKey);
        list.add(this.order);
        if (this.dataTypeExpression != null) {
            list.add(this.dataTypeExpression);
        }
        list.add(this.caseOrder);
        list.add(this.language);
        if (this.stable != null) {
            list.add(this.stable);
        }
        if (this.collationName != null) {
            list.add(this.collationName);
        }
        return list;
    }

    @Override
    public int getImplementationMethod() {
        return 0;
    }

    public AtomicComparer makeComparator(XPathContext context) throws XPathException {
        StringValue stableVal;
        String s;
        AtomicComparer atomicComparer;
        StringCollator stringCollator;
        String orderX = this.order.getChildExpression().evaluateAsString(context).toString();
        Configuration config = context.getConfiguration();
        if (this.collation != null) {
            stringCollator = this.collation;
        } else if (this.collationName != null) {
            URI collationURI;
            String cname = this.collationName.getChildExpression().evaluateAsString(context).toString();
            try {
                collationURI = new URI(cname);
                if (!collationURI.isAbsolute()) {
                    if (this.baseURI == null) {
                        throw new XPathException("Collation URI is relative, and base URI is unknown");
                    }
                    URI base = new URI(this.baseURI);
                    collationURI = base.resolve(collationURI);
                }
            } catch (URISyntaxException err) {
                throw new XPathException("Collation name " + cname + " is not a valid URI: " + err);
            }
            stringCollator = context.getConfiguration().getCollation(collationURI.toString());
            if (stringCollator == null) {
                throw new XPathException("Unknown collation " + collationURI, "XTDE1035");
            }
        } else {
            String caseOrderX = this.caseOrder.getChildExpression().evaluateAsString(context).toString();
            String languageX = this.language.getChildExpression().evaluateAsString(context).toString();
            String uri = "http://saxon.sf.net/collation";
            boolean firstParam = true;
            Properties props = new Properties();
            if (!languageX.isEmpty()) {
                ValidationFailure vf = StringConverter.StringToLanguage.INSTANCE.validate(languageX);
                if (vf != null) {
                    throw new XPathException("The lang attribute of xsl:sort must be a valid language code", "XTDE0030");
                }
                props.setProperty("lang", languageX);
                uri = uri + "?lang=" + languageX;
                firstParam = false;
            }
            if (!caseOrderX.equals("#default")) {
                props.setProperty("case-order", caseOrderX);
                uri = uri + (firstParam ? "?" : ";") + "case-order=" + caseOrderX;
                firstParam = false;
            }
            stringCollator = Version.platform.makeCollation(config, props, uri);
        }
        if (this.dataTypeExpression == null) {
            atomicComparer = AtomicSortComparer.makeSortComparer(stringCollator, this.sortKey.getChildExpression().getItemType().getAtomizedItemType().getPrimitiveType(), context);
            if (!this.emptyLeast) {
                atomicComparer = new EmptyGreatestComparer(atomicComparer);
            }
        } else {
            String dataType;
            switch (dataType = this.dataTypeExpression.getChildExpression().evaluateAsString(context).toString()) {
                case "text": {
                    atomicComparer = AtomicSortComparer.makeSortComparer(stringCollator, 513, context);
                    atomicComparer = new TextComparer(atomicComparer);
                    break;
                }
                case "number": {
                    atomicComparer = context.getConfiguration().getXsdVersion() == 10 ? NumericComparer.getInstance() : NumericComparer11.getInstance();
                    break;
                }
                default: {
                    XPathException err = new XPathException("data-type on xsl:sort must be 'text' or 'number'");
                    err.setErrorCode("XTDE0030");
                    throw err;
                }
            }
        }
        if (!(this.stable == null || (s = Whitespace.trim((stableVal = (StringValue)this.stable.getChildExpression().evaluateItem(context)).getStringValue())).equals("yes") || s.equals("no") || s.equals("true") || s.equals("false") || s.equals("1") || s.equals("0"))) {
            XPathException err = new XPathException("Value of 'stable' on xsl:sort must be yes|no|true|false|1|0");
            err.setErrorCode("XTDE0030");
            throw err;
        }
        switch (orderX) {
            case "ascending": {
                return atomicComparer;
            }
            case "descending": {
                return new DescendingComparer(atomicComparer);
            }
        }
        XPathException err1 = new XPathException("order must be 'ascending' or 'descending'");
        err1.setErrorCode("XTDE0030");
        throw err1;
    }

    public void setFinalComparator(AtomicComparer comp) {
        this.finalComparator = comp;
    }

    public AtomicComparer getFinalComparator() {
        return this.finalComparator;
    }

    public SortKeyDefinition fix(XPathContext context) throws XPathException {
        SortKeyDefinition newSKD = this.copy(new RebindingMap());
        newSKD.setLanguage(new StringLiteral(this.getLanguage().evaluateAsString(context)));
        newSKD.setOrder(new StringLiteral(this.getOrder().evaluateAsString(context)));
        if (this.collationName != null) {
            newSKD.setCollationNameExpression(new StringLiteral(this.getCollationNameExpression().evaluateAsString(context)));
        }
        newSKD.setCaseOrder(new StringLiteral(this.getCaseOrder().evaluateAsString(context)));
        if (this.dataTypeExpression != null) {
            newSKD.setDataTypeExpression(new StringLiteral(this.getDataTypeExpression().evaluateAsString(context)));
        }
        newSKD.setSortKey(new ContextItemExpression(), true);
        return newSKD;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SortKeyDefinition) {
            SortKeyDefinition s2 = (SortKeyDefinition)other;
            return Objects.equals(this.getSortKey(), s2.getSortKey()) && Objects.equals(this.getOrder(), s2.getOrder()) && Objects.equals(this.getLanguage(), s2.getLanguage()) && Objects.equals(this.getDataTypeExpression(), s2.getDataTypeExpression()) && Objects.equals(this.getStable(), s2.getStable()) && Objects.equals(this.getCollationNameExpression(), s2.getCollationNameExpression());
        }
        return false;
    }

    @Override
    public int computeHashCode() {
        int h = 0;
        h ^= this.getOrder().hashCode();
        h ^= this.getCaseOrder().hashCode();
        h ^= this.getLanguage().hashCode();
        if (this.getDataTypeExpression() != null) {
            h ^= this.getDataTypeExpression().hashCode();
        }
        if (this.getStable() != null) {
            h ^= this.getStable().hashCode();
        }
        if (this.getCollationNameExpression() != null) {
            h ^= this.getCollationNameExpression().hashCode();
        }
        return h;
    }

    @Override
    public void export(ExpressionPresenter out) throws XPathException {
        out.startElement("sortKey", this);
        if (this.finalComparator != null) {
            out.emitAttribute("comp", this.finalComparator.save());
        }
        out.setChildRole("select");
        this.sortKey.getChildExpression().export(out);
        out.setChildRole("order");
        this.order.getChildExpression().export(out);
        if (this.dataTypeExpression != null) {
            out.setChildRole("dataType");
            this.dataTypeExpression.getChildExpression().export(out);
        }
        out.setChildRole("lang");
        this.language.getChildExpression().export(out);
        out.setChildRole("caseOrder");
        this.caseOrder.getChildExpression().export(out);
        if (this.stable != null) {
            out.setChildRole("stable");
            this.stable.getChildExpression().export(out);
        }
        if (this.collationName != null) {
            out.setChildRole("collation");
            this.collationName.getChildExpression().export(out);
        }
        out.endElement();
    }
}

