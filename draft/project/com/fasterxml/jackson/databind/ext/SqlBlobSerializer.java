/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.fasterxml.jackson.databind.ext;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;

@JacksonStdImpl
public class SqlBlobSerializer
extends StdScalarSerializer<Blob> {
    public SqlBlobSerializer() {
        super(Blob.class);
    }

    @Override
    public boolean isEmpty(SerializerProvider provider, Blob value) {
        return value == null;
    }

    @Override
    public void serialize(Blob value, JsonGenerator gen, SerializerProvider ctxt) throws IOException {
        this._writeValue(value, gen, ctxt);
    }

    @Override
    public void serializeWithType(Blob value, JsonGenerator gen, SerializerProvider ctxt, TypeSerializer typeSer) throws IOException {
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen, typeSer.typeId(value, JsonToken.VALUE_EMBEDDED_OBJECT));
        this._writeValue(value, gen, ctxt);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

    protected void _writeValue(Blob value, JsonGenerator gen, SerializerProvider ctxt) throws IOException {
        InputStream in = null;
        try {
            in = value.getBinaryStream();
        } catch (SQLException e) {
            ctxt.reportMappingProblem(e, "Failed to access `java.sql.Blob` value to write as binary value", new Object[0]);
        }
        gen.writeBinary(ctxt.getConfig().getBase64Variant(), in, -1);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        JsonArrayFormatVisitor v2 = visitor.expectArrayFormat(typeHint);
        if (v2 != null) {
            v2.itemsFormat(JsonFormatTypes.INTEGER);
        }
    }
}

