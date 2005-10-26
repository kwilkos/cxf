package org.objectweb.celtix.systest.handlers;




import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import org.objectweb.handler_test.HandlerTest;
import org.objectweb.handler_test.PingException;
import org.objectweb.handler_test.types.PingFaultDetails;

@WebService(serviceName = "HandlerTestService", portName = "SoapPort", name = "HandlerTest", 
            targetNamespace = "http://objectweb.org/handler_test")
public class HandlerTestImpl implements HandlerTest {

    private WebServiceContext context; 
        
    public final List<String> ping() {

        try {
            List<String> handlerInfoList = getHandlersInfo(context.getMessageContext());
            clearHandlersInfo();
            handlerInfoList.add("servant");
            context.getMessageContext().remove("handler.info");
            System.out.println(">> servant returning list: " + handlerInfoList);
            return handlerInfoList;

        } catch (Exception e) {
            e.printStackTrace();

        } 
        return null;
    }

    public final void pingOneWay() {
    } 

    public final List<String> pingWithArgs(String handlerCommand) throws PingException {

        List<String> ret = new ArrayList<String>(); 
        ret.add(handlerCommand); 
        ret.addAll(getHandlersInfo(context.getMessageContext()));
        clearHandlersInfo();

        if (handlerCommand.contains("throw exception")) {
            PingFaultDetails details = new PingFaultDetails(); 
            details.setDetail(ret.toString());
            throw new PingException("from servant", details); 
        }

        return ret;
    } 


    @Resource public void setWebServiceContext(WebServiceContext ctx) { 
        context = ctx;
    }

    @SuppressWarnings("unchecked")
    private List<String> getHandlersInfo(MessageContext ctx) { 
//         List<String> ret = (List<String>)ctx.get("handler.info"); 
//         if (ret == null) {
//             ret = new ArrayList<String>(); 
//         }
//        return ret;
        return TestHandler.getHandlerInfo();
    } 

    private void clearHandlersInfo() {
        TestHandler.clearHandlersInfo();
    }

}
