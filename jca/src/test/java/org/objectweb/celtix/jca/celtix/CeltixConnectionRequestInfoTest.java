package org.objectweb.celtix.jca.celtix;

import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class CeltixConnectionRequestInfoTest extends TestCase {

    public CeltixConnectionRequestInfoTest(String name) {
        super(name);
    }

    public void testCeltixConnectionRequestInfoEquals() throws Exception {

        CeltixConnectionRequestInfo cr1 = new CeltixConnectionRequestInfo(Foo.class,
                                                                          new URL("file:/tmp/foo"),
                                                                          new QName("service"),
                                                                          new QName("fooPort"));
        CeltixConnectionRequestInfo cr2 = new CeltixConnectionRequestInfo(Foo.class,
                                                                          new URL("file:/tmp/foo"),
                                                                          new QName("service"),
                                                                          new QName("fooPort"));

        assertTrue("Checking equals ", cr1.equals(cr2));

        assertTrue("Checking hashcodes ", cr1.hashCode() == cr2.hashCode());

        cr1 = new CeltixConnectionRequestInfo(Foo.class, null, new QName("service"), null);

        cr2 = new CeltixConnectionRequestInfo(Foo.class, null, new QName("service"), null);

        assertTrue("Checking equals with null parameters ", cr1.equals(cr2));

        assertTrue("Checking hashcodes  with null parameters ", cr1.hashCode() == cr2.hashCode());

        cr1 = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foo"), new QName("service"),
                                              new QName("fooPort"));
        cr2 = new CeltixConnectionRequestInfo(String.class, new URL("file:/tmp/foo"), new QName("service"),
                                              new QName("fooPort"));

        assertTrue("Checking that objects are not equals ", !cr1.equals(cr2));

        cr1 = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foox"), new QName("service"),
                                              new QName("fooPort"));
        cr2 = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foo"), new QName("service"),
                                              new QName("fooPort"));

        assertTrue("Checking that objects are not equal ", !cr1.equals(cr2));

        cr1 = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foo"), new QName("service"),
                                              new QName("fooPort"));
        cr2 = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foo"), new QName("servicex"),
                                              new QName("fooPort"));

        assertTrue("Checking that objects are not equal ", !cr1.equals(cr2));

        cr1 = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foo"), new QName("service"),
                                              new QName("fooPort"));
        cr2 = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foo"), new QName("service"),
                                              new QName("fooPortx"));

        assertTrue("Checking that objects are not equal ", !cr1.equals(cr2));

        cr1 = new CeltixConnectionRequestInfo(Foo.class, new URL("file:/tmp/foo"), new QName("service"),
                                              new QName("fooPort"));
        cr2 = new CeltixConnectionRequestInfo(Foo.class, null, new QName("service"), new QName("fooPort"));

        assertTrue("Checking that objects are not equal ", !cr1.equals(cr2));

    }

    public static Test suite() {
        return new TestSuite(CeltixConnectionRequestInfoTest.class);
    }

    public static void main(String[] args) {
        TestRunner.main(new String[] {CeltixConnectionRequestInfoTest.class.getName()});
    }
}
