package org.objectweb.hello_world_soap_http;

import javax.xml.ws.Holder;
import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;

public class DocLitBareImpl implements PutLastTradedPricePortType {
    int sayHiCount;
    int putLastTradedPriceCount;
    
    public TradePriceData sayHi(Holder<TradePriceData> inout) {
        ++sayHiCount;

        TradePriceData ret = new TradePriceData();
        ret.setTickerPrice(4.5f);
        ret.setTickerSymbol("OBJECTWEB");
        inout.value = ret;
        return ret;
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
