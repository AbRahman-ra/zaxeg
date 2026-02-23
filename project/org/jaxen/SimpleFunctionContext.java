/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import java.util.HashMap;
import org.jaxen.Function;
import org.jaxen.FunctionContext;
import org.jaxen.QualifiedName;
import org.jaxen.UnresolvableException;

public class SimpleFunctionContext
implements FunctionContext {
    private HashMap functions = new HashMap();

    public void registerFunction(String namespaceURI, String localName, Function function) {
        this.functions.put(new QualifiedName(namespaceURI, localName), function);
    }

    public Function getFunction(String namespaceURI, String prefix, String localName) throws UnresolvableException {
        QualifiedName key = new QualifiedName(namespaceURI, localName);
        if (this.functions.containsKey(key)) {
            return (Function)this.functions.get(key);
        }
        throw new UnresolvableException("No Such Function " + key.getClarkForm());
    }
}

