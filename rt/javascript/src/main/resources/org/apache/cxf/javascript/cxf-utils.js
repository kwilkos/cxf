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
 
// This code is structured on to require a 'new' of an object of type CxfApacheOrgUtil.
// Alternative, it could be made 'static', but this allowed us to use this same object
// to carry some state.
 
function cxf_apache_org_util_null_trace(message)
{
}
 
function CxfApacheOrgUtil()
{
	this.ELEMENT_NODE = 1;

	if ("function" == typeof(org_apache_cxf_trace)) {
		this.trace = org_apache_cxf_trace.trace;
	} else {
		this.trace = cxf_apache_org_util_null_trace;
    }		
}

// compensate for Microsoft's weakness here.
function org_apache_cxf_getNodeLocalName(node)
{
    if("localName" in node) {
        return node.localName;
    } else {
        return node.baseName;
    }
}

CxfApacheOrgUtil.prototype.getNodeLocalName = org_apache_cxf_getNodeLocalName;

function org_apache_cxf_element_name_for_trace(node)
{
	if(node == null)
		return "Null";
	else if(node == undefined)
		return "Undefined";
	else {
	    var n = '';
	    if(node.namespaceURI != null && node.namespaceURI != '') {
   			n = n + "{" + node.namespaceURI + "}";
   		} 
   		return n + this.getNodeLocalName(node);
	}
}

CxfApacheOrgUtil.prototype.traceElementName = org_apache_cxf_element_name_for_trace; 

