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

package org.apache.cxf.tools.wsdlto.frontend.jaxws.generators;

import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.tools.common.FrontEndGenerator;
import org.apache.cxf.tools.common.FrontEndGeneratorsProfile;

/**
 * Generates a service interface, service stub, and port accessor.
 *
 */
public class JAXWSProfile implements FrontEndGeneratorsProfile {
    public List<FrontEndGenerator> getPlugins() {
        List<FrontEndGenerator> plugins = new ArrayList<FrontEndGenerator>();

        plugins.add(new SEIGenerator());
        plugins.add(new FaultGenerator());
        plugins.add(new ServerGenerator());
        plugins.add(new ImplGenerator());
        plugins.add(new ClientGenerator());
        plugins.add(new ServiceGenerator());
        plugins.add(new AntGenerator());

        return plugins;
    }
}
