/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.number;

import net.sf.saxon.expr.number.AbstractNumberer;

public class Numberer_en
extends AbstractNumberer {
    private String tensUnitsSeparatorCardinal = " ";
    private String tensUnitsSeparatorOrdinal = "-";
    private static String[] englishUnits = new String[]{"Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
    private static String[] englishTens = new String[]{"", "Ten", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
    private static String[] englishOrdinalUnits = new String[]{"Zeroth", "First", "Second", "Third", "Fourth", "Fifth", "Sixth", "Seventh", "Eighth", "Ninth", "Tenth", "Eleventh", "Twelfth", "Thirteenth", "Fourteenth", "Fifteenth", "Sixteenth", "Seventeenth", "Eighteenth", "Nineteenth"};
    private static String[] englishOrdinalTens = new String[]{"", "Tenth", "Twentieth", "Thirtieth", "Fortieth", "Fiftieth", "Sixtieth", "Seventieth", "Eightieth", "Ninetieth"};
    private static String[] englishMonths = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private static String[] englishDays = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private static String[] englishDayAbbreviations = new String[]{"Mon", "Tues", "Weds", "Thurs", "Fri", "Sat", "Sun"};
    private static int[] minUniqueDayLength = new int[]{1, 2, 1, 2, 1, 2, 2};

    public void setTensUnitsSeparatorCardinal(String separator) {
        this.tensUnitsSeparatorCardinal = separator;
    }

    public void setTensUnitsSeparatorOrdinal(String separator) {
        this.tensUnitsSeparatorOrdinal = separator;
    }

    @Override
    public void setLanguage(String language) {
        super.setLanguage(language);
        if (language.endsWith("-x-hyphen")) {
            this.setTensUnitsSeparatorOrdinal("-");
            this.setTensUnitsSeparatorCardinal("-");
        } else if (language.endsWith("-x-nohyphen")) {
            this.setTensUnitsSeparatorOrdinal(" ");
            this.setTensUnitsSeparatorCardinal(" ");
        }
    }

    @Override
    protected String ordinalSuffix(String ordinalParam, long number) {
        int penult = (int)(number % 100L) / 10;
        int ult = (int)(number % 10L);
        if (penult == 1) {
            return "th";
        }
        if (ult == 1) {
            return "st";
        }
        if (ult == 2) {
            return "nd";
        }
        if (ult == 3) {
            return "rd";
        }
        return "th";
    }

    @Override
    public String toWords(long number) {
        if (number >= 1000000000L) {
            long rem = number % 1000000000L;
            return this.toWords(number / 1000000000L) + " Billion" + (rem == 0L ? "" : (rem < 100L ? " and " : " ") + this.toWords(rem));
        }
        if (number >= 1000000L) {
            long rem = number % 1000000L;
            return this.toWords(number / 1000000L) + " Million" + (rem == 0L ? "" : (rem < 100L ? " and " : " ") + this.toWords(rem));
        }
        if (number >= 1000L) {
            long rem = number % 1000L;
            return this.toWords(number / 1000L) + " Thousand" + (rem == 0L ? "" : (rem < 100L ? " and " : " ") + this.toWords(rem));
        }
        if (number >= 100L) {
            long rem = number % 100L;
            return this.toWords(number / 100L) + " Hundred" + (rem == 0L ? "" : " and " + this.toWords(rem));
        }
        if (number < 20L) {
            return englishUnits[(int)number];
        }
        int rem = (int)(number % 10L);
        return englishTens[(int)number / 10] + (rem == 0 ? "" : this.tensUnitsSeparatorCardinal + englishUnits[rem]);
    }

    @Override
    public String toOrdinalWords(String ordinalParam, long number, int wordCase) {
        String s;
        if (number >= 1000000000L) {
            long rem = number % 1000000000L;
            s = this.toWords(number / 1000000000L) + " Billion" + (rem == 0L ? "th" : (rem < 100L ? " and " : " ") + this.toOrdinalWords(ordinalParam, rem, wordCase));
        } else if (number >= 1000000L) {
            long rem = number % 1000000L;
            s = this.toWords(number / 1000000L) + " Million" + (rem == 0L ? "th" : (rem < 100L ? " and " : " ") + this.toOrdinalWords(ordinalParam, rem, wordCase));
        } else if (number >= 1000L) {
            long rem = number % 1000L;
            s = this.toWords(number / 1000L) + " Thousand" + (rem == 0L ? "th" : (rem < 100L ? " and " : " ") + this.toOrdinalWords(ordinalParam, rem, wordCase));
        } else if (number >= 100L) {
            long rem = number % 100L;
            s = this.toWords(number / 100L) + " Hundred" + (rem == 0L ? "th" : " and " + this.toOrdinalWords(ordinalParam, rem, wordCase));
        } else {
            int rem;
            s = number < 20L ? englishOrdinalUnits[(int)number] : ((rem = (int)(number % 10L)) == 0 ? englishOrdinalTens[(int)number / 10] : englishTens[(int)number / 10] + this.tensUnitsSeparatorOrdinal + englishOrdinalUnits[rem]);
        }
        if (wordCase == 0) {
            return s.toUpperCase();
        }
        if (wordCase == 1) {
            return s.toLowerCase();
        }
        return s;
    }

    @Override
    public String monthName(int month, int minWidth, int maxWidth) {
        String name = englishMonths[month - 1];
        if (maxWidth < 3) {
            maxWidth = 3;
        }
        if (name.length() > maxWidth) {
            name = name.substring(0, maxWidth);
        }
        StringBuilder nameBuilder = new StringBuilder(name);
        while (nameBuilder.length() < minWidth) {
            nameBuilder.append(' ');
        }
        name = nameBuilder.toString();
        return name;
    }

    @Override
    public String dayName(int day, int minWidth, int maxWidth) {
        String name = englishDays[day - 1];
        if (maxWidth < 2) {
            maxWidth = 2;
        }
        if (name.length() > maxWidth && (name = englishDayAbbreviations[day - 1]).length() > maxWidth) {
            name = name.substring(0, maxWidth);
        }
        StringBuilder nameBuilder = new StringBuilder(name);
        while (nameBuilder.length() < minWidth) {
            nameBuilder.append(' ');
        }
        name = nameBuilder.toString();
        if (minWidth == 1 && maxWidth == 2) {
            name = name.substring(0, minUniqueDayLength[day - 1]);
        }
        return name;
    }
}

