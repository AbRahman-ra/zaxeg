/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.regex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntPredicate;
import net.sf.saxon.regex.BMPString;
import net.sf.saxon.regex.CaseVariants;
import net.sf.saxon.regex.GeneralUnicodeString;
import net.sf.saxon.regex.Operation;
import net.sf.saxon.regex.REFlags;
import net.sf.saxon.regex.REProgram;
import net.sf.saxon.regex.RESyntaxException;
import net.sf.saxon.regex.UnicodeBlocks;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.regex.charclass.Categories;
import net.sf.saxon.regex.charclass.CharacterClass;
import net.sf.saxon.regex.charclass.EmptyCharacterClass;
import net.sf.saxon.regex.charclass.IntSetCharacterClass;
import net.sf.saxon.regex.charclass.InverseCharacterClass;
import net.sf.saxon.regex.charclass.PredicateCharacterClass;
import net.sf.saxon.regex.charclass.SingletonCharacterClass;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.Whitespace;
import net.sf.saxon.z.IntExceptPredicate;
import net.sf.saxon.z.IntHashSet;
import net.sf.saxon.z.IntRangeSet;
import net.sf.saxon.z.IntSet;
import net.sf.saxon.z.IntSetPredicate;
import net.sf.saxon.z.IntSingletonSet;
import net.sf.saxon.z.IntValuePredicate;

public class RECompiler {
    UnicodeString pattern;
    int len;
    int idx;
    int capturingOpenParenCount;
    static final int NODE_NORMAL = 0;
    static final int NODE_TOPLEVEL = 2;
    int bracketMin;
    int bracketMax;
    boolean isXPath = true;
    boolean isXPath30 = true;
    boolean isXSD11 = false;
    IntHashSet captures = new IntHashSet();
    boolean hasBackReferences = false;
    REFlags reFlags;
    List<String> warnings;
    private static final boolean TRACING = false;

    public void setFlags(REFlags flags) {
        this.reFlags = flags;
        this.isXPath = flags.isAllowsXPath20Extensions();
        this.isXPath30 = flags.isAllowsXPath30Extensions();
        this.isXSD11 = flags.isAllowsXSD11Syntax();
    }

