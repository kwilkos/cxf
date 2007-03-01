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
package org.apache.cxf.endpoint.dynamic;

import java.util.logging.Logger;

import javax.xml.namespace.QName;

import com.sun.tools.xjc.api.Mapping;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.TypeAndAnnotation;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.service.ServiceModelVisitor;
import org.apache.cxf.service.factory.ServiceConstructionException;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;

public class TypeClassInitializer extends ServiceModelVisitor {
    private static final Logger LOG = Logger.getLogger(TypeClassInitializer.class.getName());
    
    S2JJAXBModel model;
    
    public TypeClassInitializer(ServiceInfo serviceInfo, S2JJAXBModel model) {
        super(serviceInfo);
        this.model = model;
    }

    @Override
    public void begin(MessagePartInfo part) {
        OperationInfo op = part.getMessageInfo().getOperation();
        if (op.isUnwrappedCapable() && !op.isUnwrapped()) {
            return;
        }
        
        QName name;
        if (part.isElement()) {
            name = part.getElementQName();
        } else {
            name = part.getTypeQName();
        }
        Mapping mapping = model.get(name);
        
        String clsName = null;
        if (mapping != null) {
            clsName = mapping.getType().getTypeClass().fullName();
        }
        
        if (clsName == null) {
            TypeAndAnnotation javaType = model.getJavaType(part.getTypeQName());
            
            if (javaType != null) {
                clsName = javaType.getTypeClass().fullName();
            }
        }
        
        if (clsName == null) {
            throw new ServiceConstructionException(new Message("NO_JAXB_CLASS", LOG, name));
        }
            
        Class cls;
        try {
            cls = ClassLoaderUtils.loadClass(clsName, getClass());
        } catch (ClassNotFoundException e) {
            throw new ServiceConstructionException(e);
        }
        
        part.setTypeClass(cls);
        
        super.begin(part);
    }

}
