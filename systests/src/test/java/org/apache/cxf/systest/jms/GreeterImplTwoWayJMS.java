/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.systest.jms;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.hello_world_jms.BadRecordLitFault;
import org.apache.cxf.hello_world_jms.HelloWorldPortType;
import org.apache.cxf.hello_world_jms.NoSuchCodeLitFault;
import org.apache.cxf.hello_world_jms.types.ErrorCode;
import org.apache.cxf.hello_world_jms.types.NoSuchCodeLit;
import org.apache.cxf.hello_world_jms.types.TestRpcLitFaultResponse;
import org.apache.cxf.transport.jms.JMSConstants;
import org.apache.cxf.transports.jms.context.JMSMessageHeadersType;



@WebService(serviceName = "HelloWorldService", 
            portName = "HelloWorldPort",
            endpointInterface = "org.apache.cxf.hello_world_jms.HelloWorldPortType",
            targetNamespace = "http://cxf.apache.org/hello_world_jms")
public class GreeterImplTwoWayJMS implements HelloWorldPortType {
    @Resource
    protected WebServiceContext wsContext;
    public String greetMe(String me) {
        MessageContext mc = wsContext.getMessageContext();
        JMSMessageHeadersType headers =
            (JMSMessageHeadersType) mc.get(JMSConstants.JMS_SERVER_HEADERS);
        System.out.println("get the message headers JMSCorrelationID" + headers.getJMSCorrelationID());
        System.out.println("Reached here :" + me);
        return "Hello " + me;
    }

    public String sayHi() {        
        return "Bonjour";
    }
    
    public void greetMeOneWay(String requestType) {
        System.out.println("*********  greetMeOneWay: " + requestType);
    }
    
    public TestRpcLitFaultResponse testRpcLitFault(String faultType) 
        throws BadRecordLitFault, NoSuchCodeLitFault {
        if (faultType.equals(BadRecordLitFault.class.getSimpleName())) {
            throw new BadRecordLitFault("TestBadRecordLit", "BadRecordLitFault");
        }
        if (faultType.equals(NoSuchCodeLitFault.class.getSimpleName())) {
            ErrorCode ec = new ErrorCode();
            ec.setMajor((short)1);
            ec.setMinor((short)1);
            NoSuchCodeLit nscl = new NoSuchCodeLit();
            nscl.setCode(ec);
            throw new NoSuchCodeLitFault("TestNoSuchCodeLit", nscl);
        }
        
        return new TestRpcLitFaultResponse();
    }
    
    
}
