package org.apache.cxf.staxutils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class PartialXMLStreamReader extends DepthXMLStreamReader {
    private QName endTag;
    private boolean foundEnd;
    private int endDepth;
    private int currentEvent;
    
    public PartialXMLStreamReader(XMLStreamReader r, QName endTag) {
        super(r);
        this.endTag = endTag;
        currentEvent = r.getEventType();
    }

    @Override
    public int next() throws XMLStreamException {
        if (!foundEnd) { 
            currentEvent = super.next();

            if (currentEvent == START_ELEMENT && getName().equals(endTag)) {
                foundEnd = true;
                endDepth = getDepth();
                return END_ELEMENT;
            }
            
            return currentEvent;
        } else if (endDepth > 0) {
            endDepth--;
            currentEvent = END_ELEMENT;
        } else {
            currentEvent = END_DOCUMENT;
        }
        
        return currentEvent;
    }

    @Override
    public int getEventType() {
        return currentEvent;
    }

    @Override
    public boolean hasNext() {
        return currentEvent != END_DOCUMENT;
    }
    
    
}
