package org.apache.cxf.tools.fortest.classnoanno.docbare;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.ParameterStyle;

@WebService
@SOAPBinding(parameterStyle = ParameterStyle.BARE)
public interface Stock {
    float getPrice(String tickerSymbol);
}