    private void warning(String s) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<String>(4);
        }
        this.warnings.add(s);
    }

    public List<String> getWarnings() {
        if (this.warnings == null) {
            return Collections.emptyList();
        }
        return this.warnings;
    }

    void internalError() throws Error {
        throw new Error("Internal error!");
    }

    void syntaxError(String s) throws RESyntaxException {
        throw new RESyntaxException(s, this.idx);
    }

    static Operation trace(Operation base) {
        return base;
    }

    void bracket() throws RESyntaxException {
        if (this.idx >= this.len || this.pattern.uCharAt(this.idx++) != 123) {
            this.internalError();
        }
        if (this.idx >= this.len || !RECompiler.isAsciiDigit(this.pattern.uCharAt(this.idx))) {
            this.syntaxError("Expected digit");
        }
        FastStringBuffer number = new FastStringBuffer(16);
        while (this.idx < this.len && RECompiler.isAsciiDigit(this.pattern.uCharAt(this.idx))) {
            number.cat((char)this.pattern.uCharAt(this.idx++));
        }
        try {
            this.bracketMin = Integer.parseInt(number.toString());
        } catch (NumberFormatException e) {
            this.syntaxError("Expected valid number");
        }
        if (this.idx >= this.len) {
            this.syntaxError("Expected comma or right bracket");
        }
        if (this.pattern.uCharAt(this.idx) == 125) {
            ++this.idx;
            this.bracketMax = this.bracketMin;
            return;
        }
        if (this.idx >= this.len || this.pattern.uCharAt(this.idx++) != 44) {
            this.syntaxError("Expected comma");
        }
        if (this.idx >= this.len) {
            this.syntaxError("Expected comma or right bracket");
        }
        if (this.pattern.uCharAt(this.idx) == 125) {
            ++this.idx;
            this.bracketMax = Integer.MAX_VALUE;
            return;
        }
        if (this.idx >= this.len || !RECompiler.isAsciiDigit(this.pattern.uCharAt(this.idx))) {
            this.syntaxError("Expected digit");
        }
        number.setLength(0);
        while (this.idx < this.len && RECompiler.isAsciiDigit(this.pattern.uCharAt(this.idx))) {
            number.cat((char)this.pattern.uCharAt(this.idx++));
        }
        try {
            this.bracketMax = Integer.parseInt(number.toString());
        } catch (NumberFormatException e) {
            this.syntaxError("Expected valid number");
        }
        if (this.bracketMax < this.bracketMin) {
            this.syntaxError("Bad range");
        }
        if (this.idx >= this.len || this.pattern.uCharAt(this.idx++) != 125) {
            this.syntaxError("Missing close brace");
        }
    }

    private static boolean isAsciiDigit(int ch) {
        return ch >= 48 && ch <= 57;
    }

    CharacterClass escape(boolean inSquareBrackets) throws RESyntaxException {
        if (this.pattern.uCharAt(this.idx) != 92) {
            this.internalError();
        }
        if (this.idx + 1 == this.len) {
            this.syntaxError("Escape terminates string");
        }
        this.idx += 2;
        int escapeChar = this.pattern.uCharAt(this.idx - 1);
        switch (escapeChar) {
            case 110: {
                return new SingletonCharacterClass(10);
            }
            case 114: {
                return new SingletonCharacterClass(13);
            }
            case 116: {
                return new SingletonCharacterClass(9);
            }
            case 40: 
            case 41: 
            case 42: 
            case 43: 
            case 45: 
            case 46: 
            case 63: 
            case 91: 
            case 92: 
            case 93: 
            case 94: 
            case 123: 
            case 124: 
            case 125: {
                return new SingletonCharacterClass(escapeChar);
            }
            case 36: {
                if (this.isXPath) {
                    return new SingletonCharacterClass(escapeChar);
                }
                this.syntaxError("In XSD, '$' must not be escaped");
            }
            case 115: {
                return Categories.ESCAPE_s;
            }
            case 83: {
                return Categories.ESCAPE_S;
            }
            case 105: {
                return Categories.ESCAPE_i;
            }
            case 73: {
                return Categories.ESCAPE_I;
            }
            case 99: {
                return Categories.ESCAPE_c;
            }
            case 67: {
                return Categories.ESCAPE_C;
            }
            case 100: {
                return Categories.ESCAPE_d;
            }
            case 68: {
                return Categories.ESCAPE_D;
            }
            case 119: {
                return Categories.ESCAPE_w;
            }
            case 87: {
                return Categories.ESCAPE_W;
            }
            case 80: 
            case 112: {
                UnicodeString block;
                int close;
                if (this.idx == this.len) {
                    this.syntaxError("Expected '{' after \\" + escapeChar);
                }
                if (this.pattern.uCharAt(this.idx) != 123) {
                    this.syntaxError("Expected '{' after \\" + escapeChar);
                }
                if ((close = this.pattern.uIndexOf(125, this.idx++)) == -1) {
                    this.syntaxError("No closing '}' after \\" + escapeChar);
                }
                if ((block = this.pattern.uSubstring(this.idx, close)).uLength() == 1 || block.uLength() == 2) {
                    Categories.Category primary = Categories.getCategory(block.toString());
                    if (primary == null) {
                        this.syntaxError("Unknown character category " + block.toString());
                    }
                    this.idx = close + 1;
                    if (escapeChar == 112) {
                        return primary;
                    }
                    return RECompiler.makeComplement(primary);
                }
                if (block.toString().startsWith("Is")) {
                    String blockName = block.toString().substring(2);
                    IntSet uniBlock = UnicodeBlocks.getBlock(blockName);
                    if (uniBlock == null) {
                        if (this.reFlags.isAllowUnknownBlockNames()) {
                            this.warning("Unknown Unicode block: " + blockName);
                            this.idx = close + 1;
                            return EmptyCharacterClass.getComplement();
                        }
                        this.syntaxError("Unknown Unicode block: " + blockName);
                    }
                    this.idx = close + 1;
                    IntSetCharacterClass primary = new IntSetCharacterClass(uniBlock);
                    if (escapeChar == 112) {
                        return primary;
                    }
                    return RECompiler.makeComplement(primary);
                }
                this.syntaxError("Unknown character category: " + block);
            }
            case 48: {
                this.syntaxError("Octal escapes not allowed");
            }
            case 49: 
            case 50: 
            case 51: 
            case 52: 
            case 53: 
            case 54: 
            case 55: 
            case 56: 
            case 57: {
                if (inSquareBrackets) {
                    this.syntaxError("Backreference not allowed within character class");
                    break;
                }
                if (this.isXPath) {
                    int backRef2;
                    int c1;
                    int backRef = escapeChar - 48;
                    while (this.idx < this.len && (c1 = "0123456789".indexOf(this.pattern.uCharAt(this.idx))) >= 0 && (backRef2 = backRef * 10 + c1) <= this.capturingOpenParenCount - 1) {
                        backRef = backRef2;
                        ++this.idx;
                    }
                    if (!this.captures.contains(backRef)) {
                        String explanation = backRef > this.capturingOpenParenCount - 1 ? "(no such group)" : "(group not yet closed)";
                        this.syntaxError("invalid backreference \\" + backRef + " " + explanation);
                    }
                    this.hasBackReferences = true;
                    return new BackReference(backRef);
                }
                this.syntaxError("digit not allowed after \\");
            }
        }
        this.syntaxError("Escape character '" + (char)escapeChar + "' not allowed");
        return null;
    }

    CharacterClass parseCharacterClass() throws RESyntaxException {
        if (this.pattern.uCharAt(this.idx) != 91) {
            this.internalError();
        }
        if (this.idx + 1 >= this.len || this.pattern.uCharAt(++this.idx) == 93) {
            this.syntaxError("Missing ']'");
        }
        boolean positive = true;
        boolean definingRange = false;
        int rangeStart = -1;
        IntRangeSet range = new IntRangeSet();
        CharacterClass addend = null;
        CharacterClass subtrahend = null;
        if (this.thereFollows("^")) {
            if (this.thereFollows("^-[")) {
                this.syntaxError("Nothing before subtraction operator");
            } else if (this.thereFollows("^]")) {
                this.syntaxError("Empty negative character group");
            } else {
                positive = false;
                ++this.idx;
            }
        } else if (this.thereFollows("-[")) {
            this.syntaxError("Nothing before subtraction operator");
        }
        block5: while (this.idx < this.len && this.pattern.uCharAt(this.idx) != 93) {
            int[] variants;
            int ch = this.pattern.uCharAt(this.idx);
            int simpleChar = -1;
            switch (ch) {
                case 91: {
                    this.syntaxError("Unescaped '[' within square brackets");
                    break;
                }
                case 92: {
                    CharacterClass cc = this.escape(true);
                    if (cc instanceof SingletonCharacterClass) {
                        simpleChar = ((SingletonCharacterClass)cc).getCodepoint();
                        break;
                    }
                    if (definingRange) {
                        this.syntaxError("Multi-character escape cannot follow '-'");
                        continue block5;
                    }
                    if (addend == null) {
                        addend = cc;
                        continue block5;
                    }
                    addend = RECompiler.makeUnion(addend, cc);
                    continue block5;
                }
                case 45: {
                    if (this.thereFollows("-[")) {
                        ++this.idx;
                        subtrahend = this.parseCharacterClass();
                        if (this.thereFollows("]")) break;
                        this.syntaxError("Expected closing ']' after subtraction");
                        break;
                    }
                    if (this.thereFollows("-]")) {
                        simpleChar = 45;
                        ++this.idx;
                        break;
                    }
                    if (rangeStart >= 0) {
                        definingRange = true;
                        ++this.idx;
                        continue block5;
                    }
                    if (definingRange) {
                        this.syntaxError("Bad range");
                        break;
                    }
                    if (this.thereFollows("--") && !this.thereFollows("--[")) {
                        this.syntaxError("Unescaped hyphen as start of range");
                        break;
                    }
                    if (!(this.isXSD11 || this.pattern.uCharAt(this.idx - 1) == 91 || this.pattern.uCharAt(this.idx - 1) == 94 || this.thereFollows("]") || this.thereFollows("-["))) {
                        this.syntaxError("In XSD 1.0, hyphen is allowed only at the beginning or end of a positive character group");
                        break;
                    }
                    simpleChar = 45;
                    ++this.idx;
                    break;
                }
                default: {
                    simpleChar = ch;
                    ++this.idx;
                }
            }
            if (definingRange) {
                int rangeEnd = simpleChar;
                if (rangeStart > rangeEnd) {
                    this.syntaxError("Bad character range: start > end");
                }
                range.addRange(rangeStart, rangeEnd);
                if (this.reFlags.isCaseIndependent()) {
                    if (rangeStart == 97 && rangeEnd == 122) {
                        range.addRange(65, 90);
                        for (int v = 0; v < CaseVariants.ROMAN_VARIANTS.length; ++v) {
                            range.add(CaseVariants.ROMAN_VARIANTS[v]);
                        }
                    } else if (rangeStart == 65 && rangeEnd == 90) {
                        range.addRange(97, 122);
                        for (int v = 0; v < CaseVariants.ROMAN_VARIANTS.length; ++v) {
                            range.add(CaseVariants.ROMAN_VARIANTS[v]);
                        }
                    } else {
                        for (int k = rangeStart; k <= rangeEnd; ++k) {
                            int[] variants2;
                            int[] nArray = variants2 = CaseVariants.getCaseVariants(k);
                            int n = nArray.length;
                            for (int i = 0; i < n; ++i) {
                                int variant = nArray[i];
                                range.add(variant);
                            }
                        }
                    }
                }
                definingRange = false;
                rangeStart = -1;
                continue;
            }
            if (this.thereFollows("-")) {
                if (this.thereFollows("-[")) {
                    range.add(simpleChar);
                    continue;
                }
                if (this.thereFollows("-]")) {
                    range.add(simpleChar);
                    continue;
                }
                if (this.thereFollows("--[")) {
                    range.add(simpleChar);
                    continue;
                }
                if (this.thereFollows("--")) {
                    this.syntaxError("Unescaped hyphen cannot act as end of range");
                    continue;
                }
                rangeStart = simpleChar;
                continue;
            }
            range.add(simpleChar);
            if (!this.reFlags.isCaseIndependent()) continue;
            for (int variant : variants = CaseVariants.getCaseVariants(simpleChar)) {
                range.add(variant);
            }
        }
        if (this.idx == this.len) {
            this.syntaxError("Unterminated character class");
        }
        ++this.idx;
        CharacterClass result = new IntSetCharacterClass(range);
        if (addend != null) {
            result = RECompiler.makeUnion(result, addend);
        }
        if (!positive) {
            result = RECompiler.makeComplement(result);
        }
        if (subtrahend != null) {
            result = RECompiler.makeDifference(result, subtrahend);
        }
        return result;
    }

    private boolean thereFollows(String s) {
        return this.idx + s.length() <= this.len && this.pattern.uSubstring(this.idx, this.idx + s.length()).toString().equals(s);
    }

    public static CharacterClass makeUnion(CharacterClass p1, CharacterClass p2) {
        if (p1 == EmptyCharacterClass.getInstance()) {
            return p2;
        }
        if (p2 == EmptyCharacterClass.getInstance()) {
            return p1;
        }
        IntSet is1 = p1.getIntSet();
        IntSet is2 = p2.getIntSet();
        if (is1 == null || is2 == null) {
            return new PredicateCharacterClass(p1.or(p2));
        }
        return new IntSetCharacterClass(is1.union(is2));
    }

    public static CharacterClass makeDifference(CharacterClass p1, CharacterClass p2) {
        if (p1 == EmptyCharacterClass.getInstance()) {
            return p1;
        }
        if (p2 == EmptyCharacterClass.getInstance()) {
            return p1;
        }
        IntSet is1 = p1.getIntSet();
        IntSet is2 = p2.getIntSet();
        if (is1 == null || is2 == null) {
            return new PredicateCharacterClass(new IntExceptPredicate(p1, p2));
        }
        return new IntSetCharacterClass(is1.except(is2));
    }

    public static CharacterClass makeComplement(CharacterClass p1) {
        if (p1 instanceof InverseCharacterClass) {
            return ((InverseCharacterClass)p1).getComplement();
        }
        return new InverseCharacterClass(p1);
    }

    /*
     * Unable to fully structure code
     */
    Operation parseAtom() throws RESyntaxException {
        lenAtom = 0;
        fsb = new FastStringBuffer(64);
        block10: while (this.idx < this.len) {
            if (this.idx + 1 >= this.len) ** GOTO lbl-1000
            c = this.pattern.uCharAt(this.idx + 1);
            if (this.pattern.uCharAt(this.idx) == 92) {
                idxEscape = this.idx;
                this.escape(false);
                if (this.idx < this.len) {
                    c = this.pattern.uCharAt(this.idx);
                }
                this.idx = idxEscape;
            }
            switch (c) {
                case 42: 
                case 43: 
                case 63: 
                case 123: {
                    if (lenAtom != 0) break block10;
                }
                default: lbl-1000:
                // 2 sources

                {
                    switch (this.pattern.uCharAt(this.idx)) {
                        case 40: 
                        case 41: 
                        case 46: 
                        case 91: 
                        case 93: 
                        case 124: {
                            break block10;
                        }
                        case 42: 
                        case 43: 
                        case 63: 
                        case 123: {
                            if (lenAtom != 0) break block10;
                            this.syntaxError("No expression before quantifier");
                            break block10;
                        }
                        case 125: {
                            this.syntaxError("Unescaped right curly brace");
                            break block10;
                        }
                        case 92: {
                            idxBeforeEscape = this.idx;
                            charClass = this.escape(false);
                            if (charClass instanceof BackReference || !(charClass instanceof IntValuePredicate)) {
                                this.idx = idxBeforeEscape;
                                break block10;
                            }
                            fsb.appendWideChar(((IntValuePredicate)charClass).getTarget());
                            ++lenAtom;
                            continue block10;
                        }
                        case 36: 
                        case 94: {
                            if (this.isXPath) break block10;
                        }
                        default: {
                            fsb.appendWideChar(this.pattern.uCharAt(this.idx++));
                            ++lenAtom;
                            continue block10;
                        }
                    }
                }
            }
        }
        if (fsb.isEmpty()) {
            this.internalError();
        }
        return RECompiler.trace(new Operation.OpAtom(UnicodeString.makeUnicodeString(fsb.condense())));
    }

    Operation parseTerminal(int[] flags) throws RESyntaxException {
        switch (this.pattern.uCharAt(this.idx)) {
            case 36: {
                if (!this.isXPath) break;
                ++this.idx;
                return RECompiler.trace(new Operation.OpEOL());
            }
            case 94: {
                if (!this.isXPath) break;
                ++this.idx;
                return RECompiler.trace(new Operation.OpBOL());
            }
            case 46: {
                ++this.idx;
                IntPredicate predicate = this.reFlags.isSingleLine() ? IntSetPredicate.ALWAYS_TRUE : value -> value != 10 && value != 13;
                return RECompiler.trace(new Operation.OpCharClass(predicate));
            }
            case 91: {
                CharacterClass range = this.parseCharacterClass();
                return RECompiler.trace(new Operation.OpCharClass(range));
            }
            case 40: {
                return this.parseExpr(flags);
            }
            case 41: {
                this.syntaxError("Unexpected closing ')'");
            }
            case 124: {
                this.internalError();
            }
            case 93: {
                this.syntaxError("Unexpected closing ']'");
            }
            case 0: {
                this.syntaxError("Unexpected end of input");
            }
            case 42: 
            case 43: 
            case 63: 
            case 123: {
                this.syntaxError("No expression before quantifier");
            }
            case 92: {
                int idxBeforeEscape = this.idx;
                CharacterClass esc = this.escape(false);
                if (esc instanceof BackReference) {
                    int backreference = ((BackReference)esc).getCodepoint();
                    if (this.capturingOpenParenCount <= backreference) {
                        this.syntaxError("Bad backreference");
                    }
                    return RECompiler.trace(new Operation.OpBackReference(backreference));
                }
                if (esc instanceof IntSingletonSet) {
                    this.idx = idxBeforeEscape;
                    break;
                }
                return RECompiler.trace(new Operation.OpCharClass(esc));
            }
        }
        return this.parseAtom();
    }

    Operation piece(int[] flags) throws RESyntaxException {
        Operation result;
        int[] terminalFlags = new int[]{0};
        Operation ret = this.parseTerminal(terminalFlags);
        flags[0] = flags[0] | terminalFlags[0];
        if (this.idx >= this.len) {
            return ret;
        }
        boolean greedy = true;
        int quantifierType = this.pattern.uCharAt(this.idx);
        switch (quantifierType) {
            case 42: 
            case 43: 
            case 63: {
                ++this.idx;
            }
            case 123: {
                if (quantifierType == 123) {
                    this.bracket();
                }
                if (ret instanceof Operation.OpBOL || ret instanceof Operation.OpEOL) {
                    if (quantifierType == 63 || quantifierType == 42 || quantifierType == 123 && this.bracketMin == 0) {
                        return new Operation.OpNothing();
                    }
                    quantifierType = 0;
                }
                if (ret.matchesEmptyString() != 7) break;
                if (quantifierType == 63) {
                    quantifierType = 0;
                    break;
                }
                if (quantifierType == 43) {
                    quantifierType = 42;
                    break;
                }
                if (quantifierType != 123) break;
                quantifierType = 42;
            }
        }
        if (this.idx < this.len && this.pattern.uCharAt(this.idx) == 63) {
            if (!this.isXPath) {
                this.syntaxError("Reluctant quantifiers are not allowed in XSD");
            }
            ++this.idx;
            greedy = false;
        }
        int min = 1;
        int max = 1;
        switch (quantifierType) {
            case 123: {
                min = this.bracketMin;
                max = this.bracketMax;
                break;
            }
            case 63: {
                min = 0;
                max = 1;
                break;
            }
            case 43: {
                min = 1;
                max = Integer.MAX_VALUE;
                break;
            }
            case 42: {
                min = 0;
                max = Integer.MAX_VALUE;
            }
        }
        if (max == 0) {
            result = new Operation.OpNothing();
        } else {
            if (min == 1 && max == 1) {
                return ret;
            }
            result = greedy ? (ret.getMatchLength() == -1 ? RECompiler.trace(new Operation.OpRepeat(ret, min, max, true)) : new Operation.OpGreedyFixed(ret, min, max, ret.getMatchLength())) : (ret.getMatchLength() == -1 ? new Operation.OpRepeat(ret, min, max, false) : new Operation.OpReluctantFixed(ret, min, max, ret.getMatchLength()));
        }
        return RECompiler.trace(result);
    }

    Operation parseBranch() throws RESyntaxException {
        Operation current = null;
        int[] quantifierFlags = new int[1];
        while (this.idx < this.len && this.pattern.uCharAt(this.idx) != 124 && this.pattern.uCharAt(this.idx) != 41) {
            quantifierFlags[0] = 0;
            Operation op = this.piece(quantifierFlags);
            if (current == null) {
                current = op;
                continue;
            }
            current = RECompiler.makeSequence(current, op);
        }
        if (current == null) {
            return new Operation.OpNothing();
        }
        return current;
    }

    private Operation parseExpr(int[] compilerFlags) throws RESyntaxException {
        int paren = -1;
        int group = 0;
        ArrayList<Operation> branches = new ArrayList<Operation>();
        int closeParens = this.capturingOpenParenCount;
        boolean capturing = true;
        if ((compilerFlags[0] & 2) == 0 && this.pattern.uCharAt(this.idx) == 40) {
            if (this.idx + 2 < this.len && this.pattern.uCharAt(this.idx + 1) == 63 && this.pattern.uCharAt(this.idx + 2) == 58) {
                if (!this.isXPath30) {
                    this.syntaxError("Non-capturing groups allowed only in XPath3.0");
                }
                paren = 2;
                this.idx += 3;
                capturing = false;
            } else {
                paren = 1;
                ++this.idx;
                group = this.capturingOpenParenCount++;
            }
        }
        compilerFlags[0] = compilerFlags[0] & 0xFFFFFFFD;
        branches.add(this.parseBranch());
        while (this.idx < this.len && this.pattern.uCharAt(this.idx) == 124) {
            ++this.idx;
            branches.add(this.parseBranch());
        }
        Operation op = branches.size() == 1 ? (Operation)branches.get(0) : new Operation.OpChoice(branches);
        if (paren > 0) {
            if (this.idx < this.len && this.pattern.uCharAt(this.idx) == 41) {
                ++this.idx;
            } else {
                this.syntaxError("Missing close paren");
            }
            if (capturing) {
                op = new Operation.OpCapture(op, group);
                this.captures.add(closeParens);
            }
        } else {
            op = RECompiler.makeSequence(op, new Operation.OpEndProgram());
        }
        return op;
    }

    private static Operation makeSequence(Operation o1, Operation o2) {
        if (o1 instanceof Operation.OpSequence) {
            if (o2 instanceof Operation.OpSequence) {
                List<Operation> l1 = ((Operation.OpSequence)o1).getOperations();
                List<Operation> l2 = ((Operation.OpSequence)o2).getOperations();
                l1.addAll(l2);
                return o1;
            }
            List<Operation> l1 = ((Operation.OpSequence)o1).getOperations();
            l1.add(o2);
            return o1;
        }
        if (o2 instanceof Operation.OpSequence) {
            List<Operation> l2 = ((Operation.OpSequence)o2).getOperations();
            l2.add(0, o1);
            return o2;
        }
        ArrayList<Operation> list = new ArrayList<Operation>(4);
        list.add(o1);
        list.add(o2);
        return RECompiler.trace(new Operation.OpSequence(list));
    }

    public REProgram compile(UnicodeString pattern) throws RESyntaxException {
        this.pattern = pattern;
        this.len = pattern.uLength();
        this.idx = 0;
        this.capturingOpenParenCount = 1;
        if (this.reFlags.isLiteral()) {
            Operation.OpAtom ret = new Operation.OpAtom(this.pattern);
            Operation.OpEndProgram endNode = new Operation.OpEndProgram();
            Operation seq = RECompiler.makeSequence(ret, endNode);
            return new REProgram(seq, this.capturingOpenParenCount, this.reFlags);
        }
        if (this.reFlags.isAllowWhitespace()) {
            FastStringBuffer sb = new FastStringBuffer(pattern.uLength());
            int nesting = 0;
            boolean astral = false;
            boolean escaped = false;
            for (int i = 0; i < pattern.uLength(); ++i) {
                int ch = pattern.uCharAt(i);
                if (ch > 65535) {
                    astral = true;
                }
                if (ch == 92 && !escaped) {
                    escaped = true;
                    sb.appendWideChar(ch);
                    continue;
                }
                if (ch == 91 && !escaped) {
                    ++nesting;
                    escaped = false;
                    sb.appendWideChar(ch);
                    continue;
                }
                if (ch == 93 && !escaped) {
                    --nesting;
                    escaped = false;
                    sb.appendWideChar(ch);
                    continue;
                }
                if (nesting == 0 && Whitespace.isWhitespace(ch)) continue;
                escaped = false;
                sb.appendWideChar(ch);
            }
            pattern = astral ? new GeneralUnicodeString(sb) : new BMPString(sb);
            this.pattern = pattern;
            this.len = pattern.uLength();
        }
        int[] compilerFlags = new int[]{2};
        Operation exp = this.parseExpr(compilerFlags);
        if (this.idx != this.len) {
            if (pattern.uCharAt(this.idx) == 41) {
                this.syntaxError("Unmatched close paren");
            }
            this.syntaxError("Unexpected input remains");
        }
        REProgram program = new REProgram(exp, this.capturingOpenParenCount, this.reFlags);
        if (this.hasBackReferences) {
            program.optimizationFlags |= 1;
        }
        return program;
    }

    static boolean noAmbiguity(Operation op0, Operation op1, boolean caseBlind, boolean reluctant) {
        if (op1 instanceof Operation.OpEndProgram) {
            return !reluctant;
        }
        if (op1 instanceof Operation.OpBOL || op1 instanceof Operation.OpEOL) {
            return true;
        }
        if (op1 instanceof Operation.OpRepeat && ((Operation.OpRepeat)op1).min == 0) {
            return false;
        }
        CharacterClass c0 = op0.getInitialCharacterClass(caseBlind);
        CharacterClass c1 = op1.getInitialCharacterClass(caseBlind);
        return c0.isDisjoint(c1);
    }

    class BackReference
    extends SingletonCharacterClass {
        public BackReference(int number) {
            super(number);
        }
    }
}

