/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import java.io.Serializable;

public abstract class AccessorNamingStrategy {
    public abstract String findNameForIsGetter(AnnotatedMethod var1, String var2);

    public abstract String findNameForRegularGetter(AnnotatedMethod var1, String var2);

    public abstract String findNameForMutator(AnnotatedMethod var1, String var2);

    public abstract String modifyFieldName(AnnotatedField var1, String var2);

    public static abstract class Provider
    implements Serializable {
        private static final long serialVersionUID = 1L;

        public abstract AccessorNamingStrategy forPOJO(MapperConfig<?> var1, AnnotatedClass var2);

        public abstract AccessorNamingStrategy forBuilder(MapperConfig<?> var1, AnnotatedClass var2, BeanDescription var3);

        public abstract AccessorNamingStrategy forRecord(MapperConfig<?> var1, AnnotatedClass var2);
    }

    public static class Base
    extends AccessorNamingStrategy
    implements Serializable {
        private static final long serialVersionUID = 1L;

        @Override
        public String findNameForIsGetter(AnnotatedMethod method, String name) {
            return null;
        }

        @Override
        public String findNameForRegularGetter(AnnotatedMethod method, String name) {
            return null;
        }

        @Override
        public String findNameForMutator(AnnotatedMethod method, String name) {
            return null;
        }

        @Override
        public String modifyFieldName(AnnotatedField field, String name) {
            return name;
        }
    }
}

