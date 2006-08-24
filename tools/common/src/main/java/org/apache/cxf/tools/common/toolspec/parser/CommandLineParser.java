package org.apache.cxf.tools.common.toolspec.parser;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.tools.common.toolspec.Tool;
import org.apache.cxf.tools.common.toolspec.ToolSpec;


public class CommandLineParser {

    private static final Logger LOG = LogUtils.getL7dLogger(CommandLineParser.class);
    private ToolSpec toolspec;

    public CommandLineParser(ToolSpec ts) {
        this.toolspec = ts;
    }

    public void setToolSpec(ToolSpec ts) {
        this.toolspec = ts;
    }

    public static String[] getArgsFromString(String s) {
        StringTokenizer toker = new StringTokenizer(s);
        List<Object> res = new ArrayList<Object>();

        while (toker.hasMoreTokens()) {
            res.add(toker.nextToken());
        }
        return res.toArray(new String[res.size()]);
    }

    public CommandDocument parseArguments(String args) throws BadUsageException {
        return parseArguments(getArgsFromString(args));
    }

    public CommandDocument parseArguments(String[] args) throws BadUsageException {

        if (LOG.isLoggable(Level.INFO)) {
            StringBuffer debugMsg = new StringBuffer("Parsing arguments: ");

            for (int i = 0; i < args.length; i++) {
                debugMsg.append(args[i]).append(" ");
            }
            LOG.info(debugMsg.toString());
        }

        if (toolspec == null) {
            throw new IllegalStateException("No schema known- call to acceptSc"
                                            + "hema() must be made and must succeed");
        }

        // Create a result document

        Document resultDoc = null;

        try {
            resultDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "FAIL_CREATE_DOM_MSG");
        }
        Element commandEl = resultDoc.createElementNS("http://www.xsume.com/Xutil/Command", "command");

        // resultDoc.createAttributeNS("http://www.w3.org/2001/XMLSchema-instance","schemaLocation");
        commandEl.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "xsi:schemaLocation",
                                 "http://www.xsume.com/Xutil/Command http://www.xsume.com/schema/xutil/c"
                                     + "ommand.xsd");
        commandEl.setAttribute("xmlns", "http://www.xsume.com/Xutil/Command");
        commandEl.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        resultDoc.appendChild(commandEl);

        TokenInputStream tokens = new TokenInputStream(args);

        // for all form elements...
        Element usage = toolspec.getUsage();

        NodeList usageForms = toolspec.getUsageForms();
        if (LOG.isLoggable(Level.INFO)) {
            LOG
                .info("Found " + usageForms.getLength()
                      + " alternative forms of usage, will use default form");
        }
        if (usageForms.getLength() > 0) {
            ErrorVisitor errors = new ErrorVisitor();

            for (int i = 0; i < usageForms.getLength(); i++) {
                Form form = new Form((Element)usageForms.item(i));

                int pos = tokens.getPosition();

                if (form.accept(tokens, commandEl, errors)) {
                    commandEl.setAttribute("form", form.getName());
                    break;
                } else {
                    // if no more left then return null;
                    tokens.setPosition(pos);
                    if (i == usageForms.getLength() - 1) {
                        if (LOG.isLoggable(Level.INFO)) {
                            LOG.info("No more forms left to try, returning null");
                        }
                        throwUsage(errors);
                    }
                }
            }
        } else {
            ErrorVisitor errors = new ErrorVisitor();
            Form form = new Form(usage);

            if (!form.accept(tokens, commandEl, errors)) {
                throwUsage(errors);
            }
        }

        // output the result document
        if (LOG.isLoggable(Level.FINE)) {
            try {
                Transformer serializer = TransformerFactory.newInstance()
                    .newTransformer(
                                    new StreamSource(Tool.class
                                        .getResourceAsStream("indent-no-xml-declaration.xsl")));

                serializer.transform(new DOMSource(resultDoc), new StreamResult(new PrintStream(System.out)));
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "ERROR_SERIALIZE_COMMAND_MSG", ex);
            }
        }

        return new CommandDocument(toolspec, resultDoc);
    }

    public void throwUsage(ErrorVisitor errors) throws BadUsageException {
        try {
            throw new BadUsageException(getUsage(), errors);
        } catch (TransformerException ex) {
            LOG.log(Level.SEVERE, "CANNOT_GET_USAGE_MSG", ex);
            throw new BadUsageException(errors);
        }
    }

    public String getUsage() throws TransformerException {
        // REVISIT: style usage document into a form more readily output as a
        // usage message
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = getClass().getResourceAsStream("usage.xsl");

        toolspec.transform(in, baos);
        return baos.toString();
    }

    public String getDetailedUsage() throws TransformerException {
        // REVISIT: style usage document into a form more readily output as a
        // usage message
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        toolspec.transform(getClass().getResourceAsStream("detailedUsage.xsl"), baos);
        return baos.toString();
    }

    public String getDetailedUsage(String id) {
        String result = null;
        Element element = toolspec.getElementById(id);
        NodeList annotations = element.getElementsByTagNameNS(Tool.TOOL_SPEC_PUBLIC_ID, "annotation");
        if ((annotations != null) && (annotations.getLength() > 0)) {
            result = annotations.item(0).getFirstChild().getNodeValue();
        }
        return result;
    }

    public String getToolUsage() {
        return toolspec.getAnnotation();
    }

}
