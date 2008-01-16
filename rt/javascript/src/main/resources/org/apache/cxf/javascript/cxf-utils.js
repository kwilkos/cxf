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
 
const org_apache_cxf_XSI_namespace_uri = "http://www.w3.org/2001/XMLSchema-instance";
const org_apache_cxf_XSD_namespace_uri = "http://www.w3.org/2001/XMLSchema";

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

// compensate for lack of namespace support in IE.
function org_apache_cxf_getNamespaceURI(elementNode, namespacePrefix)
{
	var namespaceURI = null;
    if (elementNode.nodeType == 9)
        return null;
    else {
        namespaceURI = org_apache_cxf_findNamespace (elementNode, namespacePrefix);
        if (namespaceURI == null)
            namespaceURI = org_apache_cxf_getNamespaceURI(elementNode.parentNode, namespacePrefix);
        else
            return namespaceURI; 
    }
    return namespaceURI;
 }
 
 
function org_apache_cxf_findNamespace(elementNode, namespacePrefix)
{
    var attributes = elementNode.attributes;
    if ((attributes!=null) && (attributes.length > 0)) {
        for (var x=0; x<attributes.length; x++) {
            var attributeNodeName = attributes.item(x).nodeName;
            var attributeNamespacePrefix = org_apache_cxf_getPrefix(attributes.item(x).nodeName);
            var attributeNamespaceSuffix = org_apache_cxf_getLocalName(attributes.item(x).nodeName);

            if ( (namespacePrefix == null) &&
                 (attributeNamespacePrefix == null) && 
                 (attributeNamespaceSuffix == "xmlns"))
                return attributes.item(x).nodeValue;
            else if ((attributeNamespacePrefix == "xmlns") && 
                     (attributeNamespaceSuffix == namespacePrefix))
                return attributes.item(x).nodeValue;
        }
        return null;
    }
}

function org_apache_cxf_get_node_namespaceURI(elementNode) 
{
	var prefix = org_apache_cxf_getPrefix(elementNode.nodeName);
	return org_apache_cxf_getNamespaceURI(elementNode, prefix);
}

CxfApacheOrgUtil.prototype.getElementNamespaceURI = org_apache_cxf_get_node_namespaceURI;

function org_apache_cxf_any_ns_matcher(style, tns, nslist, nextLocalPart)
{
	this.style = style;
	this.tns = tns;
	this.nslist = nslist;
	this.nextLocalPart = nextLocalPart;
}

org_apache_cxf_any_ns_matcher.ANY = "##any";
org_apache_cxf_any_ns_matcher.OTHER = "##other";
org_apache_cxf_any_ns_matcher.LOCAL = "##local";
org_apache_cxf_any_ns_matcher.LISTED = "listed";

function org_apache_cxf_any_ns_matcher_match(namespaceURI, localName)
{
	switch(this.style) {
		// should this match local elements?
		case org_apache_cxf_any_ns_matcher.ANY:
			return true;
		case org_apache_cxf_any_ns_matcher.OTHER:
			return namespaceURI != this.tns;
		case org_apache_cxf_any_ns_matcher.LOCAL:
			return namespaceURI == null || namespaceURI == '';
		case org_apache_cxf_any_ns_matcher.LISTED:
			for(var x in this.nslist) {
				var ns = this.nslist[x];
				if(ns == "##local") {
					if((namespaceURI == null || namespaceURI == '') 
						&& (this.nextLocalPart != null && localName != this.nextLocalPart))
						return true;  
				} else {
					if(ns == namespaceURI)
						return true;
				} 
			}
            return false;
	  }
}

org_apache_cxf_any_ns_matcher.prototype.match = org_apache_cxf_any_ns_matcher_match; 

function org_apache_cxf_getPrefix(tagName)
{
    var prefix;
    var prefixIndex = tagName.indexOf(":");
    if (prefixIndex == -1)
        return null;
    else 
        return prefix = tagName.substring(0, prefixIndex);
}

function org_apache_cxf_getLocalName(tagName)
{
    var suffix;
    var prefixIndex = tagName.indexOf(":");

    if (prefixIndex == -1)
        return tagName;
    else 
        return suffix = tagName.substring (prefixIndex+1, tagName.length);
}

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
    if(val == null || val == undefined)
        return "";
    else {
    	val = String(val);
        return val.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }
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
    for(var x = 0; x < node.childNodes.length; x ++) {
        r = r + node.childNodes[x].nodeValue;
    }
    return r;
}

CxfApacheOrgUtil.prototype.getNodeText = org_apache_cxf_getNodeText;

