package org.objectweb.hello_world_soap_http;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;

@WebService
public class DocLitBareImpl implements PutLastTradedPricePortType {
    int sayHiCount;
    int putLastTradedPriceCount;
    
    public void sayHi(Holder<TradePriceData> inout) {
        ++sayHiCount;
        inout.value.setTickerPrice(4.5f);
        inout.value.setTickerSymbol("OBJECTWEB");
    }
    
    public void putLastTradedPrice(TradePriceData body) {
        ++putLastTradedPriceCount;
    }
    
    public int getSayHiInvocationCount() {
        return sayHiCount; 
    }
    
    public int getPutLastTradedPriceCount() {
        return putLastTradedPriceCount; 
    }    
}
