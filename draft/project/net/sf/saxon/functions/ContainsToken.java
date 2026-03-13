/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CollatingFunctionFixed;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

public class ContainsToken
extends CollatingFunctionFixed {
    @Override
    public boolean isSubstringMatchingFunction() {
        return true;
    }

    private static boolean containsToken(SequenceIterator arg0, StringValue arg1, StringCollator collator) throws XPathException {
        Item item;
        if (arg1 == null) {
            return false;
        }
        String search = Whitespace.trim(arg1.getPrimitiveStringValue().toString());
        if (search.isEmpty()) {
            return false;
        }
        while ((item = arg0.next()) != null) {
            Item token;
            Whitespace.Tokenizer tokens = new Whitespace.Tokenizer(item.getStringValueCS());
            while ((token = tokens.next()) != null) {
                if (!collator.comparesEqual(search, token.getStringValue())) continue;
                tokens.close();
                arg0.close();
                return true;
            }
        }
        return false;
    }

    @Override
    public BooleanValue call(XPathContext context, Sequence[] arguments) throws XPathException {
        return BooleanValue.get(ContainsToken.containsToken(arguments[0].iterate(), (StringValue)arguments[1].head(), this.getStringCollator()));
    }
}

