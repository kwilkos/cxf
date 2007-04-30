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
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.tools.common.VelocityGenerator;
import org.apache.cxf.tools.common.model.JavaClass;
import org.apache.cxf.tools.java2wsdl.generator.AbstractGenerator;
import org.apache.cxf.tools.java2wsdl.processor.internal.jaxws.RequestWrapper;
import org.apache.cxf.tools.java2wsdl.processor.internal.jaxws.ResponseWrapper;
import org.apache.cxf.tools.util.Compiler;
import org.apache.cxf.tools.util.FileWriterUtil;

public final class WrapperBeanGenerator extends AbstractGenerator<File> {
    private Class<?> serviceClass;
    private File compileToDir;
    
    public File generate(final File sourcedir) {
        File dir = getOutputBase();
        if (dir == null) {
            dir = sourcedir;
        }
        if (dir == null) {
            dir = new File("./");
        }
        generateWrapperBeanClasses(getServiceModel(), dir);
        return dir;
    }
    
    public void setCompileToDir(File f) {
        compileToDir = f;
    }


    public Class<?> getServiceClass() {
        return this.serviceClass;
    }
    
    public void setServiceClass(Class<?> clz) {
        this.serviceClass = clz;
    }
    
    private void generateWrapperBeanClasses(final ServiceInfo serviceInfo, final File dir) {
        List<JavaClass> wrapperClasses = new ArrayList<JavaClass>();
        
        for (OperationInfo op : serviceInfo.getInterface().getOperations()) {
            if (op.getUnwrappedOperation() != null) {
                if (op.hasInput()) {
                    RequestWrapper requestWrapper = new RequestWrapper();
                    requestWrapper.setOperationInfo(op);
                    JavaClass jClass = requestWrapper.buildWrapperBeanClass();

                    if (requestWrapper.isWrapperBeanClassNotExist()) {
                        wrapperClasses.add(jClass);
                    }
                }
                if (op.hasOutput()) {
                    ResponseWrapper responseWrapper = new ResponseWrapper();
                    responseWrapper.setOperationInfo(op);
                    JavaClass jClass = responseWrapper.buildWrapperBeanClass();

                    if (responseWrapper.isWrapperBeanClassNotExist()) {
                        wrapperClasses.add(jClass);
                    }
                }
            }
        }

        if (wrapperClasses.isEmpty()) {
            return;
        }

        String templateName = "org/apache/cxf/tools/java2wsdl/generator/wsdl11/wrapperbean.vm";
        VelocityGenerator generator = new VelocityGenerator();
        generator.setBaseDir(dir.toString());

        List<File> generatedFiles = new ArrayList<File>();
        try {
            for (JavaClass wrapperClass : wrapperClasses) {
                generator.setCommonAttributes();
                generator.setAttributes("bean", wrapperClass);
            
                File file = generator.parseOutputName(wrapperClass.getPackageName(),
                                                      wrapperClass.getName());
                generatedFiles.add(file);
            
                generator.doWrite(templateName, FileWriterUtil.getWriter(file));
            
                generator.clearAttributes();
            }
        

            if (compileToDir != null) {
                //compile the classes
                Compiler compiler = new Compiler();

                List<String> files = new ArrayList<String>(generatedFiles.size());
                for (File file : generatedFiles) {
                    files.add(file.getAbsolutePath());
                }
                if (!compiler.compileFiles(files.toArray(new String[files.size()]),
                                           compileToDir)) {
                    //TODO - compile issue
                }

            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
