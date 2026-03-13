/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.om;

import java.util.HashMap;
import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;

public abstract class StandardNames {
    private static final int DFLT_NS = 0;
    private static final int XSL_NS = 1;
    private static final int SAXON_NS = 2;
    private static final int XML_NS = 3;
    private static final int XS_NS = 4;
    private static final int XSI_NS = 5;
    public static final int DFLT = 0;
    public static final int XSL = 128;
    public static final int SAXON = 256;
    public static final int XML = 384;
    public static final int XS = 512;
    public static final int XSI = 640;
    public static final int XSL_ACCEPT = 128;
    public static final int XSL_ACCUMULATOR = 129;
    public static final int XSL_ACCUMULATOR_RULE = 130;
    public static final int XSL_ANALYZE_STRING = 131;
    public static final int XSL_APPLY_IMPORTS = 132;
    public static final int XSL_APPLY_TEMPLATES = 133;
    public static final int XSL_ASSERT = 134;
    public static final int XSL_ATTRIBUTE = 135;
    public static final int XSL_ATTRIBUTE_SET = 136;
    public static final int XSL_BREAK = 137;
    public static final int XSL_CALL_TEMPLATE = 138;
    public static final int XSL_CATCH = 139;
    public static final int XSL_CHARACTER_MAP = 141;
    public static final int XSL_CHOOSE = 142;
    public static final int XSL_COMMENT = 143;
    public static final int XSL_CONTEXT_ITEM = 144;
    public static final int XSL_COPY = 145;
    public static final int XSL_COPY_OF = 146;
    public static final int XSL_DECIMAL_FORMAT = 147;
    public static final int XSL_DOCUMENT = 150;
    public static final int XSL_ELEMENT = 151;
    public static final int XSL_EXPOSE = 152;
    public static final int XSL_EVALUATE = 153;
    public static final int XSL_FALLBACK = 154;
    public static final int XSL_FOR_EACH = 155;
    public static final int XSL_FORK = 156;
    public static final int XSL_FOR_EACH_GROUP = 157;
    public static final int XSL_FUNCTION = 158;
    public static final int XSL_GLOBAL_CONTEXT_ITEM = 159;
    public static final int XSL_IF = 160;
    public static final int XSL_IMPORT = 161;
    public static final int XSL_IMPORT_SCHEMA = 162;
    public static final int XSL_INCLUDE = 163;
    public static final int XSL_ITERATE = 164;
    public static final int XSL_KEY = 165;
    public static final int XSL_MAP = 166;
    public static final int XSL_MAP_ENTRY = 167;
    public static final int XSL_MATCHING_SUBSTRING = 168;
    public static final int XSL_MERGE = 169;
    public static final int XSL_MERGE_ACTION = 170;
    public static final int XSL_MERGE_KEY = 171;
    public static final int XSL_MERGE_SOURCE = 172;
    public static final int XSL_MESSAGE = 173;
    public static final int XSL_MODE = 174;
    public static final int XSL_NAMESPACE = 175;
    public static final int XSL_NAMESPACE_ALIAS = 176;
    public static final int XSL_NEXT_ITERATION = 177;
    public static final int XSL_NEXT_MATCH = 178;
    public static final int XSL_NON_MATCHING_SUBSTRING = 179;
    public static final int XSL_NUMBER = 180;
    public static final int XSL_OTHERWISE = 181;
    public static final int XSL_ON_COMPLETION = 182;
    public static final int XSL_ON_EMPTY = 183;
    public static final int XSL_ON_NON_EMPTY = 184;
    public static final int XSL_OUTPUT = 185;
    public static final int XSL_OVERRIDE = 186;
    public static final int XSL_OUTPUT_CHARACTER = 187;
    public static final int XSL_PACKAGE = 188;
    public static final int XSL_PARAM = 189;
    public static final int XSL_PERFORM_SORT = 190;
    public static final int XSL_PRESERVE_SPACE = 191;
    public static final int XSL_PROCESSING_INSTRUCTION = 192;
    public static final int XSL_RESULT_DOCUMENT = 193;
    public static final int XSL_SEQUENCE = 194;
    public static final int XSL_SORT = 195;
    public static final int XSL_SOURCE_DOCUMENT = 196;
    public static final int XSL_STRIP_SPACE = 198;
    public static final int XSL_STYLESHEET = 199;
    public static final int XSL_TEMPLATE = 200;
    public static final int XSL_TEXT = 201;
    public static final int XSL_TRANSFORM = 202;
    public static final int XSL_TRY = 203;
    public static final int XSL_USE_PACKAGE = 204;
    public static final int XSL_VALUE_OF = 205;
    public static final int XSL_VARIABLE = 206;
    public static final int XSL_WHEN = 207;
    public static final int XSL_WHERE_POPULATED = 208;
    public static final int XSL_WITH_PARAM = 209;
    public static final int XSL_DEFAULT_COLLATION = 228;
    public static final int XSL_DEFAULT_MODE = 229;
    public static final int XSL_DEFAULT_VALIDATION = 230;
    public static final int XSL_EXCLUDE_RESULT_PREFIXES = 231;
    public static final int XSL_EXPAND_TEXT = 232;
    public static final int XSL_EXTENSION_ELEMENT_PREFIXES = 233;
    public static final int XSL_INHERIT_NAMESPACES = 234;
    public static final int XSL_TYPE = 235;
    public static final int XSL_USE_ATTRIBUTE_SETS = 236;
    public static final int XSL_USE_WHEN = 237;
    public static final int XSL_VALIDATION = 238;
    public static final int XSL_VERSION = 239;
    public static final int XSL_XPATH_DEFAULT_NAMESPACE = 240;
    public static final int SAXON_ASSIGN = 257;
    public static final int SAXON_DEEP_UPDATE = 259;
    public static final int SAXON_DO = 262;
    public static final int SAXON_DOCTYPE = 263;
    public static final int SAXON_ENTITY_REF = 264;
    public static final int SAXON_TABULATE_MAPS = 265;
    public static final int SAXON_WHILE = 271;
    public static final int SAXON_PARAM = 276;
    public static final int SAXON_PREPROCESS = 277;
    public static final int SAXON_DISTINCT = 278;
    public static final int SAXON_ORDER = 279;
    private static final String SAXON_B = "{http://saxon.sf.net/}";
    public static final String SAXON_ASYCHRONOUS = "{http://saxon.sf.net/}asynchronous";
    public static final String SAXON_EXPLAIN = "{http://saxon.sf.net/}explain";
    public static final int XML_BASE = 385;
    public static final int XML_SPACE = 386;
    public static final int XML_LANG = 387;
    public static final int XML_ID = 388;
    public static final int XML_LANG_TYPE = 389;
    public static final int XML_SPACE_TYPE = 6;
    public static final NodeName XML_ID_NAME = new FingerprintedQName("xml", "http://www.w3.org/XML/1998/namespace", "id", 388);
    public static final int XS_STRING = 513;
    public static final int XS_BOOLEAN = 514;
    public static final int XS_DECIMAL = 515;
    public static final int XS_FLOAT = 516;
    public static final int XS_DOUBLE = 517;
    public static final int XS_DURATION = 518;
    public static final int XS_DATE_TIME = 519;
    public static final int XS_TIME = 520;
    public static final int XS_DATE = 521;
    public static final int XS_G_YEAR_MONTH = 522;
    public static final int XS_G_YEAR = 523;
    public static final int XS_G_MONTH_DAY = 524;
    public static final int XS_G_DAY = 525;
    public static final int XS_G_MONTH = 526;
    public static final int XS_HEX_BINARY = 527;
    public static final int XS_BASE64_BINARY = 528;
    public static final int XS_ANY_URI = 529;
    public static final int XS_QNAME = 530;
    public static final int XS_NOTATION = 531;
    public static final int XS_INTEGER = 533;
    public static final int XS_NON_POSITIVE_INTEGER = 534;
    public static final int XS_NEGATIVE_INTEGER = 535;
    public static final int XS_LONG = 536;
    public static final int XS_INT = 537;
    public static final int XS_SHORT = 538;
    public static final int XS_BYTE = 539;
    public static final int XS_NON_NEGATIVE_INTEGER = 540;
    public static final int XS_POSITIVE_INTEGER = 541;
    public static final int XS_UNSIGNED_LONG = 542;
    public static final int XS_UNSIGNED_INT = 543;
    public static final int XS_UNSIGNED_SHORT = 544;
    public static final int XS_UNSIGNED_BYTE = 545;
    public static final int XS_NORMALIZED_STRING = 553;
    public static final int XS_TOKEN = 554;
    public static final int XS_LANGUAGE = 555;
    public static final int XS_NMTOKEN = 556;
    public static final int XS_NMTOKENS = 557;
    public static final int XS_NAME = 558;
    public static final int XS_NCNAME = 559;
    public static final int XS_ID = 560;
    public static final int XS_IDREF = 561;
    public static final int XS_IDREFS = 562;
    public static final int XS_ENTITY = 563;
    public static final int XS_ENTITIES = 564;
    public static final int XS_DATE_TIME_STAMP = 565;
    public static final int XS_ANY_TYPE = 572;
    public static final int XS_ANY_SIMPLE_TYPE = 573;
    public static final int XS_INVALID_NAME = 574;
    public static final int XS_ERROR = 575;
    public static final int XS_ALL = 576;
    public static final int XS_ALTERNATIVE = 577;
    public static final int XS_ANNOTATION = 578;
    public static final int XS_ANY = 579;
    public static final int XS_ANY_ATTRIBUTE = 580;
    public static final int XS_APPINFO = 581;
    public static final int XS_ASSERT = 582;
    public static final int XS_ASSERTION = 583;
    public static final int XS_ATTRIBUTE = 584;
    public static final int XS_ATTRIBUTE_GROUP = 585;
    public static final int XS_CHOICE = 586;
    public static final int XS_COMPLEX_CONTENT = 587;
    public static final int XS_COMPLEX_TYPE = 588;
    public static final int XS_DEFAULT_OPEN_CONTENT = 589;
    public static final int XS_DOCUMENTATION = 590;
    public static final int XS_ELEMENT = 591;
    public static final int XS_ENUMERATION = 592;
    public static final int XS_EXTENSION = 593;
    public static final int XS_FIELD = 594;
    public static final int XS_FRACTION_DIGITS = 595;
    public static final int XS_GROUP = 596;
    public static final int XS_IMPORT = 597;
    public static final int XS_INCLUDE = 598;
    public static final int XS_KEY = 599;
    public static final int XS_KEYREF = 600;
    public static final int XS_LENGTH = 601;
    public static final int XS_LIST = 602;
    public static final int XS_MAX_EXCLUSIVE = 603;
    public static final int XS_MAX_INCLUSIVE = 604;
    public static final int XS_MAX_LENGTH = 605;
    public static final int XS_MAX_SCALE = 606;
    public static final int XS_MIN_EXCLUSIVE = 607;
    public static final int XS_MIN_INCLUSIVE = 608;
    public static final int XS_MIN_LENGTH = 609;
    public static final int XS_MIN_SCALE = 610;
    public static final int XS_notation = 611;
    public static final int XS_OPEN_CONTENT = 612;
    public static final int XS_OVERRIDE = 613;
    public static final int XS_PATTERN = 614;
    public static final int XS_REDEFINE = 615;
    public static final int XS_RESTRICTION = 616;
    public static final int XS_SCHEMA = 617;
    public static final int XS_SELECTOR = 618;
    public static final int XS_SEQUENCE = 619;
    public static final int XS_SIMPLE_CONTENT = 620;
    public static final int XS_SIMPLE_TYPE = 621;
    public static final int XS_EXPLICIT_TIMEZONE = 622;
    public static final int XS_TOTAL_DIGITS = 623;
    public static final int XS_UNION = 624;
    public static final int XS_UNIQUE = 625;
    public static final int XS_WHITE_SPACE = 626;
    public static final int XS_UNTYPED = 630;
    public static final int XS_UNTYPED_ATOMIC = 631;
    public static final int XS_ANY_ATOMIC_TYPE = 632;
    public static final int XS_YEAR_MONTH_DURATION = 633;
    public static final int XS_DAY_TIME_DURATION = 634;
    public static final int XS_NUMERIC = 635;
    public static final int XSI_TYPE = 641;
    public static final int XSI_NIL = 642;
    public static final int XSI_SCHEMA_LOCATION = 643;
    public static final int XSI_NO_NAMESPACE_SCHEMA_LOCATION = 644;
    public static final int XSI_SCHEMA_LOCATION_TYPE = 645;
    private static String[] localNames = new String[1023];
    private static HashMap<String, Integer> lookup = new HashMap(1023);
    public static StructuredQName[] errorVariables = new StructuredQName[]{new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "code"), new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "description"), new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "value"), new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "module"), new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "line-number"), new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "column-number"), new StructuredQName("err", "http://www.w3.org/2005/xqt-errors", "additional")};
    public static final StructuredQName SQ_XS_INVALID_NAME;

    private StandardNames() {
    }

    private static void bindXSLTName(int constant, String localName) {
        StandardNames.localNames[constant] = localName;
        lookup.put("{http://www.w3.org/1999/XSL/Transform}" + localName, constant);
    }

    private static void bindSaxonName(int constant, String localName) {
        StandardNames.localNames[constant] = localName;
        lookup.put(SAXON_B + localName, constant);
    }

    private static void bindXMLName(int constant, String localName) {
        StandardNames.localNames[constant] = localName;
        lookup.put("{http://www.w3.org/XML/1998/namespace}" + localName, constant);
    }

    private static void bindXSName(int constant, String localName) {
        StandardNames.localNames[constant] = localName;
        lookup.put("{http://www.w3.org/2001/XMLSchema}" + localName, constant);
    }

    private static void bindXSIName(int constant, String localName) {
        StandardNames.localNames[constant] = localName;
        lookup.put("{http://www.w3.org/2001/XMLSchema-instance}" + localName, constant);
    }

    public static int getFingerprint(String uri, String localName) {
        Integer fp = lookup.get('{' + uri + '}' + localName);
        if (fp == null) {
            return -1;
        }
        return fp;
    }

    public static String getLocalName(int fingerprint) {
        return localNames[fingerprint];
    }

    public static String getURI(int fingerprint) {
        int c = fingerprint >> 7;
        switch (c) {
            case 0: {
                return "";
            }
            case 1: {
                return "http://www.w3.org/1999/XSL/Transform";
            }
            case 2: {
                return "http://saxon.sf.net/";
            }
            case 3: {
                return "http://www.w3.org/XML/1998/namespace";
            }
            case 4: {
                return "http://www.w3.org/2001/XMLSchema";
            }
            case 5: {
                return "http://www.w3.org/2001/XMLSchema-instance";
            }
        }
        throw new IllegalArgumentException("Unknown system fingerprint " + fingerprint);
    }

    public static String getClarkName(int fingerprint) {
        String uri = StandardNames.getURI(fingerprint);
        if (uri.isEmpty()) {
            return StandardNames.getLocalName(fingerprint);
        }
        return '{' + uri + '}' + StandardNames.getLocalName(fingerprint);
    }

    public static String getPrefix(int fingerprint) {
        int c = fingerprint >> 7;
        switch (c) {
            case 0: {
                return "";
            }
            case 1: {
                return "xsl";
            }
            case 2: {
                return "saxon";
            }
            case 3: {
                return "xml";
            }
            case 4: {
                return "xs";
            }
            case 5: {
                return "xsi";
            }
        }
        return null;
    }

    public static String getDisplayName(int fingerprint) {
        if (fingerprint == -1) {
            return "(anonymous type)";
        }
        if (fingerprint > 1023) {
            return "(" + fingerprint + ')';
        }
        if (fingerprint >> 7 == 0) {
            return StandardNames.getLocalName(fingerprint);
        }
        return StandardNames.getPrefix(fingerprint) + ':' + StandardNames.getLocalName(fingerprint);
    }

    public static StructuredQName getStructuredQName(int fingerprint) {
        return new StructuredQName(StandardNames.getPrefix(fingerprint), StandardNames.getURI(fingerprint), StandardNames.getLocalName(fingerprint));
    }

    public static StructuredQName getUnprefixedQName(int fingerprint) {
        return new StructuredQName("", StandardNames.getURI(fingerprint), StandardNames.getLocalName(fingerprint));
    }

    static {
        StandardNames.bindXSLTName(128, "accept");
        StandardNames.bindXSLTName(129, "accumulator");
        StandardNames.bindXSLTName(130, "accumulator-rule");
        StandardNames.bindXSLTName(131, "analyze-string");
        StandardNames.bindXSLTName(132, "apply-imports");
        StandardNames.bindXSLTName(133, "apply-templates");
        StandardNames.bindXSLTName(134, "assert");
        StandardNames.bindXSLTName(135, "attribute");
        StandardNames.bindXSLTName(136, "attribute-set");
        StandardNames.bindXSLTName(137, "break");
        StandardNames.bindXSLTName(138, "call-template");
        StandardNames.bindXSLTName(139, "catch");
        StandardNames.bindXSLTName(141, "character-map");
        StandardNames.bindXSLTName(142, "choose");
        StandardNames.bindXSLTName(143, "comment");
        StandardNames.bindXSLTName(144, "context-item");
        StandardNames.bindXSLTName(145, "copy");
        StandardNames.bindXSLTName(146, "copy-of");
        StandardNames.bindXSLTName(147, "decimal-format");
        StandardNames.bindXSLTName(150, "document");
        StandardNames.bindXSLTName(151, "element");
        StandardNames.bindXSLTName(153, "evaluate");
        StandardNames.bindXSLTName(152, "expose");
        StandardNames.bindXSLTName(154, "fallback");
        StandardNames.bindXSLTName(155, "for-each");
        StandardNames.bindXSLTName(157, "for-each-group");
        StandardNames.bindXSLTName(156, "fork");
        StandardNames.bindXSLTName(158, "function");
        StandardNames.bindXSLTName(159, "global-context-item");
        StandardNames.bindXSLTName(160, "if");
        StandardNames.bindXSLTName(161, "import");
        StandardNames.bindXSLTName(162, "import-schema");
        StandardNames.bindXSLTName(163, "include");
        StandardNames.bindXSLTName(164, "iterate");
        StandardNames.bindXSLTName(165, "key");
        StandardNames.bindXSLTName(166, "map");
        StandardNames.bindXSLTName(167, "map-entry");
        StandardNames.bindXSLTName(168, "matching-substring");
        StandardNames.bindXSLTName(169, "merge");
        StandardNames.bindXSLTName(172, "merge-source");
        StandardNames.bindXSLTName(170, "merge-action");
        StandardNames.bindXSLTName(171, "merge-key");
        StandardNames.bindXSLTName(173, "message");
        StandardNames.bindXSLTName(174, "mode");
        StandardNames.bindXSLTName(178, "next-match");
        StandardNames.bindXSLTName(180, "number");
        StandardNames.bindXSLTName(175, "namespace");
        StandardNames.bindXSLTName(176, "namespace-alias");
        StandardNames.bindXSLTName(177, "next-iteration");
        StandardNames.bindXSLTName(179, "non-matching-substring");
        StandardNames.bindXSLTName(182, "on-completion");
        StandardNames.bindXSLTName(183, "on-empty");
        StandardNames.bindXSLTName(184, "on-non-empty");
        StandardNames.bindXSLTName(181, "otherwise");
        StandardNames.bindXSLTName(185, "output");
        StandardNames.bindXSLTName(187, "output-character");
        StandardNames.bindXSLTName(186, "override");
        StandardNames.bindXSLTName(188, "package");
        StandardNames.bindXSLTName(189, "param");
        StandardNames.bindXSLTName(190, "perform-sort");
        StandardNames.bindXSLTName(191, "preserve-space");
        StandardNames.bindXSLTName(192, "processing-instruction");
        StandardNames.bindXSLTName(193, "result-document");
        StandardNames.bindXSLTName(194, "sequence");
        StandardNames.bindXSLTName(195, "sort");
        StandardNames.bindXSLTName(196, "source-document");
        StandardNames.bindXSLTName(198, "strip-space");
        StandardNames.bindXSLTName(199, "stylesheet");
        StandardNames.bindXSLTName(200, "template");
        StandardNames.bindXSLTName(201, "text");
        StandardNames.bindXSLTName(202, "transform");
        StandardNames.bindXSLTName(203, "try");
        StandardNames.bindXSLTName(204, "use-package");
        StandardNames.bindXSLTName(205, "value-of");
        StandardNames.bindXSLTName(206, "variable");
        StandardNames.bindXSLTName(209, "with-param");
        StandardNames.bindXSLTName(207, "when");
        StandardNames.bindXSLTName(208, "where-populated");
        StandardNames.bindXSLTName(228, "default-collation");
        StandardNames.bindXSLTName(229, "default-mode");
        StandardNames.bindXSLTName(230, "default-validation");
        StandardNames.bindXSLTName(232, "expand-text");
        StandardNames.bindXSLTName(231, "exclude-result-prefixes");
        StandardNames.bindXSLTName(233, "extension-element-prefixes");
        StandardNames.bindXSLTName(234, "inherit-namespaces");
        StandardNames.bindXSLTName(235, "type");
        StandardNames.bindXSLTName(236, "use-attribute-sets");
        StandardNames.bindXSLTName(237, "use-when");
        StandardNames.bindXSLTName(238, "validation");
        StandardNames.bindXSLTName(239, "version");
        StandardNames.bindXSLTName(240, "xpath-default-namespace");
        StandardNames.bindSaxonName(257, "assign");
        StandardNames.bindSaxonName(259, "deep-update");
        StandardNames.bindSaxonName(278, "distinct");
        StandardNames.bindSaxonName(262, "do");
        StandardNames.bindSaxonName(263, "doctype");
        StandardNames.bindSaxonName(264, "entity-ref");
        StandardNames.bindSaxonName(279, "order");
        StandardNames.bindSaxonName(271, "while");
        StandardNames.bindSaxonName(276, "param");
        StandardNames.bindSaxonName(277, "preprocess");
        StandardNames.bindXMLName(385, "base");
        StandardNames.bindXMLName(386, "space");
        StandardNames.bindXMLName(387, "lang");
        StandardNames.bindXMLName(388, "id");
        StandardNames.bindXMLName(389, "_langType");
        StandardNames.bindXMLName(6, "_spaceType");
        StandardNames.bindXSName(513, "string");
        StandardNames.bindXSName(514, "boolean");
        StandardNames.bindXSName(515, "decimal");
        StandardNames.bindXSName(516, "float");
        StandardNames.bindXSName(517, "double");
        StandardNames.bindXSName(518, "duration");
        StandardNames.bindXSName(519, "dateTime");
        StandardNames.bindXSName(520, "time");
        StandardNames.bindXSName(521, "date");
        StandardNames.bindXSName(522, "gYearMonth");
        StandardNames.bindXSName(523, "gYear");
        StandardNames.bindXSName(524, "gMonthDay");
        StandardNames.bindXSName(525, "gDay");
        StandardNames.bindXSName(526, "gMonth");
        StandardNames.bindXSName(527, "hexBinary");
        StandardNames.bindXSName(528, "base64Binary");
        StandardNames.bindXSName(529, "anyURI");
        StandardNames.bindXSName(530, "QName");
        StandardNames.bindXSName(531, "NOTATION");
        StandardNames.bindXSName(635, "numeric");
        StandardNames.bindXSName(533, "integer");
        StandardNames.bindXSName(534, "nonPositiveInteger");
        StandardNames.bindXSName(535, "negativeInteger");
        StandardNames.bindXSName(536, "long");
        StandardNames.bindXSName(537, "int");
        StandardNames.bindXSName(538, "short");
        StandardNames.bindXSName(539, "byte");
        StandardNames.bindXSName(540, "nonNegativeInteger");
        StandardNames.bindXSName(541, "positiveInteger");
        StandardNames.bindXSName(542, "unsignedLong");
        StandardNames.bindXSName(543, "unsignedInt");
        StandardNames.bindXSName(544, "unsignedShort");
        StandardNames.bindXSName(545, "unsignedByte");
        StandardNames.bindXSName(553, "normalizedString");
        StandardNames.bindXSName(554, "token");
        StandardNames.bindXSName(555, "language");
        StandardNames.bindXSName(556, "NMTOKEN");
        StandardNames.bindXSName(557, "NMTOKENS");
        StandardNames.bindXSName(558, "Name");
        StandardNames.bindXSName(559, "NCName");
        StandardNames.bindXSName(560, "ID");
        StandardNames.bindXSName(561, "IDREF");
        StandardNames.bindXSName(562, "IDREFS");
        StandardNames.bindXSName(563, "ENTITY");
        StandardNames.bindXSName(564, "ENTITIES");
        StandardNames.bindXSName(565, "dateTimeStamp");
        StandardNames.bindXSName(572, "anyType");
        StandardNames.bindXSName(573, "anySimpleType");
        StandardNames.bindXSName(574, "invalidName");
        StandardNames.bindXSName(575, "error");
        StandardNames.bindXSName(576, "all");
        StandardNames.bindXSName(577, "alternative");
        StandardNames.bindXSName(578, "annotation");
        StandardNames.bindXSName(579, "any");
        StandardNames.bindXSName(580, "anyAttribute");
        StandardNames.bindXSName(581, "appinfo");
        StandardNames.bindXSName(582, "assert");
        StandardNames.bindXSName(583, "assertion");
        StandardNames.bindXSName(584, "attribute");
        StandardNames.bindXSName(585, "attributeGroup");
        StandardNames.bindXSName(586, "choice");
        StandardNames.bindXSName(587, "complexContent");
        StandardNames.bindXSName(588, "complexType");
        StandardNames.bindXSName(589, "defaultOpenContent");
        StandardNames.bindXSName(590, "documentation");
        StandardNames.bindXSName(591, "element");
        StandardNames.bindXSName(592, "enumeration");
        StandardNames.bindXSName(622, "explicitTimezone");
        StandardNames.bindXSName(593, "extension");
        StandardNames.bindXSName(594, "field");
        StandardNames.bindXSName(595, "fractionDigits");
        StandardNames.bindXSName(596, "group");
        StandardNames.bindXSName(597, "import");
        StandardNames.bindXSName(598, "include");
        StandardNames.bindXSName(599, "key");
        StandardNames.bindXSName(600, "keyref");
        StandardNames.bindXSName(601, "length");
        StandardNames.bindXSName(602, "list");
        StandardNames.bindXSName(603, "maxExclusive");
        StandardNames.bindXSName(604, "maxInclusive");
        StandardNames.bindXSName(605, "maxLength");
        StandardNames.bindXSName(606, "maxScale");
        StandardNames.bindXSName(607, "minExclusive");
        StandardNames.bindXSName(608, "minInclusive");
        StandardNames.bindXSName(609, "minLength");
        StandardNames.bindXSName(610, "minScale");
        StandardNames.bindXSName(611, "notation");
        StandardNames.bindXSName(612, "openContent");
        StandardNames.bindXSName(613, "override");
        StandardNames.bindXSName(614, "pattern");
        StandardNames.bindXSName(615, "redefine");
        StandardNames.bindXSName(616, "restriction");
        StandardNames.bindXSName(617, "schema");
        StandardNames.bindXSName(618, "selector");
        StandardNames.bindXSName(619, "sequence");
        StandardNames.bindXSName(620, "simpleContent");
        StandardNames.bindXSName(621, "simpleType");
        StandardNames.bindXSName(623, "totalDigits");
        StandardNames.bindXSName(624, "union");
        StandardNames.bindXSName(625, "unique");
        StandardNames.bindXSName(626, "whiteSpace");
        StandardNames.bindXSName(630, "untyped");
        StandardNames.bindXSName(631, "untypedAtomic");
        StandardNames.bindXSName(632, "anyAtomicType");
        StandardNames.bindXSName(633, "yearMonthDuration");
        StandardNames.bindXSName(634, "dayTimeDuration");
        StandardNames.bindXSIName(641, "type");
        StandardNames.bindXSIName(642, "nil");
        StandardNames.bindXSIName(643, "schemaLocation");
        StandardNames.bindXSIName(644, "noNamespaceSchemaLocation");
        StandardNames.bindXSIName(645, "anonymous_schemaLocationType");
        SQ_XS_INVALID_NAME = StandardNames.getStructuredQName(574);
    }
}

