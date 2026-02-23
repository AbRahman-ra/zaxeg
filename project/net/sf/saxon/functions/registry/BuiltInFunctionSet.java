/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions.registry;

import java.util.HashMap;
import java.util.List;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.OperandUsage;
import net.sf.saxon.expr.StaticContext;
import net.sf.saxon.expr.parser.RetainedStaticContext;
import net.sf.saxon.functions.FunctionLibrary;
import net.sf.saxon.functions.OptionsParameter;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Function;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.ItemType;
import net.sf.saxon.type.PlainType;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

public abstract class BuiltInFunctionSet
implements FunctionLibrary {
    public static Sequence EMPTY = EmptySequence.getInstance();
    public static final int ONE = 16384;
    public static final int OPT = 24576;
    public static final int STAR = 57344;
    public static final int PLUS = 49152;
    public static final int AS_ARG0 = 1;
    public static final int AS_PRIM_ARG0 = 2;
    public static final int CITEM = 4;
    public static final int BASE = 8;
    public static final int NS = 16;
    public static final int DCOLL = 32;
    public static final int DLANG = 64;
    public static final int FILTER = 256;
    public static final int LATE = 512;
    public static final int UO = 1024;
    public static final int POSN = 2048;
    public static final int LAST = 4096;
    public static final int SIDE = 8192;
    public static final int CDOC = 16384;
    public static final int CARD0 = 32768;
    public static final int NEW = 65536;
    public static final int DEPENDS_ON_STATIC_CONTEXT = 56;
    public static final int FOCUS = 22532;
    protected static final int INS = 0x1000000;
    protected static final int ABS = 0x2000000;
    protected static final int TRA = 0x4000000;
    protected static final int NAV = 0x8000000;
    private HashMap<String, Entry> functionTable = new HashMap(200);

    public final void importFunctionSet(BuiltInFunctionSet importee) {
        if (!importee.getNamespace().equals(this.getNamespace())) {
            throw new IllegalArgumentException(importee.getNamespace());
        }
        this.functionTable.putAll(importee.functionTable);
    }

    public Entry getFunctionDetails(String name, int arity) {
        if (arity == -1) {
            for (int i = 0; i < 20; ++i) {
                Entry found = this.getFunctionDetails(name, i);
                if (found == null) continue;
                return found;
            }
            return null;
        }
        String key = name + "#" + arity;
        Entry entry = this.functionTable.get(key);
        if (entry != null) {
            return entry;
        }
        if (name.equals("concat") && arity >= 2 && this.getNamespace().equals("http://www.w3.org/2005/xpath-functions")) {
            key = "concat#-1";
            entry = this.functionTable.get(key);
            return entry;
        }
        return null;
    }

    @Override
    public Expression bind(SymbolicName.F symbolicName, Expression[] staticArgs, StaticContext env, List<String> reasons) {
        StructuredQName functionName = symbolicName.getComponentName();
        int arity = symbolicName.getArity();
        String localName = functionName.getLocalPart();
        if (functionName.hasURI(this.getNamespace()) && this.getFunctionDetails(localName, arity) != null) {
            RetainedStaticContext rsc = new RetainedStaticContext(env);
            try {
                SystemFunction fn = this.makeFunction(localName, arity);
                fn.setRetainedStaticContext(rsc);
                Expression f = fn.makeFunctionCall(staticArgs);
                f.setRetainedStaticContext(rsc);
                return f;
            } catch (XPathException e) {
                reasons.add(e.getMessage());
                return null;
            }
        }
        return null;
    }

    public SystemFunction makeFunction(String name, int arity) throws XPathException {
        SystemFunction f;
        Entry entry = this.getFunctionDetails(name, arity);
        if (entry == null) {
            String diagName;
            String string = diagName = this.getNamespace().equals("http://www.w3.org/2005/xpath-functions") ? "System function " + name : "Function Q{" + this.getNamespace() + "}" + name;
            if (this.getFunctionDetails(name, -1) == null) {
                XPathException err = new XPathException(diagName + "() does not exist or is not available in this environment");
                err.setErrorCode("XPST0017");
                err.setIsStaticError(true);
                throw err;
            }
            XPathException err = new XPathException(diagName + "() cannot be called with " + BuiltInFunctionSet.pluralArguments(arity));
            err.setErrorCode("XPST0017");
            err.setIsStaticError(true);
            throw err;
        }
        Class functionClass = entry.implementationClass;
        try {
            f = (SystemFunction)functionClass.newInstance();
        } catch (Exception err) {
            err.printStackTrace();
            throw new AssertionError((Object)("Failed to instantiate system function " + name + " - " + err.getMessage()));
        }
        f.setDetails(entry);
        f.setArity(arity);
        return f;
    }

    private static String pluralArguments(int num) {
        if (num == 0) {
            return "zero arguments";
        }
        if (num == 1) {
            return "one argument";
        }
        return num + " arguments";
    }

    @Override
    public boolean isAvailable(SymbolicName.F symbolicName) {
        StructuredQName qn = symbolicName.getComponentName();
        return qn.hasURI(this.getNamespace()) && this.getFunctionDetails(qn.getLocalPart(), symbolicName.getArity()) != null;
    }

    @Override
    public FunctionLibrary copy() {
        return this;
    }

    @Override
    public Function getFunctionItem(SymbolicName.F symbolicName, StaticContext staticContext) throws XPathException {
        StructuredQName functionName = symbolicName.getComponentName();
        int arity = symbolicName.getArity();
        if (functionName.hasURI(this.getNamespace()) && this.getFunctionDetails(functionName.getLocalPart(), arity) != null) {
            RetainedStaticContext rsc = staticContext.makeRetainedStaticContext();
            SystemFunction fn = this.makeFunction(functionName.getLocalPart(), arity);
            fn.setRetainedStaticContext(rsc);
            return fn;
        }
        return null;
    }

    protected Entry register(String name, int arity, Class<? extends SystemFunction> implementationClass, ItemType itemType, int cardinality, int properties) {
        Entry e = new Entry();
        e.name = new StructuredQName(this.getConventionalPrefix(), this.getNamespace(), name);
        e.arity = arity;
        e.implementationClass = implementationClass;
        e.itemType = itemType;
        e.cardinality = cardinality;
        e.properties = properties;
        if (e.arity == -1) {
            e.argumentTypes = new SequenceType[1];
            e.resultIfEmpty = new AtomicValue[1];
            e.usage = new OperandUsage[1];
        } else {
            e.argumentTypes = new SequenceType[arity];
            e.resultIfEmpty = new Sequence[arity];
            e.usage = new OperandUsage[arity];
        }
        this.functionTable.put(name + "#" + arity, e);
        return e;
    }

    protected void registerReducedArityVariants(String key, int min, int max) {
        Entry master = this.functionTable.get(key);
        for (int arity = min; arity <= max; ++arity) {
            Entry e = new Entry();
            e.name = master.name;
            e.arity = arity;
            e.implementationClass = master.implementationClass;
            e.itemType = master.itemType;
            e.cardinality = master.cardinality;
            e.properties = master.properties;
            e.argumentTypes = new SequenceType[arity];
            e.resultIfEmpty = new Sequence[arity];
            e.usage = new OperandUsage[arity];
            for (int i = 0; i < arity; ++i) {
                e.argumentTypes[i] = master.argumentTypes[i];
                e.resultIfEmpty[i] = master.resultIfEmpty[i];
                e.usage[i] = master.usage[i];
            }
            this.functionTable.put(e.name.getLocalPart() + "#" + arity, e);
        }
    }

    public String getNamespace() {
        return "http://www.w3.org/2005/xpath-functions";
    }

    public String getConventionalPrefix() {
        return "fn";
    }

    public static class Entry {
        public StructuredQName name;
        public Class implementationClass;
        public int arity;
        public ItemType itemType;
        public int cardinality;
        public OperandUsage[] usage;
        public SequenceType[] argumentTypes;
        public Sequence[] resultIfEmpty;
        public int properties;
        public OptionsParameter optionDetails;

        public Entry arg(int a, ItemType type, int options, Sequence resultIfEmpty) {
            int cardinality = options & 0xE000;
            OperandUsage usage = OperandUsage.NAVIGATION;
            if ((options & 0x2000000) != 0) {
                usage = OperandUsage.ABSORPTION;
            } else if ((options & 0x4000000) != 0) {
                usage = OperandUsage.TRANSMISSION;
            } else if ((options & 0x1000000) != 0) {
                usage = OperandUsage.INSPECTION;
            } else if (type instanceof PlainType) {
                usage = OperandUsage.ABSORPTION;
            }
            try {
                this.argumentTypes[a] = SequenceType.makeSequenceType(type, cardinality);
                this.resultIfEmpty[a] = resultIfEmpty;
                this.usage[a] = usage;
            } catch (ArrayIndexOutOfBoundsException err) {
                System.err.println("Internal Saxon error: Can't set argument " + a + " of " + this.name);
            }
            return this;
        }

        public Entry optionDetails(OptionsParameter details) {
            this.optionDetails = details;
            return this;
        }
    }
}

