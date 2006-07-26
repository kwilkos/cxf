package org.objectweb.celtix.tools.fortest.classnoanno.rpc;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

@WebService
@SOAPBinding(style = Style.RPC)
public interface Stock {
    float getPrice(String tickerSymbol);
}
