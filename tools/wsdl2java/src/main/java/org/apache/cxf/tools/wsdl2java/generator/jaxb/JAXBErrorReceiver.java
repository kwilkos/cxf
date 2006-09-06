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

package org.apache.cxf.tools.wsdl2java.generator.jaxb;

import com.sun.tools.xjc.ErrorReceiver;

import org.apache.cxf.tools.common.ProcessorEnvironment;

public class JAXBErrorReceiver extends ErrorReceiver {
    private ProcessorEnvironment env;

    public JAXBErrorReceiver(ProcessorEnvironment penv) {
        env = penv;
    }

    public void warning(org.xml.sax.SAXParseException saxEx) throws com.sun.tools.xjc.AbortException {
        if (env.isVerbose()) {
            saxEx.printStackTrace();
        } else {
            System.err.println("Use jaxb customization binding file to generate types warring "
                               + saxEx.getMessage());
        }

    }

    public void error(org.xml.sax.SAXParseException saxEx) throws com.sun.tools.xjc.AbortException {
        if (env.isVerbose()) {
            saxEx.printStackTrace();
        } else {
            System.err.println("Use jaxb customization binding file to generate types error "
                               + saxEx.getMessage());
        }
    }

    public void info(org.xml.sax.SAXParseException saxEx) {
        if (env.isVerbose()) {
            saxEx.printStackTrace();
        }
    }

    public void fatalError(org.xml.sax.SAXParseException saxEx) throws com.sun.tools.xjc.AbortException {
        if (env.isVerbose()) {
            saxEx.printStackTrace();
        } else {
            System.err.println("Use jaxb customization binding file to generate types fatal error "
                               + saxEx.getMessage());
        }
    }
}
