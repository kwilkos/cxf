package org.objectweb.celtix.bus.jaxws.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import org.xml.sax.InputSource;

import org.objectweb.celtix.bindings.DataReader;
import org.objectweb.celtix.bus.jaxws.DynamicDataBindingCallback;
import org.objectweb.celtix.context.ObjectMessageContext;

public class SOAPBodyDataReader<T> implements DataReader<T> {
    
    final DynamicDataBindingCallback callback;

    public SOAPBodyDataReader(DynamicDataBindingCallback cb) {
        callback = cb;
    }

    public Object read(int idx, T input) {
        Source obj = null;        
        SOAPBody src = (SOAPBody)input;    
        try {
            Document doc = src.extractContentAsDocument();
            assert doc != null;
    
            if (DOMSource.class.isAssignableFrom(callback.getSupportedFormats()[0])) {
                obj = new DOMSource();
                ((DOMSource)obj).setNode(doc);          
            } else if (SAXSource.class.isAssignableFrom(callback.getSupportedFormats()[0])) {     
                InputSource inputSource = new InputSource(getSOAPBodyStream(doc));
                obj = new SAXSource(inputSource);
            } else if (StreamSource.class.isAssignableFrom(callback.getSupportedFormats()[0])) {     
                obj = new StreamSource(getSOAPBodyStream(doc));
            } else if (Object.class.isAssignableFrom(callback.getSupportedFormats()[0])) {           
                JAXBContext context = callback.getJAXBContext();
                Unmarshaller u = context.createUnmarshaller();
                return u.unmarshal(doc);                    
            }
        } catch (Exception se) {
            se.printStackTrace();
        }
        return obj;
    }

    public Object read(QName name, int idx, T input) {
        //Complete
        return null;
    }
    
    public void readWrapper(ObjectMessageContext objCtx, boolean isOutBound, T input) {
        //Complete
    }
    
    private InputStream getSOAPBodyStream(Document doc) throws Exception {
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        if (impl == null) {
            System.setProperty(DOMImplementationRegistry.PROPERTY,
                "com.sun.org.apache.xerces.internal.dom.DOMImplementationSourceImpl");
            registry = DOMImplementationRegistry.newInstance();
            impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
        }
        LSOutput output = impl.createLSOutput();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        output.setByteStream(byteArrayOutputStream);
        LSSerializer writer = impl.createLSSerializer();
        writer.write(doc, output);
        byte[] buf = byteArrayOutputStream.toByteArray();
        return new ByteArrayInputStream(buf);
    }

}
