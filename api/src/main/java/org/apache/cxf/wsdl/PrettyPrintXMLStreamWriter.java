package org.apache.cxf.wsdl;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class PrettyPrintXMLStreamWriter implements XMLStreamWriter {

    static final Map<Class<?>, Integer> WSDL_INDENT_MAP = new HashMap<Class<?>, Integer>();
    static final int DEFAULT_INDENT_LEVEL = 2;

    XMLStreamWriter baseWriter;
    PrintWriter pw;

    int indent;
    Stack<CurrentElement> elems = new Stack<CurrentElement>();
    QName currElem;
    boolean nestedStartElement;   

    public PrettyPrintXMLStreamWriter(XMLStreamWriter writer,
                                      PrintWriter printWriter,
                                      Class<?> parent) {
        baseWriter = writer;
        pw = printWriter;
        indent = getIndentLevel(parent);
    }

    public void indent() {
        for (int i = 0; i < indent; i++) {
            pw.print(' ');
        }
        indent += DEFAULT_INDENT_LEVEL;
    }
    
    public void unindent() {
        indent -= DEFAULT_INDENT_LEVEL;
    }

    public void close() throws XMLStreamException {
        baseWriter.close();
    }

    public void flush() throws XMLStreamException {
        baseWriter.flush();
    }

    public NamespaceContext getNamespaceContext() {
        return baseWriter.getNamespaceContext();
    }


    public java.lang.String getPrefix(java.lang.String uri) throws XMLStreamException {
        return baseWriter.getPrefix(uri);
    }

    public java.lang.Object getProperty(java.lang.String name) throws IllegalArgumentException {
        return baseWriter.getProperty(name);
    }

    public void setDefaultNamespace(java.lang.String uri) throws XMLStreamException {
        baseWriter.setDefaultNamespace(uri);
    }

    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        baseWriter.setNamespaceContext(context);
    }

    public void setPrefix(java.lang.String prefix, java.lang.String uri)
        throws XMLStreamException {
        baseWriter.setPrefix(prefix, uri);
    }

    public void writeAttribute(java.lang.String localName, java.lang.String value)
        throws XMLStreamException {
        writeAttribute(null, localName, value);
    }

    public void writeAttribute(java.lang.String namespaceURI,
                        java.lang.String localName,
                        java.lang.String value) throws XMLStreamException {
        writeAttribute(null, namespaceURI, localName, value);
    }

    public void writeAttribute(java.lang.String prefix,
                        java.lang.String namespaceURI,
                        java.lang.String localName,
                        java.lang.String value) throws XMLStreamException {
        baseWriter.writeAttribute(prefix, namespaceURI, localName, value);
    }

    public void writeCData(java.lang.String data) throws XMLStreamException {
        baseWriter.writeCData(data);
    }

    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        baseWriter.writeCharacters(text, start, len);
    }

    public void writeCharacters(java.lang.String text) throws XMLStreamException {
        baseWriter.writeCharacters(text);
    }

    public void writeComment(java.lang.String data) throws XMLStreamException {
        baseWriter.writeComment(data);
    }

    public void writeDefaultNamespace(java.lang.String namespaceURI) throws XMLStreamException {
        baseWriter.writeDefaultNamespace(namespaceURI);
    }

    public void writeDTD(java.lang.String dtd) throws XMLStreamException {
        baseWriter.writeDTD(dtd);
    }

    public void writeEmptyElement(java.lang.String localName) throws XMLStreamException {
        baseWriter.writeEmptyElement(localName);
    }

    public void writeEmptyElement(java.lang.String namespaceURI, java.lang.String localName)
        throws XMLStreamException {
        writeEmptyElement(null, namespaceURI, localName);
    }

    public void writeEmptyElement(java.lang.String prefix,
                           java.lang.String localName,
                           java.lang.String namespaceURI) throws XMLStreamException {
        baseWriter.writeEmptyElement(prefix, localName, namespaceURI);
    }

    public void writeEndDocument() throws XMLStreamException {
        baseWriter.writeEndDocument();
    }

    public void writeEndElement() throws XMLStreamException {
        unindent();
        CurrentElement elem = (CurrentElement) elems.pop();
        if (elem.hasChildElements()) {
            pw.println();
            indent();
        }
        baseWriter.writeEndElement();
        if (elems.empty()) {
            pw.println();
        }
    }

    public void writeEntityRef(java.lang.String name) throws XMLStreamException {
        baseWriter.writeEntityRef(name);
    }

    public void writeNamespace(java.lang.String prefix, java.lang.String namespaceURI)
        throws XMLStreamException {
        baseWriter.writeNamespace(prefix, namespaceURI);
    }

    public void writeProcessingInstruction(java.lang.String target)
        throws XMLStreamException {
        baseWriter.writeProcessingInstruction(target);
    }

    public void writeProcessingInstruction(java.lang.String target, java.lang.String data)
        throws XMLStreamException {
        baseWriter.writeProcessingInstruction(target, data);
    }

    public void writeStartDocument() throws XMLStreamException {
        baseWriter.writeStartDocument();
    }

    public void writeStartDocument(java.lang.String version) throws XMLStreamException {
        baseWriter.writeStartDocument(version);
    }

    public void writeStartDocument(java.lang.String encoding, java.lang.String version)
        throws XMLStreamException {
        baseWriter.writeStartDocument(encoding, version);
    }
     
    public void writeStartElement(java.lang.String localName) throws XMLStreamException {
        writeStartElement(null, null, localName);
    }
     
    public void writeStartElement(java.lang.String namespaceURI, java.lang.String localName)
        throws XMLStreamException {
        writeStartElement(null, namespaceURI, localName);
    }
     
    public void writeStartElement(java.lang.String prefix,
                           java.lang.String localName,
                           java.lang.String namespaceURI) throws XMLStreamException {
        QName currElemName = new QName(namespaceURI, localName);
        if (elems.empty()) {
            indent();
        } else {
            baseWriter.writeCharacters("");
            pw.println();
            indent();
            CurrentElement elem = (CurrentElement) elems.peek();
            elem.setChildElements(true);
        }
        baseWriter.writeStartElement(prefix, localName, namespaceURI);
        elems.push(new CurrentElement(currElemName));
    }

    private int getIndentLevel(Class<?> parent) {
        Integer result = (Integer)WSDL_INDENT_MAP.get(parent);
        if (result == null) {
            return DEFAULT_INDENT_LEVEL;
        }
        return result.intValue();
    }

    static {
        WSDL_INDENT_MAP.put(Definition.class, new Integer(DEFAULT_INDENT_LEVEL));
        WSDL_INDENT_MAP.put(Binding.class, new Integer(DEFAULT_INDENT_LEVEL * 2));
        WSDL_INDENT_MAP.put(BindingFault.class, new Integer(DEFAULT_INDENT_LEVEL * 3));
        WSDL_INDENT_MAP.put(BindingInput.class, new Integer(DEFAULT_INDENT_LEVEL * 3));
        WSDL_INDENT_MAP.put(BindingOutput.class, new Integer(DEFAULT_INDENT_LEVEL * 3));
        WSDL_INDENT_MAP.put(BindingOperation.class, new Integer(DEFAULT_INDENT_LEVEL * 3));
        WSDL_INDENT_MAP.put(Message.class, new Integer(DEFAULT_INDENT_LEVEL * 2));
        WSDL_INDENT_MAP.put(Operation.class, new Integer(DEFAULT_INDENT_LEVEL * 3));
        WSDL_INDENT_MAP.put(Port.class, new Integer(DEFAULT_INDENT_LEVEL * 3));
        WSDL_INDENT_MAP.put(Service.class, new Integer(DEFAULT_INDENT_LEVEL * 2));
        WSDL_INDENT_MAP.put(Types.class, new Integer(DEFAULT_INDENT_LEVEL * 2));
    }

    class CurrentElement {
        private QName name;
        private boolean hasChildElements;

        CurrentElement(QName qname) {
            name = qname;
        }

        public QName getQName() {
            return name;
        }

        public boolean hasChildElements() {
            return hasChildElements;
        }

        public void setChildElements(boolean childElements) {
            hasChildElements = childElements;
        }
    }
     
}
