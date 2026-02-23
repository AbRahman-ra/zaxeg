/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.sf.saxon.Configuration;
import net.sf.saxon.PreparedStylesheet;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.ComponentBinding;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.instruct.GlobalContextRequirement;
import net.sf.saxon.expr.instruct.GlobalParam;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.SlotManager;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.functions.ExecutableFunctionLibrary;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.registry.ConstructorFunctionLibrary;
import net.sf.saxon.om.Action;
import net.sf.saxon.om.SpaceStrippingRule;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NodeTest;
import net.sf.saxon.query.XQueryFunctionLibrary;
import net.sf.saxon.s9api.HostLanguage;
import net.sf.saxon.serialize.CharacterMap;
import net.sf.saxon.serialize.CharacterMapIndex;
import net.sf.saxon.style.PackageVersion;
import net.sf.saxon.style.PublicStylesheetFunctionLibrary;
import net.sf.saxon.style.StylesheetFunctionLibrary;
import net.sf.saxon.style.XSLAccept;
import net.sf.saxon.trace.ExpressionPresenter;
import net.sf.saxon.trans.ComponentTest;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.VisibilityProvenance;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.rules.RuleManager;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.type.Affinity;
import net.sf.saxon.type.TypeHierarchy;

public class StylesheetPackage
extends PackageData {
    private static final boolean TRACING = false;
    private PackageVersion packageVersion = null;
    private String packageName;
    private List<StylesheetPackage> usedPackages = new ArrayList<StylesheetPackage>();
    private int xsltVersion;
    private RuleManager ruleManager;
    private CharacterMapIndex characterMapIndex;
    private boolean createsSecondaryResultDocuments;
    private List<Action> completionActions = new ArrayList<Action>();
    protected GlobalContextRequirement globalContextRequirement = null;
    private boolean containsGlobalContextItemDeclaration = false;
    protected SpaceStrippingRule stripperRules;
    private boolean stripsWhitespace = false;
    private boolean stripsTypeAnnotations = false;
    protected Properties defaultOutputProperties;
    private StructuredQName defaultMode;
    private boolean declaredModes;
    protected Map<StructuredQName, Properties> namedOutputProperties = new HashMap<StructuredQName, Properties>(4);
    protected Set<String> schemaIndex = new HashSet<String>(10);
    private FunctionLibraryList functionLibrary;
    private XQueryFunctionLibrary queryFunctions;
    private ExecutableFunctionLibrary overriding;
    private ExecutableFunctionLibrary underriding;
    private int maxFunctionArity = -1;
    private boolean retainUnusedFunctions = false;
    private boolean implicitPackage;
    private HashMap<SymbolicName, Component> componentIndex = new HashMap(20);
    protected List<Component> hiddenComponents = new ArrayList<Component>();
    protected HashMap<SymbolicName, Component> overriddenComponents = new HashMap();
    private HashMap<SymbolicName, Component> abstractComponents = new HashMap();

    public StylesheetPackage(Configuration config) {
        super(config);
        this.setHostLanguage(HostLanguage.XSLT);
        this.setAccumulatorRegistry(config.makeAccumulatorRegistry());
    }

    public HashMap<SymbolicName, Component> getComponentIndex() {
        return this.componentIndex;
    }

    public Iterable<StylesheetPackage> getUsedPackages() {
        return this.usedPackages;
    }

    public void addUsedPackage(StylesheetPackage pack) {
        this.usedPackages.add(pack);
    }

    public boolean contains(StylesheetPackage pack) {
        for (StylesheetPackage p : this.usedPackages) {
            if (p != pack && !p.contains(pack)) continue;
            return true;
        }
        return false;
    }

    public void setVersion(int version) {
        this.xsltVersion = version;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getVersion() {
        return this.xsltVersion;
    }

    public PackageVersion getPackageVersion() {
        return this.packageVersion;
    }

    public void setPackageVersion(PackageVersion version) {
        this.packageVersion = version;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public boolean isImplicitPackage() {
        return this.implicitPackage;
    }

    public void setImplicitPackage(boolean implicitPackage) {
        this.implicitPackage = implicitPackage;
    }

    public boolean isJustInTimeCompilation() {
        return false;
    }

    public void setJustInTimeCompilation(boolean justInTimeCompilation) {
    }

    public RuleManager getRuleManager() {
        return this.ruleManager;
    }

    public void setRuleManager(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    public StructuredQName getDefaultMode() {
        return this.defaultMode;
    }

    public void setDefaultMode(StructuredQName defaultMode) {
        this.defaultMode = defaultMode;
    }

    public void setDeclaredModes(boolean declared) {
        this.declaredModes = declared;
    }

    public boolean isDeclaredModes() {
        return this.declaredModes;
    }

    public SpaceStrippingRule getSpaceStrippingRule() {
        return this.stripperRules;
    }

    public CharacterMapIndex getCharacterMapIndex() {
        return this.characterMapIndex;
    }

    public void setCharacterMapIndex(CharacterMapIndex characterMapIndex) {
        this.characterMapIndex = characterMapIndex;
    }

    public boolean isCreatesSecondaryResultDocuments() {
        return this.createsSecondaryResultDocuments;
    }

    public void setCreatesSecondaryResultDocuments(boolean createsSecondaryResultDocuments) {
        this.createsSecondaryResultDocuments = createsSecondaryResultDocuments;
    }

    public boolean isStripsTypeAnnotations() {
        return this.stripsTypeAnnotations;
    }

    public void setStripsTypeAnnotations(boolean stripsTypeAnnotations) {
        this.stripsTypeAnnotations = stripsTypeAnnotations;
    }

    public SpaceStrippingRule getStripperRules() {
        return this.stripperRules;
    }

    public void setStripperRules(SpaceStrippingRule stripperRules) {
        this.stripperRules = stripperRules;
    }

    public void setDefaultOutputProperties(Properties props) {
        this.defaultOutputProperties = props;
    }

    public void setNamedOutputProperties(StructuredQName name, Properties props) {
        this.namedOutputProperties.put(name, props);
    }

    public Properties getNamedOutputProperties(StructuredQName name) {
        return this.namedOutputProperties.get(name);
    }

    public Set<String> getSchemaNamespaces() {
        return this.schemaIndex;
    }

    public void setContextItemRequirements(GlobalContextRequirement requirement) throws XPathException {
        if (this.containsGlobalContextItemDeclaration) {
            if (!requirement.isAbsentFocus() && this.globalContextRequirement.isAbsentFocus() || requirement.isMayBeOmitted() && !this.globalContextRequirement.isMayBeOmitted()) {
                throw new XPathException("The package contains two xsl:global-context-item declarations with conflicting @use attributes", "XTSE3087");
            }
            TypeHierarchy th = this.getConfiguration().getTypeHierarchy();
            if (th.relationship(requirement.getRequiredItemType(), this.globalContextRequirement.getRequiredItemType()) != Affinity.SAME_TYPE) {
                throw new XPathException("The package contains two xsl:global-context-item declarations with conflicting item types", "XTSE3087");
            }
        }
        this.containsGlobalContextItemDeclaration = true;
        this.globalContextRequirement = requirement;
    }

    public GlobalContextRequirement getContextItemRequirements() {
        return this.globalContextRequirement;
    }

    public void setStripsWhitespace(boolean strips) {
        this.stripsWhitespace = strips;
    }

    public boolean isStripsWhitespace() {
        return this.stripsWhitespace;
    }

    public void addCompletionAction(Action action) {
        this.completionActions.add(action);
    }

    protected void complete() throws XPathException {
        for (Action a : this.completionActions) {
            a.doAction();
        }
        this.allocateBinderySlots();
    }

    public void allocateBinderySlots() {
        SlotManager slotManager = this.getConfiguration().makeSlotManager();
        for (Component c : this.componentIndex.values()) {
            this.registerGlobalVariable(c, slotManager);
        }
        for (Component c : this.hiddenComponents) {
            this.registerGlobalVariable(c, slotManager);
        }
        this.setGlobalSlotManager(slotManager);
    }

    private void registerGlobalVariable(Component c, SlotManager slotManager) {
        if (c.getActor() instanceof GlobalVariable) {
            GlobalVariable var = (GlobalVariable)c.getActor();
            int slot = slotManager.allocateSlotNumber(var.getVariableQName());
            var.setPackageData(this);
            var.setBinderySlotNumber(slot);
            if (c.getVisibility() != Visibility.HIDDEN) {
                this.addGlobalVariable(var);
            }
        }
    }

    public void addComponent(Component component) {
        SymbolicName name = component.getActor().getSymbolicName();
        this.componentIndex.put(name, component);
        if (component.getVisibility() == Visibility.ABSTRACT && component.getContainingPackage() == this) {
            this.abstractComponents.put(component.getActor().getSymbolicName(), component);
        }
    }

    @Override
    public void addGlobalVariable(GlobalVariable variable) {
        super.addGlobalVariable(variable);
        SymbolicName name = variable.getSymbolicName();
        if (this.componentIndex.get(name) == null) {
            Component comp = variable.getDeclaringComponent();
            if (comp == null) {
                comp = variable.makeDeclaringComponent(Visibility.PRIVATE, this);
            }
            this.addComponent(comp);
        }
    }

    public int getMaxFunctionArity() {
        if (this.maxFunctionArity == -1) {
            for (Component c : this.componentIndex.values()) {
                if (!(c.getActor() instanceof UserFunction) || ((UserFunction)c.getActor()).getArity() <= this.maxFunctionArity) continue;
                this.maxFunctionArity = ((UserFunction)c.getActor()).getArity();
            }
        }
        return this.maxFunctionArity;
    }

    public Component getComponent(SymbolicName name) {
        return this.componentIndex.get(name);
    }

    public void addHiddenComponent(Component component) {
        this.hiddenComponents.add(component);
    }

    public Component getOverriddenComponent(SymbolicName name) {
        return this.overriddenComponents.get(name);
    }

    public void addOverriddenComponent(Component comp) {
        this.overriddenComponents.put(comp.getActor().getSymbolicName(), comp);
    }

    public void addComponentsFromUsedPackage(StylesheetPackage usedPackage, List<XSLAccept> acceptors, Set<SymbolicName> overrides) throws XPathException {
        this.usedPackages.add(usedPackage);
        this.trace("=== Adding components from " + usedPackage.getPackageName() + " to " + this.getPackageName() + " ===");
        HashMap<Component, Component> correspondence = new HashMap<Component, Component>();
        for (Map.Entry<SymbolicName, Component> namedComponentEntry : usedPackage.componentIndex.entrySet()) {
            SymbolicName name = namedComponentEntry.getKey();
            Component oldC = namedComponentEntry.getValue();
            Visibility oldV = oldC.getVisibility();
            Visibility newV = null;
            if (overrides.contains(name) && !(oldC.getActor() instanceof Mode)) {
                newV = Visibility.HIDDEN;
            } else {
                Visibility acceptedVisibility = this.explicitAcceptedVisibility(name, acceptors);
                if (acceptedVisibility != null) {
                    if (!XSLAccept.isCompatible(oldV, acceptedVisibility)) {
                        throw new XPathException("Cannot accept a " + oldV.show() + " component (" + name + ") from package " + usedPackage.getPackageName() + " with visibility " + acceptedVisibility.show(), "XTSE3040");
                    }
                    newV = acceptedVisibility;
                } else {
                    acceptedVisibility = this.wildcardAcceptedVisibility(name, acceptors);
                    if (acceptedVisibility != null && XSLAccept.isCompatible(oldV, acceptedVisibility)) {
                        newV = acceptedVisibility;
                    }
                }
                if (newV == null) {
                    newV = oldV == Visibility.PUBLIC || oldV == Visibility.FINAL ? Visibility.PRIVATE : Visibility.HIDDEN;
                }
            }
            this.trace(oldC.getActor().getSymbolicName() + " (" + oldV.show() + ") becomes " + newV.show());
            Component newC = Component.makeComponent(oldC.getActor(), newV, VisibilityProvenance.DERIVED, this, oldC.getDeclaringPackage());
            correspondence.put(oldC, newC);
            newC.setBaseComponent(oldC);
            if (overrides.contains(name)) {
                this.overriddenComponents.put(name, newC);
                if (newV != Visibility.ABSTRACT) {
                    this.abstractComponents.remove(name);
                }
            }
            if (newC.getVisibility() == Visibility.HIDDEN) {
                this.hiddenComponents.add(newC);
            } else if (this.componentIndex.get(name) != null) {
                if (!(oldC.getActor() instanceof Mode)) {
                    throw new XPathException("Duplicate " + namedComponentEntry.getKey(), "XTSE3050", oldC.getActor());
                }
            } else {
                Mode existing;
                this.componentIndex.put(name, newC);
                if (oldC.getActor() instanceof Mode && (oldV == Visibility.PUBLIC || oldV == Visibility.FINAL) && (existing = this.getRuleManager().obtainMode(name.getComponentName(), false)) != null) {
                    throw new XPathException("Duplicate " + namedComponentEntry.getKey(), "XTSE3050", oldC.getActor());
                }
            }
            if (newC.getActor() instanceof Mode && overrides.contains(name)) {
                this.addCompletionAction(() -> {
                    this.trace("Doing mode completion for " + newC.getActor().getSymbolicName());
                    List<ComponentBinding> oldBindings = newC.getBaseComponent().getComponentBindings();
                    List<ComponentBinding> newBindings = newC.getComponentBindings();
                    for (int i = 0; i < oldBindings.size(); ++i) {
                        Component target;
                        SymbolicName name12 = oldBindings.get(i).getSymbolicName();
                        if (overrides.contains(name12)) {
                            target = this.getComponent(name12);
                            if (target == null) {
                                throw new AssertionError((Object)("We know there's an override for " + name12 + ", but we can't find it"));
                            }
                        } else {
                            target = (Component)correspondence.get(oldBindings.get(i).getTarget());
                            if (target == null) {
                                throw new AssertionError((Object)("Saxon can't find the new component corresponding to " + name12));
                            }
                        }
                        ComponentBinding newBinding = new ComponentBinding(name12, target);
                        newBindings.set(i, newBinding);
                    }
                });
                continue;
            }
            this.addCompletionAction(() -> {
                this.trace("Doing normal completion for " + newC.getActor().getSymbolicName());
                List<ComponentBinding> oldBindings = newC.getBaseComponent().getComponentBindings();
                ArrayList<ComponentBinding> newBindings = new ArrayList<ComponentBinding>(oldBindings.size());
                this.makeNewComponentBindings(overrides, correspondence, oldBindings, newBindings);
                newC.setComponentBindings(newBindings);
            });
        }
        for (Component oldC : usedPackage.hiddenComponents) {
            this.trace(oldC.getActor().getSymbolicName() + " (HIDDEN, declared in " + oldC.getDeclaringPackage().getPackageName() + ") becomes HIDDEN");
            Component newC = Component.makeComponent(oldC.getActor(), Visibility.HIDDEN, VisibilityProvenance.DERIVED, this, oldC.getDeclaringPackage());
            correspondence.put(oldC, newC);
            newC.setBaseComponent(oldC);
            this.hiddenComponents.add(newC);
            this.addCompletionAction(() -> {
                List<ComponentBinding> oldBindings = newC.getBaseComponent().getComponentBindings();
                ArrayList<ComponentBinding> newBindings = new ArrayList<ComponentBinding>(oldBindings.size());
                this.makeNewComponentBindings(overrides, correspondence, oldBindings, newBindings);
                newC.setComponentBindings(newBindings);
            });
        }
        if (usedPackage.isCreatesSecondaryResultDocuments()) {
            this.setCreatesSecondaryResultDocuments(true);
        }
    }

    private void makeNewComponentBindings(Set<SymbolicName> overrides, Map<Component, Component> correspondence, List<ComponentBinding> oldBindings, List<ComponentBinding> newBindings) {
        for (ComponentBinding oldBinding : oldBindings) {
            Component target;
            SymbolicName name = oldBinding.getSymbolicName();
            if (overrides.contains(name)) {
                target = this.getComponent(name);
                if (target == null) {
                    throw new AssertionError((Object)("We know there's an override for " + name + ", but we can't find it"));
                }
            } else {
                target = correspondence.get(oldBinding.getTarget());
                if (target == null) {
                    throw new AssertionError((Object)("Saxon can't find the new component corresponding to " + name));
                }
            }
            ComponentBinding newBinding = new ComponentBinding(name, target);
            newBindings.add(newBinding);
        }
    }

    private void trace(String message) {
    }

    private Visibility explicitAcceptedVisibility(SymbolicName name, List<XSLAccept> acceptors) throws XPathException {
        for (XSLAccept acceptor : acceptors) {
            for (ComponentTest test : acceptor.getExplicitComponentTests()) {
                if (!test.matches(name)) continue;
                return acceptor.getVisibility();
            }
        }
        return null;
    }

    private Visibility wildcardAcceptedVisibility(SymbolicName name, List<XSLAccept> acceptors) throws XPathException {
        Visibility vis = null;
        for (XSLAccept acceptor : acceptors) {
            for (ComponentTest test : acceptor.getWildcardComponentTests()) {
                if (((NodeTest)((Object)test.getQNameTest())).getDefaultPriority() != -0.25 || !test.matches(name)) continue;
                vis = acceptor.getVisibility();
            }
        }
        if (vis != null) {
            return vis;
        }
        for (XSLAccept acceptor : acceptors) {
            for (ComponentTest test : acceptor.getWildcardComponentTests()) {
                if (!test.matches(name)) continue;
                vis = acceptor.getVisibility();
            }
        }
        return vis;
    }

    public void createFunctionLibrary() {
        FunctionLibraryList functionLibrary = new FunctionLibraryList();
        boolean includeHOF = !"HE".equals(this.getTargetEdition()) && !"JS".equals(this.getTargetEdition());
        functionLibrary.addFunctionLibrary(includeHOF ? this.config.getXSLT30FunctionSet() : new Configuration().getXSLT30FunctionSet());
        functionLibrary.addFunctionLibrary(new StylesheetFunctionLibrary(this, true));
        functionLibrary.addFunctionLibrary(this.config.getBuiltInExtensionLibraryList());
        functionLibrary.addFunctionLibrary(new ConstructorFunctionLibrary(this.config));
        if ("JS".equals(this.getTargetEdition())) {
            this.addIxslFunctionLibrary(functionLibrary);
        }
        this.queryFunctions = new XQueryFunctionLibrary(this.config);
        functionLibrary.addFunctionLibrary(this.queryFunctions);
        functionLibrary.addFunctionLibrary(this.config.getIntegratedFunctionLibrary());
        this.config.addExtensionBinders(functionLibrary);
        functionLibrary.addFunctionLibrary(new StylesheetFunctionLibrary(this, false));
        this.functionLibrary = functionLibrary;
    }

    protected void addIxslFunctionLibrary(FunctionLibraryList functionLibrary) {
    }

    public FunctionLibraryList getFunctionLibrary() {
        return this.functionLibrary;
    }

    public FunctionLibrary getPublicFunctions() {
        return new PublicStylesheetFunctionLibrary(this.functionLibrary);
    }

    public XQueryFunctionLibrary getXQueryFunctionLibrary() {
        return this.queryFunctions;
    }

    public void setFunctionLibraryDetails(FunctionLibraryList library, ExecutableFunctionLibrary overriding, ExecutableFunctionLibrary underriding) {
        if (library != null) {
            this.functionLibrary = library;
        }
        this.overriding = overriding;
        this.underriding = underriding;
    }

    protected UserFunction getFunction(SymbolicName.F name) {
        if (name.getArity() == -1) {
            int maximumArity = 20;
            for (int a = 0; a < maximumArity; ++a) {
                SymbolicName.F sn = new SymbolicName.F(name.getComponentName(), a);
                UserFunction uf = this.getFunction(sn);
                if (uf == null) continue;
                uf.incrementReferenceCount();
                return uf;
            }
            return null;
        }
        Component component = this.getComponentIndex().get(name);
        if (component != null) {
            UserFunction uf = (UserFunction)component.getActor();
            uf.incrementReferenceCount();
            return uf;
        }
        return null;
    }

    public boolean isRetainUnusedFunctions() {
        return this.retainUnusedFunctions;
    }

    public void setRetainUnusedFunctions() {
        this.retainUnusedFunctions = true;
    }

    public void updatePreparedStylesheet(PreparedStylesheet pss) throws XPathException {
        for (Map.Entry<SymbolicName, Component> entry : this.componentIndex.entrySet()) {
            if (entry.getValue().getVisibility() != Visibility.ABSTRACT) continue;
            this.abstractComponents.put(entry.getKey(), entry.getValue());
        }
        pss.setTopLevelPackage(this);
        if (this.isSchemaAware() || !this.schemaIndex.isEmpty()) {
            pss.setSchemaAware(true);
        }
        pss.setHostLanguage(HostLanguage.XSLT);
        FunctionLibraryList libraryList = new FunctionLibraryList();
        for (FunctionLibrary functionLibrary : this.functionLibrary.getLibraryList()) {
            if (functionLibrary instanceof StylesheetFunctionLibrary) {
                if (((StylesheetFunctionLibrary)functionLibrary).isOverrideExtensionFunction()) {
                    libraryList.addFunctionLibrary(this.overriding);
                    continue;
                }
                libraryList.addFunctionLibrary(this.underriding);
                continue;
            }
            libraryList.addFunctionLibrary(functionLibrary);
        }
        pss.setFunctionLibrary(libraryList);
        if (!pss.createsSecondaryResult()) {
            pss.setCreatesSecondaryResult(this.mayCreateSecondaryResultDocuments());
        }
        pss.setDefaultOutputProperties(this.defaultOutputProperties);
        for (Map.Entry entry : this.namedOutputProperties.entrySet()) {
            pss.setOutputProperties((StructuredQName)entry.getKey(), (Properties)entry.getValue());
        }
        if (this.characterMapIndex != null) {
            for (CharacterMap characterMap : this.characterMapIndex) {
                pss.getCharacterMapIndex().putCharacterMap(characterMap.getName(), characterMap);
            }
        }
        pss.setRuleManager(this.ruleManager);
        for (Component component : this.componentIndex.values()) {
            if (!(component.getActor() instanceof NamedTemplate)) continue;
            NamedTemplate t = (NamedTemplate)component.getActor();
            pss.putNamedTemplate(t.getTemplateName(), t);
        }
        pss.setComponentIndex(this.componentIndex);
        for (Component component : this.componentIndex.values()) {
            if (!(component.getActor() instanceof GlobalParam)) continue;
            GlobalParam gv = (GlobalParam)component.getActor();
            pss.registerGlobalParameter(gv);
        }
        if (this.globalContextRequirement != null) {
            pss.setGlobalContextRequirement(this.globalContextRequirement);
        }
    }

    private boolean mayCreateSecondaryResultDocuments() {
        if (this.createsSecondaryResultDocuments) {
            return true;
        }
        for (StylesheetPackage p : this.usedPackages) {
            if (!p.mayCreateSecondaryResultDocuments()) continue;
            return true;
        }
        return false;
    }

    public Map<SymbolicName, Component> getAbstractComponents() {
        return this.abstractComponents;
    }

    public void export(ExpressionPresenter presenter) throws XPathException {
        throw new XPathException("Exporting a stylesheet requires Saxon-EE");
    }

    public void checkForAbstractComponents() throws XPathException {
        for (Map.Entry<SymbolicName, Component> entry : this.componentIndex.entrySet()) {
            if (entry.getValue().getVisibility() != Visibility.ABSTRACT || entry.getValue().getContainingPackage() != this) continue;
            this.abstractComponents.put(entry.getKey(), entry.getValue());
        }
        if (!this.abstractComponents.isEmpty()) {
            FastStringBuffer buff = new FastStringBuffer(256);
            int count = 0;
            for (SymbolicName name : this.abstractComponents.keySet()) {
                if (count++ > 0) {
                    buff.append(", ");
                }
                buff.append(name.toString());
                if (buff.length() <= 300) continue;
                buff.append(" ...");
                break;
            }
            throw new XPathException("The package is not executable, because it contains abstract components: " + buff, "XTSE3080");
        }
    }

    public boolean isFallbackToNonStreaming() {
        return true;
    }

    public void setFallbackToNonStreaming() {
    }
}

