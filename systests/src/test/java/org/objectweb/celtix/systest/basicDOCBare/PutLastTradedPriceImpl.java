package org.objectweb.celtix.systest.basicDOCBare;
import javax.xml.ws.Holder;
import org.objectweb.hello_world_doc_lit_bare.PutLastTradedPricePortType;
import org.objectweb.hello_world_doc_lit_bare.types.TradePriceData;
public class PutLastTradedPriceImpl implements PutLastTradedPricePortType {
    public void sayHi(Holder<TradePriceData> inout) {
        inout.value.setTickerPrice(4.5f);
        inout.value.setTickerSymbol("OBJECTWEB");
    }   
    public void putLastTradedPrice(TradePriceData body) {
        System.out.println("-----TradePriceData TickerPrice : ----- " + body.getTickerPrice());
        System.out.println("-----TradePriceData TickerSymbol : ----- " + body.getTickerSymbol());

    }
    
    public String bareNoParam() {
        return "testResponse";
    }
   
}
