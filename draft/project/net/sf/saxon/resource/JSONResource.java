/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.util.HashMap;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.lib.ResourceFactory;
import net.sf.saxon.ma.json.ParseJsonFn;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.resource.AbstractResourceCollection;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.BooleanValue;
import net.sf.saxon.value.StringValue;

public class JSONResource
implements Resource {
    private String href;
    private String jsonStr;
    private AbstractResourceCollection.InputDetails details;
    public static final ResourceFactory FACTORY = (config, details) -> new JSONResource(details);

    public JSONResource(AbstractResourceCollection.InputDetails details) {
        this.href = details.resourceUri;
        if (details.encoding == null) {
            details.encoding = "UTF-8";
        }
        this.details = details;
    }

    @Override
    public String getResourceURI() {
        return this.href;
    }

    @Override
    public Item getItem(XPathContext context) throws XPathException {
        if (this.jsonStr == null) {
            this.jsonStr = this.details.obtainCharacterContent();
        }
        HashMap<String, Sequence> options = new HashMap<String, Sequence>();
        options.put("liberal", BooleanValue.FALSE);
        options.put("duplicates", new StringValue("use-first"));
        options.put("escape", BooleanValue.FALSE);
        return ParseJsonFn.parse(this.jsonStr, options, context);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }
}

