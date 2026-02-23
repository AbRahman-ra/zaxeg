/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.ItemMappingFunction;
import net.sf.saxon.expr.ItemMappingIterator;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.UnionIterator;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.PathMap;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.sort.LocalOrderComparer;
import net.sf.saxon.functions.StatefulSystemFunction;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.NamespaceResolver;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeSetPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.KeyDefinitionSet;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.value.AtomicValue;

public class KeyFn
extends SystemFunction
implements StatefulSystemFunction {
    private KeyDefinitionSet staticKeySet = null;

    public KeyManager getKeyManager() {
        return this.getRetainedStaticContext().getPackageData().getKeyManager();
    }

    public NamespaceResolver getNamespaceResolver() {
        return this.getRetainedStaticContext();
    }

    public static Expression internalKeyCall(KeyManager keyManager, KeyDefinitionSet keySet, String name, Expression value, Expression doc, RetainedStaticContext rsc) {
        KeyFn fn = (KeyFn)SystemFunction.makeFunction("key", rsc, 3);
        assert (fn != null);
        fn.staticKeySet = keySet;
        try {
            fn.fixArguments(new StringLiteral(name), value, doc);
        } catch (XPathException xPathException) {
            // empty catch block
        }
        return fn.makeFunctionCall(new StringLiteral(name), value, doc);
    }

    @Override
    public int getSpecialProperties(Expression[] arguments) {
        int prop = 25296896;
        if (this.getArity() == 2 || (arguments[2].getSpecialProperties() & 0x10000) != 0) {
            prop |= 0x10000;
        }
        return prop;
    }

    @Override
    public SystemFunction copy() {
        KeyFn k2 = (KeyFn)SystemFunction.makeFunction(this.getFunctionName().getLocalPart(), this.getRetainedStaticContext(), this.getArity());
        k2.staticKeySet = this.staticKeySet;
        return k2;
    }

    @Override
    public Expression fixArguments(Expression ... arguments) throws XPathException {
        if (arguments[0] instanceof StringLiteral && this.staticKeySet == null) {
            KeyManager keyManager = this.getKeyManager();
            String keyName = ((StringLiteral)arguments[0]).getStringValue();
            this.staticKeySet = this.getKeyDefinitionSet(keyManager, keyName);
        }
        return null;
    }

    public PathMap.PathMapNodeSet addToPathMap(PathMap pathMap, PathMap.PathMapNodeSet pathMapNodeSet) {
        if (this.staticKeySet != null) {
            PathMap.PathMapNodeSet result = new PathMap.PathMapNodeSet();
            for (KeyDefinition kd : this.staticKeySet.getKeyDefinitions()) {
                Pattern pat = kd.getMatch();
                if (pat instanceof NodeSetPattern) {
                    Expression selector = ((NodeSetPattern)pat).getSelectionExpression();
                    PathMap.PathMapNodeSet selected = selector.addToPathMap(pathMap, pathMapNodeSet);
                    Expression use = kd.getUse();
                    PathMap.PathMapNodeSet used = use.addToPathMap(pathMap, selected);
                    result.addNodeSet(selected);
                    continue;
                }
                throw new IllegalStateException("Can't add key() call to pathmap");
            }
            return result;
        }
        throw new IllegalStateException("Can't add dynamic key() call to pathmap");
    }

    @Override
    public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        NodeInfo origin = arguments.length == 3 ? (NodeInfo)KeyFn.getOrigin(context, arguments[2]) : KeyFn.getContextRoot(context);
        if (origin.getRoot().getNodeKind() != 9) {
            throw new XPathException("In the key() function, the node supplied in the third argument (or the context node if absent) must be in a tree whose root is a document node", "XTDE1270", context);
        }
        KeyDefinitionSet selectedKeySet = this.staticKeySet;
        KeyManager keyManager = this.getKeyManager();
        if (selectedKeySet == null) {
            selectedKeySet = this.getKeyDefinitionSet(keyManager, arguments[0].head().getStringValue());
        }
        return KeyFn.search(keyManager, context, arguments[1], origin, selectedKeySet);
    }

    private static NodeInfo getContextRoot(XPathContext context) throws XPathException {
        Item contextItem = context.getContextItem();
        if (contextItem == null) {
            throw new XPathException("Cannot call the key() function when there is no context item", "XTDE1270", context);
        }
        if (!(contextItem instanceof NodeInfo)) {
            throw new XPathException("Cannot call the key() function when the context item is not a node", "XTDE1270", context);
        }
        return ((NodeInfo)contextItem).getRoot();
    }

    private static Item getOrigin(XPathContext context, Sequence argument2) throws XPathException {
        Item arg2;
        try {
            arg2 = argument2.head();
        } catch (XPathException e) {
            String code = e.getErrorCodeLocalPart();
            if ("XPDY0002".equals(code) && argument2 instanceof RootExpression) {
                throw new XPathException("Cannot call the key() function when there is no context node", "XTDE1270", context);
            }
            if ("XPDY0050".equals(code)) {
                throw new XPathException("In the key() function, the node supplied in the third argument (or the context node if absent) must be in a tree whose root is a document node", "XTDE1270", context);
            }
            if ("XPTY0020".equals(code) || "XPTY0019".equals(code)) {
                throw new XPathException("Cannot call the key() function when the context item is an atomic value", "XTDE1270", context);
            }
            throw e;
        }
        return arg2;
    }

    private KeyDefinitionSet getKeyDefinitionSet(KeyManager keyManager, String keyName) throws XPathException {
        StructuredQName qName = null;
        try {
            qName = StructuredQName.fromLexicalQName(keyName, false, true, this.getNamespaceResolver());
        } catch (XPathException err) {
            throw new XPathException("Invalid key name: " + err.getMessage(), "XTDE1260");
        }
        KeyDefinitionSet selectedKeySet = keyManager.getKeyDefinitionSet(qName);
        if (selectedKeySet == null) {
            throw new XPathException("Key '" + keyName + "' has not been defined", "XTDE1260");
        }
        return selectedKeySet;
    }

    protected static Sequence search(KeyManager keyManager, XPathContext context, Sequence sought, NodeInfo origin, KeyDefinitionSet selectedKeySet) throws XPathException {
        AtomicValue keyValue;
        NodeInfo doc = origin.getRoot();
        if (selectedKeySet.isComposite()) {
            SequenceIterator soughtKey = sought.iterate();
            SequenceIterator all = keyManager.selectByCompositeKey(selectedKeySet, doc.getTreeInfo(), soughtKey, context);
            if (origin.equals(doc)) {
                return new LazySequence(all);
            }
            return new LazySequence(new ItemMappingIterator(all, new SubtreeFilter(origin)));
        }
        SequenceIterator allResults = null;
        SequenceIterator keys = sought.iterate();
        ArrayList<SequenceIterator> allKeyIterators = new ArrayList<SequenceIterator>();
        while ((keyValue = (AtomicValue)keys.next()) != null) {
            SequenceIterator someResults = keyManager.selectByKey(selectedKeySet, doc.getTreeInfo(), keyValue, context);
            allKeyIterators.add(someResults);
        }
        allResults = allKeyIterators.isEmpty() ? EmptyIterator.ofNodes() : (allKeyIterators.size() == 1 ? (SequenceIterator)allKeyIterators.get(0) : new UnionIterator(allKeyIterators, LocalOrderComparer.getInstance()));
        if (origin.equals(doc)) {
            return new LazySequence(allResults);
        }
        return new LazySequence(new ItemMappingIterator(allResults, new SubtreeFilter(origin)));
    }

    public static class SubtreeFilter
    implements ItemMappingFunction {
        private NodeInfo origin;

        public SubtreeFilter(NodeInfo origin) {
            this.origin = origin;
        }

        @Override
        public NodeInfo mapItem(Item item) {
            if (Navigator.isAncestorOrSelf(this.origin, (NodeInfo)item)) {
                return (NodeInfo)item;
            }
            return null;
        }
    }
}

