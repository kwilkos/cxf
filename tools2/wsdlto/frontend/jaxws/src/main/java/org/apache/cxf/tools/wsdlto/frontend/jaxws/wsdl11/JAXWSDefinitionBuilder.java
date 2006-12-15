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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.wsdl11;

import javax.wsdl.Definition;

import org.apache.cxf.tools.common.extensions.jaxws.CustomizationParser;
import org.apache.cxf.tools.wsdlto.core.AbstractWSDLBuilder;
import org.apache.cxf.wsdl.WSDLBuilder;
import org.apache.cxf.wsdl11.WSDLDefinitionBuilder;

public class JAXWSDefinitionBuilder extends AbstractWSDLBuilder<Definition> {

    public Definition build(String wsdlURL) {
        WSDLBuilder<Definition> builder = new WSDLDefinitionBuilder();
        return builder.build(wsdlURL);
    }
    
    public void customize(Definition t) {
        CustomizationParser customizationParser = CustomizationParser.getInstance();
        customizationParser.clean();
        customizationParser.parse(context, t);
    }
}
