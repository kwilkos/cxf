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
package org.apache.cxf.tools.java2wsdl.processor.internal.simple;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.service.factory.ReflectionServiceFactoryBean;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaException;
import org.apache.cxf.tools.common.model.JavaInterface;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.common.model.JavaParameter;
import org.apache.cxf.tools.common.model.JavaReturn;
import org.apache.cxf.tools.common.model.JavaType.Style;
import org.apache.cxf.tools.java2wsdl.processor.internal.simple.generator.AbstractSimpleGenerator;
import org.apache.cxf.tools.java2wsdl.processor.internal.simple.generator.SimpleClientGenerator;
import org.apache.cxf.tools.java2wsdl.processor.internal.simple.generator.SimpleImplGenerator;
import org.apache.cxf.tools.java2wsdl.processor.internal.simple.generator.SimpleSEIGenerator;
import org.apache.cxf.tools.java2wsdl.processor.internal.simple.generator.SimpleServerGenerator;

public class SimpleFrontEndProcessor implements Processor {
    private ToolContext context;
    private List<AbstractSimpleGenerator> generators = new ArrayList<AbstractSimpleGenerator>();
    @SuppressWarnings("unchecked")
    public void process() throws ToolException {       
        List<ServiceInfo> services = (List<ServiceInfo>)context.get(ToolConstants.SERVICE_LIST);
        ServiceInfo serviceInfo = services.get(0);
        JavaInterface jinf = serviceInfo2JavaInf(serviceInfo);
        JavaModel jm = new JavaModel();
        jm.addInterface("inf", jinf);
        jinf.setJavaModel(jm);
        context.put(JavaModel.class, jm);
        generators.add(new SimpleSEIGenerator());
        generators.add(new SimpleImplGenerator());
        generators.add(new SimpleServerGenerator());
        generators.add(new SimpleClientGenerator());
        
        for (AbstractSimpleGenerator generator : generators) {
            generator.generate(context);
        }

    }
    
    public void setEnvironment(ToolContext env) {
        this.context = env;
    }
    
    public JavaInterface serviceInfo2JavaInf(ServiceInfo service) {
        JavaInterface javaInf = new JavaInterface();
        InterfaceInfo inf = service.getInterface();
        for (OperationInfo op : inf.getOperations()) {
            JavaMethod jm = new JavaMethod();
            Method m = (Method)op.getProperty(ReflectionServiceFactoryBean.METHOD);
            jm.setName(m.getName());
            int i = 0;
            for (Type type : m.getGenericParameterTypes()) {
                JavaParameter jp = new JavaParameter();
                jp.setClassName(getClassName(type));
                jp.setStyle(Style.IN);
                jp.setName("arg" + i++);
                jm.addParameter(jp);
            }
            
            for (Type type : m.getGenericExceptionTypes()) {
                JavaException jex = new JavaException();
                String className = getClassName(type);
                jex.setClassName(className);
                jex.setName(className);
                jm.addException(jex);
            }
            
            JavaReturn jreturn = new JavaReturn();
            jreturn.setClassName(getClassName(m.getGenericReturnType()));
            jreturn.setStyle(Style.OUT);
            jm.setReturn(jreturn);
            
            javaInf.setPackageName(m.getDeclaringClass().getPackage().getName());
            javaInf.addMethod(jm);
            javaInf.setName(inf.getName().getLocalPart());
            
            jm.getParameterList();
            
            
        }
        return javaInf;
    }
    
    
    public  String getClassName(Type type) {
        if (type instanceof Class) {
            Class clz = (Class)type;
            if (clz.isArray()) {
                return clz.getComponentType().getName() + "[]";
            } else {
                return clz.getName();
            }
        } else if (type instanceof ParameterizedType) {
            return type.toString();
        } else if (type instanceof GenericArrayType) {
            return type.toString();
        } 
        
        return "";
    }
   
}
