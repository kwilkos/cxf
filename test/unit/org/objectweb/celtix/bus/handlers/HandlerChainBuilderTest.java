package org.objectweb.celtix.bus.handlers;

import java.util.Arrays;
import java.util.List;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.LogicalHandler;

import junit.framework.TestCase;

import org.easymock.EasyMock;

public class HandlerChainBuilderTest extends TestCase {

    Handler[] allHandlers = {EasyMock.createMock(LogicalHandler.class),
                             EasyMock.createMock(Handler.class),
                             EasyMock.createMock(Handler.class),
                             EasyMock.createMock(LogicalHandler.class)};
    Handler[] logicalHandlers = {allHandlers[0], allHandlers[3]}; 
    Handler[] protocolHandlers = {allHandlers[1], allHandlers[2]}; 

    public void testChainSorting() {

        
        HandlerChainBuilder builder = new HandlerChainBuilder();
        List<Handler> sortedHandlerChain = builder.sortHandlers(Arrays.asList(allHandlers));

        assertSame(logicalHandlers[0], sortedHandlerChain.get(0));
        assertSame(logicalHandlers[1], sortedHandlerChain.get(1));
        assertSame(protocolHandlers[0], sortedHandlerChain.get(2));
        assertSame(protocolHandlers[1], sortedHandlerChain.get(3));
        
    }
}
