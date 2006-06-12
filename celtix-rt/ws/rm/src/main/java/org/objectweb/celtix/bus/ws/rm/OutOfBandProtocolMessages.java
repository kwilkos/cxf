package org.objectweb.celtix.bus.ws.rm;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;


@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE,
             use = SOAPBinding.Use.LITERAL,
             style = SOAPBinding.Style.DOCUMENT)

public interface OutOfBandProtocolMessages {

    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @Oneway
    @WebMethod(operationName = "CreateSequence")
    void createSequence(
        @WebParam(targetNamespace = "http://schemas.xmlsoap.org/ws/2005/02/rm",
                  partName = "create",
                  name = "CreateSequence")
        org.objectweb.celtix.ws.rm.CreateSequenceType create
    );
    
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @Oneway
    @WebMethod(operationName = "CreateSequenceResponse")
    void createSequenceResponse(
        @WebParam(targetNamespace = "http://schemas.xmlsoap.org/ws/2005/02/rm",
                  partName = "response",
                  name = "CreateSequenceResponse")
        org.objectweb.celtix.ws.rm.CreateSequenceResponseType response
    );


    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @Oneway
    @WebMethod(operationName = "TerminateSequence")
    void terminateSequence(
        @WebParam(targetNamespace = "http://schemas.xmlsoap.org/ws/2005/02/rm",
                  partName = "terminate",
                  name = "TerminateSequence")
        org.objectweb.celtix.ws.rm.TerminateSequenceType terminate
    );
}