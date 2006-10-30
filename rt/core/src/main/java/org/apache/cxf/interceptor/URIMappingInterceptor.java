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

package org.apache.cxf.interceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.cxf.common.i18n.BundleUtils;
import org.apache.cxf.common.util.PrimitiveUtils;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.MessageInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.ServiceModelUtil;

public class URIMappingInterceptor extends AbstractInDatabindingInterceptor {
    
    private static final Logger LOG = Logger.getLogger(URIMappingInterceptor.class.getName());
    private static final ResourceBundle BUNDLE = BundleUtils.getBundle(URIMappingInterceptor.class);
    
    public URIMappingInterceptor() {
        super();
        setPhase(Phase.UNMARSHAL);
    }

    public void handleMessage(Message message) throws Fault {
        String method = (String)message.get(Message.HTTP_REQUEST_METHOD);
        LOG.info("Invoking HTTP method " + method);
        BindingOperationInfo op = message.getExchange().get(BindingOperationInfo.class);
        if (!"GET".equalsIgnoreCase(method)) {
            return;
        }
        if (op != null) {
            return;
        }
        String opName = getOperationName(message);
        LOG.info("URIMappingInterceptor get operation: " + opName);
        op = ServiceModelUtil.getOperation(message.getExchange(), opName);
        
        if (op == null || opName == null || op.getName() == null
            || StringUtils.isEmpty(op.getName().getLocalPart())
            || !opName.equals(op.getName().getLocalPart())) {
            throw new Fault(new org.apache.cxf.common.i18n.Message("NO_OPERATION", BUNDLE, opName));
        }
        message.getExchange().put(BindingOperationInfo.class, op);
        message.setContent(List.class, getParameters(message, op));
    }

    protected List<Object> getParameters(Message message, BindingOperationInfo operation) {
        List<Object> parameters = new ArrayList<Object>();
        MessageInfo msg = operation.getOperationInfo().getInput();
        int idx = parameters.size();

        Map<String, String> queries = getQueries(message);
        for (String key : queries.keySet()) {
            MessagePartInfo p = msg.getMessageParts().get(idx);
            if (p == null) {
                LOG.warning("URIMappingInterceptor MessagePartInfo NULL ");
                throw new Fault(new org.apache.cxf.common.i18n.Message("NO_PART_FOUND", BUNDLE, 
                                                                       "index: " + idx + " on key " + key));
            }

            // TODO check the parameter name here
            Object param = null;
            Class type = (Class)p.getProperty(Class.class.getName());
            
            if (type != null && type.isPrimitive()) {
                param = PrimitiveUtils.read(queries.get(key), type);
            } else {
                param = queries.get(key);
            }
            if (param != null) {
                parameters.add(param);
            } else {
                throw new RuntimeException(p.getName() + " can not be unmarshalled");
            }
        }        
        return parameters;
    }

    protected Map<String, String> getQueries(Message message) {
        Map<String, String> queries = new LinkedHashMap<String, String>();
        String query = (String)message.get(Message.QUERY_STRING);   
        if (!StringUtils.isEmpty(query)) {
            List<String> parts = Arrays.asList(query.split("&"));
            for (String part : parts) {
                String[] keyValue = part.split("=");
                queries.put(keyValue[0], keyValue[1]);
            }
            return queries;
        }

        String path = (String)message.get(Message.PATH_INFO);
        String basePath = getBasePath(message);
        List<String> parts = Arrays.asList(path.split("/"));
        int baseIndex = parts.indexOf(basePath);
        if (baseIndex + 2 > parts.size()) {
            return null;
        }
        for (int i = baseIndex + 2; i < parts.size(); i += 2) {
            if (i + 1 > parts.size()) {
                queries.put(parts.get(i), null);
            }
            queries.put(parts.get(i), parts.get(i + 1));
        }
        return queries;
    }
    
    private String getBasePath(Message message) {
        String basePath = (String)message.get(Message.BASE_PATH);     
        return StringUtils.trim(basePath, "/");
    }

    protected String getOperationName(Message message) {
        String path = (String)message.get(Message.PATH_INFO);

        String basePath = getBasePath(message);

        List<String> parts = Arrays.asList(path.split("/"));

        int baseIndex = parts.indexOf(basePath);
        if (baseIndex + 1 > parts.size()) {
            return null;
        }
        String opName = parts.get(baseIndex + 1);
        if (opName.indexOf("?") != -1) {
            opName = opName.split("\\?")[0];
        }

        return opName;
    }
}
