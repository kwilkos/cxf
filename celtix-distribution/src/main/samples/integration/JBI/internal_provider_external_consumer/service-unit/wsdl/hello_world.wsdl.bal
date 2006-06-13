<?xml version="1.0" encoding="UTF-8"?>
<wsdl:definitions name="HelloWorld" targetNamespace="http://objectweb.org/hello_world" 
    xmlns="http://schemas.xmlsoap.org/wsdl/" 
    xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/" 
    xmlns:tns="http://objectweb.org/hello_world"
    xmlns:x1="http://objectweb.org/hello_world/types"
    xmlns:wsdl="http://schemas.xmlsoap.org/wsdl/" 
    xmlns:xsd="http://www.w3.org/2001/XMLSchema">

    <wsdl:types>
        <schema targetNamespace="http://objectweb.org/hello_world/types" 
            xmlns="http://www.w3.org/2001/XMLSchema" elementFormDefault="qualified">
            <element name="sayHi">
                <complexType/>
            </element>
            <element name="greetMe">
                <complexType>
                    <sequence>
                        <element name="requestType" type="xsd:string"/>
                    </sequence>
                </complexType>
            </element>
            <element name="greetMeResponse">
                <complexType>
                    <sequence>
                        <element name="responseType" type="xsd:string"/>
                    </sequence>
                </complexType>
            </element>
	
	    <element name="pingMe">
                <complexType/>
            </element>
            <element name="pingMeResponse">
                <complexType/>
            </element>
            <element name="faultDetail">
                <complexType>
                    <sequence>
                        <element name="minor" type="short"/>
                        <element name="major" type="short"/>
                    </sequence>
                </complexType>
            </element>


        </schema>
    </wsdl:types>

    <wsdl:message name="sayHiRequest">
        <wsdl:part element="x1:sayHi" name="in"/>
    </wsdl:message>
    <wsdl:message name="sayHiResponse">
        <wsdl:part element="x1:sayHiResponse" name="out"/>
    </wsdl:message>
    <wsdl:message name="greetMeRequest">
        <wsdl:part element="x1:greetMe" name="in"/>
    </wsdl:message>
    <wsdl:message name="greetMeResponse">
        <wsdl:part element="x1:greetMeResponse" name="out"/>
    </wsdl:message>

    <wsdl:message name="pingMeRequest">
        <wsdl:part name="in" element="x1:pingMe"/>
    </wsdl:message>
    <wsdl:message name="pingMeResponse">
        <wsdl:part name="out" element="x1:pingMeResponse"/>
    </wsdl:message>
    <wsdl:message name="pingMeFault">
        <wsdl:part name="faultDetail" element="x1:faultDetail"/>
    </wsdl:message>
    

    <wsdl:portType name="Greeter">
        <wsdl:operation name="sayHi">
            <wsdl:input message="tns:sayHiRequest" name="sayHiRequest"/>
        </wsdl:operation>
        
        <wsdl:operation name="greetMe">
            <wsdl:input message="tns:greetMeRequest" name="greetMeRequest"/>
            <wsdl:output message="tns:greetMeResponse" name="greetMeResponse"/>
        </wsdl:operation>

	<wsdl:operation name="pingMe">
            <wsdl:input name="pingMeRequest" message="tns:pingMeRequest"/>
            <wsdl:output name="pingMeResponse" message="tns:pingMeResponse"/>
            <wsdl:fault name="pingMeFault" message="tns:pingMeFault"/>
        </wsdl:operation>
        
    </wsdl:portType>

    <wsdl:binding name="Greeter_SOAPBinding" type="tns:Greeter">
        <!--soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/-->
        <soap:binding style="document" transport="http://schemas.xmlsoap.org/soap/http"/>
        
        <wsdl:operation name="sayHi">
            <soap:operation soapAction="" style="document"/>
            <wsdl:input name="sayHiRequest">
                <soap:body use="literal"/>
            </wsdl:input>
        </wsdl:operation>
        
        <wsdl:operation name="greetMe">
            <soap:operation soapAction="" style="document"/>
            <wsdl:input name="greetMeRequest">
                <soap:body use="literal"/>
            </wsdl:input>
            <wsdl:output name="greetMeResponse">
                <soap:body use="literal"/>
            </wsdl:output>
        </wsdl:operation>
        
        <wsdl:operation name="pingMe">
            <soap:operation style="document"/>
            <wsdl:input>
                <soap:body use="literal"/>
            </wsdl:input>
            <wsdl:output>
                <soap:body use="literal"/>
            </wsdl:output>
            <wsdl:fault name="pingMeFault">
                <soap:fault name="pingMeFault" use="literal"/>
            </wsdl:fault>
        </wsdl:operation>


    </wsdl:binding>

    <wsdl:service name="HelloWorldService">
        <wsdl:port binding="tns:Greeter_SOAPBinding" name="SE_Endpoint">
            <soap:address location="http://localhost:9000/SoapContext/SE_Endpoint"/>
        </wsdl:port>
    </wsdl:service>

</wsdl:definitions>

