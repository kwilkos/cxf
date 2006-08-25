var WebServiceProvider = {
    'wsdlLocation': 'file:../testutils/target/classes/wsdl/hello_world.wsdl',
    'serviceName': 'SOAPServiceTest1',
    'portName': 'SoapPort_Test1',
    'targetNamespace': 'http://objectweb.org/hello_world_soap_http',
};

var ns = new Namespace('ns', "http://objectweb.org/hello_world_soap_http/types");

WebServiceProvider.invoke = function(req) {
    default xml namespace = ns;
    var resp;
    if (req.localName() == 'greetMe') {
        resp = <ns:greetMeResponse xmlns:ns={ns}/>;
        resp.ns::responseType = 'TestGreetMeResponse';
    } else {
        resp = <ns:sayHiResponse xmlns:ns={ns}/>;
        resp.ns::responseType = 'TestSayHiResponse';
    }
    return resp;
}
