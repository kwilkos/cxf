package org.objectweb.celtix.fault.http;

import javax.xml.ws.Endpoint;

import org.objectweb.celtix.testutil.common.AbstractTestServerBase;

public class AlarmRetrievalServer extends AbstractTestServerBase {

    protected void run() {
        AlarmRetrievalImpl implementor = new AlarmRetrievalImpl();

        implementor.addAlarm(1);
        implementor.addAlarm(2);

        String address = "http://localhost:9090/mtosi/v1/AlarmRetrieval";
        Endpoint.publish(address, implementor);
    }
    
    public static void main(String args[]) {
        try {
            AlarmRetrievalServer s = new AlarmRetrievalServer();
            s.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(-1);
        } finally {
            System.out.println("Server done!"); 
        }
    }
}
