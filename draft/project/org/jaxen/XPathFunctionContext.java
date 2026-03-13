/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen;

import org.jaxen.FunctionContext;
import org.jaxen.SimpleFunctionContext;
import org.jaxen.function.BooleanFunction;
import org.jaxen.function.CeilingFunction;
import org.jaxen.function.ConcatFunction;
import org.jaxen.function.ContainsFunction;
import org.jaxen.function.CountFunction;
import org.jaxen.function.FalseFunction;
import org.jaxen.function.FloorFunction;
import org.jaxen.function.IdFunction;
import org.jaxen.function.LangFunction;
import org.jaxen.function.LastFunction;
import org.jaxen.function.LocalNameFunction;
import org.jaxen.function.NameFunction;
import org.jaxen.function.NamespaceUriFunction;
import org.jaxen.function.NormalizeSpaceFunction;
import org.jaxen.function.NotFunction;
import org.jaxen.function.NumberFunction;
import org.jaxen.function.PositionFunction;
import org.jaxen.function.RoundFunction;
import org.jaxen.function.StartsWithFunction;
import org.jaxen.function.StringFunction;
import org.jaxen.function.StringLengthFunction;
import org.jaxen.function.SubstringAfterFunction;
import org.jaxen.function.SubstringBeforeFunction;
import org.jaxen.function.SubstringFunction;
import org.jaxen.function.SumFunction;
import org.jaxen.function.TranslateFunction;
import org.jaxen.function.TrueFunction;
import org.jaxen.function.ext.EndsWithFunction;
import org.jaxen.function.ext.EvaluateFunction;
import org.jaxen.function.ext.LowerFunction;
import org.jaxen.function.ext.UpperFunction;
import org.jaxen.function.xslt.DocumentFunction;

public class XPathFunctionContext
extends SimpleFunctionContext {
    private static XPathFunctionContext instance = new XPathFunctionContext();

    public static FunctionContext getInstance() {
        return instance;
    }

    public XPathFunctionContext() {
        this(true);
    }

    public XPathFunctionContext(boolean includeExtensionFunctions) {
        this.registerXPathFunctions();
        if (includeExtensionFunctions) {
            this.registerXSLTFunctions();
            this.registerExtensionFunctions();
        }
    }

    private void registerXPathFunctions() {
        this.registerFunction(null, "boolean", new BooleanFunction());
        this.registerFunction(null, "ceiling", new CeilingFunction());
        this.registerFunction(null, "concat", new ConcatFunction());
        this.registerFunction(null, "contains", new ContainsFunction());
        this.registerFunction(null, "count", new CountFunction());
        this.registerFunction(null, "false", new FalseFunction());
        this.registerFunction(null, "floor", new FloorFunction());
        this.registerFunction(null, "id", new IdFunction());
        this.registerFunction(null, "lang", new LangFunction());
        this.registerFunction(null, "last", new LastFunction());
        this.registerFunction(null, "local-name", new LocalNameFunction());
        this.registerFunction(null, "name", new NameFunction());
        this.registerFunction(null, "namespace-uri", new NamespaceUriFunction());
        this.registerFunction(null, "normalize-space", new NormalizeSpaceFunction());
        this.registerFunction(null, "not", new NotFunction());
        this.registerFunction(null, "number", new NumberFunction());
        this.registerFunction(null, "position", new PositionFunction());
        this.registerFunction(null, "round", new RoundFunction());
        this.registerFunction(null, "starts-with", new StartsWithFunction());
        this.registerFunction(null, "string", new StringFunction());
        this.registerFunction(null, "string-length", new StringLengthFunction());
        this.registerFunction(null, "substring-after", new SubstringAfterFunction());
        this.registerFunction(null, "substring-before", new SubstringBeforeFunction());
        this.registerFunction(null, "substring", new SubstringFunction());
        this.registerFunction(null, "sum", new SumFunction());
        this.registerFunction(null, "true", new TrueFunction());
        this.registerFunction(null, "translate", new TranslateFunction());
    }

    private void registerXSLTFunctions() {
        this.registerFunction(null, "document", new DocumentFunction());
    }

    private void registerExtensionFunctions() {
        this.registerFunction(null, "evaluate", new EvaluateFunction());
        this.registerFunction(null, "lower-case", new LowerFunction());
        this.registerFunction(null, "upper-case", new UpperFunction());
        this.registerFunction(null, "ends-with", new EndsWithFunction());
    }
}

