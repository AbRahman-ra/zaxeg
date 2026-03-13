/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.resource;

import java.util.Map;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.CallableFunction;
import net.sf.saxon.lib.Resource;
import net.sf.saxon.ma.map.DictionaryMap;
import net.sf.saxon.om.GroundedValue;
import net.sf.saxon.om.Item;
import net.sf.saxon.type.FunctionItemType;
import net.sf.saxon.type.SpecificFunctionType;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

public class MetadataResource
implements Resource {
    private Map<String, GroundedValue> properties;
    private String resourceURI;
    private Resource content;

    public MetadataResource(String resourceURI, Resource content, Map<String, GroundedValue> properties) {
        this.resourceURI = resourceURI;
        this.content = content;
        this.properties = properties;
    }

    @Override
    public String getContentType() {
        return this.content.getContentType();
    }

    @Override
    public String getResourceURI() {
        return this.resourceURI;
    }

    @Override
    public Item getItem(XPathContext context) {
        DictionaryMap map = new DictionaryMap();
        for (Map.Entry<String, GroundedValue> entry : this.properties.entrySet()) {
            map.initialPut(entry.getKey(), entry.getValue());
        }
        map.initialPut("name", StringValue.makeStringValue(this.resourceURI));
        Callable fetcher = (context1, arguments) -> this.content.getItem(context1);
        SpecificFunctionType fetcherType = new SpecificFunctionType(new SequenceType[0], SequenceType.SINGLE_ITEM);
        CallableFunction fetcherFunction = new CallableFunction(0, fetcher, (FunctionItemType)fetcherType);
        map.initialPut("fetch", fetcherFunction);
        return map;
    }
}

