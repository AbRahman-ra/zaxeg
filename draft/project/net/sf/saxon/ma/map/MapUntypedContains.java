/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.ma.map;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.ma.map.MapItem;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.ConversionResult;
import net.sf.saxon.type.PrimitiveUType;
import net.sf.saxon.type.StringConverter;
import net.sf.saxon.type.ValidationFailure;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.UntypedAtomicValue;

public class MapUntypedContains
extends SystemFunction {
    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        ConversionRules rules = context.getConfiguration().getConversionRules();
        MapItem map = (MapItem)arguments[0].head();
        AtomicValue key = (AtomicValue)arguments[1].head();
        if (key instanceof UntypedAtomicValue) {
            for (PrimitiveUType prim : map.getKeyUType().decompose()) {
                BuiltInAtomicType t = (BuiltInAtomicType)prim.toItemType();
                StringConverter converter = t.getStringConverter(rules);
                ConversionResult av = converter.convert(key);
                if (!(av instanceof ValidationFailure ? prim.equals((Object)PrimitiveUType.DECIMAL) && (av = (converter = BuiltInAtomicType.DOUBLE.getStringConverter(rules)).convert(key)) instanceof AtomicValue && map.get(av.asAtomic()) != null : map.get(av.asAtomic()) != null)) continue;
                return BooleanValue.TRUE;
            }
            return BooleanValue.FALSE;
        }
        if (key.isNaN()) {
            return BooleanValue.FALSE;
        }
        boolean result = map.get(key) != null;
        return BooleanValue.get(result);
    }
}

