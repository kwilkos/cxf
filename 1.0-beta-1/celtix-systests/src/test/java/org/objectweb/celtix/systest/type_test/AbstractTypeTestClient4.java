package org.objectweb.celtix.systest.type_test;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import org.objectweb.type_test.types.AnonymousType;
import org.objectweb.type_test.types.ArrayOfMRecSeqD;
import org.objectweb.type_test.types.MRecSeqA;
import org.objectweb.type_test.types.MRecSeqB;
import org.objectweb.type_test.types.MRecSeqC;
import org.objectweb.type_test.types.MRecSeqD;
import org.objectweb.type_test.types.OccuringChoiceWithAnyAttribute;
import org.objectweb.type_test.types.OccuringStructWithAnyAttribute;
import org.objectweb.type_test.types.SimpleContentExtWithAnyAttribute;
import org.objectweb.type_test.types.StructWithNillableChoice;
import org.objectweb.type_test.types.StructWithNillableStruct;
import org.objectweb.type_test.types.StructWithOccuringChoice;
import org.objectweb.type_test.types.StructWithOccuringStruct;

public abstract class AbstractTypeTestClient4 extends AbstractTypeTestClient3 {

    public AbstractTypeTestClient4(String name, QName theServicename,
            QName thePort, String theWsdlPath) {
        super(name, theServicename, thePort, theWsdlPath);
    }

    // org.objectweb.type_test.types.SimpleContentExtWithAnyAttribute;

    protected boolean equals(SimpleContentExtWithAnyAttribute x,
                             SimpleContentExtWithAnyAttribute y) {
        if (!x.getValue().equals(y.getValue())) {
            return false;
        }
        if (!equalsNilable(x.getAttrib(), y.getAttrib())) {
            return false;
        }
        return equalsQNameStringPairs(x.getOtherAttributes(), y.getOtherAttributes());
    }

    public void testSimpleContentExtWithAnyAttribute() throws Exception {
        QName xAt1Name = new QName("http://schemas.iona.com/type_test", "at_one");
        QName xAt2Name = new QName("http://schemas.iona.com/type_test", "at_two");
        QName yAt3Name = new QName("http://objectweb.org/type_test", "at_thr");
        QName yAt4Name = new QName("http://objectweb.org/type_test", "at_fou");

        SimpleContentExtWithAnyAttribute x = new SimpleContentExtWithAnyAttribute();
        x.setValue("foo");
        x.setAttrib(new Integer(2000));

        SimpleContentExtWithAnyAttribute y = new SimpleContentExtWithAnyAttribute();
        y.setValue("bar");
        y.setAttrib(new Integer(2001));

        Map<QName, String> xAttrMap = x.getOtherAttributes();
        xAttrMap.put(xAt1Name, "one");
        xAttrMap.put(xAt2Name, "two");

        Map<QName, String> yAttrMap = y.getOtherAttributes();
        yAttrMap.put(yAt3Name, "three");
        yAttrMap.put(yAt4Name, "four");

        Holder<SimpleContentExtWithAnyAttribute> yh = new Holder<SimpleContentExtWithAnyAttribute>(y);
        Holder<SimpleContentExtWithAnyAttribute> zh = new Holder<SimpleContentExtWithAnyAttribute>();
        SimpleContentExtWithAnyAttribute ret;
        if (testDocLiteral) {
            ret = docClient.testSimpleContentExtWithAnyAttribute(x, yh, zh);
        } else {
            ret = rpcClient.testSimpleContentExtWithAnyAttribute(x, yh, zh);
        }

        if (!perfTestOnly) {
            assertTrue("testSimpleContentExtWithAnyAttribute(): Incorrect value for inout param",
                equals(x, yh.value));
            assertTrue("testSimpleContentExtWithAnyAttribute(): Incorrect value for out param",
                equals(y, zh.value));
            assertTrue("testSimpleContentExtWithAnyAttribute(): Incorrect return value",
                equals(ret, x));
        }
    }

    // org.objectweb.type_test.types.OccuringStructWithAnyAttribute;

