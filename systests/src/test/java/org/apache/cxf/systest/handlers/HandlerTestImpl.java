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
package org.apache.cxf.systest.handlers;


import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
//import javax.jws.HandlerChain;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.cxf.helpers.CastUtils;
import org.apache.handler_test.HandlerTest;
import org.apache.handler_test.PingException;
import org.apache.handler_test.types.PingFaultDetails;

@WebService(serviceName = "HandlerTestService",
            portName = "SoapPort",
            endpointInterface = "org.apache.handler_test.HandlerTest",
            targetNamespace = "http://apache.org/handler_test")
@HandlerChain(file = "./handlers_invocation.xml", name = "TestHandlerChain")
public class HandlerTestImpl implements HandlerTest {

    private WebServiceContext context;

    public final List<String> ping() {

        try {
            List<String> handlerInfoList = getHandlersInfo(context.getMessageContext());
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
        //ret.addAll(getHandlersInfo(context.getMessageContext()));

        if (handlerCommand.contains("throw exception")) {
            PingFaultDetails details = new PingFaultDetails();
            details.setDetail(ret.toString());
            throw new PingException("from servant", details);
        }

        return ret;
    }


    @Resource
    public void setWebServiceContext(WebServiceContext ctx) {
        context = ctx;
    }

    private List<String> getHandlersInfo(MessageContext ctx) {
        List<String> ret = CastUtils.cast((List)ctx.get("handler.info"));
        if (ret == null) {
            ret = new ArrayList<String>();
        }
        return ret;
    }

}
