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

package org.apache.cxf.tools.java2wsdl.generator.wsdl11;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.XJC;
import com.sun.tools.xjc.api.impl.s2j.SchemaCompilerImpl;

import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.SchemaInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.model.JavaClass;
import org.apache.cxf.tools.java2wsdl.generator.AbstractGenerator;
import org.apache.cxf.tools.java2wsdl.processor.internal.jaxws.RequestWrapper;
import org.apache.cxf.tools.java2wsdl.processor.internal.jaxws.ResponseWrapper;
import org.apache.cxf.tools.util.JAXBUtils;
import org.apache.cxf.tools.wsdlto.databinding.jaxb.JAXBBindErrorListener;
import org.apache.cxf.tools.wsdlto.databinding.jaxb.TypesCodeWriter;


public final class WrapperBeanGenerator extends AbstractGenerator<File> {

    private Class<?> serviceClass;
    
    public File generate(final File sourcedir) {
        File dir = getOutputBase();
        if (dir == null) {
            dir = sourcedir;
        }
        if (dir == null) {
            dir = new File("./");
        }
        generateWrapperBeanClasses(getServiceModel(), dir);
        return sourcedir;
    }


    public Class<?> getServiceClass() {
        return this.serviceClass;
    }
    
    public void setServiceClass(Class<?> clz) {
        this.serviceClass = clz;
    }
    
    private List<InputSource> getExternalSchemaBindings(final Map<String, String> mapping) {
        List<InputSource> externalSchemaBinding = new ArrayList<InputSource>();

        for (String ns : mapping.keySet()) {
            File file = JAXBUtils.getPackageMappingSchemaBindingFile(ns, mapping.get(ns));
            externalSchemaBinding.add(new InputSource(file.toURI().toString()));
        }
        return externalSchemaBinding;
    }
    
    private void generateWrapperBeanClasses(final ServiceInfo serviceInfo, final File dir) {
        List<JavaClass> wrapperClasses = new ArrayList<JavaClass>();
        Map<String, String> nsPkgMapping = new HashMap<String, String>();
        
        for (OperationInfo op : serviceInfo.getInterface().getOperations()) {
            if (op.getUnwrappedOperation() != null) {
                if (op.hasInput()) {
                    QName wrapperBeanName = op.getInput().getMessageParts().get(0).getElementQName();
                    
                    RequestWrapper requestWrapper = new RequestWrapper();
                    requestWrapper.setName(wrapperBeanName);
                    requestWrapper.setMethod((Method) op.getProperty(Method.class.getName()));
                    JavaClass jClass = requestWrapper.getJavaClass();

                    if (requestWrapper.isWrapperAbsent()) {
                        nsPkgMapping.put(wrapperBeanName.getNamespaceURI(), jClass.getPackageName());
                    }

                    if (requestWrapper.isWrapperBeanClassNotExist()) {
                        wrapperClasses.add(jClass);
                    }
                }
                if (op.hasOutput()) {
                    QName wrapperBeanName = op.getOutput().getMessageParts().get(0).getElementQName();
                    ResponseWrapper responseWrapper = new ResponseWrapper();
                    responseWrapper.setName(wrapperBeanName);
                    responseWrapper.setMethod((Method) op.getProperty(Method.class.getName()));
                    JavaClass jClass = responseWrapper.getJavaClass();

                    if (responseWrapper.isWrapperAbsent()) {
                        nsPkgMapping.put(wrapperBeanName.getNamespaceURI(), jClass.getPackageName());
                    }

                    if (responseWrapper.isWrapperBeanClassNotExist()) {
                        wrapperClasses.add(jClass);
                    }
                }
            }
        }

        if (wrapperClasses.isEmpty()) {
            return;
        }

        
        Map<String, Element> schemas = new HashMap<String, Element>();
        for (SchemaInfo s : serviceInfo.getSchemas()) {
            schemas.put(s.getSchema().getTargetNamespace(), s.getElement());
        }

        SchemaCompilerImpl schemaCompiler = (SchemaCompilerImpl)XJC.createSchemaCompiler();
        JAXBBindErrorListener listener = new JAXBBindErrorListener(false);
        schemaCompiler.setErrorListener(listener);

        Set<String> keys = schemas.keySet();
        for (String key : keys) {
            schemaCompiler.parseSchema(key, schemas.get(key));
        }
        for (InputSource is : getExternalSchemaBindings(nsPkgMapping)) {
            schemaCompiler.parseSchema(is);
        }

        S2JJAXBModel rawJaxbModelGenCode = schemaCompiler.bind();
        JCodeModel jcodeModel = rawJaxbModelGenCode.generateCode(null, null);

        JCodeModelFilter filter = new JCodeModelFilter(jcodeModel);
        filter.include(wrapperClasses);
        
        try {
            createOutputDir(dir);
            jcodeModel.build(new TypesCodeWriter(dir, new ArrayList<String>()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