    protected boolean equals(OccuringStructWithAnyAttribute x,
                             OccuringStructWithAnyAttribute y) {
        if (!equalsNilable(x.getAtString(), y.getAtString())
            || !equalsNilable(x.getAtInt(), y.getAtInt())) {
            return false;
        }
        List<Serializable> xList = x.getVarStringAndVarInt();
        List<Serializable> yList = y.getVarStringAndVarInt();
        if (!equalsStringIntList(xList, yList)) {
            return false;
        }
        return equalsQNameStringPairs(x.getOtherAttributes(), y.getOtherAttributes());
    }

    private boolean equalsStringIntList(List<Serializable> xList, List<Serializable> yList) {
        int size = xList.size();
        if (size != yList.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (xList.get(i) instanceof String && yList.get(i) instanceof String) {
                if (!xList.get(i).equals(yList.get(i))) {
                    return false;
                }
            } else if (xList.get(i) instanceof Integer && yList.get(i) instanceof Integer) {
                Integer ix = (Integer)xList.get(i);
                Integer iy = (Integer)yList.get(i);
                if (iy.compareTo(ix) != 0) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public void testOccuringStructWithAnyAttribute() throws Exception {
        QName xAt1Name = new QName("http://schemas.iona.com/type_test", "at_one");
        QName xAt2Name = new QName("http://schemas.iona.com/type_test", "at_two");
        QName yAt3Name = new QName("http://objectweb.org/type_test", "at_thr");
        QName yAt4Name = new QName("http://objectweb.org/type_test", "at_fou");

        OccuringStructWithAnyAttribute x = new OccuringStructWithAnyAttribute();
        OccuringStructWithAnyAttribute y = new OccuringStructWithAnyAttribute();
        List<Serializable> xVarStringAndVarInt = x.getVarStringAndVarInt();
        xVarStringAndVarInt.add("x1");
        xVarStringAndVarInt.add(0);
        xVarStringAndVarInt.add("x2");
        xVarStringAndVarInt.add(1);
        x.setAtString("attribute");
        x.setAtInt(new Integer(2000));

        List<Serializable> yVarStringAndVarInt = y.getVarStringAndVarInt();
        yVarStringAndVarInt.add("there");
        yVarStringAndVarInt.add(1001);
        y.setAtString("another attribute");
        y.setAtInt(new Integer(2002));

        Map<QName, String> xAttrMap = x.getOtherAttributes();
        xAttrMap.put(xAt1Name, "one");
        xAttrMap.put(xAt2Name, "two");

        Map<QName, String> yAttrMap = y.getOtherAttributes();
        yAttrMap.put(yAt3Name, "three");
        yAttrMap.put(yAt4Name, "four");

        Holder<OccuringStructWithAnyAttribute> yh = new Holder<OccuringStructWithAnyAttribute>(y);
        Holder<OccuringStructWithAnyAttribute> zh = new Holder<OccuringStructWithAnyAttribute>();
        OccuringStructWithAnyAttribute ret;
        if (testDocLiteral) {
            ret = docClient.testOccuringStructWithAnyAttribute(x, yh, zh);
        } else {
            ret = rpcClient.testOccuringStructWithAnyAttribute(x, yh, zh);
        }

        if (!perfTestOnly) {
            assertTrue("testOccuringStructWithAnyAttribute(): Incorrect value for inout param",
                equals(x, yh.value));
            assertTrue("testOccuringStructWithAnyAttribute(): Incorrect value for inout param",
                equals(y, zh.value));
            assertTrue("testOccuringStructWithAnyAttribute(): Incorrect value for inout param",
                equals(ret, x));
        }
    }

    // org.objectweb.type_test.types.OccuringChoiceWithAnyAttribute;

    protected boolean equals(OccuringChoiceWithAnyAttribute x,
                             OccuringChoiceWithAnyAttribute y) {
        if (!equalsNilable(x.getAtString(), y.getAtString())
            || !equalsNilable(x.getAtInt(), y.getAtInt())) {
            return false;
        }
        List<Serializable> xList = x.getVarStringOrVarInt();
        List<Serializable> yList = y.getVarStringOrVarInt();
        if (!equalsStringIntList(xList, yList)) {
            return false;
        }
        return equalsQNameStringPairs(x.getOtherAttributes(), y.getOtherAttributes());
    }

    public void testOccuringChoiceWithAnyAttribute() throws Exception {
        QName xAt1Name = new QName("http://schemas.iona.com/type_test", "at_one");
        QName xAt2Name = new QName("http://schemas.iona.com/type_test", "at_two");
        QName yAt3Name = new QName("http://objectweb.org/type_test", "at_thr");
        QName yAt4Name = new QName("http://objectweb.org/type_test", "at_fou");

        OccuringChoiceWithAnyAttribute x = new OccuringChoiceWithAnyAttribute();
        OccuringChoiceWithAnyAttribute y = new OccuringChoiceWithAnyAttribute();

        List<Serializable> xVarStringOrVarInt = x.getVarStringOrVarInt();
        xVarStringOrVarInt.add("hello");
        xVarStringOrVarInt.add(1);
        x.setAtString("attribute");
        x.setAtInt(new Integer(2000));

        List<Serializable> yVarStringOrVarInt = y.getVarStringOrVarInt();
        yVarStringOrVarInt.add(1001);
        y.setAtString("the attribute");
        y.setAtInt(new Integer(2002));

        Map<QName, String> xAttrMap = x.getOtherAttributes();
        xAttrMap.put(xAt1Name, "one");
        xAttrMap.put(xAt2Name, "two");

        Map<QName, String> yAttrMap = y.getOtherAttributes();
        yAttrMap.put(yAt3Name, "three");
        yAttrMap.put(yAt4Name, "four");

        Holder<OccuringChoiceWithAnyAttribute> yh = new Holder<OccuringChoiceWithAnyAttribute>(y);
        Holder<OccuringChoiceWithAnyAttribute> zh = new Holder<OccuringChoiceWithAnyAttribute>();
        OccuringChoiceWithAnyAttribute ret;
        if (testDocLiteral) {
            ret = docClient.testOccuringChoiceWithAnyAttribute(x, yh, zh);
        } else {
            ret = rpcClient.testOccuringChoiceWithAnyAttribute(x, yh, zh);
        }

        if (!perfTestOnly) {
            assertTrue("testOccuringChoiceWithAnyAttribute(): Incorrect value for inout param",
                equals(x, yh.value));
            assertTrue("testOccuringChoiceWithAnyAttribute(): Incorrect value for out param",
                equals(y, zh.value));
            assertTrue("testOccuringChoiceWithAnyAttribute(): Incorrect return value",
                equals(ret, x));
        }
    }

    // org.objectweb.type_test.types.MRecSeqA;

    protected boolean equals(MRecSeqA x, MRecSeqA y) {
        List<MRecSeqB> xList = x.getSeqB();
        List<MRecSeqB> yList = y.getSeqB();
        int size = xList.size();
        if (size != yList.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!equals(xList.get(i), yList.get(i))) {
                return false;
            }
        }
        return x.getVarIntA() == y.getVarIntA();
    }

    protected boolean equals(MRecSeqB x, MRecSeqB y) {
        return x.getVarIntB() == y.getVarIntB()
            && equals(x.getSeqA(), y.getSeqA());
    }

    public void testMRecSeqA() throws Exception {
        MRecSeqA xA = new MRecSeqA();
        MRecSeqA yA = new MRecSeqA();
        MRecSeqA zA = new MRecSeqA();
        MRecSeqB xB = new MRecSeqB();
        MRecSeqB yB = new MRecSeqB();
        xA.setVarIntA(11);
        yA.setVarIntA(12);
        zA.setVarIntA(13);
        xB.setVarIntB(21);
        yB.setVarIntB(22);
        xB.setSeqA(yA);
        yB.setSeqA(zA);
        xA.getSeqB().add(xB);
        yA.getSeqB().add(yB);
        Holder<MRecSeqA> yh = new Holder<MRecSeqA>(yA);
        Holder<MRecSeqA> zh = new Holder<MRecSeqA>();
        MRecSeqA ret;
        if (testDocLiteral) {
            ret = docClient.testMRecSeqA(xA, yh, zh);
        } else {
            ret = rpcClient.testMRecSeqA(xA, yh, zh);
        }
        if (!perfTestOnly) {
            assertTrue("test_MRecSeqA(): Incorrect value for inout param",
                equals(xA, yh.value));
            assertTrue("test_MRecSeqA(): Incorrect value for out param",
                equals(yA, zh.value));
            assertTrue("test_MRecSeqA(): Incorrect return value",
                equals(ret, xA));
        }
    }

    // org.objectweb.type_test.types.MRecSeqC;

    protected boolean equals(MRecSeqC x, MRecSeqC y) {
        return x.getVarIntC() == y.getVarIntC()
            && equals(x.getSeqDs(), y.getSeqDs());
    }

    protected boolean equals(ArrayOfMRecSeqD x, ArrayOfMRecSeqD y) {
        List<MRecSeqD> xList = x.getSeqD();
        List<MRecSeqD> yList = y.getSeqD();
        int size = xList.size();
        if (size != yList.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (!equals(xList.get(i), yList.get(i))) {
                return false;
            }
        }
        return true;
    }

    protected boolean equals(MRecSeqD x, MRecSeqD y) {
        return x.getVarIntD() == y.getVarIntD()
            && equals(x.getSeqC(), y.getSeqC());
    }

    public void testMRecSeqC() throws Exception {
        MRecSeqC xC = new MRecSeqC();
        MRecSeqC yC = new MRecSeqC();
        MRecSeqC zC = new MRecSeqC();
        ArrayOfMRecSeqD xDs = new ArrayOfMRecSeqD();
        ArrayOfMRecSeqD yDs = new ArrayOfMRecSeqD();
        ArrayOfMRecSeqD zDs = new ArrayOfMRecSeqD();
        MRecSeqD xD = new MRecSeqD();
        MRecSeqD yD = new MRecSeqD();
        xC.setVarIntC(11);
        yC.setVarIntC(12);
        zC.setVarIntC(13);
        xD.setVarIntD(21);
        yD.setVarIntD(22);
        xDs.getSeqD().add(xD);
        yDs.getSeqD().add(yD);
        xC.setSeqDs(xDs);
        yC.setSeqDs(yDs);
        zC.setSeqDs(zDs);
        xD.setSeqC(yC);
        yD.setSeqC(zC);
        Holder<MRecSeqC> yh = new Holder<MRecSeqC>(yC);
        Holder<MRecSeqC> zh = new Holder<MRecSeqC>();
        MRecSeqC ret;
        if (testDocLiteral) {
            ret = docClient.testMRecSeqC(xC, yh, zh);
        } else {
            ret = rpcClient.testMRecSeqC(xC, yh, zh);
        }
        if (!perfTestOnly) {
            assertTrue("test_MRecSeqC(): Incorrect value for inout param",
                equals(xC, yh.value));
            assertTrue("test_MRecSeqC(): Incorrect value for out param",
                equals(yC, zh.value));
            assertTrue("test_MRecSeqC(): Incorrect return value",
                equals(ret, xC));
        }
    }

    // org.objectweb.type_test.types.StructWithNillableChoice;

    protected boolean equals(StructWithNillableChoice x, StructWithNillableChoice y) {
        if (x.getVarInteger() != y.getVarInteger()) {
            return false;
        }

        if (x.getVarString() != null) {
            return x.getVarString().equals(y.getVarString());
        } else if (x.getVarInt() != null) {
            return x.getVarInt() == y.getVarInt();
        }
        return y.getVarInt() == null && y.getVarString() == null;
    }

    // XXX 
    protected boolean isNormalized(StructWithNillableChoice x) {
        return x == null || x.getVarInt() == null && x.getVarString() == null;
    }

    public void testStructWithNillableChoice() throws Exception {
        // Test 1
        //
        // x: non-nil choice
        // y: nil choice 
        //
        StructWithNillableChoice x = new StructWithNillableChoice();
        x.setVarInteger(2);
        x.setVarInt(3);

        StructWithNillableChoice yOriginal = new StructWithNillableChoice();
        yOriginal.setVarInteger(1);

        Holder<StructWithNillableChoice> y = new Holder<StructWithNillableChoice>(yOriginal);
        Holder<StructWithNillableChoice> z = new Holder<StructWithNillableChoice>();
        StructWithNillableChoice ret;
        if (testDocLiteral) {
            ret = docClient.testStructWithNillableChoice(x, y, z);
        } else {
            ret = rpcClient.testStructWithNillableChoice(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithNillableChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithNillableChoice(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithNillableChoice(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithNillableChoice(): Incorrect form for out param",
                       isNormalized(z.value));
        }

        // Test 2
        //
        // x: nil choice 
        // y: non-nil choice
        //
        y = new Holder<StructWithNillableChoice>(x);
        x = yOriginal;
        yOriginal = y.value;
        z = new Holder<StructWithNillableChoice>();
        if (testDocLiteral) {
            ret = docClient.testStructWithNillableChoice(x, y, z);
        } else {
            ret = rpcClient.testStructWithNillableChoice(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithNillableChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithNillableChoice(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithNillableChoice(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithNillableChoice(): Incorrect form for inout param",
                       isNormalized(y.value));
            assertTrue("testStructWithNillableChoice(): Incorrect return form",
                       isNormalized(ret));
        }
    }

    // org.objectweb.type_test.types.StructWithOccuringChoice;

    protected boolean equals(StructWithOccuringChoice x, StructWithOccuringChoice y) {
        if (x.getVarInteger() != y.getVarInteger()) {
            fail(x.getVarInteger() + " != " + y.getVarInteger());
            return false;
        }

        List<Serializable> xList = x.getVarIntOrVarString();
        List<Serializable> yList = y.getVarIntOrVarString();
        int size = xList.size();
        if (size != yList.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (xList.get(i) instanceof Integer && yList.get(i) instanceof Integer) {
                Integer ix = (Integer)xList.get(i);
                Integer iy = (Integer)yList.get(i);
                if (iy.compareTo(ix) != 0) {
                    return false;
                }
            } else if (xList.get(i) instanceof String && yList.get(i) instanceof String) {
                String sx = (String)xList.get(i);
                String sy = (String)yList.get(i);
                if (!sx.equals(sy)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    protected boolean isNormalized(StructWithOccuringChoice x) {
        return x == null || x.getVarIntOrVarString().size() == 0;
    }

    public void testStructWithOccuringChoice() throws Exception {
        // Test 1
        //
        // x: choice occurs twice
        // y: choice doesn't occur
        //
        StructWithOccuringChoice x = new StructWithOccuringChoice();
        x.setVarInteger(2);
        x.getVarIntOrVarString().add(3);
        x.getVarIntOrVarString().add("hello"); 

        StructWithOccuringChoice yOriginal = new StructWithOccuringChoice();
        yOriginal.setVarInteger(1);

        Holder<StructWithOccuringChoice> y = new Holder<StructWithOccuringChoice>(yOriginal);
        Holder<StructWithOccuringChoice> z = new Holder<StructWithOccuringChoice>();
        StructWithOccuringChoice ret;
        if (testDocLiteral) {
            ret = docClient.testStructWithOccuringChoice(x, y, z);
        } else {
            ret = rpcClient.testStructWithOccuringChoice(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithOccuringChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithOccuringChoice(): Incorrect form for out param",
                       isNormalized(z.value));
        }

        // Test 2
        //
        // x: choice occurs twice
        // y: choice occurs once
        //
        yOriginal.getVarIntOrVarString().add("world");

        y = new Holder<StructWithOccuringChoice>(yOriginal);
        z = new Holder<StructWithOccuringChoice>();
        if (testDocLiteral) {
            ret = docClient.testStructWithOccuringChoice(x, y, z);
        } else {
            ret = rpcClient.testStructWithOccuringChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testStructWithOccuringChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect return value",
                       equals(x, ret));
        }

        // Test 3
        //
        // x: choice occurs once
        // y: choice occurs twice
        //
        y = new Holder<StructWithOccuringChoice>(x);
        x = yOriginal;
        yOriginal = y.value;
        z = new Holder<StructWithOccuringChoice>();
        if (testDocLiteral) {
            ret = docClient.testStructWithOccuringChoice(x, y, z);
        } else {
            ret = rpcClient.testStructWithOccuringChoice(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithOccuringChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect return value",
                       equals(x, ret));
        }

        // Test 4
        //
        // x: choice doesn't occur
        // y: choice occurs twice
        //
        x.getVarIntOrVarString().clear();

        y = new Holder<StructWithOccuringChoice>(yOriginal);
        z = new Holder<StructWithOccuringChoice>();
        if (testDocLiteral) {
            ret = docClient.testStructWithOccuringChoice(x, y, z);
        } else {
            ret = rpcClient.testStructWithOccuringChoice(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithOccuringChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithOccuringChoice(): Incorrect form for inout param",
                       isNormalized(y.value));
            assertTrue("testStructWithOccuringChoice(): Incorrect return form",
                       isNormalized(ret));
        }
    }

    // org.objectweb.type_test.types.StructWithNillableStruct;

    protected boolean equals(StructWithNillableStruct x, StructWithNillableStruct y) {
        if (x.getVarInteger() != y.getVarInteger()) {
            fail(x.getVarInteger() + " != " + y.getVarInteger());
            return false;
        }

        if (x.getVarInt() == null) {
            if (x.getVarFloat() == null) {
                return y.getVarInt() == null && y.getVarFloat() == null;
            } else {
                return false;
            }
        } else {
            if (x.getVarFloat() == null || y.getVarInt() == null || y.getVarFloat() == null) {
                return false;
            }
        }
        return x.getVarFloat().compareTo(y.getVarFloat()) == 0 
            && x.getVarInt() == y.getVarInt();
    }

    protected boolean isNormalized(StructWithNillableStruct x) {
        return x.getVarInt() == null && x.getVarFloat() == null;
    }

    public void testStructWithNillableStruct() throws Exception {
        // Test 1
        //
        // x: non-nil sequence
        // y: nil sequence (non-null holder object)
        //
        StructWithNillableStruct x = new StructWithNillableStruct();
        x.setVarInteger(100);
        x.setVarInt(101);
        x.setVarFloat(101.5f);
        StructWithNillableStruct yOriginal = new StructWithNillableStruct();
        yOriginal.setVarInteger(200);

        Holder<StructWithNillableStruct> y =
            new Holder<StructWithNillableStruct>(yOriginal);
        Holder<StructWithNillableStruct> z = new Holder<StructWithNillableStruct>();
        StructWithNillableStruct ret;
        if (testDocLiteral) {
            ret = docClient.testStructWithNillableStruct(x, y, z);
        } else {
            ret = rpcClient.testStructWithNillableStruct(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithNillableStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithNillableStruct(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithNillableStruct(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithNillableStruct(): Incorrect form for out param",
                       isNormalized(z.value));
        }

        // Test 2
        //
        // x: non-nil sequence
        // y: nil sequence (null holder object)
        //
        yOriginal.setVarInt(null);
        yOriginal.setVarFloat(null);

        y = new Holder<StructWithNillableStruct>(yOriginal);
        z = new Holder<StructWithNillableStruct>();
        if (testDocLiteral) {
            ret = docClient.testStructWithNillableStruct(x, y, z);
        } else {
            ret = rpcClient.testStructWithNillableStruct(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testStructWithNillableStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithNillableStruct(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithNillableStruct(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithNillableStruct(): Incorrect form for out param",
                       isNormalized(z.value));
        }

        // Test 3
        //
        // x: nil sequence (null holder object)
        // y: non-nil sequence
        //
        y = new Holder<StructWithNillableStruct>(x);
        x = yOriginal;
        yOriginal = y.value;
        z = new Holder<StructWithNillableStruct>();
        if (testDocLiteral) {
            ret = docClient.testStructWithNillableStruct(x, y, z);
        } else {
            ret = rpcClient.testStructWithNillableStruct(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithNillableStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithNillableStruct(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithNillableStruct(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithNillableStruct(): Incorrect form for inout param",
                       isNormalized(y.value));
            assertTrue("testStructWithNillableStruct(): Incorrect return form",
                       isNormalized(ret));
        }
    }

    // org.objectweb.type_test.types.StructWithOccuringStruct;

    protected boolean equals(StructWithOccuringStruct x, StructWithOccuringStruct y) {
        if (x.getVarInteger() != y.getVarInteger()) {
            return false;
        }

        List<Comparable> xList = x.getVarIntAndVarFloat();
        List<Comparable> yList = y.getVarIntAndVarFloat();
        int xSize = (xList == null) ? 0 : xList.size();
        int ySize = (yList == null) ? 0 : yList.size();
        if (xSize != ySize) {
            return false;
        }
        for (int i = 0; i < xSize; ++i) {
            if (xList.get(i) instanceof Integer && yList.get(i) instanceof Integer) {
                if (((Integer)xList.get(i)).compareTo((Integer)yList.get(i)) != 0) {
                    return false;
                }
            } else if (xList.get(i) instanceof Float && yList.get(i) instanceof Float) {
                if (((Float)xList.get(i)).compareTo((Float)yList.get(i)) != 0) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }

    protected boolean isNormalized(StructWithOccuringStruct x) {
        return x.getVarIntAndVarFloat() != null;
    }

    public void testStructWithOccuringStruct() throws Exception {
        // Test 1
        //
        // x: sequence occurs twice
        // y: sequence doesn't occur (null holder object)
        //
        StructWithOccuringStruct x = new StructWithOccuringStruct();
        x.setVarInteger(100);
        x.getVarIntAndVarFloat().add(101);
        x.getVarIntAndVarFloat().add(101.5f);
        x.getVarIntAndVarFloat().add(102);
        x.getVarIntAndVarFloat().add(102.5f);

        StructWithOccuringStruct yOriginal = new StructWithOccuringStruct();
        yOriginal.setVarInteger(200);

        Holder<StructWithOccuringStruct> y = new Holder<StructWithOccuringStruct>(yOriginal);
        Holder<StructWithOccuringStruct> z = new Holder<StructWithOccuringStruct>();
        StructWithOccuringStruct ret;
        if (testDocLiteral) {
            ret = docClient.testStructWithOccuringStruct(x, y, z);
        } else {
            ret = rpcClient.testStructWithOccuringStruct(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithOccuringStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithOccuringStruct(): Incorrect form for out param",
                       isNormalized(z.value));
        }

        // Test 2
        //
        // x: sequence occurs twice
        // y: sequence occurs once
        //
        yOriginal.getVarIntAndVarFloat().add(201);
        yOriginal.getVarIntAndVarFloat().add(202.5f);

        y = new Holder<StructWithOccuringStruct>(yOriginal);
        z = new Holder<StructWithOccuringStruct>();
        if (testDocLiteral) {
            ret = docClient.testStructWithOccuringStruct(x, y, z);
        } else {
            ret = rpcClient.testStructWithOccuringStruct(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testStructWithOccuringStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect return value",
                       equals(x, ret));
        }

        // Test 3
        //
        // x: sequence occurs once
        // y: sequence occurs twice
        //
        y = new Holder<StructWithOccuringStruct>(x);
        x = yOriginal;
        yOriginal = y.value;
        z = new Holder<StructWithOccuringStruct>();
        if (testDocLiteral) {
            ret = docClient.testStructWithOccuringStruct(x, y, z);
        } else {
            ret = rpcClient.testStructWithOccuringStruct(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithOccuringStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect return value",
                       equals(x, ret));
        }

        // Test 4
        //
        // x: sequence doesn't occur (array of size 0)
        // y: sequence occurs twice
        //
        x.getVarIntAndVarFloat().clear();

        y = new Holder<StructWithOccuringStruct>(yOriginal);
        z = new Holder<StructWithOccuringStruct>();
        if (testDocLiteral) {
            ret = docClient.testStructWithOccuringStruct(x, y, z);
        } else {
            ret = rpcClient.testStructWithOccuringStruct(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testStructWithOccuringStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect return value",
                       equals(x, ret));
            assertTrue("testStructWithOccuringStruct(): Incorrect form for inout param",
                       isNormalized(y.value));
            assertTrue("testStructWithOccuringStruct(): Incorrect return form",
                       isNormalized(ret));
        }
    }

    // org.objectweb.type_test.types.AnonymousType;

    protected boolean equals(AnonymousType x, AnonymousType y) {
        return x.getFoo().getFoo().equals(y.getFoo().getFoo())
            && x.getFoo().getBar().equals(y.getFoo().getBar());
    }

    public void testAnonymousType() throws Exception {
        AnonymousType x = new AnonymousType();
        AnonymousType.Foo fx = new AnonymousType.Foo();
        fx.setFoo("hello");
        fx.setBar("there");
        x.setFoo(fx);

        AnonymousType yOrig = new AnonymousType();
        AnonymousType.Foo fy = new AnonymousType.Foo();
        fy.setFoo("good");
        fy.setBar("bye");
        yOrig.setFoo(fy);

        Holder<AnonymousType> y = new Holder<AnonymousType>(yOrig);
        Holder<AnonymousType> z = new Holder<AnonymousType>();

        AnonymousType ret;
        if (testDocLiteral) {
            ret = docClient.testAnonymousType(x, y, z);
        } else {
            ret = rpcClient.testAnonymousType(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testAnonymousType(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testAnonymousType(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testAnonymousType(): Incorrect return value", equals(x, ret));
        }
    }

}
