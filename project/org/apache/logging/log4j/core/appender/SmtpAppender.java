/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package org.apache.logging.log4j.core.appender;

import java.io.Serializable;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginConfiguration;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.ValidPort;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.layout.HtmlLayout;
import org.apache.logging.log4j.core.net.SmtpManager;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.Integers;

@Plugin(name="SMTP", category="Core", elementType="appender", printObject=true)
public final class SmtpAppender
extends AbstractAppender {
    private static final int DEFAULT_BUFFER_SIZE = 512;
    private final SmtpManager manager;

    private SmtpAppender(String name, Filter filter, Layout<? extends Serializable> layout, SmtpManager manager, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.manager = manager;
    }

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    @Deprecated
    public static SmtpAppender createAppender(@PluginConfiguration Configuration config, @PluginAttribute(value="name") @Required String name, @PluginAttribute(value="to") String to, @PluginAttribute(value="cc") String cc, @PluginAttribute(value="bcc") String bcc, @PluginAttribute(value="from") String from, @PluginAttribute(value="replyTo") String replyTo, @PluginAttribute(value="subject") String subject, @PluginAttribute(value="smtpProtocol") String smtpProtocol, @PluginAttribute(value="smtpHost") String smtpHost, @PluginAttribute(value="smtpPort", defaultString="0") @ValidPort String smtpPortStr, @PluginAttribute(value="smtpUsername") String smtpUsername, @PluginAttribute(value="smtpPassword", sensitive=true) String smtpPassword, @PluginAttribute(value="smtpDebug") String smtpDebug, @PluginAttribute(value="bufferSize") String bufferSizeStr, @PluginElement(value="Layout") Layout<? extends Serializable> layout, @PluginElement(value="Filter") Filter filter, @PluginAttribute(value="ignoreExceptions") String ignore) {
        Configuration configuration;
        SmtpManager manager;
        int bufferSize;
        if (name == null) {
            LOGGER.error("No name provided for SmtpAppender");
            return null;
        }
        boolean ignoreExceptions = Booleans.parseBoolean(ignore, true);
        int smtpPort = AbstractAppender.parseInt(smtpPortStr, 0);
        boolean isSmtpDebug = Boolean.parseBoolean(smtpDebug);
        int n = bufferSize = bufferSizeStr == null ? 512 : Integers.parseInt(bufferSizeStr);
        if (layout == null) {
            layout = HtmlLayout.createDefaultLayout();
        }
        if (filter == null) {
            filter = ThresholdFilter.createFilter(null, null, null);
        }
        if ((manager = SmtpManager.getSmtpManager(configuration = config != null ? config : new DefaultConfiguration(), to, cc, bcc, from, replyTo, subject, smtpProtocol, smtpHost, smtpPort, smtpUsername, smtpPassword, isSmtpDebug, filter.toString(), bufferSize, null)) == null) {
            return null;
        }
        return new SmtpAppender(name, filter, layout, manager, ignoreExceptions, null);
    }

    @Override
    public boolean isFiltered(LogEvent event) {
        boolean filtered = super.isFiltered(event);
        if (filtered) {
            this.manager.add(event);
        }
        return filtered;
    }

    @Override
    public void append(LogEvent event) {
        this.manager.sendEvents(this.getLayout(), event);
    }

    public static class Builder
    extends AbstractAppender.Builder<Builder>
    implements org.apache.logging.log4j.core.util.Builder<SmtpAppender> {
        @PluginBuilderAttribute
        private String to;
        @PluginBuilderAttribute
        private String cc;
        @PluginBuilderAttribute
        private String bcc;
        @PluginBuilderAttribute
        private String from;
        @PluginBuilderAttribute
        private String replyTo;
        @PluginBuilderAttribute
        private String subject;
        @PluginBuilderAttribute
        private String smtpProtocol = "smtp";
        @PluginBuilderAttribute
        private String smtpHost;
        @PluginBuilderAttribute
        @ValidPort
        private int smtpPort;
        @PluginBuilderAttribute
        private String smtpUsername;
        @PluginBuilderAttribute(sensitive=true)
        private String smtpPassword;
        @PluginBuilderAttribute
        private boolean smtpDebug;
        @PluginBuilderAttribute
        private int bufferSize = 512;
        @PluginElement(value="SSL")
        private SslConfiguration sslConfiguration;

        public Builder setTo(String to) {
            this.to = to;
            return this;
        }

        public Builder setCc(String cc) {
            this.cc = cc;
            return this;
        }

        public Builder setBcc(String bcc) {
            this.bcc = bcc;
            return this;
        }

        public Builder setFrom(String from) {
            this.from = from;
            return this;
        }

        public Builder setReplyTo(String replyTo) {
            this.replyTo = replyTo;
            return this;
        }

        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder setSmtpProtocol(String smtpProtocol) {
            this.smtpProtocol = smtpProtocol;
            return this;
        }

        public Builder setSmtpHost(String smtpHost) {
            this.smtpHost = smtpHost;
            return this;
        }

        public Builder setSmtpPort(int smtpPort) {
            this.smtpPort = smtpPort;
            return this;
        }

        public Builder setSmtpUsername(String smtpUsername) {
            this.smtpUsername = smtpUsername;
            return this;
        }

        public Builder setSmtpPassword(String smtpPassword) {
            this.smtpPassword = smtpPassword;
            return this;
        }

        public Builder setSmtpDebug(boolean smtpDebug) {
            this.smtpDebug = smtpDebug;
            return this;
        }

        public Builder setBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public Builder setSslConfiguration(SslConfiguration sslConfiguration) {
            this.sslConfiguration = sslConfiguration;
            return this;
        }

        @Override
        public Builder setLayout(Layout<? extends Serializable> layout) {
            return (Builder)super.setLayout(layout);
        }

        @Override
        public Builder setFilter(Filter filter) {
            return (Builder)super.setFilter(filter);
        }

        @Override
        public SmtpAppender build() {
            if (this.getLayout() == null) {
                this.setLayout((Layout)HtmlLayout.createDefaultLayout());
            }
            if (this.getFilter() == null) {
                this.setFilter(ThresholdFilter.createFilter(null, null, null));
            }
            SmtpManager smtpManager = SmtpManager.getSmtpManager(this.getConfiguration(), this.to, this.cc, this.bcc, this.from, this.replyTo, this.subject, this.smtpProtocol, this.smtpHost, this.smtpPort, this.smtpUsername, this.smtpPassword, this.smtpDebug, this.getFilter().toString(), this.bufferSize, this.sslConfiguration);
            return new SmtpAppender(this.getName(), this.getFilter(), this.getLayout(), smtpManager, this.isIgnoreExceptions(), this.getPropertyArray());
        }
    }
}

