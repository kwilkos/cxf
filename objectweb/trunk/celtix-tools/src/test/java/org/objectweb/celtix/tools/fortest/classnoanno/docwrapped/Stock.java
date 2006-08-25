package org.objectweb.celtix.tools.fortest.classnoanno.docwrapped;
import javax.jws.WebService;

@WebService
public interface Stock {
    float getPrice(String tickerSymbol);
}
