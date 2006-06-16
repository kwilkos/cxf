package org.objectweb.celtix.jaxb;

import javax.xml.namespace.QName;
import javax.xml.stream.EventFilter;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class StaxEventFilter implements EventFilter {
    private QName[] tags;

    public StaxEventFilter(QName[] eventsToReject) {
        tags = eventsToReject;
    }

    public boolean accept(XMLEvent event) {
        if (event.isStartDocument() 
            || event.isEndDocument()) {
            return false;
        }

        if (event.isStartElement()) {
            StartElement startEl = event.asStartElement();
            QName elName = startEl.getName();
            for (QName tag : tags) {
                if (elName.equals(tag)) {
                    return false;
                }
            }
        }

        if (event.isEndElement()) {
            EndElement endEl = event.asEndElement();
            QName elName = endEl.getName();
            for (QName tag : tags) {
                if (elName.equals(tag)) {
                    return false;
                }
            }
        }
        return true;
    }
}
