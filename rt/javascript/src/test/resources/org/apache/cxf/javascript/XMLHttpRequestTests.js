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

function testOpaqueURI()
{
	var r = new XMLHttpRequest();
	if(r.readyState != r.UNSENT) {
		assertionFailed("initial state not UNSENT");
	}
	r.open("GET", "uri:opaque", false);
}

function testNonAbsolute() {
	var r = new XMLHttpRequest();
	r.open("GET", "http:relative", false);
}

function testNonHttp() {
	var r = new XMLHttpRequest();
	r.open("GET", "ftp:relative", false);
}

function testSendNotOpenError() {
	var r = new XMLHttpRequest();
	r.send();
}

function testSyncHttpFetch() {
	
	var r = new XMLHttpRequest();
	r.open("GET", "http://localhost:8808/test.html", false);
	if (r.readyState != r.OPENED) {
		assertionFailed("state not OPENED after OPEN");
	}
	r.send();
	if (r.readyState != r.DONE) {
		assertionFailed("state not DONE after sync send.")
	}
	return r.responseText;
	
}