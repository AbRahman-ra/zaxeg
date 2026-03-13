/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.dom4j.rule;

import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.rule.Action;
import org.dom4j.rule.Mode;
import org.dom4j.rule.Rule;
import org.dom4j.rule.RuleManager;

public class Stylesheet {
    private RuleManager ruleManager = new RuleManager();
    private String modeName;

    public void addRule(Rule rule) {
        this.ruleManager.addRule(rule);
    }

    public void removeRule(Rule rule) {
        this.ruleManager.removeRule(rule);
    }

    public void run(Object input) throws Exception {
        this.run(input, this.modeName);
    }

    public void run(Object input, String mode) throws Exception {
        if (input instanceof Node) {
            this.run((Node)input, mode);
        } else if (input instanceof List) {
            this.run((List)input, mode);
        }
    }

    public void run(List list) throws Exception {
        this.run(list, this.modeName);
    }

    public void run(List list, String mode) throws Exception {
        int size = list.size();
        for (int i = 0; i < size; ++i) {
            Object object = list.get(i);
            if (!(object instanceof Node)) continue;
            this.run((Node)object, mode);
        }
    }

    public void run(Node node) throws Exception {
        this.run(node, this.modeName);
    }

    public void run(Node node, String mode) throws Exception {
        Mode mod = this.ruleManager.getMode(mode);
        mod.fireRule(node);
    }

    public void applyTemplates(Object input, XPath xpath) throws Exception {
        this.applyTemplates(input, xpath, this.modeName);
    }

    public void applyTemplates(Object input, XPath xpath, String mode) throws Exception {
        Mode mod = this.ruleManager.getMode(mode);
        List list = xpath.selectNodes(input);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Node current = (Node)it.next();
            mod.fireRule(current);
        }
    }

    public void applyTemplates(Object input, org.jaxen.XPath xpath) throws Exception {
        this.applyTemplates(input, xpath, this.modeName);
    }

    public void applyTemplates(Object input, org.jaxen.XPath xpath, String mode) throws Exception {
        Mode mod = this.ruleManager.getMode(mode);
        List list = xpath.selectNodes(input);
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Node current = (Node)it.next();
            mod.fireRule(current);
        }
    }

    public void applyTemplates(Object input) throws Exception {
        this.applyTemplates(input, this.modeName);
    }

    public void applyTemplates(Object input, String mode) throws Exception {
        block5: {
            block6: {
                Mode mod;
                block4: {
                    mod = this.ruleManager.getMode(mode);
                    if (!(input instanceof Element)) break block4;
                    Element element = (Element)input;
                    int size = element.nodeCount();
                    for (int i = 0; i < size; ++i) {
                        Node node = element.node(i);
                        mod.fireRule(node);
                    }
                    break block5;
                }
                if (!(input instanceof Document)) break block6;
                Document document = (Document)input;
                int size = document.nodeCount();
                for (int i = 0; i < size; ++i) {
                    Node node = document.node(i);
                    mod.fireRule(node);
                }
                break block5;
            }
            if (!(input instanceof List)) break block5;
            List list = (List)input;
            int size = list.size();
            for (int i = 0; i < size; ++i) {
                Object object = list.get(i);
                if (object instanceof Element) {
                    this.applyTemplates((Object)((Element)object), mode);
                    continue;
                }
                if (!(object instanceof Document)) continue;
                this.applyTemplates((Object)((Document)object), mode);
            }
        }
    }

    public void clear() {
        this.ruleManager.clear();
    }

    public String getModeName() {
        return this.modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    public Action getValueOfAction() {
        return this.ruleManager.getValueOfAction();
    }

    public void setValueOfAction(Action valueOfAction) {
        this.ruleManager.setValueOfAction(valueOfAction);
    }
}

