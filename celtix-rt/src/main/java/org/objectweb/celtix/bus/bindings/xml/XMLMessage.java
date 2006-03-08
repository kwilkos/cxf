package org.objectweb.celtix.bus.bindings.xml;

import java.io.*;
import org.w3c.dom.*;

public class XMLMessage {

    private Document root;
    private XMLUtils xmlUtils = new XMLUtils();

    public XMLMessage() {
        this.root = xmlUtils.newDocument();
    }
    
    public void writeTo(OutputStream out) throws IOException {
        xmlUtils.writeTo(this.root, out);
    }

    public Document getRoot() {
        return this.root;
    }

    public void setRoot(Document r) {
        this.root = r;
    }

    public void appendChild(Node child) {
        if (root != null) {
            root.appendChild(child);
        }
    }
}
