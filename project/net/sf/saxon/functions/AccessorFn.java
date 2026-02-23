/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.ScalarSystemFunction;
import net.sf.saxon.om.Item;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.AtomicValue;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.IntegerValue;

public abstract class AccessorFn
extends ScalarSystemFunction {
    public abstract Component getComponentId();

    @Override
    public IntegerValue[] getIntegerBounds() {
        switch (this.getComponentId()) {
            case YEAR: {
                return new IntegerValue[]{Int64Value.makeIntegerValue(-100000L), Int64Value.makeIntegerValue(100000L)};
            }
            case MONTH: {
                return new IntegerValue[]{Int64Value.makeIntegerValue(-11L), Int64Value.makeIntegerValue(11L)};
            }
            case DAY: {
                return new IntegerValue[]{Int64Value.makeIntegerValue(-31L), Int64Value.makeIntegerValue(31L)};
            }
            case HOURS: {
                return new IntegerValue[]{Int64Value.makeIntegerValue(-24L), Int64Value.makeIntegerValue(24L)};
            }
            case MINUTES: {
                return new IntegerValue[]{Int64Value.makeIntegerValue(-59L), Int64Value.makeIntegerValue(59L)};
            }
            case SECONDS: {
                return new IntegerValue[]{Int64Value.makeIntegerValue(-59L), Int64Value.makeIntegerValue(59L)};
            }
        }
        return null;
    }

    public int getRequiredComponent() {
        return this.getComponentId().ordinal();
    }

    @Override
    public AtomicValue evaluate(Item item, XPathContext context) throws XPathException {
        return ((AtomicValue)item).getComponent(this.getComponentId());
    }

    @Override
    public String getCompilerName() {
        return "AccessorFnCompiler";
    }

    public static class NamespaceUriFromQName
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.NAMESPACE;
        }
    }

    public static class PrefixFromQName
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.PREFIX;
        }
    }

    public static class LocalNameFromQName
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.LOCALNAME;
        }
    }

    public static class SecondsFromDuration
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.SECONDS;
        }
    }

    public static class MinutesFromDuration
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.MINUTES;
        }
    }

    public static class HoursFromDuration
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.HOURS;
        }
    }

    public static class DaysFromDuration
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.DAY;
        }
    }

    public static class MonthsFromDuration
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.MONTH;
        }
    }

    public static class YearsFromDuration
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.YEAR;
        }
    }

    public static class TimezoneFromTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.TIMEZONE;
        }
    }

    public static class SecondsFromTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.SECONDS;
        }
    }

    public static class MinutesFromTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.MINUTES;
        }
    }

    public static class HoursFromTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.HOURS;
        }
    }

    public static class TimezoneFromDate
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.TIMEZONE;
        }
    }

    public static class DayFromDate
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.DAY;
        }
    }

    public static class MonthFromDate
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.MONTH;
        }
    }

    public static class YearFromDate
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.YEAR;
        }
    }

    public static class TimezoneFromDateTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.TIMEZONE;
        }
    }

    public static class SecondsFromDateTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.SECONDS;
        }
    }

    public static class MinutesFromDateTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.MINUTES;
        }
    }

    public static class HoursFromDateTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.HOURS;
        }
    }

    public static class DayFromDateTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.DAY;
        }
    }

    public static class MonthFromDateTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.MONTH;
        }
    }

    public static class YearFromDateTime
    extends AccessorFn {
        @Override
        public Component getComponentId() {
            return Component.YEAR;
        }
    }

    public static enum Component {
        YEAR,
        MONTH,
        DAY,
        HOURS,
        MINUTES,
        SECONDS,
        TIMEZONE,
        LOCALNAME,
        NAMESPACE,
        PREFIX,
        MICROSECONDS,
        NANOSECONDS,
        WHOLE_SECONDS,
        YEAR_ALLOWING_ZERO;

    }
}

