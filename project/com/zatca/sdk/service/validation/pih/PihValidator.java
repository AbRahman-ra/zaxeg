/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package com.zatca.sdk.service.validation.pih;

import com.zatca.config.ResourcesPaths;
import com.zatca.configuration.enums.Configuration;
import com.zatca.sdk.service.validation.Result;
import com.zatca.sdk.service.validation.StageEnum;
import com.zatca.sdk.service.validation.Validator;
import com.zatca.sdk.util.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import org.apache.log4j.Logger;

public class PihValidator
implements Validator {
    private static final Logger LOG = Logger.getLogger(PihValidator.class);
    private ResourcesPaths paths = ResourcesPaths.getInstance();

    @Override
    public Result validate(File in) {
        Result result = new Result();
        result.setStage(StageEnum.PIH);
        HashMap<String, String> errors = new HashMap<String, String>();
        try {
            String leftSidePih;
            String rightSidePih = Utils.getPihCode(in);
            String string = leftSidePih = Configuration.getInstance().getPih() != null ? Configuration.getInstance().getPih() : this.getPihFromFile(this.paths.getPihPath());
            if (!rightSidePih.equals(leftSidePih)) {
                result.setValid(false);
                errors.put("KSA-13", "PIH is inValid");
                result.setError(errors);
            }
        } catch (IOException e) {
            errors.put(e.getClass().getSimpleName(), e.getMessage());
            result.setError(errors);
            result.setValid(false);
        }
        return result;
    }

    public String getPihFromFile(String filepath) throws IOException {
        BufferedReader brTest = new BufferedReader(new FileReader(filepath));
        String pih = brTest.readLine();
        return pih;
    }
}

