/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.expr.number;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.value.DateTimeValue;

public class NamedTimeZone {
    static Set<String> knownTimeZones = new HashSet<String>(50);
    static HashMap<String, List<String>> idForCountry = new HashMap(50);
    static List<String> worldTimeZones = new ArrayList<String>(20);

    static void tz(String country, String zoneId) {
        List<String> list = idForCountry.get(country);
        if (list == null) {
            list = new ArrayList<String>(4);
        }
        list.add(zoneId);
        idForCountry.put(country, list);
    }

    static void tz(String country, String zoneId, boolean major) {
        NamedTimeZone.tz(country, zoneId);
        if (major) {
            worldTimeZones.add(zoneId);
        }
    }

    public static String getTimeZoneNameForDate(DateTimeValue date, String place) {
        Date javaDate;
        if (!date.hasTimezone()) {
            return "";
        }
        if (place == null) {
            return NamedTimeZone.formatTimeZoneOffset(date);
        }
        TimeZone zone = null;
        int tzMinutes = date.getTimezoneInMinutes();
        if (place.contains("/")) {
            zone = NamedTimeZone.getNamedTimeZone(place);
        } else {
            TimeZone tz;
            List<String> possibleZones = idForCountry.get(place.toLowerCase());
            if (possibleZones == null) {
                possibleZones = new ArrayList<String>();
            }
            long epochDate = date.getCalendar().getTime().getTime();
            for (String z : possibleZones) {
                tz = TimeZone.getTimeZone(z);
                if (tz == null || tz.getOffset(epochDate) != tzMinutes * 60000) continue;
                zone = tz;
                break;
            }
            if (zone == null) {
                for (String z : worldTimeZones) {
                    tz = TimeZone.getTimeZone(z);
                    if (tz == null || tz.getOffset(epochDate) != tzMinutes * 60000) continue;
                    zone = tz;
                    break;
                }
            }
            if (zone == null) {
                return NamedTimeZone.formatTimeZoneOffset(date);
            }
        }
        try {
            javaDate = date.getCalendar().getTime();
        } catch (IllegalArgumentException e) {
            return NamedTimeZone.formatTimeZoneOffset(date);
        }
        boolean inSummerTime = zone != null && zone.inDaylightTime(javaDate);
        return zone.getDisplayName(inSummerTime, 0);
    }

    public static String formatTimeZoneOffset(DateTimeValue timeValue) {
        FastStringBuffer sb = new FastStringBuffer(16);
        DateTimeValue.appendTimezone(timeValue.getTimezoneInMinutes(), sb);
        return sb.toString();
    }

    public static String getOlsenTimeZoneName(DateTimeValue date, String country) {
        if (!date.hasTimezone()) {
            return "";
        }
        List<String> possibleIds = idForCountry.get(country.toLowerCase());
        if (possibleIds == null) {
            return NamedTimeZone.formatTimeZoneOffset(date);
        }
        String exampleId = possibleIds.get(0);
        TimeZone exampleZone = TimeZone.getTimeZone(exampleId);
        Date javaDate = date.getCalendar().getTime();
        boolean inSummerTime = exampleZone.inDaylightTime(javaDate);
        int tzMinutes = date.getTimezoneInMinutes();
        for (int i = 0; i < possibleIds.size(); ++i) {
            String olsen = possibleIds.get(i);
            TimeZone possibleTimeZone = TimeZone.getTimeZone(olsen);
            int offset = possibleTimeZone.getOffset(javaDate.getTime());
            if (offset != tzMinutes * 60000) continue;
            return inSummerTime ? olsen + "*" : olsen;
        }
        return NamedTimeZone.formatTimeZoneOffset(date);
    }

    public static Boolean inSummerTime(DateTimeValue date, String region) {
        String olsenName;
        if (region.length() == 2) {
            List<String> possibleIds = idForCountry.get(region.toLowerCase());
            if (possibleIds == null) {
                return null;
            }
            olsenName = possibleIds.get(0);
        } else {
            olsenName = region;
        }
        TimeZone zone = TimeZone.getTimeZone(olsenName);
        return zone.inDaylightTime(date.getCalendar().getTime());
    }

    public static int civilTimeOffset(DateTimeValue date, String olsenName) {
        TimeZone zone = TimeZone.getTimeZone(olsenName);
        return zone.getOffset(date.getCalendar().getTime().getTime());
    }

