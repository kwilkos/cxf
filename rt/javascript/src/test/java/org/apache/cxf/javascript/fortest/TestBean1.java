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

package org.apache.cxf.javascript.fortest;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Bean with a selection of elements suitable for testing the JavaScript client.
 */
@XmlRootElement
@XmlType(namespace = "uri:org.apache.cxf.javascript.testns")
public class TestBean1 {
    //CHECKSTYLE:OFF
    public String stringItem;
    @XmlElement(namespace = "uri:org.apache.cxf.javascript.testns2")
    public int intItem;
    @XmlElement(defaultValue = "43")
    public long longItem;
    public byte[] base64Item;
    @XmlElement(required = false)
    public int optionalIntItem;
    @XmlElement(defaultValue = "trip", required = false, namespace = "uri:org.apache.cxf.javascript.testns2")
    public String optionalStringItem;
    @XmlElement(required = false)
    public int[] optionalIntArrayItem;
    
    //CHECKSTYLE:ON
}
