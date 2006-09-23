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

package org.apache.cxf.jaxws.support;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

import javax.xml.ws.WebFault;

import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.MessagePartInfo;
import org.apache.cxf.service.model.OperationInfo;

public final class JaxWsUtils {

    private JaxWsUtils() {
    }

    /**
     * set the holder generic type info into message part info
     * 
     * @param o
     * @param implMethod
     *            if is null, use seiMethod
     * @param seiMethod
     *            if is null, use implMethod
     */
    public static void setClassInfo(OperationInfo o, Method implMethod, Method seiMethod) {
        Method selected = null;
        if (implMethod == null && seiMethod == null) {
            throw new RuntimeException("Both implementation method and SEI method are null");
        }
        if (o.getOutput() == null) {
            return;
        }
        if (implMethod == null) {
            selected = seiMethod;
        } else {
            selected = implMethod;
        }

        Object[] para = selected.getParameterTypes();
        for (MessagePartInfo mpiOut : o.getOutput().getMessageParts()) {
            int idx = 0;
            boolean isHolder = false;
            MessagePartInfo mpiInHolder = null;
            for (MessagePartInfo mpiIn : o.getInput().getMessageParts()) {
                // check for sayHi() type no input param method
                if (para.length > 0) {
                    mpiIn.setProperty(Class.class.getName(), para[idx]);
                }
                if (mpiOut.getName().equals(mpiIn.getName())) {
                    if (mpiOut.isElement() && mpiIn.isElement()
                                    && mpiOut.getElementQName().equals(mpiIn.getElementQName())) {
                        isHolder = true;
                        mpiInHolder = mpiIn;
                        break;
                    } else if (!mpiOut.isElement() && !mpiIn.isElement()
                                    && mpiOut.getTypeQName().equals(mpiIn.getTypeQName())) {
                        isHolder = true;
                        mpiInHolder = mpiIn;
                        break;
                    }
                }
                idx++;
            }
            if (isHolder) {
                if (selected == seiMethod) {
                    Object[] paraType = selected.getGenericParameterTypes();
                    ParameterizedType paramType = (ParameterizedType) paraType[idx];
                    if (((Class) paramType.getRawType()).getName().equals("javax.xml.ws.Holder")) {
                        mpiOut.setProperty(Class.class.getName(),
                                        (Class) paramType.getActualTypeArguments()[0]);
                        mpiInHolder.setProperty(Class.class.getName(), (Class) paramType
                                        .getActualTypeArguments()[0]);
                    } else {
                        throw new RuntimeException("Expected Holder at " + idx
                                        + " parametor of input message");
                    }
                } else {
                    mpiOut.setProperty(Class.class.getName(), para[idx]);
                    mpiInHolder.setProperty(Class.class.getName(), para[idx]);
                }
                mpiOut.setProperty(JaxWsServiceFactoryBean.HOLDER, Boolean.TRUE);
                mpiInHolder.setProperty(JaxWsServiceFactoryBean.HOLDER, Boolean.TRUE);
            } else {
                mpiOut.setProperty(Class.class.getName(), selected.getReturnType());
            }
        }
        for (FaultInfo fi : o.getFaults()) {
            int i = 0;
            Class<?> cls = selected.getExceptionTypes()[i];
            fi.getMessagePartByIndex(0).setProperty(Class.class.getName(), cls);                
            if (cls.isAnnotationPresent(WebFault.class)) {
                fi.getMessagePartByIndex(i).setProperty(WebFault.class.getName(), Boolean.TRUE);
            }
            i++;
        }
    }
    
}
