package org.objectweb.celtix.geronimo.builder;

import org.apache.geronimo.webservices.WebServiceContainer;

public class CeltixWebServiceContainer implements WebServiceContainer {


    public void invoke(Request request, Response response) throws Exception {
        
        System.out.println(this + " invoke called " + request.getParameters());
        // TODO Auto-generated method stub

    }

    public void getWsdl(Request request, Response response) throws Exception {
        // TODO Auto-generated method stub
        System.out.println(this + " getWsdl called " + request.getParameters());

    }

}
