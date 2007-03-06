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

package org.apache.cxf.jaxws.javaee;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

public class WebservicesTypeTest extends TestCase {

    public void testReadWebservicesXml() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(WebservicesType.class);
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("webservices.xml");
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        Object obj =  unmarshaller.unmarshal(in);

        assertTrue("obj is an " + obj.getClass(), obj instanceof WebservicesType);
    }
}
