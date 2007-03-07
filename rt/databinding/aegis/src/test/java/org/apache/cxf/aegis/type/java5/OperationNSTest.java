package org.apache.cxf.aegis.type.java5;

import java.util.List;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.w3c.dom.Document;

import org.apache.cxf.aegis.AbstractAegisTest;
import org.apache.cxf.aegis.databinding.AegisDatabinding;
import org.apache.cxf.aegis.util.XmlConstants;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.Service;

public class OperationNSTest extends AbstractAegisTest {

    public void setUp() throws Exception {
        super.setUp();

        JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
        sf.setServiceClass(NotificationLogImpl.class);
        sf.setAddress("NotificationLogImpl");
        sf.getServiceFactory().setDataBinding(new AegisDatabinding());

        sf.create();
    }

    public void testWSDL() throws Exception {
        Document wsdl = getWSDLDocument("NotificationService");

        addNamespace("xsd", XmlConstants.XSD);
        assertValid("//xsd:element[@name='Notify']", wsdl);
    }

    @WebService(name = "NotificationLog", targetNamespace = "http://www.sics.se/NotificationLog")
    public static interface NotificationLog {

        @WebMethod(operationName = "Notify", action = "")
        @Oneway
        public void Notify(@WebParam(name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/b-2")
        Document Notify);

        @WebMethod(operationName = "query", action = "")
        @WebResult(name = "queryResponse", targetNamespace = "http://www.sics.se/NotificationLog")
        public List<Document> query(
                                    @WebParam(name = "xpath", targetNamespace = "http://www.sics.se/NotificationLog")
                                    String xpath);

        @WebMethod(operationName = "Notify2", action = "")
        @Oneway
        public void Notify2(
                            @WebParam(name = "Notify", targetNamespace = "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd")
                            Document Notify);
    }

    @WebService(endpointInterface = "org.apache.cxf.aegis.type.java5.OperationNSTest$NotificationLog",
                serviceName="NotificationService")
    public static class NotificationLogImpl implements NotificationLog {

        public void Notify(Document Notify) {
        }

        public void Notify2(Document Notify) {
        }

        public List<Document> query(String xpath) {
            return null;
        }
    }
}
