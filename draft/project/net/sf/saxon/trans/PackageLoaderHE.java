/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.CheckSumFilter;
import net.sf.saxon.event.FilterFactory;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Stripper;
import net.sf.saxon.expr.AdjacentTextNodeMerger;
import net.sf.saxon.expr.AndExpression;
import net.sf.saxon.expr.ArithmeticExpression;
import net.sf.saxon.expr.AtomicSequenceConverter;
import net.sf.saxon.expr.Atomizer;
import net.sf.saxon.expr.AttributeGetter;
import net.sf.saxon.expr.AxisExpression;
import net.sf.saxon.expr.Calculator;
import net.sf.saxon.expr.CardinalityChecker;
import net.sf.saxon.expr.CastExpression;
import net.sf.saxon.expr.CastableExpression;
import net.sf.saxon.expr.CompareToIntegerConstant;
import net.sf.saxon.expr.CompareToStringConstant;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentBinding;
import net.sf.saxon.expr.ComponentInvocation;
import net.sf.saxon.expr.ConsumingOperand;
import net.sf.saxon.expr.ContextItemExpression;
import net.sf.saxon.expr.EarlyEvaluationContext;
import net.sf.saxon.expr.EmptyTextNodeRemover;
import net.sf.saxon.expr.ErrorExpression;
import net.sf.saxon.expr.EvaluationMode;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.FilterExpression;
import net.sf.saxon.expr.FirstItemExpression;
import net.sf.saxon.expr.ForExpression;
import net.sf.saxon.expr.GeneralComparison20;
import net.sf.saxon.expr.GlobalVariableReference;
import net.sf.saxon.expr.HomogeneityChecker;
import net.sf.saxon.expr.IdentityComparison;
import net.sf.saxon.expr.InstanceOfExpression;
import net.sf.saxon.expr.IntegerRangeTest;
import net.sf.saxon.expr.IsLastExpression;
import net.sf.saxon.expr.ItemChecker;
import net.sf.saxon.expr.LastItemExpression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.ListCastableFunction;
import net.sf.saxon.expr.ListConstructorFunction;
import net.sf.saxon.expr.Literal;
import net.sf.saxon.expr.LocalBinding;
import net.sf.saxon.expr.LocalVariableReference;
import net.sf.saxon.expr.LookupAllExpression;
import net.sf.saxon.expr.LookupExpression;
import net.sf.saxon.expr.NegateExpression;
import net.sf.saxon.expr.NumberSequenceFormatter;
import net.sf.saxon.expr.OrExpression;
import net.sf.saxon.expr.QuantifiedExpression;
import net.sf.saxon.expr.RangeExpression;
import net.sf.saxon.expr.RootExpression;
import net.sf.saxon.expr.SimpleStepExpression;
import net.sf.saxon.expr.SingletonAtomizer;
import net.sf.saxon.expr.SingletonIntersectExpression;
import net.sf.saxon.expr.SlashExpression;
import net.sf.saxon.expr.StaticFunctionCall;
import net.sf.saxon.expr.StringLiteral;
import net.sf.saxon.expr.SubscriptExpression;
import net.sf.saxon.expr.SuppliedParameterReference;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.expr.TailCallLoop;
import net.sf.saxon.expr.TailExpression;
import net.sf.saxon.expr.TryCatch;
import net.sf.saxon.expr.UnionCastableFunction;
import net.sf.saxon.expr.UnionConstructorFunction;
import net.sf.saxon.expr.UntypedSequenceConverter;
import net.sf.saxon.expr.UserFunctionCall;
import net.sf.saxon.expr.ValueComparison;
import net.sf.saxon.expr.VennExpression;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.expr.accum.AccumulatorRegistry;
import net.sf.saxon.expr.accum.AccumulatorRule;
import net.sf.saxon.expr.compat.ArithmeticExpression10;
import net.sf.saxon.expr.compat.GeneralComparison10;
import net.sf.saxon.expr.flwor.LocalVariableBinding;
import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.AnalyzeString;
import net.sf.saxon.expr.instruct.ApplyImports;
import net.sf.saxon.expr.instruct.ApplyTemplates;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.Block;
import net.sf.saxon.expr.instruct.BreakInstr;
import net.sf.saxon.expr.instruct.CallTemplate;
import net.sf.saxon.expr.instruct.Choose;
import net.sf.saxon.expr.instruct.Comment;
import net.sf.saxon.expr.instruct.ComputedAttribute;
import net.sf.saxon.expr.instruct.ComputedElement;
import net.sf.saxon.expr.instruct.ConditionalBlock;
import net.sf.saxon.expr.instruct.Copy;
import net.sf.saxon.expr.instruct.CopyOf;
import net.sf.saxon.expr.instruct.Doctype;
import net.sf.saxon.expr.instruct.DocumentInstr;
import net.sf.saxon.expr.instruct.EvaluateInstr;
import net.sf.saxon.expr.instruct.FixedAttribute;
import net.sf.saxon.expr.instruct.FixedElement;
import net.sf.saxon.expr.instruct.ForEach;
import net.sf.saxon.expr.instruct.ForEachGroup;
import net.sf.saxon.expr.instruct.Fork;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.IterateInstr;
import net.sf.saxon.expr.instruct.LocalParam;
import net.sf.saxon.expr.instruct.LocalParamBlock;
import net.sf.saxon.expr.instruct.Message;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.NamespaceConstructor;
import net.sf.saxon.expr.instruct.NextIteration;
import net.sf.saxon.expr.instruct.NextMatch;
import net.sf.saxon.expr.instruct.NumberInstruction;
import net.sf.saxon.expr.instruct.OnEmptyExpr;
import net.sf.saxon.expr.instruct.OnNonEmptyExpr;
import net.sf.saxon.expr.instruct.OriginalFunction;
import net.sf.saxon.expr.instruct.ProcessingInstruction;
import net.sf.saxon.expr.instruct.ResultDocument;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.SourceDocument;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.UseAttributeSet;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.instruct.UserFunctionParameter;
import net.sf.saxon.expr.instruct.ValueOf;
import net.sf.saxon.expr.instruct.WherePopulated;
import net.sf.saxon.expr.instruct.WithParam;
import net.sf.saxon.expr.number.NumberFormatter;
import net.sf.saxon.expr.parser.ContextItemStaticInfo;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.expr.parser.Optimizer;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.expr.parser.RoleDiagnostic;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.expr.sort.AtomicComparer;
import net.sf.saxon.expr.sort.AtomicSortComparer;
import net.sf.saxon.expr.sort.CalendarValueComparer;
import net.sf.saxon.expr.sort.CodepointCollatingComparer;
import net.sf.saxon.expr.sort.CollatingAtomicComparer;
import net.sf.saxon.expr.sort.ComparableAtomicValueComparer;
import net.sf.saxon.expr.sort.ConditionalSorter;
import net.sf.saxon.expr.sort.DecimalSortComparer;
import net.sf.saxon.expr.sort.DescendingComparer;
import net.sf.saxon.expr.sort.DocumentSorter;
import net.sf.saxon.expr.sort.DoubleSortComparer;
import net.sf.saxon.expr.sort.EqualityComparer;
import net.sf.saxon.expr.sort.GenericAtomicComparer;
import net.sf.saxon.expr.sort.MergeInstr;
import net.sf.saxon.expr.sort.NumericComparer;
import net.sf.saxon.expr.sort.NumericComparer11;
import net.sf.saxon.expr.sort.SortExpression;
import net.sf.saxon.expr.sort.SortKeyDefinition;
import net.sf.saxon.expr.sort.SortKeyDefinitionList;
import net.sf.saxon.expr.sort.TextComparer;
import net.sf.saxon.expr.sort.UntypedNumericComparer;
import net.sf.saxon.functions.Count;
import net.sf.saxon.functions.Current;
import net.sf.saxon.functions.CurrentGroupCall;
import net.sf.saxon.functions.CurrentGroupingKeyCall;
import net.sf.saxon.functions.ExecutableFunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.IntegratedFunctionCall;
import net.sf.saxon.functions.MathFunctionSet;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.functions.hof.CoercedFunction;
import net.sf.saxon.functions.hof.CurriedFunction;
import net.sf.saxon.functions.hof.FunctionLiteral;
import net.sf.saxon.functions.hof.FunctionSequenceCoercer;
import net.sf.saxon.functions.hof.PartialApply;
import net.sf.saxon.functions.hof.UserFunctionReference;
import net.sf.saxon.functions.registry.ConstructorFunctionLibrary;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.lib.StringCollator;
import net.sf.saxon.lib.Validation;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.arrays.SimpleArrayItem;
import net.sf.saxon.ma.arrays.SquareArrayConstructor;
import net.sf.saxon.ma.json.JsonParser;
import net.sf.saxon.ma.map.HashTrieMap;
import net.sf.saxon.ma.map.MapFunctionSet;
import net.sf.saxon.om.Action;
import net.sf.saxon.om.AllElementsSpaceStrippingRule;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NoElementsSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.One;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.QNameParser;
import net.sf.saxon.om.SelectedElementsSpaceStrippingRule;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.AncestorQualifiedPattern;
import net.sf.saxon.pattern.AnchorPattern;
import net.sf.saxon.pattern.AnyNodeTest;
import net.sf.saxon.pattern.BasePatternWithPredicate;
import net.sf.saxon.pattern.BooleanExpressionPattern;
import net.sf.saxon.pattern.ExceptPattern;
import net.sf.saxon.pattern.GeneralNodePattern;
import net.sf.saxon.pattern.GeneralPositionalPattern;
import net.sf.saxon.pattern.IntersectPattern;
import net.sf.saxon.pattern.ItemTypePattern;
import net.sf.saxon.pattern.LocalNameTest;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.NamespaceTest;
import net.sf.saxon.pattern.NodeKindTest;
import net.sf.saxon.pattern.NodeSetPattern;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.pattern.NodeTestPattern;
import net.sf.saxon.pattern.Pattern;
import net.sf.saxon.pattern.PatternThatSetsCurrent;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.pattern.SimplePositionalPattern;
import net.sf.saxon.pattern.UnionPattern;
import net.sf.saxon.pattern.UnionQNameTest;
import net.sf.saxon.pattern.UniversalPattern;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.style.PackageVersion;
import net.sf.saxon.style.StylesheetFunctionLibrary;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trans.DecimalFormatManager;
import net.sf.saxon.trans.DecimalSymbols;
import net.sf.saxon.trans.FunctionStreamability;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.KeyManager;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.RecoveryPolicy;
import net.sf.saxon.trans.SimpleMode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.packages.IPackageLoader;
import net.sf.saxon.trans.rules.BuiltInRuleSet;
import net.sf.saxon.trans.rules.Rule;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.tree.util.Orphan;
import net.sf.saxon.tree.wrapper.VirtualCopy;
import net.sf.saxon.type.AlphaCode;
import net.sf.saxon.type.AnyItemType;
import net.sf.saxon.type.AtomicType;
import net.sf.saxon.type.BuiltInAtomicType;
import net.sf.saxon.type.Converter;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.ListType;
import net.sf.saxon.type.LocalUnionType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.type.StringToDouble;
import net.sf.saxon.type.UnionType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.BigDecimalValue;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.DoubleValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.IntegerRange;
import net.sf.saxon.value.IntegerValue;
import net.sf.saxon.value.NestedIntegerValue;
import net.sf.saxon.value.NotationValue;
import net.sf.saxon.value.QNameValue;
import net.sf.saxon.value.QualifiedNameValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.z.IntHashMap;

