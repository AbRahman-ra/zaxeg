/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.validation.schematron;

import com.zatca.sdk.service.validation.Result;
import com.zatca.sdk.service.validation.StageEnum;
import com.zatca.sdk.service.validation.Validator;
import com.zatca.sdk.service.validation.schematron.EnSchematronValidator;
import java.io.File;
import java.util.HashMap;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.log4j.Logger;

public abstract class SchematronValidator
implements Validator {
    private static final Logger LOG = Logger.getLogger(SchematronValidator.class);

    @Override
    public Result validate(File invoice) {
        LOG.info("Validate Schematron using " + this.getSchematronPath() + " for invoice : " + invoice);
        Result result = new Result();
        if (this instanceof EnSchematronValidator) {
            result.setStage(StageEnum.EN);
        } else {
            result.setStage(StageEnum.KSA);
        }
        HashMap<String, String> errors = new HashMap<String, String>();
        try {
            Processor processor = new Processor(false);
            XsltCompiler compiler = processor.newXsltCompiler();
            XsltExecutable xslt = compiler.compile(new StreamSource(new File(this.getSchematronPath())));
            XsltTransformer transformer = xslt.load();
            transformer.setSource(new StreamSource(invoice));
            XdmDestination chainResult = new XdmDestination();
            transformer.setDestination(chainResult);
            transformer.transform();
            XdmNode rootnode = chainResult.getXdmNode();
            for (XdmNode node : rootnode.children().iterator().next().children()) {
                if (node.getNodeName() == null || !"failed-assert".equals(node.getNodeName().getLocalName())) continue;
                String res = node.children().iterator().next().getStringValue();
                errors.put(node.attribute("id"), SchematronValidator.trim(res));
            }
            if (!errors.isEmpty()) {
                result.setError(errors);
                result.setValid(false);
            }
        } catch (Exception e) {
            LOG.error("Error Happen : " + e.getMessage());
            errors.put(e.getClass().getSimpleName(), e.getMessage());
            result.setError(errors);
            result.setValid(false);
        }
        return result;
    }

    protected abstract String getSchematronPath();

    private static String trim(String s) {
        s = s.replaceAll("\n", "").replaceAll("\t", " ");
        while (s.indexOf("  ") != -1) {
            s = s.replaceAll("  ", " ");
        }
        return s.trim();
    }
}

