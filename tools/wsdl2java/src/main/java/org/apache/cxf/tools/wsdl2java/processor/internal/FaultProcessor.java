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

package org.apache.cxf.tools.wsdl2java.processor.internal;

import java.util.Collection;
import java.util.Map;

import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Part;

import org.apache.cxf.tools.common.ProcessorEnvironment;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.model.JavaException;
import org.apache.cxf.tools.common.model.JavaExceptionClass;
import org.apache.cxf.tools.common.model.JavaField;
import org.apache.cxf.tools.common.model.JavaMethod;
import org.apache.cxf.tools.common.model.JavaModel;
import org.apache.cxf.tools.util.ProcessorUtil;

public class FaultProcessor extends AbstractProcessor {

   
    public FaultProcessor(ProcessorEnvironment penv) {
      super(penv);
    }

    public void process(JavaMethod method, Map<String, Fault> faults) throws ToolException {
        if (faults == null) {
            return;
        }

        //Collection<Fault> faultsValue = faults.values();
        java.util.Iterator<Fault> ite = faults.values().iterator();
        while (ite.hasNext()) {
            Fault fault = ite.next();
            processFault(method, fault);
        }
        /*for (Fault fault : faultsValue) {
            processFault(method, fault);
        }*/
    }

    private boolean isNameCollision(String packageName, String className) {  
        boolean collision = collector.containTypesClass(packageName, className)
            || collector.containSeiClass(packageName, className);
        return collision;
    }

    @SuppressWarnings("unchecked")
    private void processFault(JavaMethod method, Fault fault) throws ToolException {
        JavaModel model = method.getInterface().getJavaModel();
        Message faultMessage = fault.getMessage();
        String name = ProcessorUtil.mangleNameToClassName(faultMessage.getQName().getLocalPart());
        //Fix issue 305770
        String namespace = faultMessage.getQName().getNamespaceURI();
        //String namespace = method.getInterface().getNamespace();
        String packageName = ProcessorUtil.parsePackageName(namespace, env.mapPackageName(namespace));

        while (isNameCollision(packageName, name)) {
            name = name + "_Exception";
        }
        
        String fullClassName = packageName + "." + name;
        collector.addExceptionClassName(packageName, name, fullClassName);        

        boolean samePackage = method.getInterface().getPackageName().equals(packageName);
        method.addException(new JavaException(name, samePackage ? name : fullClassName, namespace));
        
        Map<String, Part> faultParts = faultMessage.getParts();
        Collection<Part> faultValues = faultParts.values();
        
        JavaExceptionClass expClass = new JavaExceptionClass(model);
        expClass.setName(name);
        expClass.setNamespace(namespace);
        expClass.setPackageName(packageName);
      
        for (Part part : faultValues) {
            String fName;
            String fNamespace;
            
            if (part.getElementName() != null) {
                fName = part.getElementName().getLocalPart();               
                fNamespace = part.getElementName().getNamespaceURI();
               
                /*
                 * org.apache.cxf.common.i18n.Message msg = new
                 * org.apache.cxf.common.i18n.Message("WSDL_FAULT_MSG_PART_ELEMENT_MISSING_ERROR",
                 * LOG, faultMessage, part.getName()); throw new
                 * ToolException(msg);
                 */

            } else {
                fName = part.getName();
                fNamespace = part.getTypeName().getNamespaceURI();

            }
            
            String fType = ProcessorUtil.getType(part, env, false);
            String fPackageName = ProcessorUtil.parsePackageName(fNamespace, env
                                                                 .mapPackageName(fNamespace));
            
            

            JavaField fField = new JavaField(fName, fType, fNamespace);
            fField.setQName(ProcessorUtil.getElementName(part));
            
            if (!method.getInterface().getPackageName().equals(fPackageName)) {
                fField.setClassName(ProcessorUtil.getFullClzName(part, 
                                                                 env, this.collector, false));                
            }
            if (!fType.equals(ProcessorUtil.resolvePartType(part))) {
                fField.setClassName(ProcessorUtil.getType(part, env, true));
            }

            expClass.addField(fField);
        }
        model.addExceptionClass(packageName + "." + name, expClass);
    }
}
