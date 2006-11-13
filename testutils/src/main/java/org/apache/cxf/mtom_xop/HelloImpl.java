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

package org.apache.cxf.mtom_xop;

import javax.activation.DataHandler;
import javax.jws.WebService;
import javax.xml.ws.Holder;

import org.apache.cxf.mime.Hello;

@WebService(serviceName = "HelloService",
                portName = "HelloPort",
                endpointInterface = "org.apache.cxf.mime.Hello",
                targetNamespace = "http://cxf.apache.org/mime")

public class HelloImpl implements Hello {

    public DataHandler claimForm(DataHandler data) {
        return data;
    }

    public void detail(Holder<String> name, Holder<byte[]> attachinfo) {
        name.value = "return detail + " + name.value;        
    }

    public void echoData(String body, Holder<byte[]> data) {        
        String echo = new String("echo!");
        byte[] returnData = new byte[data.value.length + 10];
        System.arraycopy(data.value, 0, returnData, 0, data.value.length);
        System.arraycopy(echo.getBytes(), 0, returnData, data.value.length, echo.getBytes().length);
        data.value = returnData;        
    }

    public void echoDataWithEnableMIMEContent(String body, Holder<byte[]> data) {
        System.out.println(body);
    }

}
