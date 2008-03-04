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

package org.apache.cxf.jaxrs.provider;

import java.text.ParseException;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.ext.HeaderProvider;
import javax.ws.rs.ext.Provider;

@Provider
public class EntityTagHeaderProvider implements HeaderProvider<EntityTag> {

    private static final String WEAK_PREFIX = "W/";
    
    public EntityTag fromString(String header) throws ParseException {
        int i = header.indexOf(WEAK_PREFIX);
        if (i != -1) {
            if (i + 2 < header.length()) {
                return new EntityTag(header.substring(i + 2), true);
            } else {
                return new EntityTag("", true);
            }
        } 
        
        return new EntityTag(header);
    }

    public boolean supports(Class type) {
        return EntityTag.class.isAssignableFrom(type);
    }

    public String toString(EntityTag tag) {
        StringBuilder sb = new StringBuilder();
        if (tag.isWeak()) {
            sb.append(WEAK_PREFIX);
        }
        sb.append(tag.getValue());
        return sb.toString();
    }

}
