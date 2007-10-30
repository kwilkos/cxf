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

import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * 
 */
@WebService(targetNamespace = "uri:org.apache.cxf.javascript.fortest")
public interface SimpleDocLitWrappedUnqualified {
    String basicTypeFunctionReturnString(@WebParam(name = "s") String s, 
                                         @WebParam(name = "i") int i, 
                                         @WebParam(name = "l") long l, 
                                         @WebParam(name = "f") float f, 
                                         @WebParam(name = "d") double d);
    int basicTypeFunctionReturnInt(@WebParam(name = "s") String s, 
                                   @WebParam(name = "i") int i, 
                                   @WebParam(name = "l") long l, 
                                   @WebParam(name = "f") float f, 
                                   @WebParam(name = "d") double d);
    void beanFunction(@WebParam(name = "bean1") TestBean1 bean, 
                      @WebParam(name = "beanArray") TestBean1[] beans);
}
