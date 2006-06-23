using System;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Web;
using System.Web.Services;

namespace HelloWorld_doclit
{
	[WebService(Namespace="http://objectweb.org/hello_world_soap_http")]
	[System.Web.Services.WebServiceBindingAttribute(Name="HelloWorldSOAPBinding", Namespace="http://objectweb.org/hello_world_soap_http")]
	public class HelloWorldService : System.Web.Services.WebService 
	{
    
		[System.Web.Services.WebMethodAttribute()]
		[System.Web.Services.Protocols.SoapDocumentMethodAttribute("http://objectweb.org/hello_world_soap_http/sayHi", Use=System.Web.Services.Description.SoapBindingUse.Literal, ParameterStyle=System.Web.Services.Protocols.SoapParameterStyle.Bare)]
		[return: System.Xml.Serialization.XmlElementAttribute("sayHiReturn", Namespace="http://objectweb.org/hello_world_soap_http")]
		public string sayHi()
		{
			string return_var = "Hello User!";
			return return_var;
		}
    	
		[System.Web.Services.WebMethodAttribute()]
		[System.Web.Services.Protocols.SoapDocumentMethodAttribute("http://objectweb.org/hello_world_soap_http/greetMe", Use=System.Web.Services.Description.SoapBindingUse.Literal, ParameterStyle=System.Web.Services.Protocols.SoapParameterStyle.Bare)]
		[return: System.Xml.Serialization.XmlElementAttribute("greetMeReturn", Namespace="http://objectweb.org/hello_world_soap_http")]
		public string greetMe([System.Xml.Serialization.XmlElementAttribute(Namespace="http://objectweb.org/hello_world_soap_http")] string greetMeParam)
		{
			string return_var;
			return_var = "Hello " + greetMeParam;
			return return_var;
		}
	}
}