// This always uses soap-env, soap, and xsi as prefixes.
function org_apache_cxf_begin_soap11_message(namespaceAttributes)
{
	var value = 
	    '<?xml version="1.0" encoding="UTF-8"?>' 
	    + '<soap-env:Envelope xmlns:soap-env="http://schemas.xmlsoap.org/soap/envelope/"'
		+ ' xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"'
		+ ' xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"'
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
    utils.client = this; // we aren't worried about multithreading!
    this.mtomparts = [];
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

const org_apache_cxf_pad_string_PAD_LEFT  = 0;
const org_apache_cxf_pad_string_PAD_RIGHT = 1;
const org_apache_cxf_pad_string_PAD_BOTH  = 2;

function org_apache_cxf_pad_string(string, len, pad, type) {
	var append = new String();

	len = isNaN(len) ? 0 : len - string.length;
	pad = typeof(pad) == 'string' ? pad : ' ';

	if (type == org_apache_cxf_pad_string_PAD_BOTH) {
		string = org_apache_cxf_pad_sring(Math.floor(len / 2) + string.length,
				pad, org_apache_cxf_pad_string_PAD_LEFT);
		return (org_apache_cxf_pad_string(Math.ceil(len / 2) + string.length,
				pad, org_apache_cxf_pad_string_PAD_RIGHT));
	}

	while ((len -= pad.length) > 0)
		append += pad;
	append += pad.substr(0, len + pad.length);

	return (type == org_apache_cxf_pad_string_PAD_LEFT
			? append.concat(string)
			: string.concat(append));
}

/*
 * Generate a uniformly distributed random integer within the range <min> ..
 * <max>. (min) - Lower limit: random >= min (default: 0) (max) - Upper limit:
 * random <= max (default: 1)
 */
function org_apache_cxf_random_int(min, max) {
    if (! isFinite(min)) min = 0;
    if (! isFinite(max)) max = 1;
    return Math.floor((Math.random () % 1) * (max - min + 1) + min);
}

function org_apache_cxf_random_hex_string(len)
{
	var random = org_apache_cxf_random_int(0, Math.pow (16, len) - 1);
    return org_apache_cxf_pad_string(random.toString(16), len, '0', org_apache_cxf_pad_string_PAD_LEFT);
}


function org_apache_cxf_make_uuid(type) {
	switch ((type || 'v4').toUpperCase()) {
		// Version 4 UUID (Section 4.4 of RFC 4122)
		case 'V4' :
			var tl = org_apache_cxf_random_hex_string(8);
			// time_low
			var tm = org_apache_cxf_random_hex_string(4);
			// time_mid
			var thav = '4' + org_apache_cxf_random_hex_string(3);
			// time_hi_and_version
			var cshar = org_apache_cxf_random_int(0, 0xFF);
			// clock_seq_hi_and_reserved
			cshar = ((cshar & ~(1 << 6)) | (1 << 7)).toString(16);
			var csl = org_apache_cxf_random_hex_string(2);
			// clock_seq_low
			var n = org_apache_cxf_random_hex_string(12);
			// node

			return (tl + '-' + tm + '-' + thav + '-' + cshar + csl + '-' + n);

			// Nil UUID (Section 4.1.7 of RFC 4122)
		case 'NIL' :
			return '00000000-0000-0000-0000-000000000000';
	}
	return null;
}

const ORG_APACHE_CXF_MTOM_REQUEST_HEADER = 'Content-Type: application/xop+xml; type="text/xml"; charset=utf-8\r\n';

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
 
    var mimeBoundary;
    
    // we can't do binary MTOM, but we can do 'text/plain' !
	if(this.mtomparts.length > 0) {
		var uuid = org_apache_cxf_make_uuid('v4');
		mimeBoundary = '@_bOuNDaRy_' + uuid;
		var ctHeader = 'Multipart/Related; start-info="text/xml"; type="application/xop+xml"; boundary="' + mimeBoundary + '"';
    	this.req.setRequestHeader("Content-Type", ctHeader);
		
	} else {
    	this.req.setRequestHeader("Content-Type", "application/xml");
	}

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
    this.utils.trace("about to send data" + this.method + " " + this.url);
    var dataToSend;
    if(this.mtomparts.length == 0) {
    	dataToSend = requestXML;
    } else {
    	dataToSend = "--" + mimeBoundary + "\r\n";
    	dataToSend = dataToSend + ORG_APACHE_CXF_MTOM_REQUEST_HEADER + "\r\n";
    	dataToSend = dataToSend + requestXML;
    	for(var bx in this.mtomparts) {
    		var part = this.mtomparts[bx];
    		dataToSend += "\r\n\r\n--" + mimeBoundary + "\r\n";
    		dataToSend += part;
    	}
   		dataToSend += "--" + mimeBoundary + "--\r\n";
    }
    
    this.req.send(dataToSend);
}

