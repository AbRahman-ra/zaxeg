/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.om.Item;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.TypeHierarchy;
import net.sf.saxon.type.UType;

public class ItemTypeCheckingFunction
implements ItemMappingFunction {
    private ItemType requiredItemType;
    private RoleDiagnostic role;
    private Location location;
    private Expression failingExpression;
    private Configuration config = null;

    public ItemTypeCheckingFunction(ItemType requiredItemType, RoleDiagnostic role, Expression failingExpression, Configuration config) {
        this.requiredItemType = requiredItemType;
        this.role = role;
        this.failingExpression = failingExpression;
        this.location = failingExpression.getLocation();
        this.config = config;
    }

    public ItemTypeCheckingFunction(ItemType requiredItemType, RoleDiagnostic role, Location locator, Configuration config) {
        this.requiredItemType = requiredItemType;
        this.role = role;
        this.location = locator;
        this.config = config;
    }

    @Override
    public Item mapItem(Item item) throws XPathException {
        this.testConformance(item, this.config);
        return item;
    }

    private void testConformance(Item item, Configuration config) throws XPathException {
        TypeHierarchy th = config.getTypeHierarchy();
        if (!(this.requiredItemType.matches(item, th) || this.requiredItemType.getUType().subsumes(UType.STRING) && BuiltInAtomicType.ANY_URI.matches(item, th))) {
            String message = this.role.composeErrorMessage(this.requiredItemType, item, th);
            String errorCode = this.role.getErrorCode();
            if ("XPDY0050".equals(errorCode)) {
                XPathException te = new XPathException(message, errorCode);
                te.setFailingExpression(this.failingExpression);
                te.setLocator(this.location);
                te.setIsTypeError(false);
                throw te;
            }
            XPathException te = new XPathException(message, errorCode);
            te.setFailingExpression(this.failingExpression);
            te.setLocator(this.location);
            te.setIsTypeError(true);
            throw te;
        }
    }
}

