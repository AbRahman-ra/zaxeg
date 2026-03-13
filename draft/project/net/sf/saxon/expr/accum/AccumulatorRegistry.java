/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.accum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sf.saxon.expr.accum.Accumulator;
import net.sf.saxon.functions.AccumulatorFn;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.style.StyleElement;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Whitespace;

public class AccumulatorRegistry {
    protected Map<StructuredQName, Accumulator> accumulatorsByName = new HashMap<StructuredQName, Accumulator>();

    public Set<Accumulator> getUsedAccumulators(String useAccumulatorsAtt, StyleElement styleElement) {
        HashSet<Accumulator> accumulators;
        block5: {
            String[] tokens;
            block6: {
                accumulators = new HashSet<Accumulator>();
                String attNames = Whitespace.trim(useAccumulatorsAtt);
                tokens = attNames.split("[ \t\r\n]+");
                if (tokens.length != 1 || !tokens[0].equals("#all")) break block6;
                for (Accumulator acc : this.getAllAccumulators()) {
                    accumulators.add(acc);
                }
                break block5;
            }
            if (tokens.length == 1 && tokens[0].isEmpty()) break block5;
            ArrayList<StructuredQName> names = new ArrayList<StructuredQName>(tokens.length);
            for (String token : tokens) {
                if (token.equals("#all")) {
                    styleElement.compileErrorInAttribute("If use-accumulators contains the token '#all', it must be the only token", "XTSE3300", "use-accumulators");
                    break;
                }
                StructuredQName name = styleElement.makeQName(token, "XTSE3300", "use-accumulators");
                if (names.contains(name)) {
                    styleElement.compileErrorInAttribute("Duplicate QName in use-accumulators attribute: " + token, "XTSE3300", "use-accumuators");
                    break;
                }
                Accumulator acc = this.getAccumulator(name);
                if (acc == null) {
                    styleElement.compileErrorInAttribute("Unknown accumulator name: " + token, "XTSE3300", "use-accumulators");
                    break;
                }
                names.add(name);
                accumulators.add(acc);
            }
        }
        return accumulators;
    }

    public void addAccumulator(Accumulator acc) {
        if (acc.getAccumulatorName() != null) {
            this.accumulatorsByName.put(acc.getAccumulatorName(), acc);
        }
    }

    public Accumulator getAccumulator(StructuredQName name) {
        return this.accumulatorsByName.get(name);
    }

    public Iterable<Accumulator> getAllAccumulators() {
        return this.accumulatorsByName.values();
    }

    public Sequence getStreamingAccumulatorValue(NodeInfo node, Accumulator accumulator, AccumulatorFn.Phase phase) throws XPathException {
        return null;
    }
}

