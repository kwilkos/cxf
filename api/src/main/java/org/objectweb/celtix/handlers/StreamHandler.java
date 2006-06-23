package org.objectweb.celtix.handlers;

import javax.xml.ws.handler.Handler;

import org.objectweb.celtix.context.StreamMessageContext;



/**
 * A StreamHandler provides an interception point which gives access
 * to the data stream immediately before being written to or read from
 * the underlying transport.  The StreamHandler allows transformations
 * on the marshalled data.
 */
public interface StreamHandler extends Handler<StreamMessageContext> {
}
