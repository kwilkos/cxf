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
package org.apache.cxf.jca.servant;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;

/**
 * The base class for all <code>Servant</code> implementations.
 * A <code>Servant</code> defines the endpoint of an invocation
 * and is usually used to wrap an instance of a Java class.
 */
public abstract class AbstractServant implements InvocationHandler {
    private final String wsdlLocation;
    private final Bus theBus;

    /**
     * Constructs a new <code>Servant</code> with the specified WSDL and Bus.
     * @param wsdlLoc A string containing the location of the wsdl file.
     * @param bus The <code>Bus</code> used to register this servant.
     */
    public AbstractServant(String wsdlLoc, Bus bus) {
        wsdlLocation = wsdlLoc;
        theBus = bus;
    }

    /**
     * The method to obtain a reference to the instance
     * that will be used for an invocation.
     * @return Object The target of the invocation.
     * @throws BusException If there is an error getting the target object.
     */
    public abstract Object getTargetObject() throws BusException;


    /**
     * Defines the actual invocation methodology.
     * @param target The <code>Object</code> that is the target object of the invocation.
     * @param method A <code>Method</code> representing the method being invoked.
     * @param args An <code>Object[]</code> representing the invocation parameters.
     * @return Object The return value of the target invocation.
     * @throws BusException If there is an error invoking the specified method.
     */
    public abstract Object invoke(Object target, Method method, Object args[]) throws Throwable;


    /**
     * Return the location of the WSDL file describing the service this
     * servant implements.
     *
     * @return String A string containing the location of the WSDL file
     * used by this <code>Servant</code>.
     */
    public String getWSDLLocation() {
        return wsdlLocation;
    }


    /**
     * The <code>Bus</code> ultimately used to register the <code>Servant</code>.
     * @return Bus The <code>Bus</code> used to register this servant.
     */
    public Bus getBus() {
        return theBus;
    }
}
