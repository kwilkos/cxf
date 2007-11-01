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
 
var jsutils = new CxfApacheOrgUtil();
 
function assertionFailed(explanation)
{
 	var assert = new Assert(explanation); // this will throw out in Java.
}

function parseXml(xmlString) 
{
	var parser = new DOMParser();
	return parser.parse(xmlString, "text/xml");
}

function deserializeTestBean1_1(xml)
{
	var dom = parseXml(xml);
	var bean = org_apache_cxf_javascript_testns_testBean1_deserialize(jsutils, dom);
	if(bean.getStringItem() != "bean1>stringItem")
		assertionFailed("deserializeTestBean1_1 stringItem " + bean.getStringItem()); 
}

