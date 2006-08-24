package org.apache.cxf.tools.fortest.classnoanno.docwrapped;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService

public interface Stock {
    @WebMethod
    @RequestWrapper
    @ResponseWrapper
    float getPrice(String tickerSymbol);
}
