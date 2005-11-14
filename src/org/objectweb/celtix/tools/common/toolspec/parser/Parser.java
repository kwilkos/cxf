package org.objectweb.celtix.tools.common.toolspec.parser;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.InputSource;

import org.objectweb.celtix.common.logging.LogUtils;
import org.objectweb.celtix.tools.common.dom.SchemaValidatingSAXParser;
import org.objectweb.celtix.tools.common.toolspec.ToolSpecContentHandler;
import org.objectweb.celtix.tools.common.toolspec.ToolSpecDocument;
/**
 * This class parses toolspec xml documents and creates {@link ToolSpecDocument}
 * instances.
 */
public class Parser {

    private static final Logger LOG = LogUtils.getL7dLogger(Parser.class);

    private final SchemaValidatingSAXParser parser = new SchemaValidatingSAXParser();

    public Parser() {
    }

    public ToolSpecDocument parse(InputStream in) {
        ToolSpecContentHandler contentHandler = new ToolSpecContentHandler();
            
        try {
            parser.getSAXParser().parse(new InputSource(in), contentHandler);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "FAIL_PARSE_INPUT_MSG", ex);
        }
        return contentHandler.getToolSpecDocument();
    }

}
