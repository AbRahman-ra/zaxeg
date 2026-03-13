/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.jaxen.javabean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.jaxen.BaseXPath;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.javabean.DocumentNavigator;
import org.jaxen.javabean.Element;

public class JavaBeanXPath
extends BaseXPath {
    private static final long serialVersionUID = -1567521943360266313L;

    public JavaBeanXPath(String xpathExpr) throws JaxenException {
        super(xpathExpr, DocumentNavigator.getInstance());
    }

    protected Context getContext(Object node) {
        if (node instanceof Context) {
            return (Context)node;
        }
        if (node instanceof Element) {
            return super.getContext(node);
        }
        if (node instanceof List) {
            ArrayList<Element> newList = new ArrayList<Element>();
            Iterator listIter = ((List)node).iterator();
            while (listIter.hasNext()) {
                newList.add(new Element(null, "root", listIter.next()));
            }
            return super.getContext(newList);
        }
        return super.getContext(new Element(null, "root", node));
    }

    public Object evaluate(Object node) throws JaxenException {
        Object result = super.evaluate(node);
        if (result instanceof Element) {
            return ((Element)result).getObject();
        }
        if (result instanceof Collection) {
            ArrayList<Object> newList = new ArrayList<Object>();
            Iterator listIter = ((Collection)result).iterator();
            while (listIter.hasNext()) {
                Object member = listIter.next();
                if (member instanceof Element) {
                    newList.add(((Element)member).getObject());
                    continue;
                }
                newList.add(member);
            }
            return newList;
        }
        return result;
    }
}

