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

package org.apache.cxf.jaxb;

import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.cxf.databinding.DataBindingContext;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;

public class JAXBDataBindingContext implements DataBindingContext {

    
    Map<BindingInfo, Map<BindingOperationInfo, Map<QName, Class>>> context;
    
    public JAXBDataBindingContext(Map<BindingInfo, Map<BindingOperationInfo, Map<QName, Class>>> pContext) {
        this.context = pContext;
    }
    
    public Map getBindingContext(BindingInfo bindingInfo) {
        if (bindingInfo == null) {
            throw new RuntimeException("Input bindingInfo param is null");
        }
        // TODO Auto-generated method stub
        if (context != null) {
            return context.get(bindingInfo);
        }
        return null;
    }

    public Map getOperationContext(BindingInfo bindingInfo, BindingOperationInfo bindingOperationInfo) {
        // TODO Auto-generated method stub
        if (bindingInfo == null) {
            throw new RuntimeException("Input bindingInfo param is null");
        }
        if (bindingOperationInfo == null) {
            throw new RuntimeException("Input bindingOperationInfo param is null");
        }
        Map bindingMap = getBindingContext(bindingInfo);
        if (bindingMap != null) {
            return (Map) bindingMap.get(bindingOperationInfo);
        }
        return null;
    }

    public Map getServiceContext() {
        // TODO Auto-generated method stub
        return context;
    }

}
