package org.objectweb.celtix.tools.common.toolspec;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

public class ToolSpecContentHandler extends DefaultHandler implements ContentHandler {

    private ToolSpecDocument document;

    public ToolSpecDocument getToolSpecDocument() {
        return document;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        if ("toolspec".equals(localName)) {
            ToolSpecDeclaration toolspecDecl = new ToolSpecDeclaration();

            toolspecDecl.setHandler(atts.getValue("handler"));
            document = new ToolSpecDocument(toolspecDecl);
        }
    }

}

