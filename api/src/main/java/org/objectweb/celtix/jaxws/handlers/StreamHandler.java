package org.objectweb.celtix.jaxws.handlers;

import javax.xml.ws.handler.Handler;



/**
 * A StreamHandler provides an interception point which gives access
 * to the data stream immediately before being written to or read from
 * the underlying transport.  The StreamHandler allows transformations
 * on the marshalled data.
 */
public interface StreamHandler extends Handler<StreamMessageContext> {
}