function org_apache_cxf_escapeXmlEntities(val) {
    if(val == null)
        return "";
    else
        return val.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

CxfApacheOrgUtil.prototype.escapeXmlEntities = org_apache_cxf_escapeXmlEntities; 
    
function org_apache_cxf_isElementNil(node) {
    if(node == null)
    	throw "null node passed to isElementNil";
    // we need to look for an attribute xsi:nil, where xsi is
    // http://www.w3.org/2001/XMLSchema-instance. we have the usual
    // problem here with namespace-awareness.
    if ('function' == typeof node.getAttributeNS) {
        var nillness = node.getAttributeNS("http://www.w3.org/2001/XMLSchema-instance", "nil");
        return nillness != null && nillness == "true";
    } else { // we assume the standard prefix and hope for the best.
        var nillness = node.getAttribute("xsi:nil");
        return nillness != null && nillness == "true";
    }
}

CxfApacheOrgUtil.prototype.isElementNil = org_apache_cxf_isElementNil; 

function org_apache_cxf_getFirstElementChild(node) {
    if(node == undefined)
       throw "undefined node to getFirstElementChild";

	var n;
	for(n = node.firstChild; n != null && n.nodeType != this.ELEMENT_NODE; n = n.nextSibling) {
	}
		
	return n;
}

CxfApacheOrgUtil.prototype.getFirstElementChild = org_apache_cxf_getFirstElementChild; 

function org_apache_cxf_getNextElementSibling(node) {
	if(node == undefined)
		throw "undefined node to getNextElementSibling";
	if(node == null)
		throw "null node to getNextElementSibling";
	var n;
	for(n = node.nextSibling; n != null && n.nodeType != this.ELEMENT_NODE; n = n.nextSibling)
		;
	return n;
}

CxfApacheOrgUtil.prototype.getNextElementSibling = org_apache_cxf_getNextElementSibling; 

function org_apache_cxf_isNodeNamedNS(node, namespaceURI, localName)
{
    if(node == undefined)
       throw "undefined node to isNodeNamedNS";

    if(namespaceURI == '' || namespaceURI == null) {
        if(node.namespaceURI == '' || node.namespaceURI == null) {
            return localName == org_apache_cxf_getNodeLocalName(node);
        } else
            return false;
    } else {
        return namespaceURI == node.namespaceURI && localName == org_apache_cxf_getNodeLocalName(node);
    }
}

CxfApacheOrgUtil.prototype.isNodeNamedNS = org_apache_cxf_isNodeNamedNS; 

//Firefox splits large text regions into multiple Text objects (4096 chars in each).
function org_apache_cxf_getNodeText(node)
{
    var r = "";
    for(x = 0; x < node.childNodes.length; x ++) {
        r = r + node.childNodes[x].nodeValue;
    }
    return r;
}

CxfApacheOrgUtil.prototype.getNodeText = org_apache_cxf_getNodeText;

// The following could be parameterized using the SoapVersion class, but does anyone believe that
// there will ever be another soap version?

function org_apache_cxf_begin_soap11_message(namespaceAttributes)
{
	var value = 
	    '<?xml version="1.0" encoding="UTF-8"?>' 
	    + '<soap-env:Envelope xmlns:soap-env="http://schemas.xmlsoap.org/soap/envelope/"'
		+ ' xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"'
	    + '><soap-env:Body '
	    + namespaceAttributes 
	    + '>';
		return value;
}

CxfApacheOrgUtil.prototype.beginSoap11Message = org_apache_cxf_begin_soap11_message; 

function org_apache_cxf_end_soap11_message()
{
	return '</soap-env:Body></soap-env:Envelope>';
}
	
CxfApacheOrgUtil.prototype.endSoap11Message = org_apache_cxf_end_soap11_message; 

/*
 * Client object sends requests and calls back with responses.
 */
	
function CxfApacheOrgClient(utils) {
	utils.trace("Client constructor");
    this.utils = utils;
    this.soapAction = "";
    this.messageType = "CALL";
    // handler functions
    this.onsuccess = null;
    this.onerror = null;
    // Firefox is noncompliant with respect to the defined constants,
    // so we define our own.
    this.READY_STATE_UNINITIALIZED = 0;
    this.READY_STATE_LOADING = 1;
    this.READY_STATE_LOADED = 2;
    this.READY_STATE_INTERACTIVE = 3;
    this.READY_STATE_DONE = 4;
}

// Caller must avoid stupid mistakes like 'GET' with a request body.
// This does not support attempts to cross-script.
// This imposes a relatively straightforward set of HTTP options.
function org_apache_cxf_client_request(url, requestXML, method, sync, headers)
{
	this.utils.trace("request " + url);
	
    this.url = url;
    this.sync = sync;

    this.req = null;

    if (method) {
        this.method = method;
    } else {
        if(requestXML) 
            this.method = "POST";
        else
            this.method="GET";
    } 

    try {
        this.req = new XMLHttpRequest();
    } catch(err) {
        this.utils.trace("Error creating XMLHttpRequest " + err);
        this.req = null;
    }

    if(this.req == null) {
        if(window.ActiveXObject) {
            this.req = new ActiveXObject("MSXML2.XMLHTTP.6.0"); // Microsoft's recommended version
        }
    }

    if(this.req == null) {
        this.utils.trace("Unable to create request object.");
        throw "ORG_APACHE_CXF_NO_REQUEST_OBJECT";
    }

	this.utils.trace("about to open " + this.method + " " + this.url);
    this.req.open(this.method, this.url, !this.sync);

    this.req.setRequestHeader("Content-Type", "application/xml");   

    if (headers) { // must be array indexed by header field.
        for (var h in headers) {
            this.req.setRequestHeader(h,headers[h]);
        }
    }

    this.req.setRequestHeader("SOAPAction", this.soapAction);
    this.req.setRequestHeader("MessageType", this.messageType);

    var requester = this; /* setup a closure */
            
    this.req.onreadystatechange = function() {
        requester.onReadyState();
    }

    // NOTE: we do not call the onerror callback for a synchronous error
    // at request time. We let the request object throw as it will. 
    // onError will only be called for asynchronous errors.
    this.utils.trace("about to send " + this.method + " " + this.url);
    this.utils.trace(requestXML);
    
    this.req.send(requestXML);
}

CxfApacheOrgClient.prototype.request = org_apache_cxf_client_request;

function org_apache_cxf_client_onReadyState() {
    var req = this.req;
    var ready = req.readyState;

    this.utils.trace("onreadystatechange " + ready);

    if (ready == this.READY_STATE_DONE) {
        var httpStatus=req.status;
        this.utils.trace("onreadystatechange DONE " + httpStatus);

        if (httpStatus==200 || httpStatus==0) {
            if(this.onsuccess != null) {
                // the onSuccess function is generated, and picks apart the response.
                this.onsuccess(req.responseXML);
            }
		} else {
            this.utils.trace("onreadystatechange DONE ERROR " + 
                             req.getAllResponseHeaders() 
                             + " " 
                             + req.statusText 
                             + " " 
                             + req.responseText);
            if(this.onerror != null) 
                this.onerror(this);
		}
	}
}

CxfApacheOrgClient.prototype.onReadyState = org_apache_cxf_client_onReadyState; 
