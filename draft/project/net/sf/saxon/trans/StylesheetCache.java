/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.trans;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.XsltExecutable;

public class StylesheetCache {
    private Map<String, XsltExecutable> cacheByText = new ConcurrentHashMap<String, XsltExecutable>();
    private Map<String, XsltExecutable> cacheByLocation = new ConcurrentHashMap<String, XsltExecutable>();
    private Map<NodeInfo, XsltExecutable> cacheByNode = new ConcurrentHashMap<NodeInfo, XsltExecutable>();

    public XsltExecutable getStylesheetByText(String style) {
        return this.cacheByText.get(style);
    }

    public XsltExecutable getStylesheetByLocation(String style) {
        return this.cacheByLocation.get(style);
    }

    public XsltExecutable getStylesheetByNode(NodeInfo style) {
        return this.cacheByNode.get(style);
    }

    public void setStylesheetByText(String style, XsltExecutable xsltExecutable) {
        this.cacheByText.put(style, xsltExecutable);
    }

    public void setStylesheetByLocation(String style, XsltExecutable xsltExecutable) {
        this.cacheByLocation.put(style, xsltExecutable);
    }

    public void setStylesheetByNode(NodeInfo style, XsltExecutable xsltExecutable) {
        this.cacheByNode.put(style, xsltExecutable);
    }
}

