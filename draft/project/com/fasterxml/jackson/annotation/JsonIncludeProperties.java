/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JacksonAnnotationValue;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Target(value={ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
@JacksonAnnotation
public @interface JsonIncludeProperties {
    public String[] value() default {};

    public static class Value
    implements JacksonAnnotationValue<JsonIncludeProperties>,
    Serializable {
        private static final long serialVersionUID = 1L;
        protected static final Value ALL = new Value(null);
        protected final Set<String> _included;

        protected Value(Set<String> included) {
            this._included = included;
        }

        public static Value from(JsonIncludeProperties src) {
            if (src == null) {
                return ALL;
            }
            return new Value(Value._asSet(src.value()));
        }

        public static Value all() {
            return ALL;
        }

        @Override
        public Class<JsonIncludeProperties> valueFor() {
            return JsonIncludeProperties.class;
        }

        public Set<String> getIncluded() {
            return this._included;
        }

        public Value withOverrides(Value overrides) {
            Set<String> otherIncluded;
            if (overrides == null || (otherIncluded = overrides.getIncluded()) == null) {
                return this;
            }
            if (this._included == null) {
                return overrides;
            }
            HashSet<String> toInclude = new HashSet<String>();
            for (String incl : otherIncluded) {
                if (!this._included.contains(incl)) continue;
                toInclude.add(incl);
            }
            return new Value(toInclude);
        }

        public String toString() {
            return String.format("JsonIncludeProperties.Value(included=%s)", this._included);
        }

        public int hashCode() {
            return this._included == null ? 0 : this._included.size();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            return o.getClass() == this.getClass() && Value._equals(this._included, ((Value)o)._included);
        }

        private static boolean _equals(Set<String> a, Set<String> b) {
            return a == null ? b == null : a.equals(b);
        }

        private static Set<String> _asSet(String[] v) {
            if (v == null || v.length == 0) {
                return Collections.emptySet();
            }
            HashSet<String> s = new HashSet<String>(v.length);
            for (String str : v) {
                s.add(str);
            }
            return s;
        }
    }
}

