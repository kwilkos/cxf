package org.apache.cxf.transport.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.MessageObserver;
import org.xmlsoap.schemas.wsdl.http.AddressType;

public class LocalTransportFactoryTest extends TestCase {
    public void testTransportFactory() throws Exception {
        LocalTransportFactory factory = new LocalTransportFactory();
        
        EndpointInfo ei = new EndpointInfo(null, "http://schemas.xmlsoap.org/soap/http");
        AddressType a = new AddressType();
        a.setLocation("http://localhost/test");
        ei.addExtensor(a);

        Destination d = factory.getDestination(ei);
        d.setMessageObserver(new EchoObserver());
        
        
        Conduit conduit = factory.getConduit(ei);
        TestMessageObserver obs = new TestMessageObserver();
        conduit.setMessageObserver(obs);
        
        Message m = new MessageImpl();
        conduit.send(m);

        OutputStream out = m.getContent(OutputStream.class);
        out.write("hello".getBytes());
        out.close();

        
        assertEquals("hello", obs.getResponseStream().toString());
    }

    static class EchoObserver implements MessageObserver {

        public void onMessage(Message message) {
            try {
                Conduit backChannel = message.getDestination().getBackChannel(message, null, null);

                backChannel.send(message);

                OutputStream out = message.getContent(OutputStream.class);
                assertNotNull(out);
                InputStream in = message.getContent(InputStream.class);
                assertNotNull(in);
                
                copy(in, out, 1024);

                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void copy(final InputStream input, final OutputStream output, final int bufferSize)
        throws IOException {
        try {
            final byte[] buffer = new byte[bufferSize];

            int n = input.read(buffer);
            while (-1 != n) {
                output.write(buffer, 0, n);
                n = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
    
    
    class TestMessageObserver implements MessageObserver {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        boolean written;
        
        public synchronized ByteArrayOutputStream getResponseStream() throws Exception {
            if (!written) {
                wait();
            }
            return response;
        }
        

        public synchronized void onMessage(Message message) {
            try {
                copy(message.getContent(InputStream.class), response, 1024);
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            } finally {
                written = true;
                notifyAll();
            }
        }
    }
}
