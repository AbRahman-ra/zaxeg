/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.util.ArrayList;
import java.util.List;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.DateTimeValue;
import net.sf.saxon.value.DateValue;
import net.sf.saxon.value.GDateValue;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.TimeValue;
import net.sf.saxon.value.Whitespace;

public class ParseIetfDate
extends SystemFunction
implements Callable {
    private String[] dayNames = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    private String[] monthNames = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private String[] timezoneNames = new String[]{"UT", "UTC", "GMT", "EST", "EDT", "CST", "CDT", "MST", "MDT", "PST", "PDT"};
    private static final String EOF = "";

    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue stringValue = (StringValue)arguments[0].head();
        if (stringValue == null) {
            return ZeroOrOne.empty();
        }
        return new ZeroOrOne<DateTimeValue>(this.parse(stringValue.getStringValue(), context));
    }

    private boolean isDayName(String string) {
        for (String s : this.dayNames) {
            if (!s.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    private boolean isMonthName(String string) {
        for (String s : this.monthNames) {
            if (!s.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    private byte getMonthNumber(String string) {
        if ("Jan".equalsIgnoreCase(string)) {
            return 1;
        }
        if ("Feb".equalsIgnoreCase(string)) {
            return 2;
        }
        if ("Mar".equalsIgnoreCase(string)) {
            return 3;
        }
        if ("Apr".equalsIgnoreCase(string)) {
            return 4;
        }
        if ("May".equalsIgnoreCase(string)) {
            return 5;
        }
        if ("Jun".equalsIgnoreCase(string)) {
            return 6;
        }
        if ("Jul".equalsIgnoreCase(string)) {
            return 7;
        }
        if ("Aug".equalsIgnoreCase(string)) {
            return 8;
        }
        if ("Sep".equalsIgnoreCase(string)) {
            return 9;
        }
        if ("Oct".equalsIgnoreCase(string)) {
            return 10;
        }
        if ("Nov".equalsIgnoreCase(string)) {
            return 11;
        }
        if ("Dec".equalsIgnoreCase(string)) {
            return 12;
        }
        return 0;
    }

    private int requireDSep(List<String> tokens, int i, String input) throws XPathException {
        boolean found = false;
        if (" ".equals(tokens.get(i))) {
            ++i;
            found = true;
        }
        if ("-".equals(tokens.get(i))) {
            ++i;
            found = true;
        }
        if (" ".equals(tokens.get(i))) {
            ++i;
            found = true;
        }
        if (!found) {
            ParseIetfDate.badDate("Date separator missing", input);
        }
        return i;
    }

    private static void badDate(String msg, String value) throws XPathException {
        XPathException err = new XPathException("Invalid IETF date value " + value + " (" + msg + ")");
        err.setErrorCode("FORG0010");
        throw err;
    }

    private boolean isTimezoneName(String string) {
        for (String s : this.timezoneNames) {
            if (!s.equalsIgnoreCase(string)) continue;
            return true;
        }
        return false;
    }

    private int getTimezoneOffsetFromName(String string) {
        if ("UT".equalsIgnoreCase(string) | "UTC".equalsIgnoreCase(string) | "GMT".equalsIgnoreCase(string)) {
            return 0;
        }
        if ("EST".equalsIgnoreCase(string)) {
            return -300;
        }
        if ("EDT".equalsIgnoreCase(string)) {
            return -240;
        }
        if ("CST".equalsIgnoreCase(string)) {
            return -360;
        }
        if ("CDT".equalsIgnoreCase(string)) {
            return -300;
        }
        if ("MST".equalsIgnoreCase(string)) {
            return -420;
        }
        if ("MDT".equalsIgnoreCase(string)) {
            return -360;
        }
        if ("PST".equalsIgnoreCase(string)) {
            return -480;
        }
        if ("PDT".equalsIgnoreCase(string)) {
            return -420;
        }
        return 0;
    }

    public DateTimeValue parse(String input, XPathContext context) throws XPathException {
        List<String> tokens = this.tokenize(input);
        int year = 0;
        byte month = 0;
        byte day = 0;
        ArrayList<TimeValue> timeValue = new ArrayList<TimeValue>();
        int i = 0;
        String currentToken = tokens.get(i);
        if (currentToken.matches("[A-Za-z]+") && this.isDayName(currentToken)) {
            if (",".equals(currentToken = tokens.get(++i))) {
                currentToken = tokens.get(++i);
            }
            if (!" ".equals(currentToken)) {
                ParseIetfDate.badDate("Space missing after day name", input);
            }
            currentToken = tokens.get(++i);
        }
        if (this.isMonthName(currentToken)) {
            month = this.getMonthNumber(currentToken);
            currentToken = tokens.get(i = this.requireDSep(tokens, i + 1, input));
            if (!currentToken.matches("[0-9]+")) {
                ParseIetfDate.badDate("Day number expected after month name", input);
            }
            if (currentToken.length() > 2) {
                ParseIetfDate.badDate("Day number exceeds two digits", input);
            }
            day = (byte)Integer.parseInt(currentToken);
            if (!" ".equals(currentToken = tokens.get(++i))) {
                ParseIetfDate.badDate("Space missing after day number", input);
            }
            ++i;
            i = this.parseTime(tokens, i, timeValue, input);
            if (!" ".equals(currentToken = tokens.get(++i))) {
                ParseIetfDate.badDate("Space missing after time string", input);
            }
            if ((currentToken = tokens.get(++i)).matches("[0-9]+")) {
                year = this.checkTwoOrFourDigits(input, currentToken);
            } else {
                ParseIetfDate.badDate("Year number expected after time", input);
            }
        } else if (currentToken.matches("[0-9]+")) {
            if (currentToken.length() > 2) {
                ParseIetfDate.badDate("First number in string expected to be day in two digits", input);
            }
            day = (byte)Integer.parseInt(currentToken);
            ++i;
            currentToken = tokens.get(i = this.requireDSep(tokens, i, input));
            if (!this.isMonthName(currentToken)) {
                ParseIetfDate.badDate("Abbreviated month name expected after day number", input);
            }
            month = this.getMonthNumber(currentToken);
            ++i;
            currentToken = tokens.get(i = this.requireDSep(tokens, i, input));
            if (currentToken.matches("[0-9]+")) {
                year = this.checkTwoOrFourDigits(input, currentToken);
            } else {
                ParseIetfDate.badDate("Year number expected after month name", input);
            }
            currentToken = tokens.get(++i);
            if (!" ".equals(currentToken)) {
                ParseIetfDate.badDate("Space missing after year number", input);
            }
            ++i;
            i = this.parseTime(tokens, i, timeValue, input);
        } else {
            ParseIetfDate.badDate("String expected to begin with month name or day name (or day number)", input);
        }
        if (!GDateValue.isValidDate(year, month, day)) {
            ParseIetfDate.badDate("Date is not valid", input);
        }
        if (!(currentToken = tokens.get(++i)).equals(EOF)) {
            ParseIetfDate.badDate("Extra content found in string after date", input);
        }
        DateValue date = new DateValue(year, month, day);
        TimeValue time = (TimeValue)timeValue.get(0);
        if (time.getHour() == 24) {
            date = DateValue.tomorrow(date.getYear(), date.getMonth(), date.getDay());
            time = new TimeValue(0, 0, 0, 0, time.getTimezoneInMinutes(), EOF);
        }
        return DateTimeValue.makeDateTimeValue(date, time);
    }

    private int checkTwoOrFourDigits(String input, String currentToken) throws XPathException {
        int year;
        if (currentToken.length() == 4) {
            year = Integer.parseInt(currentToken);
        } else if (currentToken.length() == 2) {
            year = Integer.parseInt(currentToken) + 1900;
        } else {
            ParseIetfDate.badDate("Year number must be two or four digits", input);
            year = 0;
        }
        return year;
    }

    public int parseTime(List<String> tokens, int currentPosition, List<TimeValue> result, String input) throws XPathException {
        byte second = 0;
        int microsecond = 0;
        int tz = 0;
        int i = currentPosition;
        int n = currentPosition;
        StringBuilder currentToken = new StringBuilder(tokens.get(i));
        if (!currentToken.toString().matches("[0-9]+")) {
            ParseIetfDate.badDate("Hour number expected", input);
        }
        if (currentToken.length() > 2) {
            ParseIetfDate.badDate("Hour number exceeds two digits", input);
        }
        byte hour = (byte)Integer.parseInt(currentToken.toString());
        if (!":".equals((currentToken = new StringBuilder(tokens.get(++i))).toString())) {
            ParseIetfDate.badDate("Separator ':' missing after hour", input);
        }
        if (!(currentToken = new StringBuilder(tokens.get(++i))).toString().matches("[0-9]+")) {
            ParseIetfDate.badDate("Minutes expected after hour", input);
        }
        if (currentToken.length() != 2) {
            ParseIetfDate.badDate("Minutes must be exactly two digits", input);
        }
        byte minute = (byte)Integer.parseInt(currentToken.toString());
        currentToken = new StringBuilder(tokens.get(++i));
        boolean finished = false;
        if (currentToken.toString().equals(EOF)) {
            n = i - 1;
            finished = true;
        } else if (":".equals(currentToken.toString())) {
            if (!(currentToken = new StringBuilder(tokens.get(++i))).toString().matches("[0-9]+")) {
                ParseIetfDate.badDate("Seconds expected after ':' separator after minutes", input);
            }
            if (currentToken.length() != 2) {
                ParseIetfDate.badDate("Seconds number must have exactly two digits (before decimal point)", input);
            }
            second = (byte)Integer.parseInt(currentToken.toString());
            if ((currentToken = new StringBuilder(tokens.get(++i))).toString().equals(EOF)) {
                n = i - 1;
                finished = true;
            } else if (".".equals(currentToken.toString())) {
                if (!(currentToken = new StringBuilder(tokens.get(++i))).toString().matches("[0-9]+")) {
                    ParseIetfDate.badDate("Fractional part of seconds expected after decimal point", input);
                }
                int len = Math.min(6, currentToken.length());
                currentToken = new StringBuilder(currentToken.substring(0, len));
                while (currentToken.length() < 6) {
                    currentToken.append("0");
                }
                microsecond = Integer.parseInt(currentToken.toString());
                if (i < tokens.size() - 1) {
                    currentToken = new StringBuilder(tokens.get(++i));
                }
            }
        }
        if (!finished) {
            if (" ".equals(currentToken.toString()) && (currentToken = new StringBuilder(tokens.get(++i))).toString().matches("[0-9]+")) {
                n = i - 2;
                finished = true;
            }
            if (!finished) {
                if (currentToken.toString().matches("[A-Za-z]+")) {
                    if (!this.isTimezoneName(currentToken.toString())) {
                        ParseIetfDate.badDate("Timezone name not recognised", input);
                    }
                    tz = this.getTimezoneOffsetFromName(currentToken.toString());
                    n = i;
                    finished = true;
                } else if ("+".equals(currentToken.toString()) | "-".equals(currentToken.toString())) {
                    int tLength;
                    String sign = currentToken.toString();
                    int tzOffsetHours = 0;
                    int tzOffsetMinutes = 0;
                    if (!(currentToken = new StringBuilder(tokens.get(++i))).toString().matches("[0-9]+")) {
                        ParseIetfDate.badDate("Parsing timezone offset, number expected after '" + sign + "'", input);
                    }
                    if ((tLength = currentToken.length()) > 4) {
                        ParseIetfDate.badDate("Timezone offset does not have the correct number of digits", input);
                    } else if (tLength >= 3) {
                        tzOffsetHours = Integer.parseInt(currentToken.substring(0, tLength - 2));
                        tzOffsetMinutes = Integer.parseInt(currentToken.substring(tLength - 2, tLength));
                        currentToken = new StringBuilder(tokens.get(++i));
                    } else {
                        tzOffsetHours = Integer.parseInt(currentToken.toString());
                        if (":".equals((currentToken = new StringBuilder(tokens.get(++i))).toString()) && (currentToken = new StringBuilder(tokens.get(++i))).toString().matches("[0-9]+")) {
                            if (currentToken.length() != 2) {
                                ParseIetfDate.badDate("Parsing timezone offset, minutes must be two digits", input);
                            } else {
                                tzOffsetMinutes = Integer.parseInt(currentToken.toString());
                            }
                            currentToken = new StringBuilder(tokens.get(++i));
                        }
                    }
                    if (tzOffsetMinutes > 59) {
                        ParseIetfDate.badDate("Timezone offset minutes out of range", input);
                    }
                    tz = tzOffsetHours * 60 + tzOffsetMinutes;
                    if (sign.equals("-")) {
                        tz = -tz;
                    }
                    if (currentToken.toString().equals(EOF)) {
                        n = i - 1;
                        finished = true;
                    } else if (" ".equals(currentToken.toString()) && (currentToken = new StringBuilder(tokens.get(++i))).toString().matches("[0-9]+")) {
                        n = i - 2;
                        finished = true;
                    }
                    if (!finished && "(".equals(currentToken.toString())) {
                        if (" ".equals((currentToken = new StringBuilder(tokens.get(++i))).toString())) {
                            currentToken = new StringBuilder(tokens.get(++i));
                        }
                        if (!currentToken.toString().matches("[A-Za-z]+")) {
                            ParseIetfDate.badDate("Timezone name expected after '('", input);
                        } else if (currentToken.toString().matches("[A-Za-z]+")) {
                            if (!this.isTimezoneName(currentToken.toString())) {
                                ParseIetfDate.badDate("Timezone name not recognised", input);
                            }
                            currentToken = new StringBuilder(tokens.get(++i));
                        }
                        if (" ".equals(currentToken.toString())) {
                            currentToken = new StringBuilder(tokens.get(++i));
                        }
                        if (!")".equals(currentToken.toString())) {
                            ParseIetfDate.badDate("Expected ')' after timezone name", input);
                        }
                        n = i;
                        finished = true;
                    } else if (!finished) {
                        ParseIetfDate.badDate("Unexpected content after timezone offset", input);
                    }
                } else {
                    ParseIetfDate.badDate("Unexpected content in time (after minutes)", input);
                }
            }
        }
        if (!finished) {
            throw new AssertionError((Object)"Should have finished");
        }
        if (!ParseIetfDate.isValidTime(hour, minute, second, microsecond, tz)) {
            ParseIetfDate.badDate("Time/timezone is not valid", input);
        }
        TimeValue timeValue = new TimeValue(hour, minute, second, microsecond * 1000, tz, EOF);
        result.add(timeValue);
        return n;
    }

    public static boolean isValidTime(int hour, int minute, int second, int microsecond, int tz) {
        return (hour >= 0 && hour <= 23 && minute >= 0 && minute < 60 && second >= 0 && second < 60 && microsecond >= 0 && microsecond < 1000000 || hour == 24 && minute == 0 && second == 0 && microsecond == 0) && tz >= -840 && tz <= 840;
    }

    private List<String> tokenize(String input) throws XPathException {
        ArrayList<String> tokens = new ArrayList<String>();
        if ((input = input.trim()).isEmpty()) {
            ParseIetfDate.badDate("Input is empty", input);
            return tokens;
        }
        int i = 0;
        input = input + '\u0000';
        while (true) {
            int j;
            char c;
            if ((c = input.charAt(i)) == '\u0000') {
                tokens.add(EOF);
                return tokens;
            }
            if (Whitespace.isWhite(c)) {
                j = i;
                while (Whitespace.isWhite(input.charAt(j++))) {
                }
                tokens.add(" ");
                i = j - 1;
                continue;
            }
            if (Character.isLetter(c)) {
                j = i;
                while (Character.isLetter(input.charAt(j++))) {
                }
                tokens.add(input.substring(i, j - 1));
                i = j - 1;
                continue;
            }
            if (Character.isDigit(c)) {
                j = i;
                while (Character.isDigit(input.charAt(j++))) {
                }
                tokens.add(input.substring(i, j - 1));
                i = j - 1;
                continue;
            }
            tokens.add(input.substring(i, i + 1));
            ++i;
        }
    }
}

