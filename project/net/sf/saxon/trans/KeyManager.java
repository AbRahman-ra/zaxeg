/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.IntegratedFunctionLibrary;
import net.sf.saxon.functions.IsIdRef;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ConversionRules;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StandardNames;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeInfo;
import net.sf.saxon.pattern.BasePatternWithPredicate;
import net.sf.saxon.pattern.MultipleNodeKindTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.KeyDefinitionSet;
import net.sf.saxon.trans.KeyIndex;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.EmptyIterator;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.UType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.NumericValue;
import net.sf.saxon.z.IntHashMap;

public class KeyManager {
    private PackageData packageData;
    private HashMap<StructuredQName, KeyDefinitionSet> keyDefinitions;
    private transient WeakHashMap<TreeInfo, WeakReference<IntHashMap<KeyIndex>>> docIndexes;

    public KeyManager(Configuration config, PackageData pack) {
        this.packageData = pack;
        this.keyDefinitions = new HashMap(10);
        this.docIndexes = new WeakHashMap(10);
        this.registerIdrefKey(config);
    }

    private synchronized void registerIdrefKey(Configuration config) {
        StructuredQName qName = StandardNames.getStructuredQName(562);
        if (this.keyDefinitions.get(qName) == null) {
            BasePatternWithPredicate pp = new BasePatternWithPredicate(new NodeTestPattern(new MultipleNodeKindTest(UType.ELEMENT_OR_ATTRIBUTE)), IntegratedFunctionLibrary.makeFunctionCall(new IsIdRef(), new Expression[0]));
            try {
                IndependentContext sc = new IndependentContext(config);
                sc.setPackageData(this.packageData);
                sc.setXPathLanguageLevel(31);
                RetainedStaticContext rsc = new RetainedStaticContext(sc);
                Expression sf = SystemFunction.makeCall("string", rsc, new ContextItemExpression());
                Expression use = SystemFunction.makeCall("tokenize", rsc, sf);
                SymbolicName symbolicName = new SymbolicName(165, qName);
                KeyDefinition key = new KeyDefinition(symbolicName, pp, use, null, null);
                key.setPackageData(this.packageData);
                key.setIndexedItemType(BuiltInAtomicType.STRING);
                this.addKeyDefinition(qName, key, true, config);
            } catch (XPathException err) {
                throw new AssertionError((Object)err);
            }
        }
    }

    public synchronized void preRegisterKeyDefinition(StructuredQName keyName) {
        KeyDefinitionSet keySet = this.keyDefinitions.get(keyName);
        if (keySet == null) {
            keySet = new KeyDefinitionSet(keyName, this.keyDefinitions.size());
            this.keyDefinitions.put(keyName, keySet);
        }
    }

    public synchronized void addKeyDefinition(StructuredQName keyName, KeyDefinition keydef, boolean reusable, Configuration config) throws XPathException {
        boolean backwardsCompatible;
        KeyDefinitionSet keySet = this.keyDefinitions.get(keyName);
        if (keySet == null) {
            keySet = new KeyDefinitionSet(keyName, this.keyDefinitions.size());
            this.keyDefinitions.put(keyName, keySet);
        }
        keySet.addKeyDefinition(keydef);
        if (!reusable) {
            keySet.setReusable(false);
        }
        if (backwardsCompatible = keySet.isBackwardsCompatible()) {
            List<KeyDefinition> v = keySet.getKeyDefinitions();
            for (KeyDefinition kd : v) {
                kd.setBackwardsCompatible(true);
                if (kd.getBody().getItemType().equals(BuiltInAtomicType.STRING)) continue;
                AtomicSequenceConverter exp = new AtomicSequenceConverter(kd.getBody(), BuiltInAtomicType.STRING);
                exp.allocateConverterStatically(config, false);
                kd.setBody(exp);
            }
        }
    }

    public KeyDefinitionSet getKeyDefinitionSet(StructuredQName qName) {
        return this.keyDefinitions.get(qName);
    }

    public KeyDefinitionSet findKeyDefinition(Pattern finder, Expression use, String collationName) {
        for (KeyDefinitionSet keySet : this.keyDefinitions.values()) {
            if (keySet.getKeyDefinitions().size() != 1) continue;
            for (KeyDefinition keyDef : keySet.getKeyDefinitions()) {
                if (!keyDef.getMatch().isEqual(finder) || !keyDef.getUse().isEqual(use) || !keyDef.getCollationName().equals(collationName)) continue;
                return keySet;
            }
        }
        return null;
    }

