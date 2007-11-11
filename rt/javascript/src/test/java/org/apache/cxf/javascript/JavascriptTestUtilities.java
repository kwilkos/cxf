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

package org.apache.cxf.javascript;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.test.TestUtilities;
import org.junit.Assert;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.ScriptableObject;

/**
 * Test utilities class with some Javascript capability included. 
 */
public class JavascriptTestUtilities extends TestUtilities {
    
    private static final Logger LOG = LogUtils.getL7dLogger(JavascriptTestUtilities.class);

    private ScriptableObject rhinoScope;
    private Context rhinoContext;
    
    public static class JavaScriptAssertionFailed extends RuntimeException {

        public JavaScriptAssertionFailed(String what) {
            super(what);
        }
    }
    
    public static class JsAssert extends ScriptableObject {

        public JsAssert() { }
        public void jsConstructor(String exp) {
            LOG.severe("Assertion failed: " + exp);
            throw new JavaScriptAssertionFailed(exp);
        }
        @Override
        public String getClassName() {
            return "Assert";
        }
    }
    
    public static class Trace extends ScriptableObject {

        public Trace() {
        }

        @Override
        public String getClassName() {
            return "org_apache_cxf_trace";
        }
        
        //CHECKSTYLE:OFF
        public static void jsStaticFunction_trace(String message) {
            LOG.fine(message);
        }
        //CHECKSTYLE:ON
        
    }

    public JavascriptTestUtilities(Class<?> classpathReference) {
        super(classpathReference);
    }
    
    public void initializeRhino() {
        rhinoContext = Context.enter();
        rhinoScope = rhinoContext.initStandardObjects();
        try {
            ScriptableObject.defineClass(rhinoScope, JsAssert.class);
            ScriptableObject.defineClass(rhinoScope, Trace.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        JsSimpleDomNode.register(rhinoScope);
        JsSimpleDomParser.register(rhinoScope);
    }
    
    public void readResourceIntoRhino(String resourceClasspath) throws IOException {
        Reader js = getResourceAsReader(resourceClasspath);
        rhinoContext.evaluateReader(rhinoScope, js, resourceClasspath, 1, null);
    }
    
    public void readStringIntoRhino(String js, String sourceName) {
        LOG.fine(sourceName + ":\n" + js);
        rhinoContext.evaluateString(rhinoScope, js, sourceName, 1, null);
    }
    
    public ScriptableObject getRhinoScope() {
        return rhinoScope;
    }

    public Context getRhinoContext() {
        return rhinoContext;
    }
    
    public Object rhinoEvaluate(String jsExpression) {
        return rhinoContext.evaluateString(rhinoScope, jsExpression, "<testcase>", 1, null);
    }
    
    public Object rhinoCall(String functionName, Object ... args) {
        Object fObj = rhinoScope.get(functionName, rhinoScope);
        if (!(fObj instanceof Function)) {
            throw new RuntimeException("Missing test function " + functionName);
        }
        Function function = (Function)fObj;
        try {
            return function.call(rhinoContext, rhinoScope, rhinoScope, args);
        } catch (RhinoException angryRhino) {
            String trace = angryRhino.getScriptStackTrace();
            Assert.fail("JavaScript error: " + angryRhino.toString() + " " + trace);
        } catch (JavaScriptAssertionFailed assertion) {
            Assert.fail(assertion.getMessage());
        }
        // we never reach here, but Eclipse doesn't know about Assert.fail.
        return null;
    }
}
