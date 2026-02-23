/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.style;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import net.sf.saxon.om.AttributeInfo;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.QNameException;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.Compilation;
import net.sf.saxon.style.ComponentDeclaration;
import net.sf.saxon.style.PrincipalStylesheetModule;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.style.XSLOutputCharacter;
import net.sf.saxon.trans.Err;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntHashMap;

public class XSLCharacterMap
extends StyleElement {
    String use;
    List<XSLCharacterMap> characterMapElements = null;
    boolean validated = false;
    boolean redundant = false;

    @Override
    public boolean isDeclaration() {
        return true;
    }

    public StructuredQName getCharacterMapName() {
        StructuredQName name = this.getObjectName();
        if (name == null) {
            return this.makeQName(this.getAttributeValue("", "name"), null, "name");
        }
        return name;
    }

    public boolean isRedundant() {
        return this.redundant;
    }

    @Override
    public void prepareAttributes() {
        String name = null;
        this.use = null;
        for (AttributeInfo att : this.attributes()) {
            NodeName attName = att.getNodeName();
            String value = att.getValue();
            String f = attName.getDisplayName();
            if (f.equals("name")) {
                name = Whitespace.trim(value);
                continue;
            }
            if (f.equals("use-character-maps")) {
                this.use = value;
                continue;
            }
            this.checkUnknownAttribute(attName);
        }
        if (name == null) {
            this.reportAbsence("name");
            name = "unnamedCharacterMap_" + this.hashCode();
        }
        this.setObjectName(this.makeQName(name, null, "name"));
    }

    @Override
    public void validate(ComponentDeclaration decl) throws XPathException {
        if (this.validated) {
            return;
        }
        this.checkTopLevel("XTSE0010", false);
        for (NodeInfo nodeInfo : this.children()) {
            if (nodeInfo instanceof XSLOutputCharacter) continue;
            this.compileError("Only xsl:output-character is allowed within xsl:character-map", "XTSE0010");
        }
        PrincipalStylesheetModule psm = this.getPrincipalStylesheetModule();
        ComponentDeclaration componentDeclaration = psm.getCharacterMap(this.getObjectName());
        if (componentDeclaration != null && componentDeclaration.getSourceElement() != this) {
            if (decl.getPrecedence() == componentDeclaration.getPrecedence()) {
                this.compileError("There are two character-maps with the same name and import precedence", "XTSE1580");
            } else if (decl.getPrecedence() < componentDeclaration.getPrecedence()) {
                this.redundant = true;
            }
        }
        if (this.use != null) {
            this.characterMapElements = new ArrayList<XSLCharacterMap>(5);
            StringTokenizer st = new StringTokenizer(this.use, " \t\n\r", false);
            while (st.hasMoreTokens()) {
                String displayname = st.nextToken();
                try {
                    StructuredQName qn;
                    ComponentDeclaration charMapDecl;
                    String[] parts = NameChecker.getQNameParts(displayname);
                    String uri = this.getURIForPrefix(parts[0], false);
                    if (uri == null) {
                        this.compileError("Undeclared namespace prefix " + Err.wrap(parts[0]) + " in character map name", "XTSE0280");
                    }
                    if ((charMapDecl = psm.getCharacterMap(qn = new StructuredQName(parts[0], uri, parts[1]))) == null) {
                        this.compileError("No character-map named '" + displayname + "' has been defined", "XTSE1590");
                        continue;
                    }
                    XSLCharacterMap ref = (XSLCharacterMap)charMapDecl.getSourceElement();
                    this.characterMapElements.add(ref);
                } catch (QNameException err) {
                    this.compileError("Invalid character-map name. " + err.getMessage(), "XTSE1590");
                }
            }
            for (XSLCharacterMap characterMapElement : this.characterMapElements) {
                characterMapElement.checkCircularity(this);
            }
        }
        this.validated = true;
    }

    private void checkCircularity(XSLCharacterMap origin) throws XPathException {
        if (this == origin) {
            this.compileError("The definition of the character map is circular", "XTSE1600");
            this.characterMapElements = null;
        } else {
            if (!this.validated) {
                return;
            }
            if (this.characterMapElements != null) {
                for (XSLCharacterMap characterMapElement : this.characterMapElements) {
                    characterMapElement.checkCircularity(origin);
                }
            }
        }
    }

    public void assemble(IntHashMap<String> map) {
        if (this.characterMapElements != null) {
            for (XSLCharacterMap xSLCharacterMap : this.characterMapElements) {
                xSLCharacterMap.assemble(map);
            }
        }
        for (NodeInfo nodeInfo : this.children()) {
            XSLOutputCharacter oc = (XSLOutputCharacter)nodeInfo;
            map.put(oc.getCodePoint(), oc.getReplacementString());
        }
    }

    @Override
    public void compileDeclaration(Compilation compilation, ComponentDeclaration decl) throws XPathException {
    }
}