    private synchronized KeyIndex buildIndex(KeyDefinitionSet keySet, TreeInfo doc, XPathContext context) throws XPathException {
        KeyIndex index = new KeyIndex(keySet.isRangeKey());
        index.buildIndex(keySet, doc, context);
        return index;
    }

    private void buildIndex(KeyIndex index, KeyDefinitionSet keySet, TreeInfo doc, XPathContext context) throws XPathException {
        index.buildIndex(keySet, doc, context);
    }

    public SequenceIterator selectByKey(KeyDefinitionSet keySet, TreeInfo doc, AtomicValue soughtValue, XPathContext context) throws XPathException {
        if (soughtValue == null) {
            return EmptyIterator.ofNodes();
        }
        if (keySet.isBackwardsCompatible()) {
            ConversionRules rules = context.getConfiguration().getConversionRules();
            soughtValue = Converter.convert(soughtValue, BuiltInAtomicType.STRING, rules).asAtomic();
        } else {
            BuiltInAtomicType itemType = soughtValue.getPrimitiveType();
            if (itemType.equals(BuiltInAtomicType.INTEGER) || itemType.equals(BuiltInAtomicType.DECIMAL) || itemType.equals(BuiltInAtomicType.FLOAT)) {
                soughtValue = new DoubleValue(((NumericValue)soughtValue).getDoubleValue());
            }
        }
        KeyIndex index = this.obtainIndex(keySet, doc, context);
        return index.getNodes(soughtValue);
    }

    public SequenceIterator selectByCompositeKey(KeyDefinitionSet keySet, TreeInfo doc, SequenceIterator soughtValue, XPathContext context) throws XPathException {
        KeyIndex index = this.obtainIndex(keySet, doc, context);
        return index.getComposite(soughtValue);
    }