public class PackageLoaderHE
implements IPackageLoader {
    private static final NestedIntegerValue SAXON9911 = new NestedIntegerValue(new int[]{9, 9, 1, 1});
    private Configuration config;
    protected final Stack<StylesheetPackage> packStack = new Stack();
    private XPathParser parser;
    public final Stack<List<ComponentInvocation>> fixups = new Stack();
    public final List<Action> completionActions = new ArrayList<Action>();
    public final Map<String, StylesheetPackage> allPackages = new HashMap<String, StylesheetPackage>();
    public Stack<LocalBinding> localBindings;
    private ExecutableFunctionLibrary overriding;
    private ExecutableFunctionLibrary underriding;
    private final Stack<RetainedStaticContext> contextStack = new Stack();
    public final Map<SymbolicName, UserFunction> userFunctions = new HashMap<SymbolicName, UserFunction>();
    private final Map<String, IntHashMap<Location>> locationMap = new HashMap<String, IntHashMap<Location>>();
    private final Map<Integer, Component> componentIdMap = new HashMap<Integer, Component>();
    private final Map<Component, String> externalReferences = new HashMap<Component, String>();
    private String relocatableBase = null;
    private NestedIntegerValue originalVersion = null;
    private UserFunction currentFunction;
    protected static final Map<String, ExpressionLoader> eMap = new HashMap<String, ExpressionLoader>(200);
    protected static final Map<String, String> licensableConstructs = new HashMap<String, String>(30);
    static final Map<String, PatternLoader> pMap;

    public PackageLoaderHE(Configuration config) {
        this.config = config;
        this.overriding = new ExecutableFunctionLibrary(config);
        this.underriding = new ExecutableFunctionLibrary(config);
        try {
            this.parser = config.newExpressionParser("XP", false, 31);
            QNameParser qNameParser = new QNameParser(null).withAcceptEQName(true);
            this.parser.setQNameParser(qNameParser);
        } catch (XPathException e) {
            throw new AssertionError((Object)e);
        }
    }

    public static void processAccumulatorList(PackageLoaderHE loader, SourceDocument inst, String accumulatorNames) {
        if (accumulatorNames != null) {
            ArrayList<StructuredQName> accNameList = new ArrayList<StructuredQName>();
            StringTokenizer tokenizer = new StringTokenizer(accumulatorNames);
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                StructuredQName name = StructuredQName.fromEQName(token);
                accNameList.add(name);
            }
            StylesheetPackage pack = loader.getPackStack().peek();
            loader.addCompletionAction(() -> {
                HashSet<Accumulator> list = new HashSet<Accumulator>();
                for (StructuredQName sn : accNameList) {
                    for (Accumulator test : pack.getAccumulatorRegistry().getAllAccumulators()) {
                        if (!test.getAccumulatorName().equals(sn)) continue;
                        list.add(test);
                    }
                }
                inst.setUsedAccumulators(list);
            });
        }
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public StylesheetPackage getPackage() {
        return (StylesheetPackage)this.packStack.get(0);
    }

    public Stack<StylesheetPackage> getPackStack() {
        return this.packStack;
    }

    public void addCompletionAction(Action action) {
        this.completionActions.add(action);
    }

    @Override
    public StylesheetPackage loadPackage(Source source) throws XPathException {
        ParseOptions options = new ParseOptions();
        options.setSpaceStrippingRule(AllElementsSpaceStrippingRule.getInstance());
        options.setSchemaValidationMode(4);
        options.setDTDValidationMode(4);
        final ArrayList filters = new ArrayList(1);
        FilterFactory checksumFactory = new FilterFactory(){

            @Override
            public ProxyReceiver makeFilter(Receiver next) {
                CheckSumFilter filter = new CheckSumFilter(next);
                filter.setCheckExistingChecksum(true);
                filters.add(filter);
                return filter;
            }
        };
        options.addFilter(checksumFactory);
        NodeInfo doc = this.config.buildDocumentTree(source, options).getRootNode();
        CheckSumFilter csf = (CheckSumFilter)filters.get(0);
        if (!csf.isChecksumCorrect()) {
            throw new XPathException("Package cannot be loaded: incorrect checksum", "SXPK0002");
        }
        return this.loadPackageDoc(doc);
    }

    @Override
    public StylesheetPackage loadPackageDoc(NodeInfo doc) throws XPathException {
        StylesheetPackage pack = this.config.makeStylesheetPackage();
        pack.setRuleManager(new RuleManager(pack));
        pack.setCharacterMapIndex(new CharacterMapIndex());
        pack.setJustInTimeCompilation(false);
        this.packStack.push(pack);
        NodeInfo packageElement = doc.iterateAxis(3, NodeKindTest.ELEMENT).next();
        if (!packageElement.getURI().equals("http://ns.saxonica.com/xslt/export")) {
            throw new XPathException("Incorrect namespace for XSLT export file", "SXPK0002");
        }
        if (!packageElement.getLocalPart().equals("package")) {
            throw new XPathException("Outermost element of XSLT export file must be 'package'", "SXPK0002");
        }
        String saxonVersionAtt = packageElement.getAttributeValue("", "saxonVersion");
        if (saxonVersionAtt == null) {
            saxonVersionAtt = "9.8.0.1";
        }
        this.originalVersion = NestedIntegerValue.parse(saxonVersionAtt);
        String dmk = packageElement.getAttributeValue("", "dmk");
        if (dmk != null) {
            int licenseId = this.config.registerLocalLicense(dmk);
            pack.setLocalLicenseId(licenseId);
        }
        this.loadPackageElement(packageElement, pack);
        for (Map.Entry<Component, String> entry : this.externalReferences.entrySet()) {
            Component comp = entry.getKey();
            StringTokenizer tokenizer = new StringTokenizer(entry.getValue());
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                int target = Integer.parseInt(token);
                Component targetComponent = this.componentIdMap.get(target);
                if (targetComponent == null) {
                    throw new XPathException("Unresolved external reference to component " + target);
                }
                comp.getComponentBindings().add(new ComponentBinding(targetComponent.getActor().getSymbolicName(), targetComponent));
            }
        }
        return pack;
    }

    public void needsPELicense(String name) {
        int localLicenseId = this.getPackage().getLocalLicenseId();
        this.config.checkLicensedFeature(8, name, localLicenseId);
    }

    public void needsEELicense(String name) {
        int localLicenseId = this.getPackage().getLocalLicenseId();
        this.config.checkLicensedFeature(2, name, localLicenseId);
    }

    public void loadPackageElement(NodeInfo packageElement, StylesheetPackage pack) throws XPathException {
        String implicitAtt;
        this.fixups.push(new ArrayList());
        String packageName = packageElement.getAttributeValue("", "name");
        String packageId = packageElement.getAttributeValue("", "id");
        String packageKey = packageId == null ? packageName : packageId;
        boolean relocatable = "true".equals(packageElement.getAttributeValue("", "relocatable"));
        if (packageName != null) {
            pack.setPackageName(packageName);
            this.allPackages.put(packageKey, pack);
        }
        pack.setPackageVersion(new PackageVersion(packageElement.getAttributeValue("", "packageVersion")));
        pack.setVersion(this.getIntegerAttribute(packageElement, "version"));
        pack.setSchemaAware("1".equals(packageElement.getAttributeValue("", "schemaAware")));
        if (pack.isSchemaAware()) {
            this.needsEELicense("schema-awareness");
        }
        if ((implicitAtt = packageElement.getAttributeValue("", "implicit")) != null) {
            pack.setImplicitPackage(implicitAtt.equals("true"));
        } else {
            pack.setImplicitPackage(this.originalVersion.compareTo(SAXON9911) <= 0);
        }
        pack.setStripsTypeAnnotations("1".equals(packageElement.getAttributeValue("", "stripType")));
        pack.setKeyManager(new KeyManager(pack.getConfiguration(), pack));
        pack.setDeclaredModes("1".equals(packageElement.getAttributeValue("", "declaredModes")));
        for (NodeInfo nodeInfo : packageElement.children(new NameTest(1, "http://ns.saxonica.com/xslt/export", "package", this.config.getNamePool()))) {
            StylesheetPackage subPack = this.config.makeStylesheetPackage();
            subPack.setRuleManager(new RuleManager(pack));
            subPack.setCharacterMapIndex(new CharacterMapIndex());
            subPack.setJustInTimeCompilation(false);
            this.packStack.push(subPack);
            this.loadPackageElement(nodeInfo, subPack);
            this.packStack.pop();
            pack.addUsedPackage(subPack);
        }
        FunctionLibraryList functionLibrary = new FunctionLibraryList();
        functionLibrary.addFunctionLibrary(this.config.getXSLT30FunctionSet());
        functionLibrary.addFunctionLibrary(MapFunctionSet.getInstance());
        functionLibrary.addFunctionLibrary(ArrayFunctionSet.getInstance());
        functionLibrary.addFunctionLibrary(MathFunctionSet.getInstance());
        functionLibrary.addFunctionLibrary(new StylesheetFunctionLibrary(pack, true));
        functionLibrary.addFunctionLibrary(new ConstructorFunctionLibrary(this.config));
        XQueryFunctionLibrary xQueryFunctionLibrary = new XQueryFunctionLibrary(this.config);
        functionLibrary.addFunctionLibrary(xQueryFunctionLibrary);
        functionLibrary.addFunctionLibrary(this.config.getIntegratedFunctionLibrary());
        this.config.addExtensionBinders(functionLibrary);
        functionLibrary.addFunctionLibrary(new StylesheetFunctionLibrary(pack, false));
        pack.setFunctionLibraryDetails(functionLibrary, this.overriding, this.underriding);
        RetainedStaticContext rsc = new RetainedStaticContext(this.config);
        if (relocatable) {
            this.relocatableBase = packageElement.getBaseURI();
            rsc.setStaticBaseUriString(this.relocatableBase);
        }
        rsc.setPackageData(pack);
        this.contextStack.push(rsc);
        this.localBindings = new Stack();
        this.readGlobalContext(packageElement);
        this.readSchemaNamespaces(packageElement);
        this.readKeys(packageElement);
        this.readComponents(packageElement, false);
        NodeInfo overridden = packageElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "overridden", this.config.getNamePool())).next();
        if (overridden != null) {
            this.readComponents(overridden, true);
        }
        this.readAccumulators(packageElement);
        this.readOutputProperties(packageElement);
        this.readCharacterMaps(packageElement);
        this.readSpaceStrippingRules(packageElement);
        this.readDecimalFormats(packageElement);
        this.resolveFixups();
        this.fixups.pop();
        for (Action a : this.completionActions) {
            a.doAction();
        }
        StructuredQName defaultModeName = this.getQNameAttribute(packageElement, "defaultMode");
        if (defaultModeName == null) {
            pack.setDefaultMode(Mode.UNNAMED_MODE_NAME);
        } else {
            pack.setDefaultMode(defaultModeName);
        }
    }

    private void readGlobalContext(NodeInfo packageElement) throws XPathException {
        GlobalContextRequirement req = null;
        NameTest condition = new NameTest(1, "http://ns.saxonica.com/xslt/export", "glob", this.config.getNamePool());
        for (NodeInfo nodeInfo : packageElement.children(condition)) {
            ItemType requiredType;
            String use;
            if (req == null) {
                req = new GlobalContextRequirement();
                this.packStack.peek().setContextItemRequirements(req);
            }
            if ("opt".equals(use = nodeInfo.getAttributeValue("", "use"))) {
                req.setMayBeOmitted(true);
                req.setAbsentFocus(false);
            } else if ("pro".equals(use)) {
                req.setMayBeOmitted(true);
                req.setAbsentFocus(true);
            } else if ("req".equals(use)) {
                req.setMayBeOmitted(false);
                req.setAbsentFocus(false);
            }
            if ((requiredType = this.parseItemTypeAttribute(nodeInfo, "type")) == null) continue;
            req.addRequiredItemType(requiredType);
        }
    }

    protected void readSchemaNamespaces(NodeInfo packageElement) throws XPathException {
    }

    private void readKeys(NodeInfo packageElement) throws XPathException {
        NodeInfo keyElement;
        StylesheetPackage pack = this.packStack.peek();
        AxisIterator iterator = packageElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "key", this.config.getNamePool()));
        while ((keyElement = iterator.next()) != null) {
            StructuredQName keyName = this.getQNameAttribute(keyElement, "name");
            SymbolicName symbol = new SymbolicName(165, keyName);
            String flags = keyElement.getAttributeValue("", "flags");
            boolean backwards = flags != null && flags.contains("b");
            boolean range = flags != null && flags.contains("r");
            boolean reusable = flags != null && flags.contains("u");
            boolean composite = flags != null && flags.contains("c");
            Pattern match = this.getFirstChildPattern(keyElement);
            Expression use = this.getSecondChildExpression(keyElement);
            String collationName = keyElement.getAttributeValue("", "collation");
            if (collationName == null) {
                collationName = "http://www.w3.org/2005/xpath-functions/collation/codepoint";
            }
            StringCollator collation = this.config.getCollation(collationName);
            KeyDefinition keyDefinition = new KeyDefinition(symbol, match, use, collationName, collation);
            int slots = this.getIntegerAttribute(keyElement, "slots");
            if (slots != Integer.MIN_VALUE) {
                keyDefinition.setStackFrameMap(new SlotManager(slots));
            }
            String binds = keyElement.getAttributeValue("", "binds");
            Component keyComponent = keyDefinition.makeDeclaringComponent(Visibility.PRIVATE, pack);
            this.externalReferences.put(keyComponent, binds);
            if (backwards) {
                keyDefinition.setBackwardsCompatible(true);
            }
            if (range) {
                keyDefinition.setRangeKey(true);
            }
            if (composite) {
                keyDefinition.setComposite(true);
            }
            pack.getKeyManager().addKeyDefinition(keyName, keyDefinition, reusable, pack.getConfiguration());
        }
    }

    private void readComponents(NodeInfo packageElement, boolean overridden) throws XPathException {
        NodeInfo child;
        StylesheetPackage pack = this.packStack.peek();
        AxisIterator iterator = packageElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "co", this.config.getNamePool()));
        while ((child = iterator.next()) != null) {
            Component component;
            StylesheetPackage declaringPackage;
            int id = this.getIntegerAttribute(child, "id");
            String visAtt = child.getAttributeValue("", "vis");
            Visibility vis = visAtt == null ? Visibility.PRIVATE : Visibility.valueOf(visAtt.toUpperCase());
            VisibilityProvenance provenance = visAtt == null ? VisibilityProvenance.DEFAULTED : VisibilityProvenance.EXPLICIT;
            String binds = child.getAttributeValue("", "binds");
            String dPackKey = child.getAttributeValue("", "dpack");
            if (dPackKey == null) {
                declaringPackage = pack;
            } else if (this.allPackages.containsKey(dPackKey)) {
                declaringPackage = this.allPackages.get(dPackKey);
            } else {
                declaringPackage = this.config.makeStylesheetPackage();
                declaringPackage.setPackageName(dPackKey);
                declaringPackage.setTargetEdition(this.config.getEditionCode());
                declaringPackage.setJustInTimeCompilation(false);
            }
            int base = this.getIntegerAttribute(child, "base");
            if (base != Integer.MIN_VALUE) {
                Component baseComponent = this.componentIdMap.get(base);
                if (baseComponent == null) {
                    throw new AssertionError((Object)(base + ""));
                }
                component = Component.makeComponent(baseComponent.getActor(), vis, provenance, pack, declaringPackage);
                component.setBaseComponent(baseComponent);
                if (component instanceof Component.M) {
                    pack.getRuleManager().obtainMode(baseComponent.getActor().getComponentName(), true);
                }
            } else {
                Actor cc;
                NodeInfo grandchild = child.iterateAxis(3, NodeKindTest.ELEMENT).next();
                String kind = grandchild.getLocalPart();
                boolean codeGen = false;
                switch (kind) {
                    case "template": {
                        cc = this.readNamedTemplate(grandchild);
                        codeGen = true;
                        break;
                    }
                    case "globalVariable": {
                        cc = this.readGlobalVariable(grandchild);
                        codeGen = true;
                        break;
                    }
                    case "globalParam": {
                        cc = this.readGlobalParam(grandchild);
                        break;
                    }
                    case "function": {
                        cc = this.readGlobalFunction(grandchild);
                        codeGen = ((UserFunction)cc).getDeclaredStreamability() == FunctionStreamability.UNCLASSIFIED;
                        break;
                    }
                    case "mode": {
                        cc = this.readMode(grandchild);
                        break;
                    }
                    case "attributeSet": {
                        cc = this.readAttributeSet(grandchild);
                        break;
                    }
                    default: {
                        throw new XPathException("unknown component kind " + kind);
                    }
                }
                component = Component.makeComponent(cc, vis, provenance, pack, declaringPackage);
                cc.setDeclaringComponent(component);
                cc.setDeclaredVisibility(vis);
                Optimizer optimizer = this.config.obtainOptimizer();
                StructuredQName name = cc.getComponentName();
                int evaluationModes = 6;
                if (codeGen) {
                    String objectName = name == null ? "h" + component.hashCode() : name.getLocalPart();
                    cc.setBody(optimizer.makeByteCodeCandidate(cc, cc.getBody(), objectName, evaluationModes));
                    optimizer.injectByteCodeCandidates(cc.getBody());
                } else if (cc instanceof Mode) {
                    ((Mode)cc).processRules(rule -> {
                        TemplateRule tr = (TemplateRule)rule.getAction();
                        String objectName = "match=\"" + tr.getMatchPattern() + '\"';
                        tr.setBody(optimizer.makeByteCodeCandidate(tr, tr.getBody(), objectName, evaluationModes));
                        optimizer.injectByteCodeCandidates(tr.getBody());
                    });
                }
            }
            this.externalReferences.put(component, binds);
            this.componentIdMap.put(id, component);
            if (overridden) {
                pack.addOverriddenComponent(component);
                continue;
            }
            if (component.getVisibility() == Visibility.HIDDEN) {
                pack.addHiddenComponent(component);
                continue;
            }
            pack.addComponent(component);
        }
    }

    private GlobalVariable readGlobalVariable(NodeInfo varElement) throws XPathException {
        NodeInfo bodyElement;
        int slots;
        StylesheetPackage pack = this.packStack.peek();
        StructuredQName variableName = this.getQNameAttribute(varElement, "name");
        GlobalVariable var = new GlobalVariable();
        var.setVariableQName(variableName);
        var.setPackageData(pack);
        var.setRequiredType(this.parseAlphaCode(varElement, "as"));
        String flags = varElement.getAttributeValue("", "flags");
        if (flags != null) {
            if (flags.contains("a")) {
                var.setAssignable(true);
            }
            if (flags.contains("x")) {
                var.setIndexedVariable();
            }
            if (flags.contains("r")) {
                var.setRequiredParam(true);
            }
        }
        if ((slots = this.getIntegerAttribute(varElement, "slots")) > 0) {
            var.setContainsLocals(new SlotManager(slots));
        }
        if ((bodyElement = varElement.iterateAxis(3, NodeKindTest.ELEMENT).next()) == null) {
            var.setBody(Literal.makeEmptySequence());
        } else {
            Expression body = this.loadExpression(bodyElement);
            var.setBody(body);
            RetainedStaticContext rsc = body.getRetainedStaticContext();
            body.setRetainedStaticContext(rsc);
        }
        pack.addGlobalVariable(var);
        return var;
    }

    private GlobalParam readGlobalParam(NodeInfo varElement) throws XPathException {
        NodeInfo bodyElement;
        int slots;
        StylesheetPackage pack = this.packStack.peek();
        StructuredQName variableName = this.getQNameAttribute(varElement, "name");
        this.localBindings = new Stack();
        GlobalParam var = new GlobalParam();
        var.setVariableQName(variableName);
        var.setPackageData(pack);
        var.setRequiredType(this.parseAlphaCode(varElement, "as"));
        String flags = varElement.getAttributeValue("", "flags");
        if (flags != null) {
            if (flags.contains("a")) {
                var.setAssignable(true);
            }
            if (flags.contains("x")) {
                var.setIndexedVariable();
            }
            if (flags.contains("r")) {
                var.setRequiredParam(true);
            }
            if (flags.contains("i")) {
                var.setImplicitlyRequiredParam(true);
            }
        }
        if ((slots = this.getIntegerAttribute(varElement, "slots")) > 0) {
            var.setContainsLocals(new SlotManager(slots));
        }
        if ((bodyElement = varElement.iterateAxis(3, NodeKindTest.ELEMENT).next()) == null) {
            var.setBody(Literal.makeEmptySequence());
        } else {
            Expression body = this.loadExpression(bodyElement);
            var.setBody(body);
            RetainedStaticContext rsc = body.getRetainedStaticContext();
            body.setRetainedStaticContext(rsc);
        }
        return var;
    }

    private NamedTemplate readNamedTemplate(NodeInfo templateElement) throws XPathException {
        StylesheetPackage pack = this.packStack.peek();
        this.localBindings = new Stack();
        StructuredQName templateName = this.getQNameAttribute(templateElement, "name");
        String flags = templateElement.getAttributeValue("", "flags");
        int slots = this.getIntegerAttribute(templateElement, "slots");
        SequenceType contextType = this.parseAlphaCode(templateElement, "cxt");
        ItemType contextItemType = contextType == null ? AnyItemType.getInstance() : contextType.getPrimaryType();
        NamedTemplate template = new NamedTemplate(templateName);
        template.setStackFrameMap(new SlotManager(slots));
        template.setPackageData(pack);
        template.setRequiredType(this.parseAlphaCode(templateElement, "as"));
        template.setContextItemRequirements(contextItemType, flags.contains("o"), !flags.contains("s"));
        NodeInfo bodyElement = this.getChildWithRole(templateElement, "body");
        if (bodyElement == null) {
            template.setBody(Literal.makeEmptySequence());
        } else {
            Expression body = this.loadExpression(bodyElement);
            template.setBody(body);
            RetainedStaticContext rsc = body.getRetainedStaticContext();
            body.setRetainedStaticContext(rsc);
        }
        return template;
    }

    private UserFunction readGlobalFunction(NodeInfo functionElement) throws XPathException {
        this.localBindings = new Stack();
        UserFunction function = this.readFunction(functionElement);
        this.userFunctions.put(function.getSymbolicName(), function);
        this.underriding.addFunction(function);
        return function;
    }

    public UserFunction readFunction(NodeInfo functionElement) throws XPathException {
        NodeInfo bodyElement;
        NodeInfo argElement;
        StylesheetPackage pack = this.packStack.peek();
        StructuredQName functionName = this.getQNameAttribute(functionElement, "name");
        int slots = this.getIntegerAttribute(functionElement, "slots");
        String flags = functionElement.getAttributeValue("", "flags");
        if (flags == null) {
            flags = "";
        }
        UserFunction function = this.makeFunction(flags);
        function.setFunctionName(functionName);
        function.setStackFrameMap(new SlotManager(slots));
        function.setPackageData(pack);
        function.setRetainedStaticContext(this.makeRetainedStaticContext(functionElement));
        function.setResultType(this.parseAlphaCode(functionElement, "as"));
        function.setDeclaredStreamability(FunctionStreamability.UNCLASSIFIED);
        function.incrementReferenceCount();
        int evalMode = this.getIntegerAttribute(functionElement, "eval");
        if (flags.contains("p")) {
            function.setDeterminism(UserFunction.Determinism.PROACTIVE);
        } else if (flags.contains("e")) {
            function.setDeterminism(UserFunction.Determinism.ELIDABLE);
        } else if (flags.contains("d")) {
            function.setDeterminism(UserFunction.Determinism.DETERMINISTIC);
        }
        boolean streaming = false;
        if (flags.contains("U")) {
            function.setDeclaredStreamability(FunctionStreamability.UNCLASSIFIED);
        } else if (flags.contains("A")) {
            function.setDeclaredStreamability(FunctionStreamability.ABSORBING);
            streaming = true;
        } else if (flags.contains("I")) {
            function.setDeclaredStreamability(FunctionStreamability.INSPECTION);
            streaming = true;
        } else if (flags.contains("F")) {
            function.setDeclaredStreamability(FunctionStreamability.FILTER);
            streaming = true;
        } else if (flags.contains("S")) {
            function.setDeclaredStreamability(FunctionStreamability.SHALLOW_DESCENT);
            streaming = true;
        } else if (flags.contains("D")) {
            function.setDeclaredStreamability(FunctionStreamability.DEEP_DESCENT);
            streaming = true;
        } else if (flags.contains("C")) {
            function.setDeclaredStreamability(FunctionStreamability.ASCENT);
            streaming = true;
        }
        function.setEvaluationMode(EvaluationMode.forCode(evalMode));
        this.currentFunction = function;
        ArrayList<UserFunctionParameter> params = new ArrayList<UserFunctionParameter>();
        AxisIterator argIterator = functionElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "arg", this.config.getNamePool()));
        int slot = 0;
        while ((argElement = argIterator.next()) != null) {
            UserFunctionParameter arg = new UserFunctionParameter();
            arg.setVariableQName(this.getQNameAttribute(argElement, "name"));
            arg.setRequiredType(this.parseAlphaCode(argElement, "as"));
            arg.setSlotNumber(slot++);
            params.add(arg);
            this.localBindings.push(arg);
        }
        function.setParameterDefinitions(params.toArray(new UserFunctionParameter[0]));
        if (streaming) {
            ((UserFunctionParameter)params.get(0)).setFunctionStreamability(function.getDeclaredStreamability());
        }
        if ((bodyElement = this.getChildWithRole(functionElement, "body")) == null) {
            function.setBody(Literal.makeEmptySequence());
        } else {
            Expression body = this.loadExpression(bodyElement);
            function.setBody(body);
            RetainedStaticContext rsc = body.getRetainedStaticContext();
            body.setRetainedStaticContext(rsc);
        }
        for (int i = 0; i < function.getArity(); ++i) {
            this.localBindings.pop();
        }
        if (function.getDeclaredStreamability() != FunctionStreamability.UNCLASSIFIED) {
            this.addCompletionAction(function::prepareForStreaming);
        }
        return function;
    }

    protected UserFunction makeFunction(String flags) {
        return new UserFunction();
    }

    private AttributeSet readAttributeSet(NodeInfo aSetElement) throws XPathException {
        StylesheetPackage pack = this.packStack.peek();
        this.localBindings = new Stack();
        StructuredQName aSetName = this.getQNameAttribute(aSetElement, "name");
        int slots = this.getIntegerAttribute(aSetElement, "slots");
        AttributeSet aSet = new AttributeSet();
        aSet.setName(aSetName);
        aSet.setStackFrameMap(new SlotManager(slots));
        aSet.setPackageData(pack);
        aSet.setBody(this.getFirstChildExpression(aSetElement));
        aSet.setDeclaredStreamable("s".equals(aSetElement.getAttributeValue("", "flags")));
        return aSet;
    }

    private Mode readMode(NodeInfo modeElement) throws XPathException {
        NodeInfo templateRuleElement0;
        String flags;
        StylesheetPackage pack = this.packStack.peek();
        StructuredQName modeName = this.getQNameAttribute(modeElement, "name");
        if (modeName == null) {
            modeName = Mode.UNNAMED_MODE_NAME;
        }
        SimpleMode mode = (SimpleMode)pack.getRuleManager().obtainMode(modeName, true);
        int patternSlots = this.getIntegerAttribute(modeElement, "patternSlots");
        mode.allocatePatternSlots(patternSlots);
        String onNoMatch = modeElement.getAttributeValue("", "onNo");
        if (onNoMatch != null) {
            BuiltInRuleSet base = mode.getBuiltInRuleSetForCode(onNoMatch);
            mode.setBuiltInRuleSet(base);
        }
        if ((flags = modeElement.getAttributeValue("", "flags")) != null) {
            mode.setStreamable(flags.contains("s"));
            if (flags.contains("t")) {
                mode.setExplicitProperty("typed", "yes", 1);
            }
            if (flags.contains("u")) {
                mode.setExplicitProperty("typed", "no", 1);
            }
            if (flags.contains("F")) {
                mode.setRecoveryPolicy(RecoveryPolicy.DO_NOT_RECOVER);
            }
            if (flags.contains("W")) {
                mode.setRecoveryPolicy(RecoveryPolicy.RECOVER_WITH_WARNINGS);
            }
            if (flags.contains("e")) {
                mode.setHasRules(false);
            }
        }
        List<StructuredQName> accNames = this.getListOfQNameAttribute(modeElement, "useAcc");
        this.addCompletionAction(() -> {
            AccumulatorRegistry registry = pack.getAccumulatorRegistry();
            HashSet<Accumulator> accumulators = new HashSet<Accumulator>();
            for (StructuredQName qn : accNames) {
                Accumulator acc = registry.getAccumulator(qn);
                accumulators.add(acc);
            }
            mode.setAccumulators(accumulators);
        });
        AxisIterator iterator2 = modeElement.iterateAxis(4, new NameTest(1, "http://ns.saxonica.com/xslt/export", "templateRule", this.config.getNamePool()));
        LinkedList<NodeInfo> ruleStack = new LinkedList<NodeInfo>();
        while ((templateRuleElement0 = iterator2.next()) != null) {
            ruleStack.addFirst(templateRuleElement0);
        }
        for (NodeInfo templateRuleElement : ruleStack) {
            int precedence = this.getIntegerAttribute(templateRuleElement, "prec");
            int rank = this.getIntegerAttribute(templateRuleElement, "rank");
            String priorityAtt = templateRuleElement.getAttributeValue("", "prio");
            double priority = Double.parseDouble(priorityAtt);
            int sequence = this.getIntegerAttribute(templateRuleElement, "seq");
            int part = this.getIntegerAttribute(templateRuleElement, "part");
            if (part == Integer.MIN_VALUE) {
                part = 0;
            }
            int minImportPrecedence = this.getIntegerAttribute(templateRuleElement, "minImp");
            int slots = this.getIntegerAttribute(templateRuleElement, "slots");
            boolean streamable = "1".equals(templateRuleElement.getAttributeValue("", "streamable"));
            String tflags = templateRuleElement.getAttributeValue("", "flags");
            SequenceType contextType = this.parseAlphaCode(templateRuleElement, "cxt");
            ItemType contextItemType = contextType == null ? AnyItemType.getInstance() : contextType.getPrimaryType();
            NodeInfo matchElement = this.getChildWithRole(templateRuleElement, "match");
            Pattern match = this.loadPattern(matchElement);
            this.localBindings = new Stack();
            TemplateRule template = this.config.makeTemplateRule();
            template.setMatchPattern(match);
            template.setStackFrameMap(new SlotManager(slots));
            template.setPackageData(pack);
            template.setRequiredType(this.parseAlphaCode(templateRuleElement, "as"));
            template.setDeclaredStreamable(streamable);
            template.setContextItemRequirements(contextItemType, !tflags.contains("s"));
            NodeInfo bodyElement = this.getChildWithRole(templateRuleElement, "action");
            if (bodyElement == null) {
                template.setBody(Literal.makeEmptySequence());
            } else {
                Expression body = this.loadExpression(bodyElement);
                template.setBody(body);
                RetainedStaticContext rsc = body.getRetainedStaticContext();
                body.setRetainedStaticContext(rsc);
            }
            Rule rule = mode.makeRule(match, template, precedence, minImportPrecedence, priority, sequence, part);
            rule.setRank(rank);
            mode.addRule(match, rule);
            mode.setHasRules(true);
        }
        this.addCompletionAction(mode::prepareStreamability);
        return mode;
    }

    private void readAccumulators(NodeInfo packageElement) throws XPathException {
        NodeInfo accElement;
        StylesheetPackage pack = this.packStack.peek();
        AxisIterator iterator = packageElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "accumulator", this.config.getNamePool()));
        while ((accElement = iterator.next()) != null) {
            StructuredQName accName = this.getQNameAttribute(accElement, "name");
            Accumulator acc = new Accumulator();
            Component component = Component.makeComponent(acc, Visibility.PRIVATE, VisibilityProvenance.DEFAULTED, pack, pack);
            acc.setDeclaringComponent(component);
            int iniSlots = this.getIntegerAttribute(accElement, "slots");
            acc.setSlotManagerForInitialValueExpression(new SlotManager(iniSlots));
            acc.setAccumulatorName(accName);
            String binds = accElement.getAttributeValue("", "binds");
            this.externalReferences.put(component, binds);
            boolean streamable = "1".equals(accElement.getAttributeValue("", "streamable"));
            String flags = accElement.getAttributeValue("", "flags");
            boolean universal = flags != null && flags.contains("u");
            acc.setDeclaredStreamable(streamable);
            acc.setUniversallyApplicable(universal);
            Expression init = this.getExpressionWithRole(accElement, "init");
            acc.setInitialValueExpression(init);
            NodeInfo pre = this.getChild(accElement, 1);
            this.readAccumulatorRules(acc, pre);
            NodeInfo post = this.getChild(accElement, 2);
            this.readAccumulatorRules(acc, post);
            pack.getAccumulatorRegistry().addAccumulator(acc);
        }
    }

    private void readAccumulatorRules(Accumulator acc, NodeInfo owner) throws XPathException {
        NodeInfo accRuleElement;
        AxisIterator iterator = owner.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "accRule", this.config.getNamePool()));
        boolean preDescent = owner.getLocalPart().equals("pre");
        SimpleMode mode = preDescent ? acc.getPreDescentRules() : acc.getPostDescentRules();
        int patternSlots = this.getIntegerAttribute(owner, "slots");
        mode.setStackFrameSlotsNeeded(patternSlots);
        while ((accRuleElement = iterator.next()) != null) {
            int slots = this.getIntegerAttribute(accRuleElement, "slots");
            int rank = this.getIntegerAttribute(accRuleElement, "rank");
            String flags = accRuleElement.getAttributeValue("", "flags");
            SlotManager sm = new SlotManager(slots);
            Pattern pattern = this.getFirstChildPattern(accRuleElement);
            Expression select = this.getSecondChildExpression(accRuleElement);
            AccumulatorRule rule = new AccumulatorRule(select, sm, !preDescent);
            if (flags != null && flags.contains("c")) {
                rule.setCapturing(true);
            }
            mode.addRule(pattern, mode.makeRule(pattern, rule, rank, 0, rank, 0, 0));
        }
        mode.computeRankings(1);
    }

    private void readOutputProperties(NodeInfo packageElement) {
        NodeInfo outputElement;
        StylesheetPackage pack = this.packStack.peek();
        AxisIterator iterator = packageElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "output", this.config.getNamePool()));
        while ((outputElement = iterator.next()) != null) {
            NodeInfo propertyElement;
            StructuredQName outputName = this.getQNameAttribute(outputElement, "name");
            Properties props = new Properties();
            AxisIterator iterator1 = outputElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "property", this.config.getNamePool()));
            while ((propertyElement = iterator1.next()) != null) {
                String name = propertyElement.getAttributeValue("", "name");
                if (name.startsWith("Q{")) {
                    name = name.substring(1);
                }
                String value = propertyElement.getAttributeValue("", "value");
                if (name.startsWith("{http://saxon.sf.net/}") && !name.equals("{http://saxon.sf.net/}stylesheet-version")) {
                    this.needsPELicense("Saxon output properties");
                }
                props.setProperty(name, value);
            }
            if (outputName == null) {
                pack.setDefaultOutputProperties(props);
                continue;
            }
            pack.setNamedOutputProperties(outputName, props);
        }
    }

    private void readCharacterMaps(NodeInfo packageElement) throws XPathException {
        NodeInfo charMapElement;
        StylesheetPackage pack = this.packStack.peek();
        AxisIterator iterator = packageElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "charMap", this.config.getNamePool()));
        while ((charMapElement = iterator.next()) != null) {
            NodeInfo mappingElement;
            StructuredQName mapName = this.getQNameAttribute(charMapElement, "name");
            AxisIterator iterator1 = charMapElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "m", this.config.getNamePool()));
            IntHashMap<String> map = new IntHashMap<String>();
            while ((mappingElement = iterator1.next()) != null) {
                int c = this.getIntegerAttribute(mappingElement, "c");
                String s = mappingElement.getAttributeValue("", "s");
                map.put(c, s);
            }
            CharacterMap characterMap = new CharacterMap(mapName, map);
            pack.getCharacterMapIndex().putCharacterMap(mapName, characterMap);
        }
    }

    private void readSpaceStrippingRules(NodeInfo packageElement) throws XPathException {
        NodeInfo element;
        StylesheetPackage pack = this.packStack.peek();
        AxisIterator iterator = packageElement.iterateAxis(3, NodeKindTest.ELEMENT);
        while ((element = iterator.next()) != null) {
            String s;
            switch (s = element.getLocalPart()) {
                case "strip.all": {
                    pack.setStripperRules(new AllElementsSpaceStrippingRule());
                    pack.setStripsWhitespace(true);
                    break;
                }
                case "strip.none": {
                    pack.setStripperRules(new NoElementsSpaceStrippingRule());
                    break;
                }
                case "strip": {
                    NodeInfo element2;
                    AxisIterator iterator2 = element.iterateAxis(3, NodeKindTest.ELEMENT);
                    SelectedElementsSpaceStrippingRule rules = new SelectedElementsSpaceStrippingRule(false);
                    while ((element2 = iterator2.next()) != null) {
                        Stripper.StripRuleTarget which = element2.getLocalPart().equals("s") ? Stripper.STRIP : Stripper.PRESERVE;
                        String value = element2.getAttributeValue("", "test");
                        NodeTest t = value.equals("*") ? NodeKindTest.ELEMENT : (NodeTest)this.parseAlphaCodeForItemType(element2, "test");
                        int prec = this.getIntegerAttribute(element2, "prec");
                        NodeTestPattern pat = new NodeTestPattern(t);
                        rules.addRule(pat, which, prec, prec);
                    }
                    pack.setStripperRules(rules);
                    pack.setStripsWhitespace(true);
                }
            }
        }
    }

    private void readDecimalFormats(NodeInfo packageElement) throws XPathException {
        NodeInfo formatElement;
        DecimalFormatManager decimalFormatManager = this.packStack.peek().getDecimalFormatManager();
        AxisIterator iterator = packageElement.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "decimalFormat", this.config.getNamePool()));
        String[] propertyNames = DecimalSymbols.propertyNames;
        while ((formatElement = iterator.next()) != null) {
            StructuredQName name = this.getQNameAttribute(formatElement, "name");
            DecimalSymbols symbols = name == null ? decimalFormatManager.getDefaultDecimalFormat() : decimalFormatManager.obtainNamedDecimalFormat(name);
            symbols.setHostLanguage(HostLanguage.XSLT, 31);
            block11: for (String p : propertyNames) {
                if (formatElement.getAttributeValue("", p) == null) continue;
                switch (p) {
                    case "NaN": {
                        symbols.setNaN(formatElement.getAttributeValue("", "NaN"));
                        continue block11;
                    }
                    case "infinity": {
                        symbols.setInfinity(formatElement.getAttributeValue("", "infinity"));
                        continue block11;
                    }
                    case "name": {
                        continue block11;
                    }
                    default: {
                        symbols.setIntProperty(p, this.getIntegerAttribute(formatElement, p));
                    }
                }
            }
        }
    }

    public NodeInfo getChild(NodeInfo parent, int n) {
        AxisIterator iter = parent.iterateAxis(3, NodeKindTest.ELEMENT);
        NodeInfo node = iter.next();
        for (int i = 0; i < n; ++i) {
            node = iter.next();
        }
        return node;
    }

    public NodeInfo getChildWithRole(NodeInfo parent, String role) {
        NodeInfo node;
        AxisIterator iter = parent.iterateAxis(3, NodeKindTest.ELEMENT);
        while ((node = iter.next()) != null) {
            String roleAtt = node.getAttributeValue("", "role");
            if (!role.equals(roleAtt)) continue;
            return node;
        }
        return null;
    }

    public Expression getFirstChildExpression(NodeInfo parent) throws XPathException {
        NodeInfo node = parent.iterateAxis(3, NodeKindTest.ELEMENT).next();
        return this.loadExpression(node);
    }

    public Expression getSecondChildExpression(NodeInfo parent) throws XPathException {
        NodeInfo node = this.getChild(parent, 1);
        return this.loadExpression(node);
    }

    public Expression getNthChildExpression(NodeInfo parent, int n) throws XPathException {
        NodeInfo node = this.getChild(parent, n);
        return this.loadExpression(node);
    }

    public Expression getExpressionWithRole(NodeInfo parent, String role) throws XPathException {
        NodeInfo node = this.getChildWithRole(parent, role);
        return node == null ? null : this.loadExpression(node);
    }

    public Expression loadExpression(NodeInfo element) throws XPathException {
        if (element == null) {
            return null;
        }
        String tag = element.getLocalPart();
        ExpressionLoader loader = eMap.get(tag);
        if (loader == null) {
            String message = "Cannot load expression with tag " + tag;
            String req = licensableConstructs.get(tag);
            if (req != null) {
                message = message + ". The stylesheet uses Saxon-" + req + " features";
            }
            throw new XPathException(message, "SXPK0002");
        }
        RetainedStaticContext rsc = this.makeRetainedStaticContext(element);
        this.contextStack.push(rsc);
        Expression exp = loader.loadFrom(this, element);
        exp.setRetainedStaticContextLocally(rsc);
        this.contextStack.pop();
        exp.setLocation(this.makeLocation(element));
        return exp;
    }

    private Location makeLocation(NodeInfo element) {
        String lineAtt = this.getInheritedAttribute(element, "line");
        String moduleAtt = this.getInheritedAttribute(element, "module");
        if (lineAtt != null && moduleAtt != null) {
            int line = Integer.parseInt(lineAtt);
            return this.allocateLocation(moduleAtt, line);
        }
        return Loc.NONE;
    }

    public RetainedStaticContext makeRetainedStaticContext(NodeInfo element) {
        StylesheetPackage pack = this.packStack.peek();
        String baseURIAtt = element.getAttributeValue("", "baseUri");
        String defaultCollAtt = element.getAttributeValue("", "defaultCollation");
        String defaultElementNS = element.getAttributeValue("", "defaultElementNS");
        String nsAtt = element.getAttributeValue("", "ns");
        String versionAtt = element.getAttributeValue("", "vn");
        if (baseURIAtt != null || defaultCollAtt != null || nsAtt != null || versionAtt != null || defaultElementNS != null || this.contextStack.peek().getDecimalFormatManager() == null) {
            RetainedStaticContext rsc = new RetainedStaticContext(this.config);
            rsc.setPackageData(pack);
            if (defaultCollAtt != null) {
                rsc.setDefaultCollationName(defaultCollAtt);
            } else {
                rsc.setDefaultCollationName("http://www.w3.org/2005/xpath-functions/collation/codepoint");
            }
            if (baseURIAtt != null) {
                rsc.setStaticBaseUriString(baseURIAtt);
            } else if (this.relocatableBase != null) {
                rsc.setStaticBaseUriString(this.relocatableBase);
            } else {
                String base = Navigator.getInheritedAttributeValue(element, "", "baseUri");
                if (base != null) {
                    rsc.setStaticBaseUriString(base);
                }
            }
            if (nsAtt == null) {
                nsAtt = Navigator.getInheritedAttributeValue(element, "", "ns");
            }
            if (nsAtt != null && !nsAtt.isEmpty()) {
                String[] namespaces;
                for (String ns : namespaces = nsAtt.split(" ")) {
                    int eq = ns.indexOf(61);
                    if (eq < 0) {
                        throw new IllegalStateException("ns=" + nsAtt);
                    }
                    String prefix = ns.substring(0, eq);
                    String uri = ns.substring(eq + 1);
                    if (uri.equals("~")) {
                        uri = NamespaceConstant.getUriForConventionalPrefix(prefix);
                    }
                    rsc.declareNamespace(prefix, uri);
                }
            }
            if (defaultElementNS == null) {
                defaultElementNS = Navigator.getInheritedAttributeValue(element, "", "defaultElementNS");
            }
            if (defaultElementNS != null) {
                rsc.setDefaultElementNamespace(defaultElementNS);
            }
            rsc.setDecimalFormatManager(this.packStack.peek().getDecimalFormatManager());
            return rsc;
        }
        return this.contextStack.peek();
    }

    private Pattern getFirstChildPattern(NodeInfo parent) throws XPathException {
        NodeInfo node = parent.iterateAxis(3, NodeKindTest.ELEMENT).next();
        return this.loadPattern(node);
    }

    private Pattern getSecondChildPattern(NodeInfo parent) throws XPathException {
        NodeInfo node = this.getChild(parent, 1);
        return this.loadPattern(node);
    }

    public Pattern getPatternWithRole(NodeInfo parent, String role) throws XPathException {
        NodeInfo node = this.getChildWithRole(parent, role);
        return node == null ? null : this.loadPattern(node);
    }

    private Pattern loadPattern(NodeInfo element) throws XPathException {
        String tag = element.getLocalPart();
        PatternLoader loader = pMap.get(tag);
        if (loader == null) {
            throw new XPathException("Cannot load pattern with tag " + tag, "SXPK0002");
        }
        Pattern pat = loader.loadFrom(this, element);
        pat.setLocation(this.makeLocation(element));
        pat.setRetainedStaticContext(this.makeRetainedStaticContext(element));
        return pat;
    }

    public SchemaType getTypeAttribute(NodeInfo element, String attName) {
        String val = element.getAttributeValue("", attName);
        if (val == null) {
            return null;
        }
        if (val.startsWith("xs:")) {
            return this.config.getSchemaType(new StructuredQName("xs", "http://www.w3.org/2001/XMLSchema", val.substring(3)));
        }
        StructuredQName name = this.getQNameAttribute(element, attName);
        return this.config.getSchemaType(name);
    }

    public StructuredQName getQNameAttribute(NodeInfo element, String localName) {
        String val = element.getAttributeValue("", localName);
        if (val == null) {
            return null;
        }
        return StructuredQName.fromEQName(val);
    }

    public List<StructuredQName> getListOfQNameAttribute(NodeInfo element, String localName) throws XPathException {
        String val = element.getAttributeValue("", localName);
        if (val == null) {
            return Collections.emptyList();
        }
        ArrayList<StructuredQName> result = new ArrayList<StructuredQName>();
        for (String s : val.split(" ")) {
            StructuredQName sq = this.resolveQName(s, element);
            result.add(sq);
        }
        return result;
    }

    private StructuredQName resolveQName(String val, NodeInfo element) throws XPathException {
        if (val.startsWith("Q{")) {
            return StructuredQName.fromEQName(val);
        }
        if (val.contains(":")) {
            return StructuredQName.fromLexicalQName(val, true, true, element.getAllNamespaces());
        }
        return new StructuredQName("", "", val);
    }

    public int getIntegerAttribute(NodeInfo element, String localName) throws XPathException {
        String val = element.getAttributeValue("", localName);
        if (val == null) {
            return Integer.MIN_VALUE;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            throw new XPathException("Expected integer value for " + element.getDisplayName() + "/" + localName + ", found '" + val + "'", "SXPK0002");
        }
    }

    public String getInheritedAttribute(NodeInfo element, String localName) {
        while (element != null) {
            String val = element.getAttributeValue("", localName);
            if (val != null) {
                return val;
            }
            element = element.getParent();
        }
        return null;
    }

    public SequenceType parseSequenceType(NodeInfo element, String name) throws XPathException {
        IndependentContext env = this.makeStaticContext(element);
        String attValue = element.getAttributeValue("", name);
        if (attValue == null) {
            return SequenceType.ANY_SEQUENCE;
        }
        return this.parser.parseExtendedSequenceType(attValue, env);
    }

    public SequenceType parseAlphaCode(NodeInfo element, String name) throws XPathException {
        String attValue = element.getAttributeValue("", name);
        if (attValue == null) {
            return SequenceType.ANY_SEQUENCE;
        }
        try {
            return AlphaCode.toSequenceType(attValue, this.config);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new XPathException("Invalid alpha code " + element.getDisplayName() + "/@" + name + "='" + attValue + "': " + e.getMessage());
        }
    }

    public ItemType parseAlphaCodeForItemType(NodeInfo element, String name) throws XPathException {
        String attValue = element.getAttributeValue("", name);
        if (attValue == null) {
            return AnyItemType.getInstance();
        }
        try {
            return AlphaCode.toItemType(attValue, this.config);
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new XPathException("Invalid alpha code " + element.getDisplayName() + "/@" + name + "='" + attValue + "': " + e.getMessage());
        }
    }

    private IndependentContext makeStaticContext(NodeInfo element) {
        StylesheetPackage pack = this.packStack.peek();
        IndependentContext env = new IndependentContext(this.config);
        NamespaceMap resolver = element.getAllNamespaces();
        env.setNamespaceResolver(resolver);
        env.setImportedSchemaNamespaces(pack.getSchemaNamespaces());
        env.getImportedSchemaNamespaces().add("http://ns.saxonica.com/anonymous-type");
        this.parser.setQNameParser(this.parser.getQNameParser().withNamespaceResolver(resolver));
        return env;
    }

    public ItemType parseItemTypeAttribute(NodeInfo element, String attName) throws XPathException {
        String attValue = element.getAttributeValue("", attName);
        if (attValue == null) {
            return AnyItemType.getInstance();
        }
        return this.parseItemType(element, attValue);
    }

    private ItemType parseItemType(NodeInfo element, String attValue) throws XPathException {
        IndependentContext env = this.makeStaticContext(element);
        return this.parser.parseExtendedItemType(attValue, env);
    }

    public AtomicComparer makeAtomicComparer(String name, NodeInfo element) throws XPathException {
        if (name.equals("CCC")) {
            return CodepointCollatingComparer.getInstance();
        }
        if (name.equals("CAVC")) {
            return ComparableAtomicValueComparer.getInstance();
        }
        if (name.startsWith("GAC|")) {
            StringCollator collator = this.config.getCollation(name.substring(4));
            return new GenericAtomicComparer(collator, null);
        }
        if (name.equals("CalVC")) {
            return new CalendarValueComparer(null);
        }
        if (name.equals("EQC")) {
            return EqualityComparer.getInstance();
        }
        if (name.equals("NC")) {
            return NumericComparer.getInstance();
        }
        if (name.equals("NC11")) {
            return NumericComparer11.getInstance();
        }
        if (name.equals("QUNC")) {
            return new UntypedNumericComparer();
        }
        if (name.equals("DblSC")) {
            return DoubleSortComparer.getInstance();
        }
        if (name.equals("DecSC")) {
            return DecimalSortComparer.getInstance();
        }
        if (name.startsWith("CAC|")) {
            StringCollator collator = this.config.getCollation(name.substring(4));
            return new CollatingAtomicComparer(collator);
        }
        if (name.startsWith("AtSC|")) {
            int nextBar = name.indexOf(124, 5);
            String fps = name.substring(5, nextBar);
            int fp = Integer.parseInt(fps);
            String collName = name.substring(nextBar + 1);
            return AtomicSortComparer.makeSortComparer(this.config.getCollation(collName), fp, new EarlyEvaluationContext(this.config));
        }
        if (name.startsWith("DESC|")) {
            AtomicComparer base = this.makeAtomicComparer(name.substring(5), element);
            return new DescendingComparer(base);
        }
        if (name.startsWith("TEXT|")) {
            AtomicComparer base = this.makeAtomicComparer(name.substring(5), element);
            return new TextComparer(base);
        }
        throw new XPathException("Unknown comparer " + name, "SXPK0002");
    }

    private SortKeyDefinitionList loadSortKeyDefinitions(NodeInfo element) throws XPathException {
        NodeInfo sortKeyElement;
        ArrayList<SortKeyDefinition> skdl = new ArrayList<SortKeyDefinition>(4);
        AxisIterator iterator = element.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "sortKey", this.config.getNamePool()));
        while ((sortKeyElement = iterator.next()) != null) {
            SortKeyDefinition skd = new SortKeyDefinition();
            String compAtt = sortKeyElement.getAttributeValue("", "comp");
            if (compAtt != null) {
                AtomicComparer ac = this.makeAtomicComparer(compAtt, sortKeyElement);
                skd.setFinalComparator(ac);
            }
            skd.setSortKey(this.getExpressionWithRole(sortKeyElement, "select"), true);
            skd.setOrder(this.getExpressionWithRole(sortKeyElement, "order"));
            skd.setLanguage(this.getExpressionWithRole(sortKeyElement, "lang"));
            skd.setCollationNameExpression(this.getExpressionWithRole(sortKeyElement, "collation"));
            skd.setCaseOrder(this.getExpressionWithRole(sortKeyElement, "caseOrder"));
            skd.setStable(this.getExpressionWithRole(sortKeyElement, "stable"));
            skd.setDataTypeExpression(this.getExpressionWithRole(sortKeyElement, "dataType"));
            skdl.add(skd);
        }
        return new SortKeyDefinitionList(skdl.toArray(new SortKeyDefinition[0]));
    }

    private WithParam[] loadWithParams(NodeInfo element, Expression parent, boolean needTunnel) throws XPathException {
        NodeInfo wpElement;
        ArrayList<WithParam> wps = new ArrayList<WithParam>(4);
        AxisIterator iterator = element.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "withParam", this.config.getNamePool()));
        while ((wpElement = iterator.next()) != null) {
            String flags = wpElement.getAttributeValue("", "flags");
            boolean isTunnel = flags != null && flags.contains("t");
            if (needTunnel != isTunnel) continue;
            WithParam wp = new WithParam();
            wp.setVariableQName(this.getQNameAttribute(wpElement, "name"));
            wp.setSelectExpression(parent, this.getFirstChildExpression(wpElement));
            wp.setRequiredType(this.parseAlphaCode(wpElement, "as"));
            wp.setTypeChecked(flags != null && flags.contains("c"));
            wps.add(wp);
        }
        return wps.toArray(new WithParam[0]);
    }

    private Properties importProperties(String value) {
        try {
            String line;
            StringReader reader = new StringReader(value);
            Properties props = new Properties();
            LineNumberReader lnr = new LineNumberReader(reader);
            while ((line = lnr.readLine()) != null) {
                String val;
                int eq = line.indexOf(61);
                String key = line.substring(0, eq);
                String string = val = eq == line.length() - 1 ? "" : line.substring(eq + 1);
                if (key.equals("item-separator") || key.equals("Q{http://saxon.sf.net/}newline")) {
                    try {
                        val = JsonParser.unescape(val, 0, "", -1);
                    } catch (XPathException xPathException) {
                        // empty catch block
                    }
                }
                if (key.startsWith("Q{")) {
                    key = key.substring(1);
                }
                props.setProperty(key, val);
            }
            return props;
        } catch (IOException e) {
            throw new AssertionError((Object)e);
        }
    }

    private static int getLevelCode(String levelAtt) {
        int level;
        if (levelAtt == null) {
            level = 0;
        } else if (levelAtt.equals("single")) {
            level = 0;
        } else if (levelAtt.equals("multi")) {
            level = 1;
        } else if (levelAtt.equals("any")) {
            level = 2;
        } else if (levelAtt.equals("simple")) {
            level = 3;
        } else {
            throw new AssertionError();
        }
        return level;
    }

    protected static List<Expression> getChildExpressionList(PackageLoaderHE loader, NodeInfo element) throws XPathException {
        NodeInfo child;
        ArrayList<Expression> children = new ArrayList<Expression>();
        AxisIterator iter = element.iterateAxis(3, NodeKindTest.ELEMENT);
        while ((child = iter.next()) != null) {
            children.add(loader.loadExpression(child));
        }
        return children;
    }

    protected static Expression[] getChildExpressionArray(PackageLoaderHE loader, NodeInfo element) throws XPathException {
        List<Expression> children = PackageLoaderHE.getChildExpressionList(loader, element);
        return children.toArray(new Expression[0]);
    }

    protected static int getOperator(String opAtt) {
        int op;
        switch (opAtt) {
            case "=": {
                op = 6;
                break;
            }
            case "!=": {
                op = 22;
                break;
            }
            case "<=": {
                op = 14;
                break;
            }
            case ">=": {
                op = 13;
                break;
            }
            case "<": {
                op = 12;
                break;
            }
            case ">": {
                op = 11;
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        return op;
    }

    private static int parseValueComparisonOperator(String opAtt) {
        int op;
        switch (opAtt) {
            case "eq": {
                op = 50;
                break;
            }
            case "ne": {
                op = 51;
                break;
            }
            case "le": {
                op = 55;
                break;
            }
            case "ge": {
                op = 54;
                break;
            }
            case "lt": {
                op = 53;
                break;
            }
            case "gt": {
                op = 52;
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        return op;
    }

    private void resolveFixups() throws XPathException {
        StylesheetPackage pack = this.packStack.peek();
        for (ComponentInvocation call : this.fixups.peek()) {
            if (this.processComponentReference(pack, call)) break;
        }
        pack.allocateBinderySlots();
    }

    protected boolean processComponentReference(StylesheetPackage pack, ComponentInvocation call) throws XPathException {
        SymbolicName sn = call.getSymbolicName();
        Component c = pack.getComponent(sn);
        if (c == null) {
            if (sn.getComponentName().hasURI("http://www.w3.org/1999/XSL/Transform") && sn.getComponentName().getLocalPart().equals("original")) {
                return true;
            }
            throw new XPathException("Loading compiled package: unresolved component reference to " + sn);
        }
        if (call instanceof GlobalVariableReference) {
            ((GlobalVariableReference)call).setTarget(c);
        } else if (call instanceof UserFunctionCall) {
            ((UserFunctionCall)call).setFunction((UserFunction)c.getActor());
            ((UserFunctionCall)call).setStaticType(((UserFunction)c.getActor()).getResultType());
        } else if (call instanceof CallTemplate) {
            ((CallTemplate)call).setTargetTemplate((NamedTemplate)c.getActor());
        } else if (call instanceof UseAttributeSet) {
            ((UseAttributeSet)call).setTarget((AttributeSet)c.getActor());
        } else if (call instanceof ApplyTemplates) {
            ((ApplyTemplates)call).setMode((SimpleMode)c.getActor());
        } else {
            throw new XPathException("Unknown component reference " + call.getClass());
        }
        return false;
    }

    private Location allocateLocation(String module, int lineNumber) {
        Location loc;
        IntHashMap<Location> lineMap = this.locationMap.get(module);
        if (lineMap == null) {
            lineMap = new IntHashMap();
            this.locationMap.put(module, lineMap);
        }
        if ((loc = lineMap.get(lineNumber)) == null) {
            loc = new Loc(module, lineNumber, -1);
            lineMap.put(lineNumber, loc);
        }
        return loc;
    }

    static {
        licensableConstructs.put("gcEE", "EE");
        licensableConstructs.put("indexedFilter", "EE");
        licensableConstructs.put("indexedFilter2", "EE");
        licensableConstructs.put("indexedLookup", "EE");
        licensableConstructs.put("stream", "EE");
        licensableConstructs.put("switch", "EE");
        licensableConstructs.put("acFnRef", "PE");
        licensableConstructs.put("assign", "PE");
        licensableConstructs.put("do", "PE");
        licensableConstructs.put("javaCall", "PE");
        licensableConstructs.put("while", "PE");
        eMap.put("among", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new SingletonIntersectExpression(lhs, 23, rhs);
        });
        eMap.put("analyzeString", (loader, element) -> {
            Expression select = loader.getExpressionWithRole(element, "select");
            Expression regex = loader.getExpressionWithRole(element, "regex");
            Expression flags = loader.getExpressionWithRole(element, "flags");
            Expression matching = loader.getExpressionWithRole(element, "matching");
            Expression nonMatching = loader.getExpressionWithRole(element, "nonMatching");
            AnalyzeString instr = new AnalyzeString(select, regex, flags, matching, nonMatching, null);
            instr.precomputeRegex(loader.getConfiguration(), null);
            return instr;
        });
        eMap.put("and", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new AndExpression(lhs, rhs);
        });
        eMap.put("applyImports", (loader, element) -> {
            ApplyImports inst = new ApplyImports();
            WithParam[] actuals = loader.loadWithParams(element, inst, false);
            WithParam[] tunnels = loader.loadWithParams(element, inst, true);
            inst.setActualParams(actuals);
            inst.setTunnelParams(tunnels);
            return inst;
        });
        eMap.put("applyT", (loader, element) -> {
            StylesheetPackage pack = loader.packStack.peek();
            Expression select = loader.getFirstChildExpression(element);
            StructuredQName modeAtt = loader.getQNameAttribute(element, "mode");
            SimpleMode mode = modeAtt != null ? (SimpleMode)pack.getRuleManager().obtainMode(modeAtt, true) : (SimpleMode)pack.getRuleManager().obtainMode(null, true);
            String flags = element.getAttributeValue("", "flags");
            if (flags == null) {
                flags = "";
            }
            boolean useCurrentMode = flags.contains("c");
            boolean useTailRecursion = flags.contains("t");
            boolean implicitSelect = flags.contains("i");
            boolean inStreamableConstruct = flags.contains("d");
            ApplyTemplates inst = new ApplyTemplates(select, useCurrentMode, useTailRecursion, implicitSelect, inStreamableConstruct, mode, loader.packStack.peek().getRuleManager());
            Expression sep = loader.getExpressionWithRole(element, "separator");
            if (sep != null) {
                inst.setSeparatorExpression(sep);
            }
            WithParam[] actuals = loader.loadWithParams(element, inst, false);
            WithParam[] tunnels = loader.loadWithParams(element, inst, true);
            inst.setActualParams(actuals);
            inst.setTunnelParams(tunnels);
            int bindingSlot = loader.getIntegerAttribute(element, "bSlot");
            inst.setBindingSlot(bindingSlot);
            return inst;
        });
        eMap.put("arith", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            String code = element.getAttributeValue("", "calc");
            Calculator calc = Calculator.reconstructCalculator(code);
            int operator = Calculator.operatorFromCode(code.charAt(1));
            int token = Calculator.getTokenFromOperator(operator);
            ArithmeticExpression exp = new ArithmeticExpression(lhs, token, rhs);
            exp.setCalculator(calc);
            return exp;
        });
        eMap.put("arith10", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            String code = element.getAttributeValue("", "calc");
            Calculator calc = Calculator.reconstructCalculator(code);
            int operator = Calculator.operatorFromCode(code.charAt(1));
            int token = Calculator.getTokenFromOperator(operator);
            ArithmeticExpression10 exp = new ArithmeticExpression10(lhs, token, rhs);
            exp.setCalculator(calc);
            return exp;
        });
        eMap.put("array", (loader, element) -> {
            List<Expression> children = PackageLoaderHE.getChildExpressionList(loader, element);
            ArrayList<GroundedValue> values = new ArrayList<GroundedValue>(children.size());
            for (Expression child : children) {
                values.add(((Literal)child).getValue());
            }
            return Literal.makeLiteral(new SimpleArrayItem(values));
        });
        eMap.put("arrayBlock", (loader, element) -> {
            List<Expression> children = PackageLoaderHE.getChildExpressionList(loader, element);
            return new SquareArrayConstructor(children);
        });
        eMap.put("atomic", (loader, element) -> {
            String valAtt = element.getAttributeValue("", "val");
            AtomicType type = (AtomicType)loader.parseAlphaCodeForItemType(element, "type");
            AtomicValue val = type.getStringConverter(loader.config.getConversionRules()).convertString(valAtt).asAtomic();
            return Literal.makeLiteral(val);
        });
        eMap.put("atomSing", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            RoleDiagnostic role = RoleDiagnostic.reconstruct(element.getAttributeValue("", "diag"));
            String cardAtt = element.getAttributeValue("", "card");
            boolean allowEmpty = "?".equals(cardAtt);
            return new SingletonAtomizer(body, role, allowEmpty);
        });
        eMap.put("att", (loader, element) -> {
            SchemaType schemaType;
            String[] parts;
            String displayName = element.getAttributeValue("", "name");
            try {
                parts = NameChecker.getQNameParts(displayName);
            } catch (QNameException err) {
                throw new XPathException(err);
            }
            String uri = element.getAttributeValue("", "nsuri");
            if (uri == null) {
                uri = "";
            }
            StructuredQName name = new StructuredQName(parts[0], uri, parts[1]);
            FingerprintedQName attName = new FingerprintedQName(name, loader.config.getNamePool());
            int validation = 4;
            String valAtt = element.getAttributeValue("", "validation");
            if (valAtt != null) {
                validation = Validation.getCode(valAtt);
            }
            if ((schemaType = loader.getTypeAttribute(element, "type")) != null) {
                validation = 8;
            }
            Expression content = loader.getFirstChildExpression(element);
            FixedAttribute att = new FixedAttribute(attName, validation, (SimpleType)schemaType);
            att.setSelect(content);
            return att;
        });
        eMap.put("attVal", (loader, element) -> {
            StructuredQName name = loader.getQNameAttribute(element, "name");
            FingerprintedQName attName = new FingerprintedQName(name, loader.config.getNamePool());
            AttributeGetter getter = new AttributeGetter(attName);
            getter.setRequiredChecks(loader.getIntegerAttribute(element, "chk"));
            return getter;
        });
        eMap.put("axis", (loader, element) -> {
            String axisName = element.getAttributeValue("", "name");
            int axis = AxisInfo.getAxisNumber(axisName);
            NodeTest nt = (NodeTest)loader.parseAlphaCodeForItemType(element, "nodeTest");
            return new AxisExpression(axis, nt);
        });
        eMap.put("break", (loader, element) -> new BreakInstr());
        eMap.put("callT", (loader, element) -> {
            StructuredQName name;
            SymbolicName symbol;
            StylesheetPackage pack = loader.packStack.peek();
            Component target = pack.getComponent(symbol = new SymbolicName(200, name = loader.getQNameAttribute(element, "name")));
            NamedTemplate t = target == null ? new NamedTemplate(name) : (NamedTemplate)target.getActor();
            String flags = element.getAttributeValue("", "flags");
            boolean useTailRecursion = flags != null && flags.contains("t");
            boolean inStreamableConstruct = flags != null && flags.contains("d");
            CallTemplate inst = new CallTemplate(t, name, useTailRecursion, inStreamableConstruct);
            WithParam[] actuals = loader.loadWithParams(element, inst, false);
            WithParam[] tunnels = loader.loadWithParams(element, inst, true);
            inst.setActualParameters(actuals, tunnels);
            int bindingSlot = loader.getIntegerAttribute(element, "bSlot");
            inst.setBindingSlot(bindingSlot);
            loader.fixups.peek().add(inst);
            return inst;
        });
        eMap.put("cast", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            String flags = element.getAttributeValue("", "flags");
            boolean allowEmpty = flags.contains("e");
            if (flags.contains("a")) {
                SequenceType seqType = loader.parseAlphaCode(element, "as");
                return new CastExpression(body, (AtomicType)seqType.getPrimaryType(), allowEmpty);
            }
            if (flags.contains("l")) {
                StructuredQName typeName = StructuredQName.fromEQName(element.getAttributeValue("", "as"));
                SchemaType type = loader.config.getSchemaType(typeName);
                NamespaceMap resolver = element.getAllNamespaces();
                ListConstructorFunction ucf = new ListConstructorFunction((ListType)type, resolver, allowEmpty);
                return new StaticFunctionCall(ucf, new Expression[]{body});
            }
            if (flags.contains("u")) {
                if (element.getAttributeValue("", "as") != null) {
                    StructuredQName typeName = StructuredQName.fromEQName(element.getAttributeValue("", "as"));
                    SchemaType type = loader.config.getSchemaType(typeName);
                    NamespaceMap resolver = element.getAllNamespaces();
                    UnionConstructorFunction ucf = new UnionConstructorFunction((UnionType)((Object)type), resolver, allowEmpty);
                    return new StaticFunctionCall(ucf, new Expression[]{body});
                }
                LocalUnionType type = (LocalUnionType)loader.parseAlphaCode(element, "to").getPrimaryType();
                NamespaceMap resolver = element.getAllNamespaces();
                UnionConstructorFunction ucf = new UnionConstructorFunction(type, resolver, allowEmpty);
                return new StaticFunctionCall(ucf, new Expression[]{body});
            }
            throw new AssertionError((Object)("Unknown simple type variety " + flags));
        });
        eMap.put("castable", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            String flags = element.getAttributeValue("", "flags");
            boolean allowEmpty = flags.contains("e");
            if (flags.contains("a")) {
                SequenceType seqType = loader.parseAlphaCode(element, "as");
                return new CastableExpression(body, (AtomicType)seqType.getPrimaryType(), allowEmpty);
            }
            if (flags.contains("l")) {
                StructuredQName typeName = StructuredQName.fromEQName(element.getAttributeValue("", "as"));
                SchemaType type = loader.config.getSchemaType(typeName);
                NamespaceMap resolver = element.getAllNamespaces();
                ListCastableFunction ucf = new ListCastableFunction((ListType)type, resolver, allowEmpty);
                return new StaticFunctionCall(ucf, new Expression[]{body});
            }
            if (flags.contains("u")) {
                if (element.getAttributeValue("", "as") != null) {
                    StructuredQName typeName = StructuredQName.fromEQName(element.getAttributeValue("", "as"));
                    SchemaType type = loader.config.getSchemaType(typeName);
                    NamespaceMap resolver = element.getAllNamespaces();
                    UnionCastableFunction ucf = new UnionCastableFunction((UnionType)((Object)type), resolver, allowEmpty);
                    return new StaticFunctionCall(ucf, new Expression[]{body});
                }
                LocalUnionType type = (LocalUnionType)loader.parseAlphaCode(element, "to").getPrimaryType();
                NamespaceMap resolver = element.getAllNamespaces();
                UnionCastableFunction ucf = new UnionCastableFunction(type, resolver, allowEmpty);
                return new StaticFunctionCall(ucf, new Expression[]{body});
            }
            throw new AssertionError((Object)("Unknown simple type variety " + flags));
        });
        eMap.put("check", (loader, element) -> {
            int c;
            String cardAtt;
            Expression body = loader.getFirstChildExpression(element);
            switch (cardAtt = element.getAttributeValue("", "card")) {
                case "?": {
                    c = 24576;
                    break;
                }
                case "*": {
                    c = 57344;
                    break;
                }
                case "+": {
                    c = 49152;
                    break;
                }
                case "\u00b0": 
                case "0": {
                    c = 8192;
                    break;
                }
                case "1": {
                    c = 16384;
                    break;
                }
                default: {
                    throw new IllegalStateException("Occurrence indicator: '" + cardAtt + "'");
                }
            }
            RoleDiagnostic role = RoleDiagnostic.reconstruct(element.getAttributeValue("", "diag"));
            return CardinalityChecker.makeCardinalityChecker(body, c, role);
        });
        eMap.put("choose", (loader, element) -> {
            NodeInfo child;
            ArrayList<Expression> conditions = new ArrayList<Expression>();
            ArrayList<Expression> actions = new ArrayList<Expression>();
            AxisIterator iter = element.iterateAxis(3, NodeKindTest.ELEMENT);
            boolean odd = true;
            while ((child = iter.next()) != null) {
                if (odd) {
                    conditions.add(loader.loadExpression(child));
                } else {
                    actions.add(loader.loadExpression(child));
                }
                odd = !odd;
            }
            return new Choose(conditions.toArray(new Expression[0]), actions.toArray(new Expression[0]));
        });
        eMap.put("coercedFn", (loader, element) -> {
            CoercedFunction coercedFn;
            ItemType type = loader.parseItemTypeAttribute(element, "type");
            Expression target = loader.getFirstChildExpression(element);
            if (target instanceof UserFunctionReference) {
                CoercedFunction coercedFn2 = coercedFn = new CoercedFunction((SpecificFunctionType)type);
                SymbolicName name = ((UserFunctionReference)target).getSymbolicName();
                loader.completionActions.add(() -> coercedFn2.setTargetFunction(loader.userFunctions.get(name)));
            } else if (target instanceof FunctionLiteral) {
                Function targetFn = (Function)((Literal)target).getValue();
                coercedFn = new CoercedFunction(targetFn, (SpecificFunctionType)type);
            } else {
                throw new AssertionError();
            }
            return Literal.makeLiteral(coercedFn);
        });
        eMap.put("comment", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            Comment inst = new Comment();
            inst.setSelect(select);
            return inst;
        });
        eMap.put("compareToInt", (loader, element) -> {
            BigInteger i = new BigInteger(element.getAttributeValue("", "val"));
            String opAtt = element.getAttributeValue("", "op");
            Expression lhs = loader.getFirstChildExpression(element);
            return new CompareToIntegerConstant(lhs, PackageLoaderHE.parseValueComparisonOperator(opAtt), i.longValue());
        });
        eMap.put("compareToString", (loader, element) -> {
            String s = element.getAttributeValue("", "val");
            String opAtt = element.getAttributeValue("", "op");
            Expression lhs = loader.getFirstChildExpression(element);
            return new CompareToStringConstant(lhs, PackageLoaderHE.parseValueComparisonOperator(opAtt), s);
        });
        eMap.put("compAtt", (loader, element) -> {
            SchemaType schemaType;
            Expression name = loader.getExpressionWithRole(element, "name");
            Expression namespace = loader.getExpressionWithRole(element, "namespace");
            Expression content = loader.getExpressionWithRole(element, "select");
            int validation = 4;
            String valAtt = element.getAttributeValue("", "validation");
            if (valAtt != null) {
                validation = Validation.getCode(valAtt);
            }
            if ((schemaType = loader.getTypeAttribute(element, "type")) != null) {
                validation = 8;
            }
            ComputedAttribute att = new ComputedAttribute(name, namespace, null, validation, (SimpleType)schemaType, false);
            att.setSelect(content);
            return att;
        });
        eMap.put("compElem", (loader, element) -> {
            SchemaType schemaType;
            Expression name = loader.getExpressionWithRole(element, "name");
            Expression namespace = loader.getExpressionWithRole(element, "namespace");
            Expression content = loader.getExpressionWithRole(element, "content");
            int validation = 4;
            String valAtt = element.getAttributeValue("", "validation");
            if (valAtt != null) {
                validation = Validation.getCode(valAtt);
            }
            if ((schemaType = loader.getTypeAttribute(element, "type")) != null) {
                validation = 8;
            }
            String flags = element.getAttributeValue("", "flags");
            ComputedElement inst = new ComputedElement(name, namespace, schemaType, validation, true, false);
            if (flags != null) {
                inst.setInheritanceFlags(flags);
            }
            inst.setContentExpression(content);
            return inst.simplify();
        });
        eMap.put("compiledExpression", PackageLoaderHE::getFirstChildExpression);
        eMap.put("conditionalSort", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new ConditionalSorter(lhs, (DocumentSorter)rhs);
        });
        eMap.put("condCont", (loader, element) -> {
            Expression base = loader.getFirstChildExpression(element);
            return new WherePopulated(base);
        });
        eMap.put("condSeq", (loader, element) -> {
            Expression[] args = PackageLoaderHE.getChildExpressionArray(loader, element);
            return new ConditionalBlock(args);
        });
        eMap.put("consume", (loader, element) -> {
            Expression arg = loader.getFirstChildExpression(element);
            return new ConsumingOperand(arg);
        });
        eMap.put("convert", (loader, element) -> {
            Converter c;
            Expression body = loader.getFirstChildExpression(element);
            ItemType fromType = loader.parseAlphaCodeForItemType(element, "from");
            ItemType toType = loader.parseAlphaCodeForItemType(element, "to");
            AtomicSequenceConverter asc = new AtomicSequenceConverter(body, (PlainType)toType);
            if ("p".equals(element.getAttributeValue("", "flags"))) {
                c = toType.equals(BuiltInAtomicType.DOUBLE) ? new Converter.PromoterToDouble() : new Converter.PromoterToFloat();
                asc.setConverter(c);
            } else {
                c = asc.allocateConverter(loader.config, false, fromType);
                asc.setConverter(c);
            }
            String diag = element.getAttributeValue("", "diag");
            if (diag != null) {
                asc.setRoleDiagnostic(RoleDiagnostic.reconstruct(diag));
            }
            return asc;
        });
        eMap.put("copy", (loader, element) -> {
            SchemaType schemaType;
            int validation = 4;
            String valAtt = element.getAttributeValue("", "validation");
            if (valAtt != null) {
                validation = Validation.getCode(valAtt);
            }
            if ((schemaType = loader.getTypeAttribute(element, "type")) != null) {
                validation = 8;
            }
            String sType = element.getAttributeValue("", "sit");
            Copy inst = new Copy(false, false, schemaType, validation);
            inst.setContentExpression(loader.getFirstChildExpression(element));
            String flags = element.getAttributeValue("", "flags");
            inst.setCopyNamespaces(flags.contains("c"));
            inst.setBequeathNamespacesToChildren(flags.contains("i"));
            inst.setInheritNamespacesFromParent(flags.contains("n"));
            if (sType != null) {
                SequenceType st = AlphaCode.toSequenceType(sType, loader.getConfiguration());
                inst.setSelectItemType(st.getPrimaryType());
            }
            return inst;
        });
        eMap.put("copyOf", (loader, element) -> {
            SchemaType schemaType;
            Expression select = loader.getFirstChildExpression(element);
            String flags = element.getAttributeValue("", "flags");
            if (flags == null) {
                flags = "";
            }
            boolean copyNamespaces = flags.contains("c");
            boolean rejectDups = flags.contains("d");
            int validation = 4;
            String valAtt = element.getAttributeValue("", "validation");
            if (valAtt != null) {
                validation = Validation.getCode(valAtt);
            }
            if ((schemaType = loader.getTypeAttribute(element, "type")) != null) {
                validation = 8;
            }
            CopyOf inst = new CopyOf(select, copyNamespaces, validation, schemaType, rejectDups);
            inst.setCopyAccumulators(flags.contains("m"));
            inst.setCopyLineNumbers(flags.contains("l"));
            inst.setSchemaAware(flags.contains("s"));
            inst.setCopyForUpdate(flags.contains("u"));
            return inst;
        });
        eMap.put("currentGroup", (loader, element) -> new CurrentGroupCall());
        eMap.put("currentGroupingKey", (loader, element) -> new CurrentGroupingKeyCall());
        eMap.put("curriedFunc", (loader, element) -> {
            Expression target = loader.getFirstChildExpression(element);
            Function targetFn = (Function)((Literal)target).getValue();
            NodeInfo args = loader.getChild(element, 1);
            int count = Count.count(args.iterateAxis(3, NodeKindTest.ELEMENT));
            Sequence[] argValues = new Sequence[count];
            count = 0;
            for (NodeInfo nodeInfo : args.children(NodeKindTest.ELEMENT)) {
                if (nodeInfo.getLocalPart().equals("x")) {
                    argValues[count++] = null;
                    continue;
                }
                Expression arg = loader.loadExpression(nodeInfo);
                argValues[count++] = ((Literal)arg).getValue();
            }
            CurriedFunction f = new CurriedFunction(targetFn, argValues);
            return Literal.makeLiteral(f);
        });
        eMap.put("cvUntyped", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            ItemType toType = loader.parseAlphaCodeForItemType(element, "to");
            if (((SimpleType)((Object)toType)).isNamespaceSensitive()) {
                return UntypedSequenceConverter.makeUntypedSequenceRejector(loader.config, body, (PlainType)toType);
            }
            UntypedSequenceConverter cv = UntypedSequenceConverter.makeUntypedSequenceConverter(loader.config, body, (PlainType)toType);
            String diag = element.getAttributeValue("", "diag");
            if (diag != null) {
                cv.setRoleDiagnostic(RoleDiagnostic.reconstruct(diag));
            }
            return cv;
        });
        eMap.put("data", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            String diag = element.getAttributeValue("", "diag");
            return new Atomizer(body, diag == null ? null : RoleDiagnostic.reconstruct(diag));
        });
        eMap.put("dbl", (loader, element) -> {
            String val = element.getAttributeValue("", "val");
            double d = StringToDouble.getInstance().stringToNumber(val);
            return Literal.makeLiteral(new DoubleValue(d));
        });
        eMap.put("dec", (loader, element) -> {
            String val = element.getAttributeValue("", "val");
            return Literal.makeLiteral(BigDecimalValue.makeDecimalValue(val, false).asAtomic());
        });
        eMap.put("doc", (loader, element) -> {
            String flags;
            SchemaType schemaType;
            int validation = 4;
            String valAtt = element.getAttributeValue("", "validation");
            if (valAtt != null) {
                validation = Validation.getCode(valAtt);
            }
            if ((schemaType = loader.getTypeAttribute(element, "type")) != null) {
                validation = 8;
            }
            boolean textOnly = (flags = element.getAttributeValue("", "flags")) != null && flags.contains("t");
            String base = element.getAttributeValue("", "base");
            String constantText = element.getAttributeValue("", "text");
            Expression body = loader.getFirstChildExpression(element);
            DocumentInstr inst = new DocumentInstr(textOnly, constantText);
            inst.setContentExpression(body);
            inst.setValidationAction(validation, schemaType);
            return inst;
        });
        eMap.put("docOrder", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            boolean intra = element.getAttributeValue("", "intra").equals("1");
            return new DocumentSorter(select, intra);
        });
        eMap.put("dot", (loader, element) -> {
            ContextItemExpression cie = new ContextItemExpression();
            SequenceType st = loader.parseAlphaCode(element, "type");
            ItemType type = st.getPrimaryType();
            boolean maybeAbsent = false;
            if ("a".equals(element.getAttributeValue("", "flags"))) {
                maybeAbsent = true;
            }
            ContextItemStaticInfo info = loader.getConfiguration().makeContextItemStaticInfo(type, maybeAbsent);
            cie.setStaticInfo(info);
            return cie;
        });
        eMap.put("elem", (loader, element) -> {
            SchemaType schemaType;
            String[] parts;
            String displayName = element.getAttributeValue("", "name");
            try {
                parts = NameChecker.getQNameParts(displayName);
            } catch (QNameException err) {
                throw new XPathException(err);
            }
            String nsuri = element.getAttributeValue("", "nsuri");
            StructuredQName name = new StructuredQName(parts[0], nsuri, parts[1]);
            FingerprintedQName elemName = new FingerprintedQName(name, loader.config.getNamePool());
            String ns = element.getAttributeValue("", "namespaces");
            NamespaceMap bindings = NamespaceMap.emptyMap();
            if (ns != null && !ns.isEmpty()) {
                String[] pairs = ns.split(" ");
                boolean i = false;
                for (String pair : pairs) {
                    int eq = pair.indexOf(61);
                    if (eq >= 0) {
                        String uri;
                        String prefix = pair.substring(0, eq);
                        if (prefix.equals("#")) {
                            prefix = "";
                        }
                        if ((uri = pair.substring(eq + 1)).equals("~")) {
                            uri = NamespaceConstant.getUriForConventionalPrefix(prefix);
                        }
                        bindings = bindings.put(prefix, uri);
                        continue;
                    }
                    RetainedStaticContext rsc = loader.contextStack.peek();
                    String prefix = pair;
                    if (prefix.equals("#")) {
                        prefix = "";
                    }
                    String uri = rsc.getURIForPrefix(prefix, true);
                    assert (uri != null);
                    bindings = bindings.put(prefix, uri);
                }
            }
            int validation = 4;
            String valAtt = element.getAttributeValue("", "validation");
            if (valAtt != null) {
                validation = Validation.getCode(valAtt);
            }
            if ((schemaType = loader.getTypeAttribute(element, "type")) != null) {
                validation = 8;
            }
            Expression content = loader.getFirstChildExpression(element);
            FixedElement elem = new FixedElement(elemName, bindings, true, true, schemaType, validation);
            String flags = element.getAttributeValue("", "flags");
            if (flags != null) {
                elem.setInheritanceFlags(flags);
            }
            elem.setContentExpression(content);
            return elem;
        });
        eMap.put("empty", (loader, element) -> Literal.makeLiteral(EmptySequence.getInstance()));
        eMap.put("emptyTextNodeRemover", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            return new EmptyTextNodeRemover(body);
        });
        eMap.put("error", (loader, element) -> {
            String message = element.getAttributeValue("", "message");
            String code = element.getAttributeValue("", "code");
            boolean isTypeErr = "1".equals(element.getAttributeValue("", "isTypeErr"));
            return new ErrorExpression(message, code, isTypeErr);
        });
        eMap.put("evaluate", (loader, element) -> {
            String namespaces;
            SequenceType required = loader.parseAlphaCode(element, "as");
            Expression xpath = loader.getExpressionWithRole(element, "xpath");
            Expression contextItem = loader.getExpressionWithRole(element, "cxt");
            Expression baseUri = loader.getExpressionWithRole(element, "baseUri");
            Expression namespaceContext = loader.getExpressionWithRole(element, "nsCxt");
            Expression schemaAware = loader.getExpressionWithRole(element, "sa");
            Expression dynamicParams = loader.getExpressionWithRole(element, "wp");
            Expression optionsOp = loader.getExpressionWithRole(element, "options");
            EvaluateInstr inst = new EvaluateInstr(xpath, required, contextItem, baseUri, namespaceContext, schemaAware);
            if (optionsOp != null) {
                inst.setOptionsExpression(optionsOp);
            }
            if ((namespaces = element.getAttributeValue("", "schNS")) != null) {
                String[] uris;
                for (String string : uris = namespaces.split(" ")) {
                    void var16_17;
                    if (string.equals("##")) {
                        String string2 = "";
                    }
                    inst.importSchemaNamespace((String)var16_17);
                }
            }
            NameTest test = new NameTest(1, "http://ns.saxonica.com/xslt/export", "withParam", loader.getConfiguration().getNamePool());
            ArrayList<WithParam> nonTunnelParams = new ArrayList<WithParam>();
            int slotNumber = 0;
            for (NodeInfo nodeInfo : element.children(test)) {
                WithParam withParam = new WithParam();
                StructuredQName paramName = loader.getQNameAttribute(nodeInfo, "name");
                withParam.setVariableQName(paramName);
                withParam.setSlotNumber(slotNumber++);
                SequenceType reqType = loader.parseAlphaCode(nodeInfo, "as");
                withParam.setRequiredType(reqType);
                withParam.setSelectExpression(inst, loader.getFirstChildExpression(nodeInfo));
                nonTunnelParams.add(withParam);
            }
            inst.setActualParameters(nonTunnelParams.toArray(new WithParam[0]));
            if (dynamicParams != null) {
                inst.setDynamicParams(dynamicParams);
            }
            return inst;
        });
        eMap.put("every", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            int slot = loader.getIntegerAttribute(element, "slot");
            StructuredQName name = loader.getQNameAttribute(element, "var");
            SequenceType requiredType = loader.parseAlphaCode(element, "as");
            QuantifiedExpression qEx = new QuantifiedExpression();
            qEx.setOperator(33);
            qEx.setSequence(select);
            qEx.setRequiredType(requiredType);
            qEx.setSlotNumber(slot);
            qEx.setVariableQName(name);
            loader.localBindings.push(qEx);
            Expression action = loader.getSecondChildExpression(element);
            loader.localBindings.pop();
            qEx.setAction(action);
            return qEx;
        });
        eMap.put("except", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new VennExpression(lhs, 24, rhs);
        });
        eMap.put("false", (loader, element) -> Literal.makeLiteral(BooleanValue.FALSE));
        eMap.put("filter", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            String flags = element.getAttributeValue("", "flags");
            FilterExpression fe = new FilterExpression(lhs, rhs);
            fe.setFlags(flags);
            return fe;
        });
        eMap.put("first", (loader, element) -> {
            Expression base = loader.getFirstChildExpression(element);
            return FirstItemExpression.makeFirstItemExpression(base);
        });
        eMap.put("fn", (loader, element) -> {
            Expression e;
            RetainedStaticContext rsc = loader.makeRetainedStaticContext(element);
            loader.contextStack.push(rsc);
            Expression[] args = PackageLoaderHE.getChildExpressionArray(loader, element);
            String name = element.getAttributeValue("", "name");
            if (name.equals("_STRING-JOIN_2.0")) {
                name = "string-join";
            }
            if ((e = SystemFunction.makeCall(name, rsc, args)) == null) {
                throw new XPathException("Unknown system function " + name + "#" + args.length);
            }
            if (e instanceof SystemFunctionCall) {
                NodeInfo att;
                SystemFunction fn = ((SystemFunctionCall)e).getTargetFunction();
                fn.setRetainedStaticContext(rsc);
                AxisIterator iter = element.iterateAxis(2);
                Properties props = new Properties();
                while ((att = (NodeInfo)iter.next()) != null) {
                    props.setProperty(att.getLocalPart(), att.getStringValue());
                }
                fn.importAttributes(props);
                loader.addCompletionAction(() -> fn.fixArguments(args));
            }
            loader.contextStack.pop();
            return e;
        });
        eMap.put("fnCoercer", (loader, element) -> {
            SpecificFunctionType type = (SpecificFunctionType)loader.parseAlphaCode(element, "to").getPrimaryType();
            RoleDiagnostic role = RoleDiagnostic.reconstruct(element.getAttributeValue("", "diag"));
            Expression arg = loader.getFirstChildExpression(element);
            return new FunctionSequenceCoercer(arg, type, role);
        });
        eMap.put("fnRef", (loader, element) -> {
            loader.needsPELicense("higher order functions");
            String name = element.getAttributeValue("", "name");
            int arity = loader.getIntegerAttribute(element, "arity");
            RetainedStaticContext rsc = loader.makeRetainedStaticContext(element);
            SystemFunction f = null;
            if (name.startsWith("Q{")) {
                String uri;
                StructuredQName qName = StructuredQName.fromEQName(name);
                switch (uri = qName.getURI()) {
                    case "http://www.w3.org/2005/xpath-functions/math": {
                        f = MathFunctionSet.getInstance().makeFunction(qName.getLocalPart(), arity);
                        break;
                    }
                    case "http://www.w3.org/2005/xpath-functions/map": {
                        f = MapFunctionSet.getInstance().makeFunction(qName.getLocalPart(), arity);
                        break;
                    }
                    case "http://www.w3.org/2005/xpath-functions/array": {
                        f = ArrayFunctionSet.getInstance().makeFunction(qName.getLocalPart(), arity);
                        break;
                    }
                    case "http://saxon.sf.net/": {
                        f = loader.getConfiguration().bindSaxonExtensionFunction(qName.getLocalPart(), arity);
                    }
                }
            } else {
                f = SystemFunction.makeFunction(name, rsc, arity);
            }
            if (f == null) {
                throw new XPathException("Unknown system function " + name + "#" + arity, "SXPK0002");
            }
            return new FunctionLiteral(f);
        });
        eMap.put("follows", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new IdentityComparison(lhs, 39, rhs);
        });
        eMap.put("for", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            int slot = loader.getIntegerAttribute(element, "slot");
            StructuredQName name = loader.getQNameAttribute(element, "var");
            SequenceType requiredType = loader.parseAlphaCode(element, "as");
            ForExpression forEx = new ForExpression();
            forEx.setSequence(select);
            forEx.setRequiredType(requiredType);
            forEx.setSlotNumber(slot);
            forEx.setVariableQName(name);
            loader.localBindings.push(forEx);
            Expression action = loader.getSecondChildExpression(element);
            loader.localBindings.pop();
            forEx.setAction(action);
            return forEx;
        });
        eMap.put("forEach", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            Expression threads = loader.getExpressionWithRole(element, "threads");
            if (threads == null) {
                return new ForEach(lhs, rhs);
            }
            ForEach forEach = new ForEach(lhs, rhs, false, threads);
            Expression sep = loader.getExpressionWithRole(element, "separator");
            if (sep != null) {
                forEach.setSeparatorExpression(sep);
            }
            return loader.getConfiguration().obtainOptimizer().generateMultithreadedInstruction(forEach);
        });
        eMap.put("forEachGroup", (loader, element) -> {
            byte algo;
            String algorithmAtt = element.getAttributeValue("", "algorithm");
            if ("by".equals(algorithmAtt)) {
                algo = 0;
            } else if ("adjacent".equals(algorithmAtt)) {
                algo = 1;
            } else if ("starting".equals(algorithmAtt)) {
                algo = 2;
            } else if ("ending".equals(algorithmAtt)) {
                algo = 3;
            } else {
                throw new AssertionError();
            }
            String flags = element.getAttributeValue("", "flags");
            boolean composite = flags != null && flags.contains("c");
            boolean inFork = flags != null && flags.contains("k");
            Expression select = loader.getExpressionWithRole(element, "select");
            Expression key = algo == 0 || algo == 1 ? loader.getExpressionWithRole(element, "key") : loader.getPatternWithRole(element, "match");
            SortKeyDefinitionList sortKeys = loader.loadSortKeyDefinitions(element);
            if (sortKeys.size() == 0) {
                sortKeys = null;
            }
            Expression collationNameExp = loader.getExpressionWithRole(element, "collation");
            Expression content = loader.getExpressionWithRole(element, "content");
            StringCollator collator = null;
            if (collationNameExp instanceof StringLiteral) {
                String collationName = ((StringLiteral)collationNameExp).getStringValue();
                collator = loader.config.getCollation(collationName);
            }
            ForEachGroup feg = new ForEachGroup(select, content, algo, key, collator, collationNameExp, sortKeys);
            feg.setComposite(composite);
            feg.setIsInFork(inFork);
            return feg;
        });
        eMap.put("fork", (loader, element) -> {
            Expression[] args = PackageLoaderHE.getChildExpressionArray(loader, element);
            return new Fork(args);
        });
        eMap.put("gc", (loader, element) -> {
            String opAtt = element.getAttributeValue("", "op");
            int op = PackageLoaderHE.getOperator(opAtt);
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            String compAtt = element.getAttributeValue("", "comp");
            AtomicComparer comp = loader.makeAtomicComparer(compAtt, element);
            GeneralComparison20 gc = new GeneralComparison20(lhs, op, rhs);
            gc.setAtomicComparer(comp);
            return gc;
        });
        eMap.put("gc10", (loader, element) -> {
            String opAtt = element.getAttributeValue("", "op");
            int op = PackageLoaderHE.getOperator(opAtt);
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            String compAtt = element.getAttributeValue("", "comp");
            GeneralComparison10 gc = new GeneralComparison10(lhs, op, rhs);
            AtomicComparer comp = loader.makeAtomicComparer(compAtt, element);
            gc.setAtomicComparer(comp);
            return gc;
        });
        eMap.put("gVarRef", (loader, element) -> {
            StructuredQName name = loader.getQNameAttribute(element, "name");
            GlobalVariableReference ref = new GlobalVariableReference(name);
            int bindingSlot = loader.getIntegerAttribute(element, "bSlot");
            ref.setBindingSlot(bindingSlot);
            loader.fixups.peek().add(ref);
            return ref;
        });
        eMap.put("homCheck", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            return new HomogeneityChecker(body);
        });
        eMap.put("ifCall", (loader, element) -> {
            Expression[] args = PackageLoaderHE.getChildExpressionArray(loader, element);
            StructuredQName name = loader.getQNameAttribute(element, "name");
            Expression exp = null;
            if (name.hasURI("http://www.w3.org/2005/xpath-functions/math")) {
                exp = MathFunctionSet.getInstance().makeFunction(name.getLocalPart(), args.length).makeFunctionCall(args);
            } else if (name.hasURI("http://www.w3.org/2005/xpath-functions/map")) {
                exp = MapFunctionSet.getInstance().makeFunction(name.getLocalPart(), args.length).makeFunctionCall(args);
            } else if (name.hasURI("http://www.w3.org/2005/xpath-functions/array")) {
                exp = ArrayFunctionSet.getInstance().makeFunction(name.getLocalPart(), args.length).makeFunctionCall(args);
            } else if (name.hasURI("http://saxon.sf.net/")) {
                loader.needsPELicense("Saxon extension functions");
                exp = null;
            }
            if (exp == null) {
                SymbolicName.F sName = new SymbolicName.F(name, args.length);
                SequenceType type = loader.parseAlphaCode(element, "type");
                IndependentContext ic = new IndependentContext(loader.config);
                RetainedStaticContext rsc = loader.makeRetainedStaticContext(element);
                ic.setBaseURI(rsc.getStaticBaseUriString());
                ic.setPackageData(rsc.getPackageData());
                ic.setXPathLanguageLevel(31);
                ic.setDefaultElementNamespace(rsc.getDefaultElementNamespace());
                ic.setNamespaceResolver(rsc);
                ic.setBackwardsCompatibilityMode(rsc.isBackwardsCompatibility());
                ic.setDefaultCollationName(rsc.getDefaultCollationName());
                ic.setDefaultFunctionNamespace(rsc.getDefaultFunctionNamespace());
                ic.setDecimalFormatManager(rsc.getDecimalFormatManager());
                ArrayList<String> reasons = new ArrayList<String>();
                exp = loader.config.getIntegratedFunctionLibrary().bind(sName, args, ic, reasons);
                if (exp == null) {
                    exp = loader.config.getBuiltInExtensionLibraryList().bind(sName, args, ic, reasons);
                }
                if (exp instanceof SystemFunctionCall) {
                    NodeInfo att;
                    SystemFunction fn = ((SystemFunctionCall)exp).getTargetFunction();
                    fn.setRetainedStaticContext(loader.makeRetainedStaticContext(element));
                    AxisIterator iter = element.iterateAxis(2);
                    Properties props = new Properties();
                    while ((att = (NodeInfo)iter.next()) != null) {
                        props.setProperty(att.getLocalPart(), att.getStringValue());
                    }
                    fn.importAttributes(props);
                }
                if (exp == null) {
                    StringBuilder msg = new StringBuilder("IntegratedFunctionCall to " + sName + " not found");
                    for (String reason : reasons) {
                        msg.append(". ").append(reason);
                    }
                    throw new XPathException(msg.toString());
                }
                if (exp instanceof IntegratedFunctionCall) {
                    ((IntegratedFunctionCall)exp).getFunction().supplyStaticContext(ic, -1, args);
                    ((IntegratedFunctionCall)exp).setResultType(type);
                }
            }
            return exp;
        });
        eMap.put("inlineFn", (loader, element) -> {
            NodeInfo first = loader.getChild(element, 0);
            UserFunction uf = loader.readFunction(first);
            return new UserFunctionReference(uf);
        });
        eMap.put("instance", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            SequenceType type = loader.parseAlphaCode(element, "of");
            return new InstanceOfExpression(body, type);
        });
        eMap.put("int", (loader, element) -> {
            BigInteger i = new BigInteger(element.getAttributeValue("", "val"));
            return Literal.makeLiteral(IntegerValue.makeIntegerValue(i));
        });
        eMap.put("intersect", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new VennExpression(lhs, 23, rhs);
        });
        eMap.put("intRangeTest", (loader, element) -> {
            Expression val = loader.getFirstChildExpression(element);
            Expression min = loader.getSecondChildExpression(element);
            Expression max = loader.getNthChildExpression(element, 2);
            return new IntegerRangeTest(val, min, max);
        });
        eMap.put("is", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new IdentityComparison(lhs, 20, rhs);
        });
        eMap.put("isLast", (loader, element) -> {
            boolean cond = element.getAttributeValue("", "test").equals("1");
            return new IsLastExpression(cond);
        });
        eMap.put("iterate", (loader, element) -> {
            Expression select = loader.getExpressionWithRole(element, "select");
            LocalParamBlock params = (LocalParamBlock)loader.getExpressionWithRole(element, "params");
            Expression onCompletion = loader.getExpressionWithRole(element, "on-completion");
            Expression action = loader.getExpressionWithRole(element, "action");
            return new IterateInstr(select, params, action, onCompletion);
        });
        eMap.put("lastOf", (loader, element) -> {
            Expression base = loader.getFirstChildExpression(element);
            return new LastItemExpression(base);
        });
        eMap.put("let", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            int slot = loader.getIntegerAttribute(element, "slot");
            int evalMode = loader.getIntegerAttribute(element, "eval");
            StructuredQName name = loader.getQNameAttribute(element, "var");
            SequenceType requiredType = loader.parseAlphaCode(element, "as");
            LetExpression let = new LetExpression();
            let.setSequence(select);
            let.setRequiredType(requiredType);
            let.setSlotNumber(slot);
            let.setVariableQName(name);
            let.setEvaluationMode(EvaluationMode.forCode(evalMode));
            loader.localBindings.push(let);
            Expression action = loader.getSecondChildExpression(element);
            loader.localBindings.pop();
            let.setAction(action);
            return let;
        });
        eMap.put("literal", (loader, element) -> {
            NodeInfo child;
            ArrayList<Item> children = new ArrayList<Item>();
            AxisIterator iter = element.iterateAxis(3, NodeKindTest.ELEMENT);
            while ((child = iter.next()) != null) {
                Expression e = loader.loadExpression(child);
                children.add(((Literal)e).getValue().head());
            }
            return Literal.makeLiteral(SequenceExtent.makeSequenceExtent(children));
        });
        eMap.put("lookup", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            Expression key = loader.getSecondChildExpression(element);
            return new LookupExpression(select, key);
        });
        eMap.put("lookupAll", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            return new LookupAllExpression(select);
        });
        eMap.put("map", (loader, element) -> {
            List<Expression> children = PackageLoaderHE.getChildExpressionList(loader, element);
            AtomicValue key = null;
            HashTrieMap map = new HashTrieMap();
            for (Expression child : children) {
                if (key == null) {
                    key = (AtomicValue)((Literal)child).getValue();
                    continue;
                }
                GroundedValue value = ((Literal)child).getValue();
                map.initialPut(key, value);
                key = null;
            }
            return Literal.makeLiteral(map);
        });
        eMap.put("merge", (loader, element) -> {
            NodeInfo msElem;
            MergeInstr inst = new MergeInstr();
            AxisIterator kids = element.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "mergeSrc", loader.config.getNamePool()));
            ArrayList<MergeInstr.MergeSource> list = new ArrayList<MergeInstr.MergeSource>();
            while ((msElem = kids.next()) != null) {
                Expression selectRows;
                Expression forEachStream;
                SchemaType schemaType;
                String valAtt;
                final MergeInstr.MergeSource ms = new MergeInstr.MergeSource(inst);
                String mergeSourceName = msElem.getAttributeValue("", "name");
                if (mergeSourceName != null) {
                    ms.sourceName = mergeSourceName;
                }
                if ((valAtt = msElem.getAttributeValue("", "validation")) != null) {
                    ms.validation = Validation.getCode(valAtt);
                }
                if ((schemaType = loader.getTypeAttribute(msElem, "type")) != null) {
                    ms.schemaType = schemaType;
                    ms.validation = 8;
                }
                String flagsAtt = msElem.getAttributeValue("", "flags");
                ms.streamable = "s".equals(flagsAtt);
                if (ms.streamable) {
                    loader.addCompletionAction(ms::prepareForStreaming);
                }
                RetainedStaticContext rsc = loader.makeRetainedStaticContext(element);
                ms.baseURI = rsc.getStaticBaseUriString();
                String accumulatorNames = msElem.getAttributeValue("", "accum");
                if (accumulatorNames == null) {
                    accumulatorNames = "";
                }
                final ArrayList<StructuredQName> accNameList = new ArrayList<StructuredQName>();
                StringTokenizer tokenizer = new StringTokenizer(accumulatorNames);
                while (tokenizer.hasMoreTokens()) {
                    String token = tokenizer.nextToken();
                    StructuredQName name = StructuredQName.fromEQName(token);
                    accNameList.add(name);
                }
                loader.addCompletionAction(new Action(){
                    final StylesheetPackage pack;
                    {
                        this.pack = loader.getPackStack().peek();
                    }

                    @Override
                    public void doAction() {
                        HashSet<Accumulator> list = new HashSet<Accumulator>();
                        for (StructuredQName sn : accNameList) {
                            for (Accumulator test : this.pack.getAccumulatorRegistry().getAllAccumulators()) {
                                if (!test.getAccumulatorName().equals(sn)) continue;
                                list.add(test);
                            }
                        }
                        ms.accumulators = list;
                    }
                });
                Expression forEachItem = loader.getExpressionWithRole(msElem, "forEachItem");
                if (forEachItem != null) {
                    ms.initForEachItem(inst, forEachItem);
                }
                if ((forEachStream = loader.getExpressionWithRole(msElem, "forEachStream")) != null) {
                    ms.initForEachStream(inst, forEachStream);
                }
                if ((selectRows = loader.getExpressionWithRole(msElem, "selectRows")) != null) {
                    ms.initRowSelect(inst, selectRows);
                }
                SortKeyDefinitionList keys = loader.loadSortKeyDefinitions(msElem);
                ms.setMergeKeyDefinitionSet(keys);
                list.add(ms);
            }
            Expression mergeAction = loader.getExpressionWithRole(element, "action");
            MergeInstr.MergeSource[] mergeSources = list.toArray(new MergeInstr.MergeSource[0]);
            inst.init(mergeSources, mergeAction);
            loader.completionActions.add(inst::fixupGroupReferences);
            return inst;
        });
        eMap.put("mergeAdj", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            return new AdjacentTextNodeMerger(body);
        });
        eMap.put("message", (loader, element) -> {
            Expression select = loader.getExpressionWithRole(element, "select");
            Expression terminate = loader.getExpressionWithRole(element, "terminate");
            Expression error = loader.getExpressionWithRole(element, "error");
            return new Message(select, terminate, error);
        });
        eMap.put("minus", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            return new NegateExpression(body);
        });
        eMap.put("namespace", (loader, element) -> {
            Expression name = loader.getFirstChildExpression(element);
            Expression select = loader.getSecondChildExpression(element);
            NamespaceConstructor inst = new NamespaceConstructor(name);
            inst.setSelect(select);
            return inst;
        });
        eMap.put("nextIteration", (loader, element) -> {
            NodeInfo wp;
            NextIteration inst = new NextIteration();
            AxisIterator kids = element.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "withParam", loader.config.getNamePool()));
            ArrayList<WithParam> params = new ArrayList<WithParam>();
            while ((wp = kids.next()) != null) {
                WithParam withParam = new WithParam();
                String flags = wp.getAttributeValue("", "flags");
                StructuredQName paramName = loader.getQNameAttribute(wp, "name");
                withParam.setVariableQName(paramName);
                int slot = loader.getIntegerAttribute(wp, "slot");
                withParam.setSlotNumber(slot);
                withParam.setRequiredType(SequenceType.ANY_SEQUENCE);
                withParam.setSelectExpression(inst, loader.getFirstChildExpression(wp));
                withParam.setRequiredType(loader.parseAlphaCode(wp, "as"));
                withParam.setTypeChecked(flags != null && flags.contains("c"));
                params.add(withParam);
            }
            inst.setParameters(params.toArray(new WithParam[0]));
            return inst;
        });
        eMap.put("nextMatch", (loader, element) -> {
            String flags = element.getAttributeValue("", "flags");
            boolean useTailRecursion = false;
            if (flags != null && flags.contains("t")) {
                useTailRecursion = true;
            }
            NextMatch inst = new NextMatch(useTailRecursion);
            WithParam[] actuals = loader.loadWithParams(element, inst, false);
            WithParam[] tunnels = loader.loadWithParams(element, inst, true);
            inst.setActualParams(actuals);
            inst.setTunnelParams(tunnels);
            return inst;
        });
        eMap.put("node", (loader, element) -> {
            NodeInfo node;
            int kind = loader.getIntegerAttribute(element, "kind");
            String content = element.getAttributeValue("", "content");
            String baseURI = element.getAttributeValue("", "baseURI");
            switch (kind) {
                case 1: 
                case 9: {
                    StreamSource source = new StreamSource(new StringReader(content), baseURI);
                    node = loader.config.buildDocumentTree(source).getRootNode();
                    if (kind != 1) break;
                    node = VirtualCopy.makeVirtualCopy(node.iterateAxis(3, NodeKindTest.ELEMENT).next());
                    break;
                }
                case 3: 
                case 8: {
                    Orphan o = new Orphan(loader.getConfiguration());
                    o.setNodeKind((short)kind);
                    o.setStringValue(content);
                    node = o;
                    break;
                }
                default: {
                    Orphan o = new Orphan(loader.getConfiguration());
                    o.setNodeKind((short)kind);
                    o.setStringValue(content);
                    String prefix = element.getAttributeValue("", "prefix");
                    String ns = element.getAttributeValue("", "ns");
                    String local = element.getAttributeValue("", "localName");
                    if (local != null) {
                        FingerprintedQName name = new FingerprintedQName(prefix == null ? "" : prefix, ns == null ? "" : ns, local);
                        o.setNodeName(name);
                    }
                    node = o;
                    break;
                }
            }
            return Literal.makeLiteral(new One<NodeInfo>(node));
        });
        eMap.put("nodeNum", (loader, element) -> {
            String levelAtt = element.getAttributeValue("", "level");
            int level = PackageLoaderHE.getLevelCode(levelAtt);
            Expression select = loader.getExpressionWithRole(element, "select");
            Pattern count = loader.getPatternWithRole(element, "count");
            Pattern from = loader.getPatternWithRole(element, "from");
            return new NumberInstruction(select, level, count, from);
        });
        eMap.put("numSeqFmt", (loader, element) -> {
            Expression value = loader.getExpressionWithRole(element, "value");
            Expression format = loader.getExpressionWithRole(element, "format");
            if (format == null) {
                format = new StringLiteral("1");
            }
            Expression groupSize = loader.getExpressionWithRole(element, "gpSize");
            Expression groupSeparator = loader.getExpressionWithRole(element, "gpSep");
            Expression letterValue = loader.getExpressionWithRole(element, "letterValue");
            Expression ordinal = loader.getExpressionWithRole(element, "ordinal");
            Expression startAt = loader.getExpressionWithRole(element, "startAt");
            Expression lang = loader.getExpressionWithRole(element, "lang");
            String flags = element.getAttributeValue("", "flags");
            boolean backwardsCompatible = flags != null && flags.contains("1");
            NumberFormatter formatter = null;
            NumberSequenceFormatter ni = new NumberSequenceFormatter(value, format, groupSize, groupSeparator, letterValue, ordinal, startAt, lang, formatter, backwardsCompatible);
            ni.preallocateNumberer(loader.config);
            return ni;
        });
        eMap.put("onEmpty", (loader, element) -> {
            Expression base = loader.getFirstChildExpression(element);
            return new OnEmptyExpr(base);
        });
        eMap.put("onNonEmpty", (loader, element) -> {
            Expression base = loader.getFirstChildExpression(element);
            return new OnNonEmptyExpr(base);
        });
        eMap.put("or", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new OrExpression(lhs, rhs);
        });
        eMap.put("origF", (loader, element) -> {
            StructuredQName name = loader.getQNameAttribute(element, "name");
            String packKey = element.getAttributeValue("", "pack");
            StylesheetPackage declPack = loader.allPackages.get(packKey);
            if (declPack == null) {
                throw new XPathException("Unknown package key " + packKey);
            }
            int arity = loader.getIntegerAttribute(element, "arity");
            SymbolicName.F sn = new SymbolicName.F(name, arity);
            Component target = declPack.getComponent(sn);
            OriginalFunction orig = new OriginalFunction(target);
            return new FunctionLiteral(orig);
        });
        eMap.put("origFC", (loader, element) -> {
            StructuredQName name = loader.getQNameAttribute(element, "name");
            String packKey = element.getAttributeValue("", "pack");
            StylesheetPackage declPack = loader.allPackages.get(packKey);
            if (declPack == null) {
                throw new XPathException("Unknown package key " + packKey);
            }
            Expression[] args = PackageLoaderHE.getChildExpressionArray(loader, element);
            int arity = args.length;
            SymbolicName.F sn = new SymbolicName.F(name, arity);
            Component target = declPack.getComponent(sn);
            OriginalFunction orig = new OriginalFunction(target);
            return new StaticFunctionCall(orig, args);
        });
        eMap.put("param", (loader, element) -> {
            Expression convert;
            StructuredQName name = loader.getQNameAttribute(element, "name");
            int slot = loader.getIntegerAttribute(element, "slot");
            LocalParam param = new LocalParam();
            param.setVariableQName(name);
            param.setSlotNumber(slot);
            Expression select = loader.getExpressionWithRole(element, "select");
            if (select != null) {
                param.setSelectExpression(select);
                param.computeEvaluationMode();
            }
            if ((convert = loader.getExpressionWithRole(element, "conversion")) != null) {
                param.setConversion(convert);
            }
            param.setRequiredType(loader.parseAlphaCode(element, "as"));
            String flags = element.getAttributeValue("", "flags");
            if (flags != null) {
                param.setTunnel(flags.contains("t"));
                param.setRequiredParam(flags.contains("r"));
                param.setImplicitlyRequiredParam(flags.contains("i"));
            }
            loader.localBindings.add(param);
            return param;
        });
        eMap.put("params", (loader, element) -> {
            NodeInfo child;
            ArrayList<LocalParam> children = new ArrayList<LocalParam>();
            AxisIterator iter = element.iterateAxis(3, NodeKindTest.ELEMENT);
            while ((child = iter.next()) != null) {
                children.add((LocalParam)loader.loadExpression(child));
            }
            return new LocalParamBlock(children.toArray(new LocalParam[0]));
        });
        eMap.put("partialApply", (loader, element) -> {
            int count = Count.count(element.iterateAxis(3, NodeKindTest.ELEMENT));
            Expression base = null;
            Expression[] args = new Expression[count - 1];
            count = 0;
            for (NodeInfo nodeInfo : element.children(NodeKindTest.ELEMENT)) {
                if (count == 0) {
                    base = loader.loadExpression(nodeInfo);
                } else {
                    args[count - 1] = nodeInfo.getLocalPart().equals("null") ? null : loader.loadExpression(nodeInfo);
                }
                ++count;
            }
            return new PartialApply(base, args);
        });
        eMap.put("precedes", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new IdentityComparison(lhs, 38, rhs);
        });
        eMap.put("procInst", (loader, element) -> {
            Expression name = loader.getFirstChildExpression(element);
            Expression select = loader.getSecondChildExpression(element);
            ProcessingInstruction inst = new ProcessingInstruction(name);
            inst.setSelect(select);
            return inst;
        });
        eMap.put("qName", (loader, element) -> {
            QualifiedNameValue val;
            String preAtt = element.getAttributeValue("", "pre");
            String uriAtt = element.getAttributeValue("", "uri");
            String locAtt = element.getAttributeValue("", "loc");
            AtomicType type = BuiltInAtomicType.QNAME;
            if (element.getAttributeValue("", "type") != null) {
                type = (AtomicType)loader.parseItemTypeAttribute(element, "type");
            }
            if (type.getPrimitiveType() == 530) {
                val = new QNameValue(preAtt, uriAtt, locAtt, type, false);
            } else {
                val = new NotationValue(preAtt, uriAtt, locAtt, null);
                val.setTypeLabel(type);
            }
            return Literal.makeLiteral(val);
        });
        eMap.put("range", (loader, element) -> {
            int from = loader.getIntegerAttribute(element, "from");
            int to = loader.getIntegerAttribute(element, "to");
            return Literal.makeLiteral(new IntegerRange(from, to));
        });
        eMap.put("resultDoc", (loader, element) -> {
            NodeInfo child;
            loader.packStack.peek().setCreatesSecondaryResultDocuments(true);
            Expression href = null;
            Expression format = null;
            Expression content = null;
            String globalProps = element.getAttributeValue("", "global");
            String localProps = element.getAttributeValue("", "local");
            Properties globals = globalProps == null ? new Properties() : loader.importProperties(globalProps);
            Properties locals = localProps == null ? new Properties() : loader.importProperties(localProps);
            HashMap<StructuredQName, Expression> dynamicProperties = new HashMap<StructuredQName, Expression>();
            AxisIterator iter = element.iterateAxis(3, NodeKindTest.ELEMENT);
            while ((child = iter.next()) != null) {
                Expression exp = loader.loadExpression(child);
                String role = child.getAttributeValue("", "role");
                if ("href".equals(role)) {
                    href = exp;
                    continue;
                }
                if ("format".equals(role)) {
                    format = exp;
                    continue;
                }
                if ("content".equals(role)) {
                    content = exp;
                    continue;
                }
                StructuredQName name = StructuredQName.fromEQName(role);
                dynamicProperties.put(name, exp);
            }
            int validation = 4;
            String valAtt = element.getAttributeValue("", "validation");
            if (valAtt != null) {
                validation = Validation.getCode(valAtt);
            }
            SchemaType schemaType = null;
            StructuredQName typeAtt = loader.getQNameAttribute(element, "type");
            if (typeAtt != null) {
                schemaType = loader.config.getSchemaType(typeAtt);
                validation = 8;
            }
            ResultDocument instr = new ResultDocument(globals, locals, href, format, validation, schemaType, dynamicProperties, loader.packStack.peek().getCharacterMapIndex());
            instr.setContentExpression(content);
            if ("a".equals(element.getAttributeValue("", "flags"))) {
                instr.setAsynchronous(true);
            }
            return instr;
        });
        eMap.put("root", (loader, element) -> new RootExpression());
        eMap.put("saxonDoctype", (loader, element) -> {
            Expression arg = loader.getFirstChildExpression(element);
            return new Doctype(arg);
        });
        eMap.put("sequence", (loader, element) -> {
            Expression[] args = PackageLoaderHE.getChildExpressionArray(loader, element);
            return new Block(args);
        });
        eMap.put("slash", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            String simpleAtt = element.getAttributeValue("", "simple");
            if ("1".equals(simpleAtt)) {
                return new SimpleStepExpression(lhs, rhs);
            }
            SlashExpression se = new SlashExpression(lhs, rhs);
            if ("2".equals(simpleAtt)) {
                se.setContextFree(true);
            }
            return se;
        });
        eMap.put("some", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            int slot = loader.getIntegerAttribute(element, "slot");
            StructuredQName name = loader.getQNameAttribute(element, "var");
            SequenceType requiredType = loader.parseAlphaCode(element, "as");
            QuantifiedExpression qEx = new QuantifiedExpression();
            qEx.setOperator(32);
            qEx.setSequence(select);
            qEx.setRequiredType(requiredType);
            qEx.setSlotNumber(slot);
            qEx.setVariableQName(name);
            loader.localBindings.push(qEx);
            Expression action = loader.getSecondChildExpression(element);
            loader.localBindings.pop();
            qEx.setAction(action);
            return qEx;
        });
        eMap.put("sort", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            SortKeyDefinitionList sortKeys = loader.loadSortKeyDefinitions(element);
            return new SortExpression(body, sortKeys);
        });
        eMap.put("sourceDoc", (loader, element) -> {
            int valSpecified = loader.getIntegerAttribute(element, "validation");
            int validation = valSpecified == Integer.MIN_VALUE ? 4 : valSpecified;
            SchemaType schemaType = null;
            StructuredQName typeAtt = loader.getQNameAttribute(element, "schemaType");
            if (typeAtt != null) {
                schemaType = loader.getConfiguration().getSchemaType(typeAtt);
                validation = 8;
            }
            ParseOptions options = new ParseOptions(loader.getConfiguration().getParseOptions());
            options.setSchemaValidationMode(validation);
            options.setTopLevelType(schemaType);
            String flags = element.getAttributeValue("", "flags");
            if (flags != null) {
                if (flags.contains("s")) {
                    loader.addCompletionAction(() -> options.setSpaceStrippingRule(loader.getPackage().getSpaceStrippingRule()));
                }
                if (flags.contains("l")) {
                    options.setLineNumbering(true);
                }
                if (flags.contains("a")) {
                    options.setExpandAttributeDefaults(true);
                }
                if (flags.contains("d")) {
                    options.setDTDValidationMode(1);
                }
                if (flags.contains("i")) {
                    options.setXIncludeAware(true);
                }
            }
            Expression body = loader.getExpressionWithRole(element, "body");
            Expression href = loader.getExpressionWithRole(element, "href");
            SourceDocument inst = new SourceDocument(href, body, options);
            String accumulatorNames = element.getAttributeValue("", "accum");
            PackageLoaderHE.processAccumulatorList(loader, inst, accumulatorNames);
            return inst;
        });
        eMap.put("str", (loader, element) -> StringLiteral.makeLiteral(new StringValue(element.getAttributeValue("", "val"))));
        eMap.put("subscript", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new SubscriptExpression(lhs, rhs);
        });
        eMap.put("supplied", (loader, element) -> {
            int slot = loader.getIntegerAttribute(element, "slot");
            return new SuppliedParameterReference(slot);
        });
        eMap.put("tail", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            int start = loader.getIntegerAttribute(element, "start");
            return new TailExpression(select, start);
        });
        eMap.put("tailCallLoop", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            return new TailCallLoop(loader.currentFunction, body);
        });
        eMap.put("to", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new RangeExpression(lhs, rhs);
        });
        eMap.put("treat", (loader, element) -> {
            Expression body = loader.getFirstChildExpression(element);
            ItemType type = loader.parseAlphaCodeForItemType(element, "as");
            RoleDiagnostic role = RoleDiagnostic.reconstruct(element.getAttributeValue("", "diag"));
            return new ItemChecker(body, type, role);
        });
        eMap.put("true", (loader, element) -> Literal.makeLiteral(BooleanValue.TRUE));
        eMap.put("try", (loader, element) -> {
            NodeInfo catchElement;
            Expression tryExp = loader.getFirstChildExpression(element);
            TryCatch tryCatch = new TryCatch(tryExp);
            if ("r".equals(element.getAttributeValue("", "flags"))) {
                tryCatch.setRollbackOutput(true);
            }
            AxisIterator iter = element.iterateAxis(3, new NameTest(1, "http://ns.saxonica.com/xslt/export", "catch", loader.config.getNamePool()));
            NamePool pool = loader.getConfiguration().getNamePool();
            while ((catchElement = iter.next()) != null) {
                String errAtt = catchElement.getAttributeValue("", "errors");
                String[] tests = errAtt.split(" ");
                ArrayList<QNameTest> list = new ArrayList<QNameTest>();
                for (String t : tests) {
                    if (t.equals("*")) {
                        list.add(AnyNodeTest.getInstance());
                        continue;
                    }
                    if (t.startsWith("*:")) {
                        list.add(new LocalNameTest(pool, 1, t.substring(2)));
                        continue;
                    }
                    if (t.endsWith("}*")) {
                        list.add(new NamespaceTest(pool, 1, t.substring(2, t.length() - 2)));
                        continue;
                    }
                    StructuredQName qName = StructuredQName.fromEQName(t);
                    list.add(new NameTest(1, new FingerprintedQName(qName, pool), pool));
                }
                QNameTest test = list.size() == 1 ? (QNameTest)list.get(0) : new UnionQNameTest(list);
                Expression catchExpr = loader.getFirstChildExpression(catchElement);
                tryCatch.addCatchExpression(test, catchExpr);
            }
            return tryCatch;
        });
        eMap.put("ufCall", (loader, element) -> {
            Expression[] args = PackageLoaderHE.getChildExpressionArray(loader, element);
            StructuredQName name = loader.getQNameAttribute(element, "name");
            UserFunctionCall call = new UserFunctionCall();
            call.setFunctionName(name);
            call.setArguments(args);
            int bindingSlot = loader.getIntegerAttribute(element, "bSlot");
            call.setBindingSlot(bindingSlot);
            String eval = element.getAttributeValue("", "eval");
            if (eval != null) {
                String[] evals = eval.split(" ");
                EvaluationMode[] evalModes = new EvaluationMode[evals.length];
                for (int i = 0; i < evals.length; ++i) {
                    evalModes[i] = EvaluationMode.forCode(Integer.parseInt(evals[i]));
                }
                call.setArgumentEvaluationModes(evalModes);
            }
            loader.fixups.peek().add(call);
            return call;
        });
        eMap.put("ufRef", (loader, element) -> {
            StructuredQName name = loader.getQNameAttribute(element, "name");
            int arity = loader.getIntegerAttribute(element, "arity");
            SymbolicName.F symbolicName = new SymbolicName.F(name, arity);
            UserFunctionReference call = new UserFunctionReference(symbolicName);
            int bindingSlot = loader.getIntegerAttribute(element, "bSlot");
            call.setBindingSlot(bindingSlot);
            loader.fixups.peek().add(call);
            return call;
        });
        eMap.put("union", (loader, element) -> {
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            return new VennExpression(lhs, 1, rhs);
        });
        eMap.put("useAS", (loader, element) -> {
            StructuredQName name = loader.getQNameAttribute(element, "name");
            boolean streamable = "s".equals(element.getAttributeValue("", "flags"));
            UseAttributeSet use = new UseAttributeSet(name, streamable);
            int bindingSlot = loader.getIntegerAttribute(element, "bSlot");
            use.setBindingSlot(bindingSlot);
            loader.fixups.peek().add(use);
            return use;
        });
        eMap.put("valueOf", (loader, element) -> {
            Expression select = loader.getFirstChildExpression(element);
            String flags = element.getAttributeValue("", "flags");
            boolean doe = flags != null && flags.contains("d");
            boolean notIfEmpty = flags != null && flags.contains("e");
            return new ValueOf(select, doe, notIfEmpty);
        });
        eMap.put("varRef", (loader, element) -> {
            StructuredQName name = loader.getQNameAttribute(element, "name");
            Stack<LocalBinding> locals = loader.localBindings;
            LocalBinding binding = null;
            for (int i = locals.size() - 1; i >= 0; --i) {
                LocalBinding b = (LocalBinding)locals.get(i);
                if (!b.getVariableQName().equals(name)) continue;
                binding = b;
                break;
            }
            if (binding == null) {
                throw new XPathException("No binding found for local variable " + name);
            }
            int slot = loader.getIntegerAttribute(element, "slot");
            LocalVariableReference ref = new LocalVariableReference(binding);
            ref.setSlotNumber(slot);
            return ref;
        });
        eMap.put("vc", (loader, element) -> {
            String opAtt = element.getAttributeValue("", "op");
            int op = PackageLoaderHE.parseValueComparisonOperator(opAtt);
            Expression lhs = loader.getFirstChildExpression(element);
            Expression rhs = loader.getSecondChildExpression(element);
            ValueComparison vc = new ValueComparison(lhs, op, rhs);
            String compAtt = element.getAttributeValue("", "comp");
            AtomicComparer comp = loader.makeAtomicComparer(compAtt, element);
            vc.setAtomicComparer(comp);
            String onEmptyAtt = element.getAttributeValue("", "onEmpty");
            if (onEmptyAtt != null) {
                vc.setResultWhenEmpty(BooleanValue.get("1".equals(onEmptyAtt)));
            }
            return vc;
        });
        pMap = new HashMap<String, PatternLoader>(200);
        pMap.put("p.anchor", (loader, element) -> AnchorPattern.getInstance());
        pMap.put("p.any", (loader, element) -> new UniversalPattern());
        pMap.put("p.booleanExp", (loader, element) -> {
            Expression exp = loader.getFirstChildExpression(element);
            return new BooleanExpressionPattern(exp);
        });
        pMap.put("p.genNode", (loader, element) -> {
            NodeTest type = (NodeTest)loader.parseAlphaCodeForItemType(element, "test");
            Expression exp = loader.getFirstChildExpression(element);
            return new GeneralNodePattern(exp, type);
        });
        pMap.put("p.genPos", (loader, element) -> {
            NodeTest type = (NodeTest)loader.parseAlphaCodeForItemType(element, "test");
            Expression exp = loader.getFirstChildExpression(element);
            String flags = element.getAttributeValue("", "flags");
            GeneralPositionalPattern gpp = new GeneralPositionalPattern(type, exp);
            gpp.setUsesPosition(!"P".equals(flags));
            return gpp;
        });
        pMap.put("p.nodeSet", (loader, element) -> {
            ItemType type = loader.parseAlphaCodeForItemType(element, "test");
            Expression select = loader.getFirstChildExpression(element);
            NodeSetPattern pat = new NodeSetPattern(select);
            pat.setItemType(type);
            return pat;
        });
        pMap.put("p.nodeTest", (loader, element) -> {
            ItemType test = loader.parseAlphaCodeForItemType(element, "test");
            if (test instanceof NodeTest) {
                return new NodeTestPattern((NodeTest)test);
            }
            return new ItemTypePattern(test);
        });
        pMap.put("p.venn", (loader, element) -> {
            String operator;
            Pattern p0 = loader.getFirstChildPattern(element);
            Pattern p1 = loader.getSecondChildPattern(element);
            switch (operator = element.getAttributeValue("", "op")) {
                case "union": {
                    return new UnionPattern(p0, p1);
                }
                case "intersect": {
                    return new IntersectPattern(p0, p1);
                }
                case "except": {
                    return new ExceptPattern(p0, p1);
                }
            }
            return null;
        });
        pMap.put("p.simPos", (loader, element) -> {
            NodeTest test = (NodeTest)loader.parseAlphaCodeForItemType(element, "test");
            int pos = loader.getIntegerAttribute(element, "pos");
            return new SimplePositionalPattern(test, pos);
        });
        pMap.put("p.withCurrent", (loader, element) -> {
            LocalVariableBinding let = new LocalVariableBinding(Current.FN_CURRENT, SequenceType.SINGLE_ITEM);
            let.setSlotNumber(0);
            loader.localBindings.push(let);
            Pattern p0 = loader.getFirstChildPattern(element);
            loader.localBindings.pop();
            return new PatternThatSetsCurrent(p0, let);
        });
        pMap.put("p.withUpper", (loader, element) -> {
            String axisName = element.getAttributeValue("", "axis");
            int axis = AxisInfo.getAxisNumber(axisName);
            Pattern basePattern = loader.getFirstChildPattern(element);
            Pattern upperPattern = loader.getSecondChildPattern(element);
            return new AncestorQualifiedPattern(basePattern, upperPattern, axis);
        });
        pMap.put("p.withPredicate", (loader, element) -> {
            Pattern basePattern = loader.getFirstChildPattern(element);
            Expression predicate = loader.getSecondChildExpression(element);
            return new BasePatternWithPredicate(basePattern, predicate);
        });
    }

    public static interface PatternLoader {
        public Pattern loadFrom(PackageLoaderHE var1, NodeInfo var2) throws XPathException;
    }

    public static interface ExpressionLoader {
        public Expression loadFrom(PackageLoaderHE var1, NodeInfo var2) throws XPathException;
    }
}

