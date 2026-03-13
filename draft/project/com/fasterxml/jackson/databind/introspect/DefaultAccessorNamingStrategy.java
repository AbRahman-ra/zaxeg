/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AccessorNamingStrategy;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jdk14.JDK14Util;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DefaultAccessorNamingStrategy
extends AccessorNamingStrategy {
    protected final MapperConfig<?> _config;
    protected final AnnotatedClass _forClass;
    protected final BaseNameValidator _baseNameValidator;
    protected final boolean _stdBeanNaming;
    protected final String _getterPrefix;
    protected final String _isGetterPrefix;
    protected final String _mutatorPrefix;

    protected DefaultAccessorNamingStrategy(MapperConfig<?> config, AnnotatedClass forClass, String mutatorPrefix, String getterPrefix, String isGetterPrefix, BaseNameValidator baseNameValidator) {
        this._config = config;
        this._forClass = forClass;
        this._stdBeanNaming = config.isEnabled(MapperFeature.USE_STD_BEAN_NAMING);
        this._mutatorPrefix = mutatorPrefix;
        this._getterPrefix = getterPrefix;
        this._isGetterPrefix = isGetterPrefix;
        this._baseNameValidator = baseNameValidator;
    }

    @Override
    public String findNameForIsGetter(AnnotatedMethod am, String name) {
        Class<?> rt;
        if (this._isGetterPrefix != null && ((rt = am.getRawType()) == Boolean.class || rt == Boolean.TYPE) && name.startsWith(this._isGetterPrefix)) {
            return this._stdBeanNaming ? this.stdManglePropertyName(name, 2) : this.legacyManglePropertyName(name, 2);
        }
        return null;
    }

    @Override
    public String findNameForRegularGetter(AnnotatedMethod am, String name) {
        if (this._getterPrefix != null && name.startsWith(this._getterPrefix)) {
            if ("getCallbacks".equals(name) ? this._isCglibGetCallbacks(am) : "getMetaClass".equals(name) && this._isGroovyMetaClassGetter(am)) {
                return null;
            }
            return this._stdBeanNaming ? this.stdManglePropertyName(name, this._getterPrefix.length()) : this.legacyManglePropertyName(name, this._getterPrefix.length());
        }
        return null;
    }

    @Override
    public String findNameForMutator(AnnotatedMethod am, String name) {
        if (this._mutatorPrefix != null && name.startsWith(this._mutatorPrefix)) {
            return this._stdBeanNaming ? this.stdManglePropertyName(name, this._mutatorPrefix.length()) : this.legacyManglePropertyName(name, this._mutatorPrefix.length());
        }
        return null;
    }

    @Override
    public String modifyFieldName(AnnotatedField field, String name) {
        return name;
    }

    protected String legacyManglePropertyName(String basename, int offset) {
        int end = basename.length();
        if (end == offset) {
            return null;
        }
        char c = basename.charAt(offset);
        if (this._baseNameValidator != null && !this._baseNameValidator.accept(c, basename, offset)) {
            return null;
        }
        char d = Character.toLowerCase(c);
        if (c == d) {
            return basename.substring(offset);
        }
        StringBuilder sb = new StringBuilder(end - offset);
        sb.append(d);
        for (int i = offset + 1; i < end; ++i) {
            c = basename.charAt(i);
            if (c == (d = Character.toLowerCase(c))) {
                sb.append(basename, i, end);
                break;
            }
            sb.append(d);
        }
        return sb.toString();
    }

    protected String stdManglePropertyName(String basename, int offset) {
        int end = basename.length();
        if (end == offset) {
            return null;
        }
        char c0 = basename.charAt(offset);
        if (this._baseNameValidator != null && !this._baseNameValidator.accept(c0, basename, offset)) {
            return null;
        }
        char c1 = Character.toLowerCase(c0);
        if (c0 == c1) {
            return basename.substring(offset);
        }
        if (offset + 1 < end && Character.isUpperCase(basename.charAt(offset + 1))) {
            return basename.substring(offset);
        }
        StringBuilder sb = new StringBuilder(end - offset);
        sb.append(c1);
        sb.append(basename, offset + 1, end);
        return sb.toString();
    }

    protected boolean _isCglibGetCallbacks(AnnotatedMethod am) {
        Class<?> compType;
        String className;
        Class<?> rt = am.getRawType();
        if (rt.isArray() && (className = (compType = rt.getComponentType()).getName()).contains(".cglib")) {
            return className.startsWith("net.sf.cglib") || className.startsWith("org.hibernate.repackage.cglib") || className.startsWith("org.springframework.cglib");
        }
        return false;
    }

    protected boolean _isGroovyMetaClassGetter(AnnotatedMethod am) {
        return am.getRawType().getName().startsWith("groovy.lang");
    }

    public static class RecordNaming
    extends DefaultAccessorNamingStrategy {
        protected final Set<String> _fieldNames = new HashSet<String>();

        public RecordNaming(MapperConfig<?> config, AnnotatedClass forClass) {
            super(config, forClass, null, "get", "is", null);
            for (String name : JDK14Util.getRecordFieldNames(forClass.getRawType())) {
                this._fieldNames.add(name);
            }
        }

        @Override
        public String findNameForRegularGetter(AnnotatedMethod am, String name) {
            if (this._fieldNames.contains(name)) {
                return name;
            }
            return super.findNameForRegularGetter(am, name);
        }
    }

    public static class FirstCharBasedValidator
    implements BaseNameValidator {
        private final boolean _allowLowerCaseFirstChar;
        private final boolean _allowNonLetterFirstChar;

        protected FirstCharBasedValidator(boolean allowLowerCaseFirstChar, boolean allowNonLetterFirstChar) {
            this._allowLowerCaseFirstChar = allowLowerCaseFirstChar;
            this._allowNonLetterFirstChar = allowNonLetterFirstChar;
        }

        public static BaseNameValidator forFirstNameRule(boolean allowLowerCaseFirstChar, boolean allowNonLetterFirstChar) {
            if (!allowLowerCaseFirstChar && !allowNonLetterFirstChar) {
                return null;
            }
            return new FirstCharBasedValidator(allowLowerCaseFirstChar, allowNonLetterFirstChar);
        }

        @Override
        public boolean accept(char firstChar, String basename, int offset) {
            if (Character.isLetter(firstChar)) {
                return this._allowLowerCaseFirstChar || !Character.isLowerCase(firstChar);
            }
            return this._allowNonLetterFirstChar;
        }
    }

    public static class Provider
    extends AccessorNamingStrategy.Provider
    implements Serializable {
        private static final long serialVersionUID = 1L;
        protected final String _setterPrefix;
        protected final String _withPrefix;
        protected final String _getterPrefix;
        protected final String _isGetterPrefix;
        protected final BaseNameValidator _baseNameValidator;

        public Provider() {
            this("set", "with", "get", "is", null);
        }

        protected Provider(Provider p, String setterPrefix, String withPrefix, String getterPrefix, String isGetterPrefix) {
            this(setterPrefix, withPrefix, getterPrefix, isGetterPrefix, p._baseNameValidator);
        }

        protected Provider(Provider p, BaseNameValidator vld) {
            this(p._setterPrefix, p._withPrefix, p._getterPrefix, p._isGetterPrefix, vld);
        }

        protected Provider(String setterPrefix, String withPrefix, String getterPrefix, String isGetterPrefix, BaseNameValidator vld) {
            this._setterPrefix = setterPrefix;
            this._withPrefix = withPrefix;
            this._getterPrefix = getterPrefix;
            this._isGetterPrefix = isGetterPrefix;
            this._baseNameValidator = vld;
        }

        public Provider withSetterPrefix(String prefix) {
            return new Provider(this, prefix, this._withPrefix, this._getterPrefix, this._isGetterPrefix);
        }

        public Provider withBuilderPrefix(String prefix) {
            return new Provider(this, this._setterPrefix, prefix, this._getterPrefix, this._isGetterPrefix);
        }

        public Provider withGetterPrefix(String prefix) {
            return new Provider(this, this._setterPrefix, this._withPrefix, prefix, this._isGetterPrefix);
        }

        public Provider withIsGetterPrefix(String prefix) {
            return new Provider(this, this._setterPrefix, this._withPrefix, this._getterPrefix, prefix);
        }

        public Provider withFirstCharAcceptance(boolean allowLowerCaseFirstChar, boolean allowNonLetterFirstChar) {
            return this.withBaseNameValidator(FirstCharBasedValidator.forFirstNameRule(allowLowerCaseFirstChar, allowNonLetterFirstChar));
        }

        public Provider withBaseNameValidator(BaseNameValidator vld) {
            return new Provider(this, vld);
        }

        @Override
        public AccessorNamingStrategy forPOJO(MapperConfig<?> config, AnnotatedClass targetClass) {
            return new DefaultAccessorNamingStrategy(config, targetClass, this._setterPrefix, this._getterPrefix, this._isGetterPrefix, this._baseNameValidator);
        }

        @Override
        public AccessorNamingStrategy forBuilder(MapperConfig<?> config, AnnotatedClass builderClass, BeanDescription valueTypeDesc) {
            AnnotationIntrospector ai = config.isAnnotationProcessingEnabled() ? config.getAnnotationIntrospector() : null;
            JsonPOJOBuilder.Value builderConfig = ai == null ? null : ai.findPOJOBuilderConfig(builderClass);
            String mutatorPrefix = builderConfig == null ? this._withPrefix : builderConfig.withPrefix;
            return new DefaultAccessorNamingStrategy(config, builderClass, mutatorPrefix, this._getterPrefix, this._isGetterPrefix, this._baseNameValidator);
        }

        @Override
        public AccessorNamingStrategy forRecord(MapperConfig<?> config, AnnotatedClass recordClass) {
            return new RecordNaming(config, recordClass);
        }
    }

    public static interface BaseNameValidator {
        public boolean accept(char var1, String var2, int var3);
    }
}

