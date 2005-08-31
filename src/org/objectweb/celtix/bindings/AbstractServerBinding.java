package org.objectweb.celtix.bindings;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.wsdl.WSDLException;
import javax.xml.ws.Binding;
import javax.xml.ws.Endpoint;

import org.objectweb.celtix.Bus;
import org.objectweb.celtix.addressing.EndpointReferenceType;
import org.objectweb.celtix.bus.EndpointImpl;
import org.objectweb.celtix.context.InputStreamMessageContext;
import org.objectweb.celtix.transports.ServerTransport;
import org.objectweb.celtix.transports.ServerTransportCallback;
import org.objectweb.celtix.transports.TransportFactory;
import org.objectweb.celtix.wsdl.EndpointReferenceUtils;

public abstract class AbstractServerBinding implements ServerBinding {

    private static Logger logger = Logger.getLogger(AbstractServerBinding.class.getName());

    protected final Bus bus;
    protected final EndpointReferenceType reference;
    protected ServerTransport transport;
    protected Endpoint endpoint;

    public AbstractServerBinding(Bus b, EndpointReferenceType ref, Endpoint ep) {
        bus = b;
        reference = ref;
        endpoint = ep;
    }

    public Binding getBinding() {
        return endpoint.getBinding();
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void activate() throws WSDLException, IOException {
        String address = EndpointReferenceUtils.getAddress(reference);
        logger.info("Activating server binding with address: " + address);
        TransportFactory tf = getDefaultTransportFactory(address);
        transport = tf.createServerTransport(reference);
        transport.activate(new ServerTransportCallback() {

            public void dispatch(InputStreamMessageContext ctx, ServerTransport t) {
                Runnable r = new ServerBindingCallback(ctx, transport, AbstractServerBinding.this);
                // wait for documentation and implementation of JAX-WS RI to
                // sync up
                Endpoint ep = AbstractServerBinding.this.getEndpoint();
                EndpointImpl epi = (EndpointImpl)ep;
                Executor ex = epi.getExecutor();

                // the default executor (obtained from the bus) does not
                // work - but any of the following do
                /*
                 * ex = new Executor() { public void execute(Runnable command) {
                 * command.run(); } }; ex = Executors.newSingleThreadExecutor();
                 */

                ex = Executors.newFixedThreadPool(1);
                assert null != ex;
                assert null != r;
                logger.info("Submitting Runnable to Executor.");
                ex.execute(r);

            }

        });
    }

    public void deactivate() throws IOException {
        transport.deactivate();
    }

    protected abstract TransportFactory getDefaultTransportFactory(String address);

}
