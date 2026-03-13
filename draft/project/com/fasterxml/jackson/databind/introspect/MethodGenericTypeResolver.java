/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Objects;

final class MethodGenericTypeResolver {
    MethodGenericTypeResolver() {
    }

    public static TypeResolutionContext narrowMethodTypeParameters(Method candidate, JavaType requestedType, TypeFactory typeFactory, TypeResolutionContext emptyTypeResCtxt) {
        TypeBindings newTypeBindings = MethodGenericTypeResolver.bindMethodTypeParameters(candidate, requestedType, emptyTypeResCtxt);
        return newTypeBindings == null ? emptyTypeResCtxt : new TypeResolutionContext.Basic(typeFactory, newTypeBindings);
    }

    static TypeBindings bindMethodTypeParameters(Method candidate, JavaType requestedType, TypeResolutionContext emptyTypeResCtxt) {
        TypeVariable<Method>[] methodTypeParameters = candidate.getTypeParameters();
        if (methodTypeParameters.length == 0 || requestedType.getBindings().isEmpty()) {
            return null;
        }
        Type genericReturnType = candidate.getGenericReturnType();
        if (!(genericReturnType instanceof ParameterizedType)) {
            return null;
        }
        ParameterizedType parameterizedGenericReturnType = (ParameterizedType)genericReturnType;
        if (!Objects.equals(requestedType.getRawClass(), parameterizedGenericReturnType.getRawType())) {
            return null;
        }
        Type[] methodReturnTypeArguments = parameterizedGenericReturnType.getActualTypeArguments();
        ArrayList<String> names = new ArrayList<String>(methodTypeParameters.length);
        ArrayList<JavaType> types = new ArrayList<JavaType>(methodTypeParameters.length);
        for (int i = 0; i < methodReturnTypeArguments.length; ++i) {
            Type methodReturnTypeArgument = methodReturnTypeArguments[i];
            TypeVariable<?> typeVar = MethodGenericTypeResolver.maybeGetTypeVariable(methodReturnTypeArgument);
            if (typeVar == null) continue;
            String typeParameterName = typeVar.getName();
            if (typeParameterName == null) {
                return null;
            }
            JavaType bindTarget = requestedType.getBindings().getBoundType(i);
            if (bindTarget == null) {
                return null;
            }
            TypeVariable<?> methodTypeVariable = MethodGenericTypeResolver.findByName(methodTypeParameters, typeParameterName);
            if (methodTypeVariable == null) {
                return null;
            }
            if (!MethodGenericTypeResolver.pessimisticallyValidateBounds(emptyTypeResCtxt, bindTarget, methodTypeVariable.getBounds())) continue;
            int existingIndex = names.indexOf(typeParameterName);
            if (existingIndex != -1) {
                JavaType existingBindTarget = types.get(existingIndex);
                if (bindTarget.equals(existingBindTarget)) continue;
                boolean existingIsSubtype = existingBindTarget.isTypeOrSubTypeOf(bindTarget.getRawClass());
                boolean newIsSubtype = bindTarget.isTypeOrSubTypeOf(existingBindTarget.getRawClass());
                if (!existingIsSubtype && !newIsSubtype) {
                    return null;
                }
                if (!(existingIsSubtype ^ newIsSubtype) || !newIsSubtype) continue;
                types.set(existingIndex, bindTarget);
                continue;
            }
            names.add(typeParameterName);
            types.add(bindTarget);
        }
        if (names.isEmpty()) {
            return null;
        }
        return TypeBindings.create(names, types);
    }

    private static TypeVariable<?> maybeGetTypeVariable(Type type) {
        if (type instanceof TypeVariable) {
            return (TypeVariable)type;
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            if (wildcardType.getLowerBounds().length != 0) {
                return null;
            }
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length == 1) {
                return MethodGenericTypeResolver.maybeGetTypeVariable(upperBounds[0]);
            }
        }
        return null;
    }

    private static ParameterizedType maybeGetParameterizedType(Type type) {
        if (type instanceof ParameterizedType) {
            return (ParameterizedType)type;
        }
        if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType)type;
            if (wildcardType.getLowerBounds().length != 0) {
                return null;
            }
            Type[] upperBounds = wildcardType.getUpperBounds();
            if (upperBounds.length == 1) {
                return MethodGenericTypeResolver.maybeGetParameterizedType(upperBounds[0]);
            }
        }
        return null;
    }

    private static boolean pessimisticallyValidateBounds(TypeResolutionContext context, JavaType boundType, Type[] upperBound) {
        for (Type type : upperBound) {
            if (MethodGenericTypeResolver.pessimisticallyValidateBound(context, boundType, type)) continue;
            return false;
        }
        return true;
    }

    private static boolean pessimisticallyValidateBound(TypeResolutionContext context, JavaType boundType, Type type) {
        if (!boundType.isTypeOrSubTypeOf(context.resolveType(type).getRawClass())) {
            return false;
        }
        ParameterizedType parameterized = MethodGenericTypeResolver.maybeGetParameterizedType(type);
        if (parameterized != null && Objects.equals(boundType.getRawClass(), parameterized.getRawType())) {
            Type[] typeArguments = parameterized.getActualTypeArguments();
            TypeBindings bindings = boundType.getBindings();
            if (bindings.size() != typeArguments.length) {
                return false;
            }
            for (int i = 0; i < bindings.size(); ++i) {
                Type typeArg;
                JavaType boundTypeBound = bindings.getBoundType(i);
                if (MethodGenericTypeResolver.pessimisticallyValidateBound(context, boundTypeBound, typeArg = typeArguments[i])) continue;
                return false;
            }
        }
        return true;
    }

    private static TypeVariable<?> findByName(TypeVariable<?>[] typeVariables, String name) {
        if (typeVariables == null || name == null) {
            return null;
        }
        for (TypeVariable<?> typeVariable : typeVariables) {
            if (!name.equals(typeVariable.getName())) continue;
            return typeVariable;
        }
        return null;
    }
}

