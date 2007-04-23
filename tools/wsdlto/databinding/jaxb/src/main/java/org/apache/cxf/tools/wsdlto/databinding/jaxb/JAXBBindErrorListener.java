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

package org.apache.cxf.tools.wsdlto.databinding.jaxb;
import com.sun.tools.xjc.api.ErrorListener;

import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.common.ToolException;



public class JAXBBindErrorListener implements ErrorListener {
    private ToolContext env;

    public JAXBBindErrorListener(ToolContext penv) {
        env = penv;
    }

    public void error(org.xml.sax.SAXParseException exception) {
        throw new ToolException(exception.getLocalizedMessage());

    }

    public void fatalError(org.xml.sax.SAXParseException exception) {
        throw new ToolException(exception.getLocalizedMessage());
    }

    public void info(org.xml.sax.SAXParseException exception) {

    }

    public void warning(org.xml.sax.SAXParseException exception) {
        if (this.env.isVerbose()) {
            System.err.println("Parsing schema warning " + exception.toString());
        }
    }
}
