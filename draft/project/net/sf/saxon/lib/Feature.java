/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.lib;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.EnvironmentVariableResolver;
import net.sf.saxon.lib.ModuleURIResolver;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.SchemaURIResolver;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.om.NamePool;

public class Feature<T> {
    public String name;
    public final int code;
    public int requiredEdition;
    public Class<? extends T> type;
    public T defaultValue;
    private static final int HE = 0;
    private static final int PE = 1;
    private static final int EE = 2;
    private static final Map<String, Feature> index = new TreeMap<String, Feature>();
    public static final Feature<Boolean> ALLOW_EXTERNAL_FUNCTIONS = new Feature<Boolean>("http://saxon.sf.net/feature/allow-external-functions", 1, 0, Boolean.class, false);
    public static final Feature<Boolean> ALLOW_MULTITHREADING = new Feature<Boolean>("http://saxon.sf.net/feature/allow-multithreading", 2, 2, Boolean.class, false);
    public static final Feature<Boolean> ALLOW_OLD_JAVA_URI_FORMAT = new Feature<Boolean>("http://saxon.sf.net/feature/allow-old-java-uri-format", 3, 1, Boolean.class, false);
    public static final Feature<Boolean> ALLOW_SYNTAX_EXTENSIONS = new Feature<Boolean>("http://saxon.sf.net/feature/allowSyntaxExtensions", 4, 1, Boolean.class, false);
    public static final Feature<Boolean> ASSERTIONS_CAN_SEE_COMMENTS = new Feature<Boolean>("http://saxon.sf.net/feature/assertionsCanSeeComments", 5, 2, Boolean.class, false);
    public static final Feature<CollationURIResolver> COLLATION_URI_RESOLVER = new Feature<Object>("http://saxon.sf.net/feature/collation-uri-resolver", 6, 0, CollationURIResolver.class, null);
    public static final Feature<String> COLLATION_URI_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/collation-uri-resolver-class", 7, 0, String.class, null);
    public static final Feature<CollectionFinder> COLLECTION_FINDER = new Feature<Object>("http://saxon.sf.net/feature/collection-finder", 8, 0, CollectionFinder.class, null);
    public static final Feature<String> COLLECTION_FINDER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/collection-finder-class", 9, 0, String.class, null);
    public static final Feature<Boolean> COMPILE_WITH_TRACING = new Feature<Boolean>("http://saxon.sf.net/feature/compile-with-tracing", 12, 0, Boolean.class, false);
    public static final Feature<Configuration> CONFIGURATION = new Feature<Object>("http://saxon.sf.net/feature/configuration", 13, 0, Configuration.class, null);
    public static final Feature<String> CONFIGURATION_FILE = new Feature<Object>("http://saxon.sf.net/feature/configuration-file", 14, 1, String.class, null);
    public static final Feature<Boolean> DEBUG_BYTE_CODE = new Feature<Boolean>("http://saxon.sf.net/feature/debugByteCode", 15, 2, Boolean.class, false);
    public static final Feature<String> DEBUG_BYTE_CODE_DIR = new Feature<Object>("http://saxon.sf.net/feature/debugByteCodeDir", 16, 2, String.class, null);
    public static final Feature<String> DEFAULT_COLLATION = new Feature<Object>("http://saxon.sf.net/feature/defaultCollation", 17, 0, String.class, null);
    public static final Feature<String> DEFAULT_COLLECTION = new Feature<Object>("http://saxon.sf.net/feature/defaultCollection", 18, 0, String.class, null);
    public static final Feature<String> DEFAULT_COUNTRY = new Feature<Object>("http://saxon.sf.net/feature/defaultCountry", 19, 0, String.class, null);
    public static final Feature<String> DEFAULT_LANGUAGE = new Feature<Object>("http://saxon.sf.net/feature/defaultLanguage", 20, 0, String.class, null);
    public static final Feature<String> DEFAULT_REGEX_ENGINE = new Feature<Object>("http://saxon.sf.net/feature/defaultRegexEngine", 21, 0, String.class, null);
    public static final Feature<Boolean> DISABLE_XSL_EVALUATE = new Feature<Boolean>("http://saxon.sf.net/feature/disableXslEvaluate", 22, 2, Boolean.class, false);
    public static final Feature<Boolean> DISPLAY_BYTE_CODE = new Feature<Boolean>("http://saxon.sf.net/feature/displayByteCode", 23, 2, Boolean.class, false);
    public static final Feature<Boolean> DTD_VALIDATION = new Feature<Boolean>("http://saxon.sf.net/feature/validation", 24, 0, Boolean.class, false);
    public static final Feature<Boolean> DTD_VALIDATION_RECOVERABLE = new Feature<Boolean>("http://saxon.sf.net/feature/dtd-validation-recoverable", 25, 0, Boolean.class, false);
    public static final Feature<Boolean> EAGER_EVALUATION = new Feature<Boolean>("http://saxon.sf.net/feature/eagerEvaluation", 26, 0, Boolean.class, false);
    public static final Feature<String> ENTITY_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/entityResolverClass", 27, 0, String.class, null);
    public static final Feature<EnvironmentVariableResolver> ENVIRONMENT_VARIABLE_RESOLVER = new Feature<Object>("http://saxon.sf.net/feature/environmentVariableResolver", 28, 0, EnvironmentVariableResolver.class, null);
    public static final Feature<String> ENVIRONMENT_VARIABLE_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/environmentVariableResolverClass", 29, 0, String.class, null);
    public static final Feature<String> ERROR_LISTENER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/errorListenerClass", 30, 0, String.class, null);
    public static final Feature<Boolean> EXPAND_ATTRIBUTE_DEFAULTS = new Feature<Boolean>("http://saxon.sf.net/feature/expandAttributeDefaults", 31, 0, Boolean.class, false);
    public static final Feature<Boolean> EXPATH_FILE_DELETE_TEMPORARY_FILES = new Feature<Boolean>("http://saxon.sf.net/feature/expathFileDeleteTemporaryFiles", 32, 1, Boolean.class, false);
    public static final Feature<Boolean> GENERATE_BYTE_CODE = new Feature<Boolean>("http://saxon.sf.net/feature/generateByteCode", 33, 2, Boolean.class, false);
    public static final Feature<Boolean> IGNORE_SAX_SOURCE_PARSER = new Feature<Boolean>("http://saxon.sf.net/feature/ignoreSAXSourceParser", 34, 0, Boolean.class, false);
    public static final Feature<Boolean> IMPLICIT_SCHEMA_IMPORTS = new Feature<Boolean>("http://saxon.sf.net/feature/implicitSchemaImports", 35, 2, Boolean.class, false);
    public static final Feature<Boolean> LAZY_CONSTRUCTION_MODE = new Feature<Boolean>("http://saxon.sf.net/feature/lazyConstructionMode", 36, 0, Boolean.class, false);
    public static final Feature<String> LICENSE_FILE_LOCATION = new Feature<Object>("http://saxon.sf.net/feature/licenseFileLocation", 37, 1, String.class, null);
    public static final Feature<Boolean> LINE_NUMBERING = new Feature<Boolean>("http://saxon.sf.net/feature/linenumbering", 38, 0, Boolean.class, false);
    public static final Feature<Boolean> MARK_DEFAULTED_ATTRIBUTES = new Feature<Boolean>("http://saxon.sf.net/feature/markDefaultedAttributes", 39, 0, Boolean.class, false);
    public static final Feature<Integer> MAX_COMPILED_CLASSES = new Feature<Object>("http://saxon.sf.net/feature/maxCompiledClasses", 40, 2, Integer.class, null);
    public static final Feature<String> MESSAGE_EMITTER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/messageEmitterClass", 41, 0, String.class, null);
    public static final Feature<ModuleURIResolver> MODULE_URI_RESOLVER = new Feature<Object>("http://saxon.sf.net/feature/moduleURIResolver", 42, 0, ModuleURIResolver.class, null);
    public static final Feature<String> MODULE_URI_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/moduleURIResolverClass", 43, 0, String.class, null);
    public static final Feature<Boolean> MONITOR_HOT_SPOT_BYTE_CODE = new Feature<Boolean>("http://saxon.sf.net/feature/monitorHotSpotByteCode", 44, 2, Boolean.class, false);
    public static final Feature<Boolean> MULTIPLE_SCHEMA_IMPORTS = new Feature<Boolean>("http://saxon.sf.net/feature/multipleSchemaImports", 45, 2, Boolean.class, false);
    public static final Feature<NamePool> NAME_POOL = new Feature<Object>("http://saxon.sf.net/feature/namePool", 46, 0, NamePool.class, null);
    public static final Feature<Object> OCCURRENCE_LIMITS = new Feature<Object>("http://saxon.sf.net/feature/occurrenceLimits", 47, 2, Object.class, null);
    public static final Feature<Object> OPTIMIZATION_LEVEL = new Feature<Object>("http://saxon.sf.net/feature/optimizationLevel", 48, 0, Object.class, null);
    public static final Feature<OutputURIResolver> OUTPUT_URI_RESOLVER = new Feature<Object>("http://saxon.sf.net/feature/outputURIResolver", 49, 0, OutputURIResolver.class, null);
    public static final Feature<String> OUTPUT_URI_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/outputURIResolverClass", 50, 0, String.class, null);
    public static final Feature<Boolean> PRE_EVALUATE_DOC_FUNCTION = new Feature<Boolean>("http://saxon.sf.net/feature/preEvaluateDocFunction", 51, 0, Boolean.class, false);
    public static final Feature<Boolean> PREFER_JAXP_PARSER = new Feature<Boolean>("http://saxon.sf.net/feature/preferJaxpParser", 52, 0, Boolean.class, false);
    public static final Feature<Boolean> RECOGNIZE_URI_QUERY_PARAMETERS = new Feature<Boolean>("http://saxon.sf.net/feature/recognize-uri-query-parameters", 53, 0, Boolean.class, false);
    public static final Feature<Integer> RECOVERY_POLICY = new Feature<Object>("http://saxon.sf.net/feature/recoveryPolicy", 54, 0, Integer.class, null);
    public static final Feature<String> RECOVERY_POLICY_NAME = new Feature<Object>("http://saxon.sf.net/feature/recoveryPolicyName", 55, 0, String.class, null);
    public static final Feature<Integer> RESULT_DOCUMENT_THREADS = new Feature<Object>("http://saxon.sf.net/feature/resultDocumentThreads", 56, 2, Integer.class, null);
    public static final Feature<Boolean> RETAIN_DTD_ATTRIBUTE_TYPES = new Feature<Boolean>("http://saxon.sf.net/feature/retain-dtd-attribute-types", 57, 0, Boolean.class, false);
    public static final Feature<SchemaURIResolver> SCHEMA_URI_RESOLVER = new Feature<Object>("http://saxon.sf.net/feature/schemaURIResolver", 58, 2, SchemaURIResolver.class, null);
    public static final Feature<String> SCHEMA_URI_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/schemaURIResolverClass", 59, 2, String.class, null);
    public static final Feature<Integer> SCHEMA_VALIDATION = new Feature<Object>("http://saxon.sf.net/feature/schema-validation", 60, 2, Integer.class, null);
    public static final Feature<String> SCHEMA_VALIDATION_MODE = new Feature<Object>("http://saxon.sf.net/feature/schema-validation-mode", 61, 2, String.class, null);
    public static final Feature<String> SERIALIZER_FACTORY_CLASS = new Feature<Object>("http://saxon.sf.net/feature/serializerFactoryClass", 62, 0, String.class, null);
    public static final Feature<String> SOURCE_PARSER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/sourceParserClass", 63, 0, String.class, null);
    public static final Feature<String> SOURCE_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/sourceResolverClass", 64, 0, String.class, null);
    public static final Feature<Boolean> STABLE_COLLECTION_URI = new Feature<Boolean>("http://saxon.sf.net/feature/stableCollectionUri", 65, 0, Boolean.class, false);
    public static final Feature<Boolean> STABLE_UNPARSED_TEXT = new Feature<Boolean>("http://saxon.sf.net/feature/stableUnparsedText", 66, 0, Boolean.class, false);
    public static final Feature<String> STANDARD_ERROR_OUTPUT_FILE = new Feature<Object>("http://saxon.sf.net/feature/standardErrorOutputFile", 67, 0, String.class, null);
    public static final Feature<String> STREAMABILITY = new Feature<Object>("http://saxon.sf.net/feature/streamability", 68, 2, String.class, null);
    public static final Feature<Boolean> STRICT_STREAMABILITY = new Feature<Boolean>("http://saxon.sf.net/feature/strictStreamability", 69, 2, Boolean.class, false);
    public static final Feature<Boolean> STREAMING_FALLBACK = new Feature<Boolean>("http://saxon.sf.net/feature/streamingFallback", 70, 0, Boolean.class, false);
    public static final Feature<String> STRIP_WHITESPACE = new Feature<Object>("http://saxon.sf.net/feature/strip-whitespace", 71, 0, String.class, null);
    public static final Feature<String> STYLE_PARSER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/styleParserClass", 72, 0, String.class, null);
    public static final Feature<Boolean> SUPPRESS_EVALUATION_EXPIRY_WARNING = new Feature<Boolean>("http://saxon.sf.net/feature/suppressEvaluationExpiryWarning", 73, 1, Boolean.class, false);
    public static final Feature<Boolean> SUPPRESS_XPATH_WARNINGS = new Feature<Boolean>("http://saxon.sf.net/feature/suppressXPathWarnings", 74, 0, Boolean.class, false);
    public static final Feature<Boolean> SUPPRESS_XSLT_NAMESPACE_CHECK = new Feature<Boolean>("http://saxon.sf.net/feature/suppressXsltNamespaceCheck", 75, 0, Boolean.class, false);
    public static final Feature<Integer> THRESHOLD_FOR_COMPILING_TYPES = new Feature<Object>("http://saxon.sf.net/feature/thresholdForCompilingTypes", 76, 0, Integer.class, null);
    public static final Feature<Boolean> TIMING = new Feature<Boolean>("http://saxon.sf.net/feature/timing", 77, 0, Boolean.class, false);
    public static final Feature<Boolean> TRACE_EXTERNAL_FUNCTIONS = new Feature<Boolean>("http://saxon.sf.net/feature/trace-external-functions", 78, 1, Boolean.class, false);
    public static final Feature<TraceListener> TRACE_LISTENER = new Feature<Object>("http://saxon.sf.net/feature/traceListener", 79, 0, TraceListener.class, null);
    public static final Feature<String> TRACE_LISTENER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/traceListenerClass", 80, 0, String.class, null);
    public static final Feature<String> TRACE_LISTENER_OUTPUT_FILE = new Feature<Object>("http://saxon.sf.net/feature/traceListenerOutputFile", 81, 0, String.class, null);
    public static final Feature<Boolean> TRACE_OPTIMIZER_DECISIONS = new Feature<Boolean>("http://saxon.sf.net/feature/trace-optimizer-decisions", 82, 1, Boolean.class, false);
    public static final Feature<Integer> TREE_MODEL = new Feature<Object>("http://saxon.sf.net/feature/treeModel", 83, 0, Integer.class, null);
    public static final Feature<String> TREE_MODEL_NAME = new Feature<Object>("http://saxon.sf.net/feature/treeModelName", 84, 0, String.class, null);
    public static final Feature<UnparsedTextURIResolver> UNPARSED_TEXT_URI_RESOLVER = new Feature<Object>("http://saxon.sf.net/feature/unparsedTextURIResolver", 85, 0, UnparsedTextURIResolver.class, null);
    public static final Feature<String> UNPARSED_TEXT_URI_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/unparsedTextURIResolverClass", 86, 0, String.class, null);
    public static final Feature<String> URI_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/uriResolverClass", 87, 0, String.class, null);
    public static final Feature<Boolean> USE_PI_DISABLE_OUTPUT_ESCAPING = new Feature<Boolean>("http://saxon.sf.net/feature/use-pi-disable-output-escaping", 88, 0, Boolean.class, false);
    public static final Feature<Boolean> USE_TYPED_VALUE_CACHE = new Feature<Boolean>("http://saxon.sf.net/feature/use-typed-value-cache", 89, 2, Boolean.class, false);
    public static final Feature<Boolean> USE_XSI_SCHEMA_LOCATION = new Feature<Boolean>("http://saxon.sf.net/feature/useXsiSchemaLocation", 90, 2, Boolean.class, false);
    public static final Feature<Boolean> VALIDATION_COMMENTS = new Feature<Boolean>("http://saxon.sf.net/feature/validation-comments", 91, 2, Boolean.class, false);
    public static final Feature<Boolean> VALIDATION_WARNINGS = new Feature<Boolean>("http://saxon.sf.net/feature/validation-warnings", 92, 2, Boolean.class, false);
    public static final Feature<Boolean> VERSION_WARNING = new Feature<Boolean>("http://saxon.sf.net/feature/version-warning", 93, 0, Boolean.class, false);
    public static final Feature<Boolean> XINCLUDE = new Feature<Boolean>("http://saxon.sf.net/feature/xinclude-aware", 94, 0, Boolean.class, false);
    public static final Feature<String> XML_VERSION = new Feature<Object>("http://saxon.sf.net/feature/xml-version", 95, 0, String.class, null);
    public static final Feature<Boolean> XML_PARSER_FEATURE = new Feature<Boolean>("http://saxon.sf.net/feature/parserFeature?uri=", 96, 0, Boolean.class, false);
    public static final Feature<Boolean> XML_PARSER_PROPERTY = new Feature<Boolean>("http://saxon.sf.net/feature/parserProperty?uri=", 97, 0, Boolean.class, false);
    public static final Feature<Boolean> XQUERY_ALLOW_UPDATE = new Feature<Boolean>("http://saxon.sf.net/feature/xqueryAllowUpdate", 98, 2, Boolean.class, false);
    public static final Feature<String> XQUERY_CONSTRUCTION_MODE = new Feature<Object>("http://saxon.sf.net/feature/xqueryConstructionMode", 99, 0, String.class, null);
    public static final Feature<Object> XQUERY_DEFAULT_ELEMENT_NAMESPACE = new Feature<Object>("http://saxon.sf.net/feature/xqueryDefaultElementNamespace", 100, 0, Object.class, null);
    public static final Feature<Object> XQUERY_DEFAULT_FUNCTION_NAMESPACE = new Feature<Object>("http://saxon.sf.net/feature/xqueryDefaultFunctionNamespace", 101, 0, Object.class, null);
    public static final Feature<Boolean> XQUERY_EMPTY_LEAST = new Feature<Boolean>("http://saxon.sf.net/feature/xqueryEmptyLeast", 102, 0, Boolean.class, false);
    public static final Feature<Boolean> XQUERY_INHERIT_NAMESPACES = new Feature<Boolean>("http://saxon.sf.net/feature/xqueryInheritNamespaces", 103, 0, Boolean.class, false);
    public static final Feature<Boolean> XQUERY_MULTIPLE_MODULE_IMPORTS = new Feature<Boolean>("http://saxon.sf.net/feature/xqueryMultipleModuleImports", 104, 2, Boolean.class, false);
    public static final Feature<Boolean> XQUERY_PRESERVE_BOUNDARY_SPACE = new Feature<Boolean>("http://saxon.sf.net/feature/xqueryPreserveBoundarySpace", 105, 0, Boolean.class, false);
    public static final Feature<Boolean> XQUERY_PRESERVE_NAMESPACES = new Feature<Boolean>("http://saxon.sf.net/feature/xqueryPreserveNamespaces", 106, 0, Boolean.class, false);
    public static final Feature<String> XQUERY_REQUIRED_CONTEXT_ITEM_TYPE = new Feature<Object>("http://saxon.sf.net/feature/xqueryRequiredContextItemType", 107, 0, String.class, null);
    public static final Feature<Boolean> XQUERY_SCHEMA_AWARE = new Feature<Boolean>("http://saxon.sf.net/feature/xquerySchemaAware", 108, 2, Boolean.class, false);
    public static final Feature<String> XQUERY_STATIC_ERROR_LISTENER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/xqueryStaticErrorListenerClass", 109, 0, String.class, null);
    public static final Feature<String> XQUERY_VERSION = new Feature<Object>("http://saxon.sf.net/feature/xqueryVersion", 110, 0, String.class, null);
    public static final Feature<String> XSD_VERSION = new Feature<Object>("http://saxon.sf.net/feature/xsd-version", 111, 2, String.class, null);
    public static final Feature<Boolean> XSLT_ENABLE_ASSERTIONS = new Feature<Boolean>("http://saxon.sf.net/feature/enableAssertions", 112, 1, Boolean.class, false);
    public static final Feature<String> XSLT_INITIAL_MODE = new Feature<Object>("http://saxon.sf.net/feature/initialMode", 113, 0, String.class, null);
    public static final Feature<String> XSLT_INITIAL_TEMPLATE = new Feature<Object>("http://saxon.sf.net/feature/initialTemplate", 114, 0, String.class, null);
    public static final Feature<Boolean> XSLT_SCHEMA_AWARE = new Feature<Boolean>("http://saxon.sf.net/feature/xsltSchemaAware", 115, 2, Boolean.class, false);
    public static final Feature<String> XSLT_STATIC_ERROR_LISTENER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/stylesheetErrorListener", 116, 0, String.class, null);
    public static final Feature<String> XSLT_STATIC_URI_RESOLVER_CLASS = new Feature<Object>("http://saxon.sf.net/feature/stylesheetURIResolver", 117, 0, String.class, null);
    public static final Feature<String> XSLT_VERSION = new Feature<Object>("http://saxon.sf.net/feature/xsltVersion", 118, 0, String.class, null);
    public static final Feature<Integer> REGEX_BACKTRACKING_LIMIT = new Feature<Object>("http://saxon.sf.net/feature/regexBacktrackingLimit", 119, 0, Integer.class, null);
    public static final Feature<Integer> XPATH_VERSION_FOR_XSD = new Feature<Object>("http://saxon.sf.net/feature/xpathVersionForXsd", 120, 2, Integer.class, null);
    public static final Feature<Integer> XPATH_VERSION_FOR_XSLT = new Feature<Object>("http://saxon.sf.net/feature/xpathVersionForXslt", 121, 0, Integer.class, null);
    public static final Feature<Integer> THRESHOLD_FOR_FUNCTION_INLINING = new Feature<Object>("http://saxon.sf.net/feature/thresholdForFunctionInlining", 122, 2, Integer.class, null);
    public static final Feature<Integer> THRESHOLD_FOR_HOTSPOT_BYTE_CODE = new Feature<Object>("http://saxon.sf.net/feature/thresholdForHotspotByteCode", 123, 2, Integer.class, null);
    public static final Feature<Object> ALLOWED_PROTOCOLS = new Feature<Object>("http://saxon.sf.net/feature/allowedProtocols", 124, 2, Object.class, null);
    public static final Feature<Boolean> RETAIN_NODE_FOR_DIAGNOSTICS = new Feature<Boolean>("http://saxon.sf.net/feature/retainNodeForDiagnostics", 125, 2, Boolean.class, false);
    public static final Feature<Boolean> ALLOW_UNRESOLVED_SCHEMA_COMPONENTS = new Feature<Boolean>("http://saxon.sf.net/feature/allowUnresolvedSchemaComponents", 126, 2, Boolean.class, false);
    public static final Feature<String> ZIP_URI_PATTERN = new Feature<Object>("http://saxon.sf.net/feature/zipUriPattern", 127, 2, String.class, null);

    private Feature(String name, int code, int requiredEdition, Class<? extends T> type, T defaultValue) {
        this.name = name;
        this.code = code;
        this.requiredEdition = requiredEdition;
        this.type = type;
        this.defaultValue = defaultValue;
        index.put(name, this);
    }

    public static Feature<?> byName(String name) {
        return index.get(name);
    }

    public static Iterator<String> getNames() {
        return index.keySet().iterator();
    }
}

