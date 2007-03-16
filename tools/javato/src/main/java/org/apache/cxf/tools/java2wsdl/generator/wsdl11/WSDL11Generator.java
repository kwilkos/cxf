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

import java.io.*;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;

import org.apache.cxf.tools.common.ToolException;
import org.apache.cxf.tools.java2wsdl.generator.AbstractGenerator;
import org.apache.cxf.wsdl11.ServiceWSDLBuilder;

public class WSDL11Generator extends AbstractGenerator<Definition> {

    public Definition generate(File file) {
        createOutputDir(file);
        Definition def = null;
        try {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
            WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
            def = new ServiceWSDLBuilder(getServiceModel()).build();
            wsdlWriter.writeWSDL(def, os);
        } catch (WSDLException wex) {
            wex.printStackTrace();
        } catch (FileNotFoundException fnfe) {
            throw new ToolException("Output file " + file + " not found", fnfe);
        }
        return def;
    }
}

