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

package org.apache.cxf.transport.servlet;

import java.io.InputStream;

import javax.naming.InitialContext;
import javax.naming.NamingException;


import org.apache.cxf.resource.ResourceResolver;


public class ServletContextResourceResolver implements ResourceResolver {

    public final InputStream getAsStream(final String string) {
        return null;
    }

    public final <T> T resolve(final String entryName, final Class<T> clz) {
        Object obj = null;
        try {
            InitialContext ic = new InitialContext();
            obj = ic.lookup(entryName);
        } catch (NamingException e) {
            //do nothing
        }
        
        if (obj != null && obj.getClass().isAssignableFrom(clz)) {
            return clz.cast(obj);
        }
        return null;
    }
}
