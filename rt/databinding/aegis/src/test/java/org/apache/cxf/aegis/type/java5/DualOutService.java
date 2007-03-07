package org.apache.cxf.aegis.type.java5;

import java.util.ArrayList;
import java.util.Collection;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.Holder;

@WebService(serviceName="DualOutService")
public class DualOutService
{
    @WebMethod
    public String getValues(@WebParam(mode=WebParam.Mode.OUT) Holder<String> out2)
    {
        out2.value = "hi";
        return "hi";
    }
    
    @WebMethod
    public Collection<String> getStrings()
    {
        return new ArrayList<String>();
    }
}
