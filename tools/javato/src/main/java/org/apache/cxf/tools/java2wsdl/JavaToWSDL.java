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

package org.apache.cxf.tools.java2wsdl;

import java.util.HashSet;

import org.apache.cxf.common.i18n.Message;
import org.apache.cxf.tools.common.AbstractCXFToolContainer;
import org.apache.cxf.tools.common.Processor;
import org.apache.cxf.tools.common.ToolConstants;
import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.common.toolspec.ToolRunner;
import org.apache.cxf.tools.common.toolspec.ToolSpec;
import org.apache.cxf.tools.common.toolspec.parser.BadUsageException;
import org.apache.cxf.tools.common.toolspec.parser.ErrorVisitor;
import org.apache.cxf.tools.java2wsdl.processor.JavaToProcessor;
import org.apache.cxf.tools.util.AnnotationUtil;

public class JavaToWSDL extends AbstractCXFToolContainer {
   
    private static final String TOOL_NAME = "java2wsdl";

    public JavaToWSDL(ToolSpec toolspec) throws Exception {
        super(TOOL_NAME, toolspec);
    }

    public void execute(boolean exitOnFinish) throws ToolException {
        Processor processor = null;

        try {
            super.execute(exitOnFinish);
            if (!hasInfoOption()) {
                ToolContext env = new ToolContext();
                env.setParameters(getParametersMap(new HashSet()));
                if (isVerboseOn()) {
                    env.put(ToolConstants.CFG_VERBOSE, Boolean.TRUE);
                }

                processor = new JavaToProcessor();

                processor.setEnvironment(env);
                processor.process();
            }
        } catch (ToolException ex) {            
            if (ex.getCause() instanceof BadUsageException) {
                printUsageException(TOOL_NAME, (BadUsageException)ex.getCause());
                if (isVerboseOn()) {
                    ex.printStackTrace();
                }
            }
            throw ex;
        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            if (isVerboseOn()) {
                ex.printStackTrace();
            }
            
            throw new ToolException(ex.getMessage(), ex.getCause());
        }
    }

    public Class getServiceClass(ToolContext context) {
        return AnnotationUtil.loadClass((String)context.get(ToolConstants.CFG_CLASSNAME),
                                        getClass().getClassLoader());
    }

    public static void main(String[] pargs) { 
        try {
            runTool(pargs);
        } catch (Exception ex) {
            System.err.println("Error : " + ex.getMessage());
            System.err.println();
            ex.printStackTrace();
        }
    }
    
    public static void runTool(String[] pargs) throws Exception {
        ToolRunner.runTool(JavaToWSDL.class, JavaToWSDL.class
                .getResourceAsStream("java2wsdl.xml"), false, pargs);
    }

    public void checkParams(ErrorVisitor errors) throws ToolException {
        if (errors.getErrors().size() > 0) {
            Message msg = new Message("PARAMETER_MISSSING", LOG);
            throw new ToolException(msg, new BadUsageException(getUsage(), errors));
        }
    }
}
