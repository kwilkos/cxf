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

package org.apache.cxf.cxf1220.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.cxf.cxf1220.useenum.RefEnumInAttribute;

/**
 * This object lives in it's own namespace to model the structure of the
 * original report.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OrderType")
public class Bean {
    @XmlElement(name = "REF_ENUM", 
                namespace = "urn:oasis:names:draft:ubl:schema:xsd:CommonBasicComponents-2", 
                required = true)
    protected RefEnumInAttribute refEnum;

}
