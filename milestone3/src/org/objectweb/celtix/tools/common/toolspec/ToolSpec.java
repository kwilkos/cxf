package org.objectweb.celtix.tools.common.toolspec;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.dom.ExtendedDocumentBuilder;

public class ToolSpec {

    private static final Logger LOG = LogUtils.getL7dLogger(ToolSpec.class);
    private static ExtendedDocumentBuilder builder = new ExtendedDocumentBuilder();

    private Document doc;
    private Tool handler;

    public ToolSpec() {
    }

    public ToolSpec(InputStream in) throws ToolException {
        this(in, true);
    }

    public ToolSpec(InputStream in, boolean validate) throws ToolException {
        if (in == null) {
            throw new NullPointerException("Cannot create a ToolSpec object from a null stream");
        }
        try {
            builder.setValidating(validate);
            this.doc = builder.parse(in);
        } catch (Exception ex) {
            throw new ToolException("Failure when parsing toolspec stream", ex);
        }
    }

    public ToolSpec(Document d) {
        if (d == null) {
            throw new NullPointerException("Cannot create a ToolSpec object from "
                                           + "a null org.w3c.dom.Document");
        }
        this.doc = d;
    }

    public ExtendedDocumentBuilder getDocumentBuilder() {
        return builder;
    }

    public boolean isValidInputStream(String id) {
        Element streams = getStreams();

        if (streams == null) {
            return false;
        }
        NodeList nl = streams.getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "instream");

        for (int i = 0; i < nl.getLength(); i++) {
            if (((Element)nl.item(i)).getAttribute("id").equals(id)) {
                return true;
            }
        }
        return false;
    }

    public Element getElementById(String id) {
        return doc.getElementById(id);
    }

    public boolean hasHandler() {
        return doc.getDocumentElement().hasAttribute("handler");
    }

    public Tool getHandler() throws ToolException {
        if (!hasHandler()) {
            return null;
        }

        if (handler == null) {
            String handlerClz = doc.getDocumentElement().getAttribute("handler");

            try {
                handler = (Tool)Class.forName(handlerClz).newInstance();
            } catch (Exception ex) {
                throw new ToolException("Handler could not be instantiated: " + handlerClz, ex);
            }
        }
        return handler;
    }

    public Tool getHandler(ClassLoader loader) throws ToolException {
        if (!hasHandler()) {
            return null;
        }

        if (handler == null) {
            String handlerClz = doc.getDocumentElement().getAttribute("handler");

            try {
                handler = (Tool)Class.forName(handlerClz, true, loader).newInstance();
            } catch (Exception ex) {
                throw new ToolException("Handler could not be instantiated: " + handlerClz, ex);
            }
        }
        return handler;
    }

    public Element getStreams() {
        NodeList nl = doc.getDocumentElement().getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "streams");

        if (nl.getLength() > 0) {
            return (Element)nl.item(0);
        } else {
            return null;
        }
    }

    public List getInstreamIds() {
        List<Object> res = new ArrayList<Object>();
        Element streams = getStreams();

        if (streams != null) {
            NodeList nl = streams.getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "instream");

            for (int i = 0; i < nl.getLength(); i++) {
                res.add(((Element)nl.item(i)).getAttribute("id"));
            }
        }
        return Collections.unmodifiableList(res);
    }

    public List getOutstreamIds() {
        List<Object> res = new ArrayList<Object>();
        Element streams = getStreams();

        if (streams != null) {
            NodeList nl = streams.getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "outstream");

            for (int i = 0; i < nl.getLength(); i++) {
                res.add(((Element)nl.item(i)).getAttribute("id"));
            }
        }
        return Collections.unmodifiableList(res);
    }

    public Element getUsage() {
        return (Element)doc.getDocumentElement().getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "usage")
            .item(0);
    }

    public void transform(InputStream stylesheet, OutputStream out) throws TransformerException {
        Transformer trans = TransformerFactory.newInstance().newTransformer(new StreamSource(stylesheet));
        trans.transform(new DOMSource(doc), new StreamResult(out));
    }

    public Element getPipeline() {
        NodeList nl = doc.getDocumentElement().getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "pipeline");

        if (nl.getLength() > 0) {
            return (Element)nl.item(0);
        } else {
            return null;
        }
    }

    public NodeList getUsageForms() {
        return getUsage().getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "form");
    }

    /**
     * Arguments can have streamref attributes which associate them with a
     * stream. Tools usually request streams and rely on them being ready. If an
     * argument is given a streamref, then the container constructs a stream
     * from the argument value. This would usually be a simple FileInputStream
     * or FileOutputStream. The mechanics of this are left for the container to
     * sort out, but that is the reason why this getter method exists.
     */
    public String getStreamRefName(String streamId) {
        if (getUsage() != null) {
            NodeList nl = getUsage().getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "associatedArgument");

            for (int i = 0; i < nl.getLength(); i++) {
                if (((Element)nl.item(i)).getAttribute("streamref").equals(streamId)) {
                    return ((Element)nl.item(i).getParentNode()).getAttribute("id");
                }
            }
            nl = getUsage().getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "argument");
            for (int i = 0; i < nl.getLength(); i++) {
                if (((Element)nl.item(i)).getAttribute("streamref").equals(streamId)) {
                    return ((Element)nl.item(i)).getAttribute("id");
                }
            }
        }
        return null;
    }

    public String getParameterDefault(String name) {
        Element el = getElementById(name);
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Element with id " + name + " is " + el);
        }
        if (el != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("local name is " + el.getLocalName());
            }
            if (el.getLocalName().equals("argument")) {
                if (el.hasAttribute("default")) {
                    return el.getAttribute("default");
                }
            } else if (el.getLocalName().equals("option")) {
                NodeList assArgs = el.getElementsByTagNameNS("http://www.xsume.com/Xpipe/ToolSpecification",
                                                             "associatedArgument");

                if (assArgs.getLength() > 0) {
                    Element assArg = (Element)assArgs.item(0);

                    if (assArg.hasAttribute("default")) {
                        return assArg.getAttribute("default");
                    }
                }
            }
        }
        return null;
    }

    public String getAnnotation() {
        String result = null;
        Element element = doc.getDocumentElement();
        NodeList list = element.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            if ((list.item(i).getNodeType() == Node.ELEMENT_NODE)
                && (list.item(i).getNodeName().equals("annotation"))) {
                result = list.item(i).getFirstChild().getNodeValue();
                break;
            }
        }
        return result;
    }

}
