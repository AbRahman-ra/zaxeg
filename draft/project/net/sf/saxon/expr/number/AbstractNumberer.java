/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.number;

import java.util.Locale;
import net.sf.saxon.expr.number.Alphanumeric;
import net.sf.saxon.expr.number.NumericGroupFormatter;
import net.sf.saxon.expr.number.RegularGroupFormatter;
import net.sf.saxon.lib.Numberer;
import net.sf.saxon.regex.EmptyString;
import net.sf.saxon.regex.UnicodeString;
import net.sf.saxon.tree.util.FastStringBuffer;

public abstract class AbstractNumberer
implements Numberer {
    private String country;
    private String language;
    public static final int UPPER_CASE = 0;
    public static final int LOWER_CASE = 1;
    public static final int TITLE_CASE = 2;
    protected static final int[] westernDigits = new int[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57};
    protected static final String latinUpper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    protected static final String latinLower = "abcdefghijklmnopqrstuvwxyz";
    protected static final String greekUpper = "\u0391\u0392\u0393\u0394\u0395\u0396\u0397\u0398\u0399\u039a\u039b\u039c\u039d\u039e\u039f\u03a0\u03a1\u03a2\u03a3\u03a4\u03a5\u03a6\u03a7\u03a8\u03a9";
    protected static final String greekLower = "\u03b1\u03b2\u03b3\u03b4\u03b5\u03b6\u03b7\u03b8\u03b9\u03ba\u03bb\u03bc\u03bd\u03be\u03bf\u03c0\u03c1\u03c2\u03c3\u03c4\u03c5\u03c6\u03c7\u03c8\u03c9";
    protected static final String cyrillicUpper = "\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417\u0418\u041a\u041b\u041c\u041d\u041e\u041f\u0420\u0421\u0421\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042b\u042d\u042e\u042f";
    protected static final String cyrillicLower = "\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438\u043a\u043b\u043c\u043d\u043e\u043f\u0440\u0441\u0441\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044b\u044d\u044e\u044f";
    protected static final String hebrew = "\u05d0\u05d1\u05d2\u05d3\u05d4\u05d5\u05d6\u05d7\u05d8\u05d9\u05db\u05dc\u05de\u05e0\u05e1\u05e2\u05e4\u05e6\u05e7\u05e8\u05e9\u05ea";
    protected static final String hiraganaA = "\u3042\u3044\u3046\u3048\u304a\u304b\u304d\u304f\u3051\u3053\u3055\u3057\u3059\u305b\u305d\u305f\u3061\u3064\u3066\u3068\u306a\u306b\u306c\u306d\u306e\u306f\u3072\u3075\u3078\u307b\u307e\u307f\u3080\u3081\u3082\u3084\u3086\u3088\u3089\u308a\u308b\u308c\u308d\u308f\u3092\u3093";
    protected static final String katakanaA = "\u30a2\u30a4\u30a6\u30a8\u30aa\u30ab\u30ad\u30af\u30b1\u30b3\u30b5\u30b7\u30b9\u30bb\u30bd\u30bf\u30c1\u30c4\u30c6\u30c8\u30ca\u30cb\u30cc\u30cd\u30ce\u30cf\u30d2\u30d5\u30d8\u30db\u30de\u30df\u30e0\u30e1\u30e2\u30e4\u30e6\u30e8\u30e9\u30ea\u30eb\u30ec\u30ed\u30ef\u30f2\u30f3";
    protected static final String hiraganaI = "\u3044\u308d\u306f\u306b\u307b\u3078\u3068\u3061\u308a\u306c\u308b\u3092\u308f\u304b\u3088\u305f\u308c\u305d\u3064\u306d\u306a\u3089\u3080\u3046\u3090\u306e\u304a\u304f\u3084\u307e\u3051\u3075\u3053\u3048\u3066\u3042\u3055\u304d\u3086\u3081\u307f\u3057\u3091\u3072\u3082\u305b\u3059";
    protected static final String katakanaI = "\u30a4\u30ed\u30cf\u30cb\u30db\u30d8\u30c8\u30c1\u30ea\u30cc\u30eb\u30f2\u30ef\u30ab\u30e8\u30bf\u30ec\u30bd\u30c4\u30cd\u30ca\u30e9\u30e0\u30a6\u30f0\u30ce\u30aa\u30af\u30e4\u30de\u30b1\u30d5\u30b3\u30a8\u30c6\u30a2\u30b5\u30ad\u30e6\u30e1\u30df\u30b7\u30f1\u30d2\u30e2\u30bb\u30b9";
    private static String[] romanThousands = new String[]{"", "m", "mm", "mmm", "mmmm", "mmmmm", "mmmmmm", "mmmmmmm", "mmmmmmmm", "mmmmmmmmm"};
    private static String[] romanHundreds = new String[]{"", "c", "cc", "ccc", "cd", "d", "dc", "dcc", "dccc", "cm"};
    private static String[] romanTens = new String[]{"", "x", "xx", "xxx", "xl", "l", "lx", "lxx", "lxxx", "xc"};
    private static String[] romanUnits = new String[]{"", "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix"};
    private static final int[] kanjiDigits = new int[]{12295, 19968, 20108, 19977, 22235, 20116, 20845, 19971, 20843, 20061};

    @Override
    public Locale defaultedLocale() {
        return null;
    }

    @Override
    public void setCountry(String country) {
        this.country = country;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguage() {
        return this.language;
    }

    @Override
    public String getCountry() {
        return this.country;
    }

    @Override
    public final String format(long number, UnicodeString picture, int groupSize, String groupSeparator, String letterValue, String ordinal) {
        return this.format(number, picture, new RegularGroupFormatter(groupSize, groupSeparator, EmptyString.THE_INSTANCE), letterValue, ordinal);
    }

    @Override
    public String format(long number, UnicodeString picture, NumericGroupFormatter numGroupFormatter, String letterValue, String ordinal) {
        if (number < 0L) {
            return "" + number;
        }
        if (picture == null || picture.uLength() == 0) {
            return "" + number;
        }
        int pictureLength = picture.uLength();
        FastStringBuffer sb = new FastStringBuffer(16);
        int formchar = picture.uCharAt(0);
        FastStringBuffer fsb = new FastStringBuffer(2);
        switch (formchar) {
            case 48: 
            case 49: {
                sb.append(this.toRadical(number, westernDigits, pictureLength, numGroupFormatter));
                if (ordinal == null || ordinal.isEmpty()) break;
                sb.append(this.ordinalSuffix(ordinal, number));
                break;
            }
            case 65: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, latinUpper);
            }
            case 97: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, latinLower);
            }
            case 87: 
            case 119: {
                int wordCase = picture.uLength() == 1 ? (formchar == 87 ? 0 : 1) : 2;
                if (ordinal != null && !ordinal.isEmpty()) {
                    return this.toOrdinalWords(ordinal, number, wordCase);
                }
                return this.toWords(number, wordCase);
            }
            case 105: {
                if (number == 0L) {
                    return "0";
                }
                if (letterValue == null || letterValue.isEmpty() || letterValue.equals("traditional")) {
                    return AbstractNumberer.toRoman(number);
                }
                this.alphaDefault(number, 'i', sb);
                break;
            }
            case 73: {
                if (number == 0L) {
                    return "0";
                }
                if (letterValue == null || letterValue.isEmpty() || letterValue.equals("traditional")) {
                    return AbstractNumberer.toRoman(number).toUpperCase();
                }
                this.alphaDefault(number, 'I', sb);
                break;
            }
            case 9312: {
                if (number == 0L) {
                    return "\u24ea";
                }
                if (number > 20L && number <= 35L) {
                    return "" + (char)(12881L + number - 21L);
                }
                if (number > 35L && number <= 50L) {
                    return "" + (char)(12977L + number - 36L);
                }
                if (number > 50L) {
                    return "" + number;
                }
                return "" + (char)(9312L + number - 1L);
            }
            case 9332: {
                if (number == 0L || number > 20L) {
                    return "" + number;
                }
                return "" + (char)(9332L + number - 1L);
            }
            case 9352: {
                if (number == 0L) {
                    return "\ud83c\udd00";
                }
                if (number > 20L) {
                    return "" + number;
                }
                return "" + (char)(9352L + number - 1L);
            }
            case 10102: {
                if (number == 0L) {
                    return "\u24ff";
                }
                if (number > 10L && number <= 20L) {
                    return "" + (char)(9451L + number - 11L);
                }
                if (number > 20L) {
                    return "" + number;
                }
                return "" + (char)(10102L + number - 1L);
            }
            case 10112: {
                if (number == 0L) {
                    return "\ud83c\udd0b";
                }
                if (number > 10L) {
                    return "" + number;
                }
                return "" + (char)(10112L + number - 1L);
            }
            case 9461: {
                if (number == 0L || number > 10L) {
                    return "" + number;
                }
                return "" + (char)(9461L + number - 1L);
            }
            case 10122: {
                if (number == 0L) {
                    return "\ud83c\udd0c";
                }
                if (number > 10L) {
                    return "" + number;
                }
                return "" + (char)(10122L + number - 1L);
            }
            case 12832: {
                if (number == 0L || number > 10L) {
                    return "" + number;
                }
                return "" + (char)(12832L + number - 1L);
            }
            case 12928: {
                if (number == 0L || number > 10L) {
                    return "" + number;
                }
                return "" + (char)(12928L + number - 1L);
            }
            case 65799: {
                if (number == 0L || number > 10L) {
                    return "" + number;
                }
                fsb.appendWideChar(65799 + (int)number - 1);
                return fsb.toString();
            }
            case 69216: {
                if (number == 0L || number > 10L) {
                    return "" + number;
                }
                fsb.appendWideChar(69216 + (int)number - 1);
                return fsb.toString();
            }
            case 69714: {
                if (number == 0L || number > 10L) {
                    return "" + number;
                }
                fsb.appendWideChar(69714 + (int)number - 1);
                return fsb.toString();
            }
            case 119648: {
                if (number == 0L || number >= 10L) {
                    return "" + number;
                }
                fsb.appendWideChar(119648 + (int)number - 1);
                return fsb.toString();
            }
            case 127234: {
                if (number == 0L) {
                    fsb.appendWideChar(127233);
                    return fsb.toString();
                }
                if (number >= 10L) {
                    return "" + number;
                }
                fsb.appendWideChar(127234 + (int)number - 1);
                return fsb.toString();
            }
            case 913: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, greekUpper);
            }
            case 945: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, greekLower);
            }
            case 1040: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, cyrillicUpper);
            }
            case 1072: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, cyrillicLower);
            }
            case 1488: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, hebrew);
            }
            case 12354: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, hiraganaA);
            }
            case 12450: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, katakanaA);
            }
            case 12356: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, hiraganaI);
            }
            case 12452: {
                if (number == 0L) {
                    return "0";
                }
                return this.toAlphaSequence(number, katakanaI);
            }
            case 19968: {
                return this.toJapanese(number);
            }
            default: {
                int digitValue = Alphanumeric.getDigitValue(formchar);
                if (digitValue >= 0) {
                    int zero = formchar - digitValue;
                    int[] digits = new int[10];
                    for (int z = 0; z <= 9; ++z) {
                        digits[z] = zero + z;
                    }
                    return this.toRadical(number, digits, pictureLength, numGroupFormatter);
                }
                if (formchar < 4352 && Character.isLetter((char)formchar) && number > 0L) {
                    this.alphaDefault(number, (char)formchar, sb);
                    break;
                }
                sb.append(this.toRadical(number, westernDigits, pictureLength, numGroupFormatter));
                if (ordinal == null || ordinal.isEmpty()) break;
                sb.append(this.ordinalSuffix(ordinal, number));
            }
        }
        return sb.toString();
    }

    protected String ordinalSuffix(String ordinalParam, long number) {
        return "";
    }

    protected void alphaDefault(long number, char formchar, FastStringBuffer sb) {
        int min = formchar;
        int max = formchar;
        while (Character.isLetterOrDigit((char)(max + '\u0001'))) {
            ++max;
        }
        sb.append(this.toAlpha(number, min, max));
    }

    protected String toAlpha(long number, int min, int max) {
        if (number <= 0L) {
            return "" + number;
        }
        int range = max - min + 1;
        char last = (char)((number - 1L) % (long)range + (long)min);
        if (number > (long)range) {
            return this.toAlpha((number - 1L) / (long)range, min, max) + last;
        }
        return "" + last;
    }

    protected String toAlphaSequence(long number, String alphabet) {
        if (number <= 0L) {
            return "" + number;
        }
        int range = alphabet.length();
        char last = alphabet.charAt((int)((number - 1L) % (long)range));
        if (number > (long)range) {
            return this.toAlphaSequence((number - 1L) / (long)range, alphabet) + last;
        }
        return "" + last;
    }

    private String toRadical(long number, int[] digits, int pictureLength, NumericGroupFormatter numGroupFormatter) {
        FastStringBuffer temp = AbstractNumberer.convertDigitSystem(number, digits, pictureLength);
        if (numGroupFormatter == null) {
            return temp.toString();
        }
        return numGroupFormatter.format(temp);
    }

    public static FastStringBuffer convertDigitSystem(long number, int[] digits, int requiredLength) {
        FastStringBuffer temp = new FastStringBuffer(16);
        int base = digits.length;
        FastStringBuffer s = new FastStringBuffer(16);
        int count = 0;
        for (long n = number; n > 0L; n /= (long)base) {
            int digit = digits[(int)(n % (long)base)];
            s.prependWideChar(digit);
            ++count;
        }
        for (int i = 0; i < requiredLength - count; ++i) {
            temp.appendWideChar(digits[0]);
        }
        temp.append(s);
        return temp;
    }

    public static String toRoman(long n) {
        if (n <= 0L || n > 9999L) {
            return "" + n;
        }
        return romanThousands[(int)n / 1000] + romanHundreds[(int)n / 100 % 10] + romanTens[(int)n / 10 % 10] + romanUnits[(int)n % 10];
    }

    public String toJapanese(long number) {
        FastStringBuffer fsb = new FastStringBuffer(16);
        if (number == 0L) {
            fsb.appendWideChar(12295);
        } else if (number <= 9999L) {
            AbstractNumberer.toJapanese((int)number, fsb, false);
        } else {
            fsb.append("" + number);
        }
        return fsb.toString();
    }

    private static void toJapanese(int nr, FastStringBuffer fsb, boolean isInitial) {
        if (nr != 0) {
            if (nr <= 9) {
                if (nr != 1 || !isInitial) {
                    fsb.appendWideChar(kanjiDigits[nr]);
                }
            } else if (nr == 10) {
                fsb.appendWideChar(21313);
            } else if (nr <= 99) {
                AbstractNumberer.toJapanese(nr / 10, fsb, true);
                fsb.appendWideChar(21313);
                AbstractNumberer.toJapanese(nr % 10, fsb, false);
            } else if (nr <= 999) {
                AbstractNumberer.toJapanese(nr / 100, fsb, true);
                fsb.appendWideChar(30334);
                AbstractNumberer.toJapanese(nr % 100, fsb, false);
            } else if (nr <= 9999) {
                AbstractNumberer.toJapanese(nr / 1000, fsb, true);
                fsb.appendWideChar(21315);
                AbstractNumberer.toJapanese(nr % 1000, fsb, false);
            }
        }
    }

    public abstract String toWords(long var1);

    public String toWords(long number, int wordCase) {
        String s = number == 0L ? "Zero" : this.toWords(number);
        switch (wordCase) {
            case 0: {
                return s.toUpperCase();
            }
            case 1: {
                return s.toLowerCase();
            }
        }
        return s;
    }

    public abstract String toOrdinalWords(String var1, long var2, int var4);

    @Override
    public abstract String monthName(int var1, int var2, int var3);

    @Override
    public abstract String dayName(int var1, int var2, int var3);

    @Override
    public String halfDayName(int minutes, int minWidth, int maxWidth) {
        String s;
        if (minutes == 0 && maxWidth >= 8 && "gb".equals(this.country)) {
            s = "Midnight";
        } else if (minutes < 720) {
            switch (maxWidth) {
                case 1: {
                    s = "A";
                    break;
                }
                case 2: 
                case 3: {
                    s = "Am";
                    break;
                }
                default: {
                    s = "A.M.";
                    break;
                }
            }
        } else if (minutes == 720 && maxWidth >= 8 && "gb".equals(this.country)) {
            s = "Noon";
        } else {
            switch (maxWidth) {
                case 1: {
                    s = "P";
                    break;
                }
                case 2: 
                case 3: {
                    s = "Pm";
                    break;
                }
                default: {
                    s = "P.M.";
                }
            }
        }
        return s;
    }

    @Override
    public String getOrdinalSuffixForDateTime(String component) {
        return "yes";
    }

    @Override
    public String getEraName(int year) {
        return year > 0 ? "AD" : "BC";
    }

    @Override
    public String getCalendarName(String code) {
        if (code.equals("AD")) {
            return "Gregorian";
        }
        return code;
    }
}

