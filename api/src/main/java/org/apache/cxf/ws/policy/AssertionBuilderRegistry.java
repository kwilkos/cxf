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

package org.apache.cxf.ws.policy;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.cxf.extension.Registry;
import org.apache.neethi.Assertion;


/**
 * AssertionBuilderRegistry is used to manage AssertionBuilders and
 * create Assertion objects from given xml elements.
 */
public interface AssertionBuilderRegistry extends Registry<QName, AssertionBuilder> {
    
    /**
     * Returns an assertion that is built using the specified xml element.
     * 
     * @param element the element from which to build an Assertion.
     * @return an Assertion that is built using the specified element.
     */
    Assertion build(Element element);
}
