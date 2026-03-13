/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.validation.xsd;

import com.zatca.config.ResourcesPaths;
import com.zatca.sdk.service.validation.Result;
import com.zatca.sdk.service.validation.StageEnum;
import com.zatca.sdk.service.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class XsdValidator
implements Validator {
    private static final Logger LOG = Logger.getLogger(XsdValidator.class);
    private ResourcesPaths paths = ResourcesPaths.getInstance();

    @Override
    public Result validate(File in) {
        return this.validateXMLSchema(in);
    }

    private Result validateXMLSchema(File invoice) {
        LOG.info("Validate XSD for invoice : " + invoice);
        Result result = new Result();
        result.setStage(StageEnum.XSD);
        try {
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(new File(this.paths.getXsdPth()));
            javax.xml.validation.Validator validator = schema.newValidator();
            validator.validate(new StreamSource(invoice));
        } catch (IOException | SAXException e) {
            HashMap<String, String> errors = new HashMap<String, String>();
            if (e instanceof SAXParseException) {
                errors.put(e.getClass().getSimpleName(), "Schema validation failed; XML does not comply with UBL 2.1 standards in line with ZATCA specifications");
            } else {
                errors.put(e.getClass().getSimpleName(), e.getMessage());
            }
            result.setError(errors);
            result.setValid(false);
        }
        return result;
    }
}

