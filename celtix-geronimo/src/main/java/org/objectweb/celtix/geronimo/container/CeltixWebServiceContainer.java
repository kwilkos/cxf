package org.objectweb.celtix.geronimo.container;

import org.apache.geronimo.webservices.WebServiceContainer;

public class CeltixWebServiceContainer implements WebServiceContainer {

    private transient GeronimoServerTransport serverTransport; 
    
    public void invoke(Request request, Response response) throws Exception {
        
        if (serverTransport == null) {
            throw new IllegalStateException("no transport set for " + this);
        }
        serverTransport.invoke(request, response);
    }

    public void getWsdl(Request request, Response response) throws Exception {
        // TODO Auto-generated method stub
        System.out.println(this + " getWsdl called " + request.getParameters());
        
    }

    public GeronimoServerTransport getServerTransport() {
        return serverTransport;
    }

    public void setServerTransport(GeronimoServerTransport aServerTransport) {
        serverTransport = aServerTransport;
    }

}
