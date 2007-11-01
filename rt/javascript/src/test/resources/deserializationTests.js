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
	if(bean.getIntItem() != 0)
		assertionFailed("deserializeTestBean1_1 intItem " + bean.getIntItem());
	if(bean.getLongItem() != 0)
		assertionFailed("deserializeTestBean1_1 longItem " + bean.getLongItem());
	if(bean.getOptionalIntItem() != 0)
		assertionFailed("deserializeTestBean1_1 optionalIntItem " + bean.getOptionalIntItem());
	if(bean.getOptionalStringItem() != null)
		assertionFailed("deserializeTestBean1_1 optionalStringItem " + bean.getOptionalStringItem());
	if(bean.getOptionalIntArrayItem() == null)
		assertionFailed("deserializeTestBean1_1 optionalIntArrayItem null");
	if(bean.getOptionalIntArrayItem().length != 0)
		assertionFailed("deserializeTestBean1_1 optionalIntArrayItem length != 0");
	if(bean.getDoubleItem() != -1.0)
		assertionFailed("deserializeTestBean1_1 doubleItem " + bean.getDoubleItem());
}

function deserializeTestBean1_2(xml)
{
	var dom = parseXml(xml);
	var bean = org_apache_cxf_javascript_testns_testBean1_deserialize(jsutils, dom);
	if(bean.getStringItem() != null)
		assertionFailed("deserializeTestBean1_2 stringItem not null: " + bean.getStringItem());
	if(bean.getIntItem() != 21)
		assertionFailed("deserializeTestBean1_2 intItem " + bean.getIntItem());
 	if(bean.getLongItem() != 200000001)
		assertionFailed("deserializeTestBean1_2 longItem " + bean.getLongItem());
	if(bean.getOptionalIntItem() != 456123)
		assertionFailed("deserializeTestBean1_2 optionalIntItem " + bean.getOptionalIntItem());
	if(bean.getOptionalStringItem() != null)
		assertionFailed("deserializeTestBean1_2 optionalStringItem " + bean.getOptionalStringItem());
	if(bean.getOptionalIntArrayItem() == null)
		assertionFailed("deserializeTestBean1_2 optionalIntArrayItem null");
	if(bean.getOptionalIntArrayItem().length != 4)
		assertionFailed("deserializeTestBean1_2 optionalIntArrayItem length != 4");
	if(bean.getOptionalIntArrayItem()[0] != 3)
		assertionFailed("deserializeTestBean1_2 optionalIntArrayItem[0] " + bean.getOptionalIntArrayItem()[0]);
	if(bean.getOptionalIntArrayItem()[1] != 1)
		assertionFailed("deserializeTestBean1_2 optionalIntArrayItem[1] " + bean.getOptionalIntArrayItem()[1]);
	if(bean.getOptionalIntArrayItem()[2] != 4)
		assertionFailed("deserializeTestBean1_2 optionalIntArrayItem[2] " + bean.getOptionalIntArrayItem()[2]);
	if(bean.getOptionalIntArrayItem()[3] != 1)
		assertionFailed("deserializeTestBean1_2 optionalIntArrayItem[3] " + bean.getOptionalIntArrayItem()[3]);
	if(bean.getDoubleItem() != -1.0)
		assertionFailed("deserializeTestBean1_2 doubleItem " + bean.getDoubleItem());
		
}