    public static TimeZone getNamedTimeZone(String olsonName) {
        if (knownTimeZones.contains(olsonName)) {
            return TimeZone.getTimeZone(olsonName);
        }
        return null;
    }

    static {
        Collections.addAll(knownTimeZones, TimeZone.getAvailableIDs());
        NamedTimeZone.tz("us", "America/New_York", true);
        NamedTimeZone.tz("us", "America/Chicago", true);
        NamedTimeZone.tz("us", "America/Denver", true);
        NamedTimeZone.tz("us", "America/Los_Angeles", true);
        NamedTimeZone.tz("us", "America/Anchorage", true);
        NamedTimeZone.tz("us", "America/Halifax", true);
        NamedTimeZone.tz("us", "Pacific/Honolulu", true);
        NamedTimeZone.tz("ca", "Canada/Pacific");
        NamedTimeZone.tz("ca", "Canada/Mountain");
        NamedTimeZone.tz("ca", "Canada/Central");
        NamedTimeZone.tz("ca", "Canada/Eastern");
        NamedTimeZone.tz("ca", "Canada/Atlantic");
        NamedTimeZone.tz("au", "Australia/Sydney", true);
        NamedTimeZone.tz("au", "Australia/Darwin", true);
        NamedTimeZone.tz("au", "Australia/Perth", true);
        NamedTimeZone.tz("ru", "Europe/Moscow", true);
        NamedTimeZone.tz("ru", "Europe/Samara");
        NamedTimeZone.tz("ru", "Asia/Yekaterinburg");
        NamedTimeZone.tz("ru", "Asia/Novosibirsk");
        NamedTimeZone.tz("ru", "Asia/Krasnoyarsk");
        NamedTimeZone.tz("ru", "Asia/Irkutsk");
        NamedTimeZone.tz("ru", "Asia/Chita");
        NamedTimeZone.tz("ru", "Asia/Vladivostok");
        NamedTimeZone.tz("an", "Europe/Andorra");
        NamedTimeZone.tz("ae", "Asia/Abu_Dhabi");
        NamedTimeZone.tz("af", "Asia/Kabul");
        NamedTimeZone.tz("al", "Europe/Tirana");
        NamedTimeZone.tz("am", "Asia/Yerevan");
        NamedTimeZone.tz("ao", "Africa/Luanda");
        NamedTimeZone.tz("ar", "America/Buenos_Aires");
        NamedTimeZone.tz("as", "Pacific/Samoa");
        NamedTimeZone.tz("at", "Europe/Vienna");
        NamedTimeZone.tz("aw", "America/Aruba");
        NamedTimeZone.tz("az", "Asia/Baku");
        NamedTimeZone.tz("ba", "Europe/Sarajevo");
        NamedTimeZone.tz("bb", "America/Barbados");
        NamedTimeZone.tz("bd", "Asia/Dhaka");
        NamedTimeZone.tz("be", "Europe/Brussels", true);
        NamedTimeZone.tz("bf", "Africa/Ouagadougou");
        NamedTimeZone.tz("bg", "Europe/Sofia");
        NamedTimeZone.tz("bh", "Asia/Bahrain");
        NamedTimeZone.tz("bi", "Africa/Bujumbura");
        NamedTimeZone.tz("bm", "Atlantic/Bermuda");
        NamedTimeZone.tz("bn", "Asia/Brunei");
        NamedTimeZone.tz("bo", "America/La_Paz");
        NamedTimeZone.tz("br", "America/Sao_Paulo");
        NamedTimeZone.tz("bs", "America/Nassau");
        NamedTimeZone.tz("bw", "Gaborone");
        NamedTimeZone.tz("by", "Europe/Minsk");
        NamedTimeZone.tz("bz", "America/Belize");
        NamedTimeZone.tz("cd", "Africa/Kinshasa");
        NamedTimeZone.tz("ch", "Europe/Zurich");
        NamedTimeZone.tz("ci", "Africa/Abidjan");
        NamedTimeZone.tz("cl", "America/Santiago");
        NamedTimeZone.tz("cn", "Asia/Shanghai");
        NamedTimeZone.tz("co", "America/Bogota");
        NamedTimeZone.tz("cr", "America/Costa_Rica");
        NamedTimeZone.tz("cu", "America/Cuba");
        NamedTimeZone.tz("cv", "Atlantic/Cape_Verde");
        NamedTimeZone.tz("cy", "Asia/Nicosia");
        NamedTimeZone.tz("cz", "Europe/Prague");
        NamedTimeZone.tz("de", "Europe/Berlin");
        NamedTimeZone.tz("dj", "Africa/Djibouti");
        NamedTimeZone.tz("dk", "Europe/Copenhagen");
        NamedTimeZone.tz("do", "America/Santo_Domingo");
        NamedTimeZone.tz("dz", "Africa/Algiers");
        NamedTimeZone.tz("ec", "America/Quito");
        NamedTimeZone.tz("ee", "Europe/Tallinn");
        NamedTimeZone.tz("eg", "Africa/Cairo");
        NamedTimeZone.tz("er", "Africa/Asmara");
        NamedTimeZone.tz("es", "Europe/Madrid");
        NamedTimeZone.tz("fi", "Europe/Helsinki");
        NamedTimeZone.tz("fj", "Pacific/Fiji");
        NamedTimeZone.tz("fk", "America/Stanley");
        NamedTimeZone.tz("fr", "Europe/Paris");
        NamedTimeZone.tz("ga", "Africa/Libreville");
        NamedTimeZone.tz("gb", "Europe/London");
        NamedTimeZone.tz("gd", "America/Grenada");
        NamedTimeZone.tz("ge", "Asia/Tbilisi");
        NamedTimeZone.tz("gh", "Africa/Accra");
        NamedTimeZone.tz("gm", "Africa/Banjul");
        NamedTimeZone.tz("gn", "Africa/Conakry");
        NamedTimeZone.tz("gr", "Europe/Athens");
        NamedTimeZone.tz("gy", "America/Guyana");
        NamedTimeZone.tz("hk", "Asia/Hong_Kong");
        NamedTimeZone.tz("hn", "America/Tegucigalpa");
        NamedTimeZone.tz("hr", "Europe/Zagreb");
        NamedTimeZone.tz("ht", "America/Port-au-Prince");
        NamedTimeZone.tz("hu", "Europe/Budapest");
        NamedTimeZone.tz("id", "Asia/Jakarta");
        NamedTimeZone.tz("ie", "Europe/Dublin");
        NamedTimeZone.tz("il", "Asia/Tel_Aviv", true);
        NamedTimeZone.tz("in", "Asia/Calcutta", true);
        NamedTimeZone.tz("iq", "Asia/Baghdad");
        NamedTimeZone.tz("ir", "Asia/Tehran");
        NamedTimeZone.tz("is", "Atlantic/Reykjavik");
        NamedTimeZone.tz("it", "Europe/Rome");
        NamedTimeZone.tz("jm", "America/Jamaica");
        NamedTimeZone.tz("jo", "Asia/Amman");
        NamedTimeZone.tz("jp", "Asia/Tokyo", true);
        NamedTimeZone.tz("ke", "Africa/Nairobi");
        NamedTimeZone.tz("kg", "Asia/Bishkek");
        NamedTimeZone.tz("kh", "Asia/Phnom_Penh");
        NamedTimeZone.tz("kp", "Asia/Pyongyang");
        NamedTimeZone.tz("kr", "Asia/Seoul");
        NamedTimeZone.tz("kw", "Asia/Kuwait");
        NamedTimeZone.tz("lb", "Asia/Beirut");
        NamedTimeZone.tz("li", "Europe/Liechtenstein");
        NamedTimeZone.tz("lk", "Asia/Colombo");
        NamedTimeZone.tz("lr", "Africa/Monrovia");
        NamedTimeZone.tz("ls", "Africa/Maseru");
        NamedTimeZone.tz("lt", "Europe/Vilnius");
        NamedTimeZone.tz("lu", "Europe/Luxembourg");
        NamedTimeZone.tz("lv", "Europe/Riga");
        NamedTimeZone.tz("ly", "Africa/Tripoli");
        NamedTimeZone.tz("ma", "Africa/Rabat");
        NamedTimeZone.tz("mc", "Europe/Monaco");
        NamedTimeZone.tz("md", "Europe/Chisinau");
        NamedTimeZone.tz("mg", "Indian/Antananarivo");
        NamedTimeZone.tz("mk", "Europe/Skopje");
        NamedTimeZone.tz("ml", "Africa/Bamako");
        NamedTimeZone.tz("mm", "Asia/Rangoon");
        NamedTimeZone.tz("mn", "Asia/Ulaanbaatar");
        NamedTimeZone.tz("mo", "Asia/Macao");
        NamedTimeZone.tz("mq", "America/Martinique");
        NamedTimeZone.tz("mt", "Europe/Malta");
        NamedTimeZone.tz("mu", "Indian/Mauritius");
        NamedTimeZone.tz("mv", "Indian/Maldives");
        NamedTimeZone.tz("mw", "Africa/Lilongwe");
        NamedTimeZone.tz("mx", "America/Mexico_City");
        NamedTimeZone.tz("my", "Asia/Kuala_Lumpur");
        NamedTimeZone.tz("na", "Africa/Windhoek");
        NamedTimeZone.tz("ne", "Africa/Niamey");
        NamedTimeZone.tz("ng", "Africa/Lagos");
        NamedTimeZone.tz("ni", "America/Managua");
        NamedTimeZone.tz("nl", "Europe/Amsterdam");
        NamedTimeZone.tz("no", "Europe/Oslo");
        NamedTimeZone.tz("np", "Asia/Kathmandu");
        NamedTimeZone.tz("nz", "Pacific/Aukland");
        NamedTimeZone.tz("om", "Asia/Muscat");
        NamedTimeZone.tz("pa", "America/Panama");
        NamedTimeZone.tz("pe", "America/Lima");
        NamedTimeZone.tz("pg", "Pacific/Port_Moresby");
        NamedTimeZone.tz("ph", "Asia/Manila");
        NamedTimeZone.tz("pk", "Asia/Karachi");
        NamedTimeZone.tz("pl", "Europe/Warsaw");
        NamedTimeZone.tz("pr", "America/Puerto_Rico");
        NamedTimeZone.tz("pt", "Europe/Lisbon");
        NamedTimeZone.tz("py", "America/Asuncion");
        NamedTimeZone.tz("qa", "Asia/Qatar");
        NamedTimeZone.tz("ro", "Europe/Bucharest");
        NamedTimeZone.tz("rs", "Europe/Belgrade");
        NamedTimeZone.tz("rw", "Africa/Kigali");
        NamedTimeZone.tz("sa", "Asia/Riyadh");
        NamedTimeZone.tz("sd", "Africa/Khartoum");
        NamedTimeZone.tz("se", "Europe/Stockholm");
        NamedTimeZone.tz("sg", "Asia/Singapore");
        NamedTimeZone.tz("si", "Europe/Ljubljana");
        NamedTimeZone.tz("sk", "Europe/Bratislava");
        NamedTimeZone.tz("sl", "Africa/Freetown");
        NamedTimeZone.tz("so", "Africa/Mogadishu");
        NamedTimeZone.tz("sr", "America/Paramaribo");
        NamedTimeZone.tz("sv", "America/El_Salvador");
        NamedTimeZone.tz("sy", "Asia/Damascus");
        NamedTimeZone.tz("sz", "Africa/Mbabane");
        NamedTimeZone.tz("td", "Africa/Ndjamena");
        NamedTimeZone.tz("tg", "Africa/Lome");
        NamedTimeZone.tz("th", "Asia/Bangkok");
        NamedTimeZone.tz("tj", "Asia/Dushanbe");
        NamedTimeZone.tz("tm", "Asia/Ashgabat");
        NamedTimeZone.tz("tn", "Africa/Tunis");
        NamedTimeZone.tz("to", "Pacific/Tongatapu");
        NamedTimeZone.tz("tr", "Asia/Istanbul");
        NamedTimeZone.tz("tw", "Asia/Taipei");
        NamedTimeZone.tz("tz", "Africa/Dar_es_Salaam");
        NamedTimeZone.tz("ua", "Europe/Kiev");
        NamedTimeZone.tz("ug", "Africa/Kampala");
        NamedTimeZone.tz("uk", "Europe/London", true);
        NamedTimeZone.tz("uy", "America/Montevideo");
        NamedTimeZone.tz("uz", "Asia/Tashkent");
        NamedTimeZone.tz("ve", "America/Caracas");
        NamedTimeZone.tz("vn", "Asia/Hanoi");
        NamedTimeZone.tz("za", "Africa/Johannesburg");
        NamedTimeZone.tz("zm", "Africa/Lusaka");
        NamedTimeZone.tz("zw", "Africa/Harare");
    }
}

