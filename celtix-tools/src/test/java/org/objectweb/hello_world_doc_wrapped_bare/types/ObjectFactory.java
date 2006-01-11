
package org.objectweb.hello_world_doc_wrapped_bare.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.objectweb.hello_world_doc_wrapped_bare.types package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    public static final QName ADDNUMBERSQNAME = 
        new QName("http://objectweb.org/hello_world_doc_wrapped_bare/types", "addNumbers");
    private static final QName ADDNUMBERSRESPONSEQNAME = 
        new QName("http://objectweb.org/hello_world_doc_wrapped_bare/types", "addNumbersResponse");
    private static final QName BAREDOCUMENTQNAME = 
        new QName("http://objectweb.org/hello_world_doc_wrapped_bare/types", "BareDocument");
    private static final QName BADBRECORDLITQNAME = 
        new QName("http://objectweb.org/hello_world_doc_wrapped_bare/types", "BadRecordLit");

    /**
     * Create a new ObjectFactory that can be used to 
     * create new instances of schema derived 
     * classes for package: org.objectweb.hello_world_doc_wrapped_bare.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GreetMeResponse }
     * 
     */
    public GreetMeResponse createGreetMeResponse() {
        return new GreetMeResponse();
    }

    /**
     * Create an instance of {@link BareDocumentResponse }
     * 
     */
    public BareDocumentResponse createBareDocumentResponse() {
        return new BareDocumentResponse();
    }

    /**
     * Create an instance of {@link TestDocLitFaultResponse }
     * 
     */
    public TestDocLitFaultResponse createTestDocLitFaultResponse() {
        return new TestDocLitFaultResponse();
    }

    /**
     * Create an instance of {@link SayHiResponse }
     * 
     */
    public SayHiResponse createSayHiResponse() {
        return new SayHiResponse();
    }

    /**
     * Create an instance of {@link SayHi }
     * 
     */
    public SayHi createSayHi() {
        return new SayHi();
    }

    /**
     * Create an instance of {@link GreetMeOneWay }
     * 
     */
    public GreetMeOneWay createGreetMeOneWay() {
        return new GreetMeOneWay();
    }

    /**
     * Create an instance of {@link GreetMe }
     * 
     */
    public GreetMe createGreetMe() {
        return new GreetMe();
    }

    /**
     * Create an instance of {@link BadRecord }
     * 
     */
    public BadRecord createBadRecord() {
        return new BadRecord();
    }

    /**
     * Create an instance of {@link GreetMeSometimeResponse }
     * 
     */
    public GreetMeSometimeResponse createGreetMeSometimeResponse() {
        return new GreetMeSometimeResponse();
    }

    /**
     * Create an instance of {@link NoSuchCodeLit }
     * 
     */
    public NoSuchCodeLit createNoSuchCodeLit() {
        return new NoSuchCodeLit();
    }

    /**
     * Create an instance of {@link GreetMeSometime }
     * 
     */
    public GreetMeSometime createGreetMeSometime() {
        return new GreetMeSometime();
    }

    /**
     * Create an instance of {@link AddNumbersResponse }
     * 
     */
    public AddNumbersResponse createAddNumbersResponse() {
        return new AddNumbersResponse();
    }

    /**
     * Create an instance of {@link ErrorCode }
     * 
     */
    public ErrorCode createErrorCode() {
        return new ErrorCode();
    }

    /**
     * Create an instance of {@link AddNumbers }
     * 
     */
    public AddNumbers createAddNumbers() {
        return new AddNumbers();
    }

    /**
     * Create an instance of {@link TestDocLitFault }
     * 
     */
    public TestDocLitFault createTestDocLitFault() {
        return new TestDocLitFault();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddNumbers }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://objectweb.org/hello_world_doc_wrapped_bare/types", 
                    name = "addNumbers")
    public JAXBElement<AddNumbers> createAddNumbers(AddNumbers value) {
        return new JAXBElement<AddNumbers>(ADDNUMBERSQNAME, AddNumbers.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AddNumbersResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://objectweb.org/hello_world_doc_wrapped_bare/types", 
                    name = "addNumbersResponse")
    public JAXBElement<AddNumbersResponse> createAddNumbersResponse(AddNumbersResponse value) {
        return new JAXBElement<AddNumbersResponse>(ADDNUMBERSRESPONSEQNAME, 
            AddNumbersResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://objectweb.org/hello_world_doc_wrapped_bare/types", 
                    name = "BareDocument")
    public JAXBElement<String> createBareDocument(String value) {
        return new JAXBElement<String>(this.BAREDOCUMENTQNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://objectweb.org/hello_world_doc_wrapped_bare/types", 
                    name = "BadRecordLit")
    public JAXBElement<String> createBadRecordLit(String value) {
        return new JAXBElement<String>(this.BADBRECORDLITQNAME, String.class, null, value);
    }

}
