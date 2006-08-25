package org.objectweb.celtix.systest.type_test;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import org.objectweb.type_test.types1.ComplexRestriction;
import org.objectweb.type_test.types1.ComplexRestriction2;
import org.objectweb.type_test.types1.ComplexRestriction3;
import org.objectweb.type_test.types1.ComplexRestriction4;
import org.objectweb.type_test.types1.ComplexRestriction5;

public abstract class AbstractTypeTestClient5 extends AbstractTypeTestClient4 {

    public AbstractTypeTestClient5(String name, QName theServicename,
            QName thePort, String theWsdlPath) {
        super(name, theServicename, thePort, theWsdlPath);
    }

    // org.objectweb.type_test.types1.ComplexRestriction

    public void testComplexRestriction() throws Exception {
        // normal case, maxLength=10
        ComplexRestriction x = new ComplexRestriction();
        x.setValue("str_x");
        ComplexRestriction yOrig = new ComplexRestriction();
        yOrig.setValue("string_yyy");
        Holder<ComplexRestriction> y = new Holder<ComplexRestriction>(yOrig);
        Holder<ComplexRestriction> z = new Holder<ComplexRestriction>();

        ComplexRestriction ret;
        if (testDocLiteral) {
            ret = docClient.testComplexRestriction(x, y, z);
        } else {
            ret = rpcClient.testComplexRestriction(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testComplexRestriction(): Incorrect value for inout param",
                         x.getValue(), y.value.getValue());
            assertEquals("testComplexRestriction(): Incorrect value for out param",
                         yOrig.getValue(), z.value.getValue());
            assertEquals("testComplexRestriction(): Incorrect return value",
                         x.getValue(), ret.getValue());
        }

        // abnormal case
        if (testDocLiteral) {
            try {
                x = new ComplexRestriction();
                x.setValue("string_x");
                yOrig = new ComplexRestriction();
                yOrig.setValue("string_yyyyyy");
                y = new Holder<ComplexRestriction>(yOrig);
                z = new Holder<ComplexRestriction>();

                ret = docClient.testComplexRestriction(x, y, z);
                fail("maxLength=10 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
    }

    // org.objectweb.type_test.types1.ComplexRestriction2

    public void testComplexRestriction2() throws Exception {
        // normal case, length=10
        ComplexRestriction2 x = new ComplexRestriction2();
        x.setValue("string_xxx");
        ComplexRestriction2 yOrig = new ComplexRestriction2();
        yOrig.setValue("string_yyy");
        Holder<ComplexRestriction2> y = new Holder<ComplexRestriction2>(yOrig);
        Holder<ComplexRestriction2> z = new Holder<ComplexRestriction2>();

        ComplexRestriction2 ret;
        if (testDocLiteral) {
            ret = docClient.testComplexRestriction2(x, y, z);
        } else {
            ret = rpcClient.testComplexRestriction2(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testComplexRestriction2(): Incorrect value for inout param",
                         x.getValue(), y.value.getValue());
            assertEquals("testComplexRestriction2(): Incorrect value for out param",
                         yOrig.getValue(), z.value.getValue());
            assertEquals("testComplexRestriction2(): Incorrect return value",
                         x.getValue(), ret.getValue());
        }

        // abnormal case
        if (testDocLiteral) {
            try {
                x = new ComplexRestriction2();
                x.setValue("str_x");
                yOrig = new ComplexRestriction2();
                yOrig.setValue("string_yyy");
                y = new Holder<ComplexRestriction2>(yOrig);
                z = new Holder<ComplexRestriction2>();

                ret = docClient.testComplexRestriction2(x, y, z);
                fail("length=10 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
    }

    // org.objectweb.type_test.types1.ComplexRestriction3

    public void testComplexRestriction3() throws Exception {
        // normal case, maxLength=10 for ComplexRestriction
        // && minLength=5 for ComplexRestriction3
        ComplexRestriction3 x = new ComplexRestriction3();
        x.setValue("str_x");
        ComplexRestriction3 yOrig = new ComplexRestriction3();
        yOrig.setValue("string_yyy");
        Holder<ComplexRestriction3> y = new Holder<ComplexRestriction3>(yOrig);
        Holder<ComplexRestriction3> z = new Holder<ComplexRestriction3>();

        ComplexRestriction3 ret;
        if (testDocLiteral) {
            ret = docClient.testComplexRestriction3(x, y, z);
        } else {
            ret = rpcClient.testComplexRestriction3(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testComplexRestriction3(): Incorrect value for inout param",
                         x.getValue(), y.value.getValue());
            assertEquals("testComplexRestriction3(): Incorrect value for out param",
                         yOrig.getValue(), z.value.getValue());
            assertEquals("testComplexRestriction3(): Incorrect return value",
                         x.getValue(), ret.getValue());
        }

        // abnormal cases
        if (testDocLiteral) {
            try {
                x = new ComplexRestriction3();
                x.setValue("str");
                y = new Holder<ComplexRestriction3>(yOrig);
                z = new Holder<ComplexRestriction3>();
                ret = docClient.testComplexRestriction3(x, y, z);
                fail("maxLength=10 && minLength=5 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }

            try {
                x = new ComplexRestriction3();
                x.setValue("string_x");
                yOrig = new ComplexRestriction3();
                yOrig.setValue("string_yyyyyy");
                y = new Holder<ComplexRestriction3>(yOrig);
                z = new Holder<ComplexRestriction3>();

                ret = docClient.testComplexRestriction3(x, y, z);
                fail("maxLength=10 && minLength=5 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
    }

    // org.objectweb.type_test.types1.ComplexRestriction4

    public void testComplexRestriction4() throws Exception {
        // normal case, maxLength=10 for ComplexRestriction
        // && maxLength=5 for ComplexRestriction4
        ComplexRestriction4 x = new ComplexRestriction4();
        x.setValue("str_x");
        ComplexRestriction4 yOrig = new ComplexRestriction4();
        yOrig.setValue("y");
        Holder<ComplexRestriction4> y = new Holder<ComplexRestriction4>(yOrig);
        Holder<ComplexRestriction4> z = new Holder<ComplexRestriction4>();

        ComplexRestriction4 ret;
        if (testDocLiteral) {
            ret = docClient.testComplexRestriction4(x, y, z);
        } else {
            ret = rpcClient.testComplexRestriction4(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testComplexRestriction4(): Incorrect value for inout param",
                         x.getValue(), y.value.getValue());
            assertEquals("testComplexRestriction4(): Incorrect value for out param",
                         yOrig.getValue(), z.value.getValue());
            assertEquals("testComplexRestriction4(): Incorrect return value",
                         x.getValue(), ret.getValue());
        }

        // abnormal case
        if (testDocLiteral) {
            try {
                x = new ComplexRestriction4();
                x.setValue("str_xxx");
                y = new Holder<ComplexRestriction4>(yOrig);
                z = new Holder<ComplexRestriction4>();

                ret = docClient.testComplexRestriction4(x, y, z);
                fail("maxLength=5 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
    }

    // org.objectweb.type_test.types1.ComplexRestriction5

    public void testComplexRestriction5() throws Exception {
        // normal case, maxLength=50 && minLength=5 for ComplexRestriction5
        ComplexRestriction5 x = new ComplexRestriction5();
        x.setValue("http://www.iona.com");
        ComplexRestriction5 yOrig = new ComplexRestriction5();
        yOrig.setValue("http://www.iona.com/info/services/oss/");
        Holder<ComplexRestriction5> y = new Holder<ComplexRestriction5>(yOrig);
        Holder<ComplexRestriction5> z = new Holder<ComplexRestriction5>();

        ComplexRestriction5 ret;
        if (testDocLiteral) {
            ret = docClient.testComplexRestriction5(x, y, z);
        } else {
            ret = rpcClient.testComplexRestriction5(x, y, z);
        }
        if (!perfTestOnly) {
            assertEquals("testComplexRestriction5(): Incorrect value for inout param",
                         x.getValue(), y.value.getValue());
            assertEquals("testComplexRestriction5(): Incorrect value for out param",
                         yOrig.getValue(), z.value.getValue());
            assertEquals("testComplexRestriction5(): Incorrect return value",
                         x.getValue(), ret.getValue());
        }

        // abnormal cases
        if (testDocLiteral) {
            try {
                x = new ComplexRestriction5();
                x.setValue("uri");
                y = new Holder<ComplexRestriction5>(yOrig);
                z = new Holder<ComplexRestriction5>();
                ret = docClient.testComplexRestriction5(x, y, z);
                fail("maxLength=50 && minLength=5 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }

            try {
                x = new ComplexRestriction5();
                x.setValue("http://www.iona.com");
                yOrig = new ComplexRestriction5();
                yOrig.setValue("http://www.iona.com/info/services/oss/info_services_oss_train.html");
                y = new Holder<ComplexRestriction5>(yOrig);
                z = new Holder<ComplexRestriction5>();
                ret = docClient.testComplexRestriction5(x, y, z);
                fail("maxLength=50 && minLength=5 restriction is violated.");
            } catch (Exception ex) {
                //ex.printStackTrace();
            }
        }
    }

}
