package org.objectweb.celtix.systest.handlers;


import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.objectweb.handler_test.types.PingResponse;
import org.objectweb.handler_test.types.PingWithArgs;
import org.objectweb.hello_world_soap_http.types.GreetMe;


public class TestHandler<T extends LogicalMessageContext> 
    extends TestHandlerBase implements LogicalHandler<T> {

    private final JAXBContext jaxbCtx;

    public TestHandler() {
        this(true);
    } 

    public TestHandler(boolean serverSide) {
        super(serverSide); 

        try {
            jaxbCtx = JAXBContext.newInstance(GreetMe.class.getPackage().getName());
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
 
    } 

    public String getHandlerId() { 
        return "handler" + getId();
    } 

    public boolean handleMessage(T ctx) {
        methodCalled("handleMessage");


        boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        boolean ret = handleMessageRet; 

        if (!isServerSideHandler()) {
            return true;
        }


        QName operationName = (QName)ctx.get(MessageContext.WSDL_OPERATION);
        assert operationName != null : "unable to get operation name from " + ctx;

        if ("ping".equals(operationName.getLocalPart())) {
            ret = handlePingMessage(outbound, ctx);
        } else if ("pingWithArgs".equals(operationName.getLocalPart())) {
            ret = handlePingWithArgsMessage(outbound, ctx); 
        }
        return ret;
    }


    private boolean handlePingWithArgsMessage(boolean outbound, T ctx) { 

        
        LogicalMessage msg = ctx.getMessage();
        Object payload = msg.getPayload(jaxbCtx); 
        addHandlerId(ctx.getMessage(), ctx, outbound);

        boolean ret = true;
        if (payload instanceof PingWithArgs) {
            String arg = ((PingWithArgs)payload).getHandlersCommand();
            
            StringTokenizer strtok = new StringTokenizer(arg, " ");
            String hid = strtok.nextToken();
            String direction = strtok.nextToken();
            String command = strtok.nextToken();
            
            if (outbound) {
                return ret;
            }

            if (getHandlerId().equals(hid)) {
                if ("inbound".equals(direction)) {
                    if ("stop".equals(command)) {
                        PingResponse resp = new PingResponse();
                        getHandlerInfoList(ctx).add(getHandlerId()); 
                        resp.getHandlersInfo().addAll(getHandlerInfoList(ctx));
                        msg.setPayload(resp, jaxbCtx);
                        ret = false;
                    } else if ("throw".equals(command)) {
                        throwException(strtok.nextToken());
                    }
                }

            }
        }
        return ret;
    } 


    private void throwException(String exType) { 
        if (exType.contains("ProtocolException")) {
            throw new ProtocolException("from server handler");
        }
    } 

    private boolean handlePingMessage(boolean outbound, T ctx) { 

        LogicalMessage msg = ctx.getMessage();
        addHandlerId(msg, ctx, outbound);
        return handleMessageRet;
    } 

    private void addHandlerId(LogicalMessage msg, T ctx, boolean outbound) { 

        Object obj = msg.getPayload(jaxbCtx);
        if (obj instanceof PingResponse) {
            PingResponse origResp = (PingResponse)obj;
            PingResponse newResp = new PingResponse();
            newResp.getHandlersInfo().addAll(origResp.getHandlersInfo());
            newResp.getHandlersInfo().add(getHandlerId());
            msg.setPayload(newResp, jaxbCtx);
        } else if (!outbound && obj == null) {
            getHandlerInfoList(ctx).add(getHandlerId());
        }
    } 


    public boolean handleFault(T ctx) {
        methodCalled("handleFault");
        //boolean outbound = (Boolean)ctx.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        return true;
    }

    public void close(MessageContext arg0) {
        methodCalled("close");
    }

    public void init(Map arg0) {
        methodCalled("init");
    }

    public void destroy() {
        methodCalled("destroy");
    }

    public String toString() { 
        return getHandlerId(); 
    } 
}    
