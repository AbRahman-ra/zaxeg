/*
 * Decompiled with CFR 0.153-SNAPSHOT (d6f6758-dirty).
 */
package net.sf.saxon.functions;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.Configuration;
import net.sf.saxon.Controller;
import net.sf.saxon.event.Builder;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.event.Sender;
import net.sf.saxon.expr.Callable;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.functions.SystemFunction;
import net.sf.saxon.lib.ParseOptions;
import net.sf.saxon.om.IgnorableSpaceStrippingRule;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.ZeroOrOne;
import net.sf.saxon.style.StylesheetPackage;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.tiny.TinyDocumentImpl;
import net.sf.saxon.value.ObjectValue;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.StringValue;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

public class ParseXml
extends SystemFunction
implements Callable {
    @Override
    public ZeroOrOne call(XPathContext context, Sequence[] arguments) throws XPathException {
        StringValue input = (StringValue)arguments[0].head();
        return input == null ? ZeroOrOne.empty() : new ZeroOrOne<NodeInfo>(this.evalParseXml(input, context));
    }

    private NodeInfo evalParseXml(StringValue inputArg, XPathContext context) throws XPathException {
        String baseURI = this.getRetainedStaticContext().getStaticBaseUriString();
        RetentiveErrorHandler errorHandler = new RetentiveErrorHandler();
        try {
            Builder b;
            Controller controller = context.getController();
            if (controller == null) {
                throw new XPathException("parse-xml() function is not available in this environment");
            }
            Configuration config = controller.getConfiguration();
            StringReader sr = new StringReader(inputArg.getStringValue());
            InputSource is = new InputSource(sr);
            is.setSystemId(baseURI);
            SAXSource source = new SAXSource(is);
            source.setSystemId(baseURI);
            Receiver s = b = controller.makeBuilder();
            ParseOptions options = new ParseOptions(config.getParseOptions());
            PackageData pd = this.getRetainedStaticContext().getPackageData();
            if (pd instanceof StylesheetPackage) {
                options.setSpaceStrippingRule(((StylesheetPackage)pd).getSpaceStrippingRule());
                if (((StylesheetPackage)pd).isStripsTypeAnnotations()) {
                    s = config.getAnnotationStripper(s);
                }
            } else {
                options.setSpaceStrippingRule(IgnorableSpaceStrippingRule.getInstance());
            }
            options.setErrorHandler(errorHandler);
            s.setPipelineConfiguration(b.getPipelineConfiguration());
            Sender.send(source, s, options);
            TinyDocumentImpl node = (TinyDocumentImpl)b.getCurrentRoot();
            node.setBaseURI(baseURI);
            node.getTreeInfo().setUserData("saxon:document-uri", "");
            b.reset();
            return node;
        } catch (XPathException err) {
            XPathException xe = new XPathException("First argument to parse-xml() is not a well-formed and namespace-well-formed XML document. XML parser reported: " + err.getMessage(), "FODC0006");
            errorHandler.captureRetainedErrors(xe);
            xe.maybeSetContext(context);
            throw xe;
        }
    }

    public static class RetentiveErrorHandler
    implements ErrorHandler {
        public List<SAXParseException> errors = new ArrayList<SAXParseException>();
        public boolean failed = false;

        @Override
        public void error(SAXParseException exception) {
            this.errors.add(exception);
        }

        @Override
        public void warning(SAXParseException exception) {
        }

        @Override
        public void fatalError(SAXParseException exception) {
            this.errors.add(exception);
            this.failed = true;
        }

        public void captureRetainedErrors(XPathException xe) {
            List<SAXParseException> retainedErrors = this.errors;
            if (!retainedErrors.isEmpty()) {
                ArrayList<ObjectValue<SAXParseException>> wrappedErrors = new ArrayList<ObjectValue<SAXParseException>>();
                for (SAXParseException e : retainedErrors) {
                    wrappedErrors.add(new ObjectValue<SAXParseException>(e));
                }
                xe.setErrorObject(SequenceExtent.makeSequenceExtent(wrappedErrors));
            }
        }
    }
}