CxfApacheOrgClient.prototype.request = org_apache_cxf_client_request;

function org_apache_cxf_client_onReadyState() {
    var req = this.req;
    var ready = req.readyState;

    this.utils.trace("onreadystatechange " + ready);

    if (ready == this.READY_STATE_DONE) {
    	var httpStatus;
    	try {
        	httpStatus=req.status;
    	} catch(e) {
    		// Firefox throws when there was an error here. 
    		this.utils.trace("onreadystatechange DONE ERROR retrieving status (connection error?)");
    		if(this.onerror != null) {
   			    this.onerror(e);
    		}
    		return;

    	}
    	
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

function org_apache_cxf_package_mtom(value) {
	var uuid = org_apache_cxf_make_uuid('v4');
    var placeholder = '<xop:Include xmlns:xop="http://www.w3.org/2004/08/xop/include" '
        +'href="cid:' + uuid + '" />';
    var mtomObject = 'Content-Type: text/plain; charset="utf-8";\r\nContent-ID: <' + uuid + '>\r\n\r\n' + value + '\r\n';
    this.client.mtomparts.push(mtomObject);
	return placeholder;
}

CxfApacheOrgUtil.prototype.packageMtom = org_apache_cxf_package_mtom; 

// Holder object used for xs:any
// The namespaceURI and localName identify the global element from the schema.
// The object to go with it goes into object.
// If the Any is an array, put the array into the object slot.

function org_apache_cxf_any_holder(namespaceURI, localName, object) {
	this.typeMarker = "org_apache_cxf_any_holder";
	this.namespaceURI = namespaceURI;
	this.localName = localName;
	this.qname = "{" + namespaceURI + "}" + localName;
	this.object = object;
	this.raw = false;
}

// the following will simply dump the supplied XML into the message.
function org_apache_cxf_raw_any_holder(xml)
{
	this.typeMarker = "org_apache_cxf_raw_any_holder";
	this.xml = xml;
	this.raw = true;
	this.xsiType = false;
}

// The following will get an xsi:type attribute in addition to dumping the XML into
// the message.
function org_apache_cxf_raw_typed_any_holder(namespaceURI, localName, xml)
{
	this.typeMarker = "org_apache_cxf_raw_any_holder";
	this.namespaceURI = namespaceURI;
	this.localName = localName;
	this.xml = xml;
	this.raw = true;
	this.xsiType = true;
}

function org_apache_cxf_get_xsi_type(elementNode)
{
    var attributes = elementNode.attributes;
    if ((attributes!=null) && (attributes.length > 0)) {
        for (var x=0; x<attributes.length; x++) {
            var attributeNodeName = attributes.item(x).nodeName;
            var attributeNamespacePrefix = org_apache_cxf_getPrefix(attributes.item(x).nodeName);
            var attributeNamespaceSuffix = org_apache_cxf_getLocalName(attributes.item(x).nodeName);
			if(attributeNamespaceSuffix == 'type') {
				// perhaps this is ours
				var ns = org_apache_cxf_getNamespaceURI(elementNode, attributeNamespacePrefix);
				if(ns == org_apache_cxf_XSI_namespace_uri) {
					return attributes.item(x).nodeValue;
				}
			}
        }
        return null;
    }
}

// return the an object if we can deserialize an object, otherwise return the element itself.
function org_apache_cxf_deserialize_anyType(cxfjsutils, element)
{
	var type = org_apache_cxf_get_xsi_type(element);
	if(type != null) {
		// type is a :-qualified name.
		var namespacePrefix = org_apache_cxf_getPrefix(type);
        var localName = org_apache_cxf_getLocalName(type);
        var uri = org_apache_cxf_getNamespaceURI(element, namespacePrefix);
        if(uri == org_apache_cxf_XSD_namespace_uri) {
        	// we expect a Text node below
        	var textNode = element.firstChild;
        	if(textNode == null)
        		return null;
        	var text = textNode.nodeValue;
        	if(text == null)
        		return null;
        	// For any of the basic types, assume that the nodeValue is what the doctor ordered,
            //  converted to the appropriate type.
        	// For some of the more interesting types this needs more work.
        	if(localName == "int" || localName == "unsignedInt" || localName == "long" || localName == "unsignedLong") {
        		return parseInt(text);
        	}
        	if(localName == "float" || localName == "double")
        		return parseFloat(text);
        	if(localName == "boolean") 
        		return text == 'true';
        	return text;
        }
		var qname = "{" + uri + "}" + localName;
		var deserializer = cxfjsutils.interfaceObject.globalElementDeserializers[qname];
		if(deserializer != null) {
			return deserializer(cxfjsutils, element);
		}
	}
	return element;
}
	
