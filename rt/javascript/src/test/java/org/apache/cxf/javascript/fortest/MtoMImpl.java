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

package org.apache.cxf.javascript.fortest;

import javax.jws.WebService;
import javax.xml.ws.soap.MTOM;

/**
 * 
 */
@org.apache.cxf.feature.Features(features = "org.apache.cxf.feature.LoggingFeature")   
@WebService(targetNamespace = "uri:org.apache.cxf.javascript.fortest")
@MTOM
public class MtoMImpl implements MtoM {
    
    private MtoMParameterBeanNoDataHandler lastBean;
    private MtoMParameterBeanWithDataHandler lastDHBean;

    public void reset() {
        lastBean = null;
        lastDHBean = null;
    }

    public MtoMParameterBeanNoDataHandler getLastBean() {
        return lastBean;
    }

    public void receiveNonXmlDH(MtoMParameterBeanNoDataHandler param) {
        lastBean = param;
    }

    public MtoMParameterBeanWithDataHandler getLastDHBean() {
        return lastDHBean;
    }

    public void receiveNonXmlDH(MtoMParameterBeanWithDataHandler param) {
        lastDHBean = param;
    }

    public void receiveNonXmlNoDH(MtoMParameterBeanNoDataHandler param) {
        lastBean = param;
    }
}
