/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.type;

import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;

public interface SchemaComponent {
    public static final FunctionItemType COMPONENT_FUNCTION_TYPE = new SpecificFunctionType(new SequenceType[]{SequenceType.SINGLE_STRING}, SequenceType.ANY_SEQUENCE);

    public ValidationStatus getValidationStatus();

    public int getRedefinitionLevel();

    public static enum ValidationStatus {
        UNVALIDATED,
        FIXED_UP,
        VALIDATING,
        VALIDATED,
        INVALID,
        INCOMPLETE;

    }
}

