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

function assertionFailed(explanation)
{
 	var assert = new Assert(explanation); // this will throw out in Java.
}

var globalNotifier = null;
var globalErrorStatus = null;
var globalErrorStatusText = null;
var globalResponseObject = null;

function test1ErrorCallback(httpStatus, httpStatusText) 
{
	globalErrorStatus = httpStatus;
	globalStatusText = httpStatusText;
	globalNotifier.notify();
}

// Because there is an explicit response wrapper declared, we have a JavaScript
// object here that wraps up the simple 'string'. It is easier to validate it
// from Java, I think.
function test1SuccessCallback(responseObject) 
{
	globalResponseObject = responseObject;
	globalNotifier.notify();
}

function test1(url, doubleArg, floatArg, intArg, longArg, stringArg) 
{
	org_apache_cxf_trace.trace("Enter test1.");
	throw "frustration";
	globalNotifier = new org_apache_cxf_notifier();
	
	var intf = new org_apache_cxf_javascript_fortest_SimpleDocLitWrapped();
	intf.url = url;
	intf.basicTypeFunctionReturnString(test1SuccessCallback, test1ErrorCallback, 
	                                   doubleArg, floatArg, intArg, longArg, stringArg);
    // Return the notifier as a convenience to the Java code.
	return globalNotifier;
}
