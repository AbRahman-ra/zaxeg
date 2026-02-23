/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.List;
import net.sf.saxon.expr.Component;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.NameTest;
import net.sf.saxon.pattern.QNameTest;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.SourceBinding;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.style.XSLAcceptExpose;
import net.sf.saxon.style.XSLGlobalParam;
import net.sf.saxon.style.XSLGlobalVariable;
import net.sf.saxon.trans.ComponentTest;
import net.sf.saxon.trans.Mode;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.Visibility;
import net.sf.saxon.trans.XPathException;

public class XSLExpose
extends XSLAcceptExpose {
    protected void checkCompatibility(SymbolicName name, Visibility declared, Visibility exposed) {
        if (!XSLExpose.isCompatible(declared, exposed)) {
            String code = "XTSE3010";
            this.compileError("The " + name + " is declared as " + declared.show() + " and cannot be exposed as " + exposed.show(), code);
        }
    }

    public static boolean isCompatible(Visibility declared, Visibility exposed) {
        if (declared == null || declared == exposed) {
            return true;
        }
        switch (declared) {
            case PUBLIC: {
                return exposed == Visibility.PUBLIC || exposed == Visibility.PRIVATE || exposed == Visibility.FINAL || exposed == Visibility.HIDDEN;
            }
            case ABSTRACT: {
                return exposed == Visibility.ABSTRACT || exposed == Visibility.HIDDEN;
            }
            case FINAL: {
                return exposed == Visibility.PRIVATE || exposed == Visibility.FINAL || exposed == Visibility.HIDDEN;
            }
        }
        return false;
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        PrincipalStylesheetModule psm = this.getPrincipalStylesheetModule();
        Visibility exposedVisibility = this.getVisibility();
        for (ComponentTest test : this.getExplicitComponentTests()) {
            QNameTest nameTest = test.getQNameTest();
            if (!(nameTest instanceof NameTest)) continue;
            StructuredQName qName = ((NameTest)nameTest).getMatchingNodeName();
            int kind = test.getComponentKind();
            SymbolicName sName = kind == 158 ? new SymbolicName.F(((NameTest)nameTest).getMatchingNodeName(), test.getArity()) : new SymbolicName(kind, ((NameTest)nameTest).getMatchingNodeName());
            boolean found = false;
            switch (kind) {
                case 200: {
                    NamedTemplate template = psm.getNamedTemplate(qName);
                    boolean bl = found = template != null;
                    if (!found) break;
                    Visibility declared = template.getDeclaredVisibility();
                    this.checkCompatibility(template.getSymbolicName(), declared, exposedVisibility);
                    break;
                }
                case 206: {
                    SourceBinding binding = psm.getGlobalVariableBinding(qName);
                    if (binding != null && !(binding.getSourceElement() instanceof XSLGlobalParam)) {
                        found = true;
                    }
                    if (!found) break;
                    GlobalVariable var = ((XSLGlobalVariable)binding.getSourceElement()).getCompiledVariable();
                    Visibility declared = var.getDeclaredVisibility();
                    this.checkCompatibility(var.getSymbolicName(), declared, this.getVisibility());
                    break;
                }
                case 136: {
                    List<ComponentDeclaration> declarations = psm.getAttributeSetDeclarations(qName);
                    boolean bl = found = declarations != null && !declarations.isEmpty();
                    if (!found) break;
                    Visibility declared = declarations.get(0).getSourceElement().getDeclaredVisibility();
                    this.checkCompatibility(sName, declared, this.getVisibility());
                    break;
                }
                case 174: {
                    Mode mode = psm.getRuleManager().obtainMode(qName, false);
                    boolean bl = found = mode != null;
                    if (found) {
                        this.checkCompatibility(sName, mode.getDeclaredVisibility(), this.getVisibility());
                    }
                    if (this.getVisibility() != Visibility.ABSTRACT) break;
                    this.compileError("The visibility of a mode cannot be abstract");
                    break;
                }
                case 158: {
                    StylesheetPackage pack = psm.getStylesheetPackage();
                    if (test.getArity() == -1) {
                        for (int i = 0; i <= pack.getMaxFunctionArity(); ++i) {
                            sName = new SymbolicName.F(((NameTest)nameTest).getMatchingNodeName(), i);
                            Component fn = pack.getComponent(sName);
                            if (fn == null) continue;
                            found = true;
                            UserFunction userFunction = (UserFunction)fn.getActor();
                            this.checkCompatibility(sName, userFunction.getDeclaredVisibility(), this.getVisibility());
                        }
                        break;
                    }
                    Component fn = pack.getComponent(sName);
                    boolean bl = found = fn != null;
                    if (!found) break;
                    UserFunction userFunction = (UserFunction)fn.getActor();
                    this.checkCompatibility(sName, userFunction.getDeclaredVisibility(), this.getVisibility());
                }
            }
            if (found) continue;
            this.compileError("No " + sName.toString() + " exists in the containing package", "XTSE3020");
        }
    }
}