    public KeyIndex obtainIndex(KeyDefinitionSet keySet, TreeInfo doc, XPathContext context) throws XPathException {
        if (keySet.isReusable()) {
            return this.obtainSharedIndex(keySet, doc, context);
        }
        return this.obtainLocalIndex(keySet, doc, context);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private KeyIndex obtainSharedIndex(KeyDefinitionSet keySet, TreeInfo doc, XPathContext context) throws XPathException {
        int keySetNumber = keySet.getKeySetNumber();
        KeyIndex index = this.getSharedIndex(doc, keySetNumber);
        if (index != null) {
            KeyIndex.Status status = index.getStatus();
            if (status == KeyIndex.Status.UNDER_CONSTRUCTION) {
                if (index.isCreatedInThisThread()) {
                    XPathException de = new XPathException("Key definition " + keySet.getKeyName().getDisplayName() + " is circular");
                    de.setXPathContext(context);
                    de.setErrorCode("XTDE0640");
                    throw de;
                }
                index = null;
            } else if (status == KeyIndex.Status.FAILED) {
                throw new XPathException("Construction of index for key " + keySet.getKeyName().getDisplayName() + " was unsuccessful");
            }
        }
        if (index == null) {
            index = new KeyIndex(keySet.isRangeKey());
            KeyManager keyManager = this;
            synchronized (keyManager) {
                index.setStatus(KeyIndex.Status.UNDER_CONSTRUCTION);
                KeyIndex index2 = this.putSharedIndex(doc, keySetNumber, index, context);
                if (index2.getStatus() == KeyIndex.Status.BUILT) {
                    return index2;
                }
                index = index2;
            }
            this.buildIndex(index, keySet, doc, context);
            keyManager = this;
            synchronized (keyManager) {
                index.setStatus(KeyIndex.Status.BUILT);
                index = this.putSharedIndex(doc, keySetNumber, index, context);
            }
        }
        return index;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private KeyIndex obtainLocalIndex(KeyDefinitionSet keySet, TreeInfo doc, XPathContext context) throws XPathException {
        int keySetNumber = keySet.getKeySetNumber();
        KeyIndex index = this.getLocalIndex(doc, keySetNumber, context);
        if (index != null) {
            KeyIndex.Status status = index.getStatus();
            if (status == KeyIndex.Status.UNDER_CONSTRUCTION) {
                if (index.isCreatedInThisThread()) {
                    XPathException de = new XPathException("Key definition " + keySet.getKeyName().getDisplayName() + " is circular");
                    de.setXPathContext(context);
                    de.setErrorCode("XTDE0640");
                    throw de;
                }
                index = null;
            } else if (status == KeyIndex.Status.FAILED) {
                throw new XPathException("Construction of index for key " + keySet.getKeyName().getDisplayName() + " was unsuccessful");
            }
        }
        if (index == null) {
            index = new KeyIndex(keySet.isRangeKey());
            KeyManager keyManager = this;
            synchronized (keyManager) {
                index.setStatus(KeyIndex.Status.UNDER_CONSTRUCTION);
                KeyIndex index2 = this.putLocalIndex(doc, keySetNumber, index, context);
                if (index2.getStatus() == KeyIndex.Status.BUILT) {
                    return index2;
                }
                index = index2;
            }
            this.buildIndex(index, keySet, doc, context);
            keyManager = this;
            synchronized (keyManager) {
                index.setStatus(KeyIndex.Status.BUILT);
                index = this.putLocalIndex(doc, keySetNumber, index, context);
            }
        }
        return index;
    }

    private synchronized KeyIndex putSharedIndex(TreeInfo doc, int keyFingerprint, KeyIndex index, XPathContext context) {
        IntHashMap indexList;
        WeakReference<IntHashMap<KeyIndex>> indexRef;
        if (this.docIndexes == null) {
            this.docIndexes = new WeakHashMap(10);
        }
        if ((indexRef = this.docIndexes.get(doc)) == null || indexRef.get() == null) {
            indexList = new IntHashMap(10);
            Controller controller = context.getController();
            if (controller.getDocumentPool().contains(doc)) {
                context.getController().setUserData(doc, "saxon:key-index-list", indexList);
            } else {
                doc.setUserData("saxon:key-index-list", indexList);
            }
            this.docIndexes.put(doc, new WeakReference<IntHashMap>(indexList));
        } else {
            indexList = (IntHashMap)indexRef.get();
        }
        KeyIndex result = (KeyIndex)indexList.get(keyFingerprint);
        if (result == null || result.getStatus() != KeyIndex.Status.BUILT) {
            indexList.put(keyFingerprint, index);
            result = index;
        }
        return result;
    }

    private synchronized KeyIndex putLocalIndex(TreeInfo doc, int keyFingerprint, KeyIndex index, XPathContext context) {
        KeyIndex result;
        Controller controller = context.getController();
        IntHashMap<Map<Long, KeyIndex>> masterIndex = controller.getLocalIndexes();
        Map<Long, KeyIndex> docIndexes = masterIndex.get(keyFingerprint);
        if (docIndexes == null) {
            docIndexes = new HashMap<Long, KeyIndex>();
            masterIndex.put(keyFingerprint, docIndexes);
        }
        if ((result = docIndexes.get(doc.getDocumentNumber())) == null || result.getStatus() != KeyIndex.Status.BUILT) {
            docIndexes.put(doc.getDocumentNumber(), index);
            result = index;
        }
        return result;
    }

    private synchronized KeyIndex getSharedIndex(TreeInfo doc, int keyFingerprint) {
        WeakReference<IntHashMap<KeyIndex>> ref;
        if (this.docIndexes == null) {
            this.docIndexes = new WeakHashMap(10);
        }
        if ((ref = this.docIndexes.get(doc)) == null) {
            return null;
        }
        IntHashMap indexList = (IntHashMap)ref.get();
        if (indexList == null) {
            return null;
        }
        return (KeyIndex)indexList.get(keyFingerprint);
    }

    private synchronized KeyIndex getLocalIndex(TreeInfo doc, int keyFingerprint, XPathContext context) {
        Controller controller = context.getController();
        IntHashMap<Map<Long, KeyIndex>> masterIndex = controller.getLocalIndexes();
        Map<Long, KeyIndex> docIndexes = masterIndex.get(keyFingerprint);
        if (docIndexes == null) {
            return null;
        }
        return docIndexes.get(doc.getDocumentNumber());
    }

    public synchronized void clearDocumentIndexes(TreeInfo doc) {
        this.docIndexes.remove(doc);
    }

    public Collection<KeyDefinitionSet> getAllKeyDefinitionSets() {
        return this.keyDefinitions.values();
    }

    public int getNumberOfKeyDefinitions() {
        return this.keyDefinitions.size();
    }

    public void exportKeys(ExpressionPresenter out, Map<Component, Integer> componentIdMap) throws XPathException {
        for (Map.Entry<StructuredQName, KeyDefinitionSet> e : this.keyDefinitions.entrySet()) {
            boolean reusable = e.getValue().isReusable();
            List<KeyDefinition> list = e.getValue().getKeyDefinitions();
            for (KeyDefinition kd : list) {
                if (kd.getObjectName().equals(StandardNames.getStructuredQName(562))) continue;
                kd.export(out, reusable, componentIdMap);
            }
        }
    }
}

