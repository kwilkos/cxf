package org.objectweb.celtix.systest.handlers;




import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.handler_test.HandlerTest;

@WebService(serviceName = "HandlerTestService", portName = "SoapPort", name = "HandlerTest", 
            targetNamespace = "http://objectweb.org/handler_test")
public class HandlerTestImpl implements HandlerTest {

    private WebServiceContext context; 
        
    public final List<String> ping() {

        try {
            List<String> handlerInfoList = getHandlersInfo(context.getMessageContext());
            handlerInfoList.add("servant");
            System.out.println("\n\n\nhandler list: " + handlerInfoList);
            context.getMessageContext().remove("handler.info");

            return handlerInfoList;

        } catch (Exception e) {
            e.printStackTrace();

        } 
        return null;
    }

    public final List<String> pingWithArgs(String handlerCommand) { 
        List<String> ret = new ArrayList<String>(); 
        ret.add(handlerCommand); 
        ret.addAll(getHandlersInfo(context.getMessageContext()));
        return ret;
    } 


    @Resource public void setWebServiceContext(WebServiceContext ctx) { 
        context = ctx;
    }

    @SuppressWarnings("unchecked")
    private List<String> getHandlersInfo(MessageContext ctx) { 
        List<String> ret = (List<String>)ctx.get("handler.info"); 
        if (ret == null) {
            ret = new ArrayList<String>(); 
        }
        return ret;
    } 

}
