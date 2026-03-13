/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.lookup;

import java.util.Objects;
import javax.naming.NamingException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.net.JndiManager;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="jndi", category="Lookup")
public class JndiLookup
extends AbstractLookup {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final Marker LOOKUP = MarkerManager.getMarker("LOOKUP");
    static final String CONTAINER_JNDI_RESOURCE_PATH_PREFIX = "java:comp/env/";

    public JndiLookup() {
        if (!JndiManager.isJndiLookupEnabled()) {
            throw new IllegalStateException("JNDI must be enabled by setting log4j2.enableJndiLookup=true");
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public String lookup(LogEvent event, String key) {
        if (key == null) {
            return null;
        }
        String jndiName = this.convertJndiName(key);
        try (JndiManager jndiManager = JndiManager.getDefaultManager();){
            String string = Objects.toString(jndiManager.lookup(jndiName), null);
            return string;
        } catch (NamingException e) {
            LOGGER.warn(LOOKUP, "Error looking up JNDI resource [{}].", (Object)jndiName, (Object)e);
            return null;
        }
    }

    private String convertJndiName(String jndiName) {
        if (!jndiName.startsWith(CONTAINER_JNDI_RESOURCE_PATH_PREFIX) && jndiName.indexOf(58) == -1) {
            return CONTAINER_JNDI_RESOURCE_PATH_PREFIX + jndiName;
        }
        return jndiName;
    }
}

