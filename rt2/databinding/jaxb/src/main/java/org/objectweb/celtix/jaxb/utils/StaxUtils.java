package org.objectweb.celtix.jaxb.utils;

import java.io.*;
import java.util.*;

import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.*;

public final class StaxUtils {
    private static final XMLInputFactory   XML_INPUT_FACTORY   = XMLInputFactory.newInstance();
    private static final XMLOutputFactory  XML_OUTPUT_FACTORY  = XMLOutputFactory.newInstance();
    
    private StaxUtils() {
    }

    public static XMLInputFactory getXMLInputFactory() {
        return XML_INPUT_FACTORY;
    }

    public static XMLOutputFactory getXMLOutputFactory() {
        return XML_OUTPUT_FACTORY;
    }

    public static XMLStreamWriter createXMLStreamWriter(OutputStream out) {
        return createXMLStreamWriter(out, null);
    }

    public static XMLStreamWriter createXMLStreamWriter(OutputStream out, String encoding) {
        if (encoding == null) {
            encoding = "UTF-8";
        }
        
        try {
            return getXMLOutputFactory().createXMLStreamWriter(out, encoding);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Cant' create XMLStreamWriter", e);
        }
    }

    public static XMLStreamReader createFilteredReader(XMLStreamReader reader, StreamFilter filter) {
        try {
            return getXMLInputFactory().createFilteredReader(reader, filter);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Cant' create XMLStreamReader", e);
        }        
    }

    public static XMLStreamReader createXMLStreamReader(InputStream in) {
        return createXMLStreamReader(in, null);
    }

    public static XMLStreamReader createXMLStreamReader(InputStream in, String encoding) {
        if (encoding == null) {
            encoding = "UTF-8";
        }
        
        try {
            return getXMLInputFactory().createXMLStreamReader(in, encoding);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Cant' create XMLStreamReader", e);
        }
    }

    public static XMLStreamReader createXMLStreamReader(Reader reader) {
        try {
            return getXMLInputFactory().createXMLStreamReader(reader);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Cant' create XMLStreamReader", e);
        }
    }

    public static void nextEvent(XMLStreamReader dr) {
        try {
            dr.next();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't parse stream.", e);
        }
    }

    public static boolean toNextElement(DepthXMLStreamReader reader) {
        if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
            return true;
        }
        
        if (reader.getEventType() == XMLStreamReader.END_ELEMENT) {
            return false;
        }
        
        try {
            int depth = reader.getDepth();
            
            for (int event = reader.getEventType();
                 reader.getDepth() >= depth && reader.hasNext();
                 event = reader.next()) {
                if (event == XMLStreamReader.START_ELEMENT && reader.getDepth() == depth + 1) {
                    return true;
                } else if (event == XMLStreamReader.END_ELEMENT) {
                    depth--;
                }
            }
            return false;
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't parse stream.", e);
        }
    }

    public static boolean toNextText(DepthXMLStreamReader reader) {
        if (reader.getEventType() == XMLStreamReader.CHARACTERS) {
            return true;
        }
        
        
        try {
            int depth = reader.getDepth();
            
            for (int event = reader.getEventType();
                 reader.getDepth() >= depth && reader.hasNext();
                 event = reader.next()) {
                if (event == XMLStreamReader.CHARACTERS && reader.getDepth() == depth + 1) {
                    return true;
                }
            }
            return false;
        } catch (XMLStreamException e) {
            throw new RuntimeException("Couldn't parse stream.", e);
        }
    }
}

