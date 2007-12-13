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

package org.apache.cxf.jaxrs.interceptor;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.jaxrs.JAXRSServiceImpl;
import org.apache.cxf.jaxrs.JAXRSUtils;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.jaxrs.model.OperationResourceInfo;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.Service;

public class JAXRSInInterceptor extends AbstractPhaseInterceptor<Message> {

    public static final String RELATIVE_PATH = "relative.path";

    //private static final Logger LOG = Logger.getLogger(RESTDispatchInterceptor.class.getName());
    //private static final ResourceBundle BUNDLE = BundleUtils.getBundle(RESTDispatchInterceptor.class);

    public JAXRSInInterceptor() {
        super(Phase.PRE_STREAM);
    }

    public void handleMessage(Message message) {
        String path = (String)message.get(Message.PATH_INFO);
        String address = (String)message.get(Message.BASE_PATH);
        String httpMethod = (String)message.get(Message.HTTP_REQUEST_METHOD);

        if (address.startsWith("http")) {
            int idx = address.indexOf('/', 7);
            if (idx != -1) {
                address = address.substring(idx);
            }
        }

        if (path.startsWith(address)) {
            path = path.substring(address.length());
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
        }

        if (!path.endsWith("/")) {
            //path = path.substring(0, path.length() - 1);
            path = path + "/";
        }
        message.put(RELATIVE_PATH, path);

        //1. Matching target resource classes and method
        Service service = message.getExchange().get(Service.class);
        List<ClassResourceInfo> resources = ((JAXRSServiceImpl)service).getClassResourceInfos();

        Map<String, String> values = new HashMap<String, String>();
        OperationResourceInfo ori = JAXRSUtils.findTargetResourceClass(resources, path, httpMethod, values);

        if (ori == null) {
            //throw new Fault(new org.apache.cxf.common.i18n.Message("NO_OP", BUNDLE, method, path));
        }
        message.getExchange().put(OperationResourceInfo.class, ori);

        //2. Process parameters
        InputStream is = message.getContent(InputStream.class);
        List<Object> params = JAXRSUtils.processParameters(ori.getMethod(), path, httpMethod, values, is);

        message.setContent(List.class, params);

    }

}
