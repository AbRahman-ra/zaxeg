/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.jaxen.QualifiedName;
import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;

public class SimpleVariableContext
implements VariableContext,
Serializable {
    private static final long serialVersionUID = 961322093794516518L;
    private Map variables = new HashMap();

    public void setVariableValue(String namespaceURI, String localName, Object value) {
        this.variables.put(new QualifiedName(namespaceURI, localName), value);
    }

    public void setVariableValue(String localName, Object value) {
        this.variables.put(new QualifiedName(null, localName), value);
    }

    public Object getVariableValue(String namespaceURI, String prefix, String localName) throws UnresolvableException {
        QualifiedName key = new QualifiedName(namespaceURI, localName);
        if (this.variables.containsKey(key)) {
            return this.variables.get(key);
        }
        throw new UnresolvableException("Variable " + key.getClarkForm());
    }
}

