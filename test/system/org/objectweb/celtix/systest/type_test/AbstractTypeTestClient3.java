package org.objectweb.celtix.systest.type_test;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.Holder;

import org.w3c.dom.Element;

import org.objectweb.type_test.types.ChoiceOfChoice;
import org.objectweb.type_test.types.ChoiceOfSeq;
import org.objectweb.type_test.types.ChoiceWithBinary;
import org.objectweb.type_test.types.ChoiceWithGroupChoice;
import org.objectweb.type_test.types.ChoiceWithGroupSeq;
import org.objectweb.type_test.types.ChoiceWithGroups;
import org.objectweb.type_test.types.ComplexTypeWithAttributeGroup;
import org.objectweb.type_test.types.ComplexTypeWithAttributeGroup1;
import org.objectweb.type_test.types.ComplexTypeWithAttributes;
import org.objectweb.type_test.types.DerivedChoiceBaseArray;
import org.objectweb.type_test.types.DerivedChoiceBaseChoice;
import org.objectweb.type_test.types.DerivedChoiceBaseStruct;
import org.objectweb.type_test.types.DerivedEmptyBaseEmptyAll;
import org.objectweb.type_test.types.DerivedEmptyBaseEmptyChoice;
import org.objectweb.type_test.types.DerivedNoContent;
import org.objectweb.type_test.types.DerivedStructBaseChoice;
import org.objectweb.type_test.types.DerivedStructBaseEmpty;
import org.objectweb.type_test.types.DerivedStructBaseStruct;
import org.objectweb.type_test.types.ExtBase64Binary;
import org.objectweb.type_test.types.GroupDirectlyInComplexType;
import org.objectweb.type_test.types.IDTypeAttribute;
import org.objectweb.type_test.types.MultipleOccursSequenceInSequence;
import org.objectweb.type_test.types.OccuringChoice;
import org.objectweb.type_test.types.OccuringChoice1;
import org.objectweb.type_test.types.OccuringChoice2;
import org.objectweb.type_test.types.OccuringStruct;
import org.objectweb.type_test.types.OccuringStruct1;
import org.objectweb.type_test.types.OccuringStruct2;
import org.objectweb.type_test.types.RestrictedChoiceBaseChoice;
import org.objectweb.type_test.types.SequenceWithGroupChoice;
import org.objectweb.type_test.types.SequenceWithGroupSeq;
import org.objectweb.type_test.types.SequenceWithGroups;
import org.objectweb.type_test.types.SequenceWithOccuringGroup;
import org.objectweb.type_test.types.SimpleChoice;
import org.objectweb.type_test.types.SimpleStruct;
import org.objectweb.type_test.types.StructWithAny;
import org.objectweb.type_test.types.StructWithAnyArray;
import org.objectweb.type_test.types.StructWithBinary;
import org.objectweb.type_test.types.UnboundedArray;

public abstract class AbstractTypeTestClient3 extends AbstractTypeTestClient2 {

    public AbstractTypeTestClient3(String name, QName theServicename,
            QName thePort, String theWsdlPath) {
        super(name, theServicename, thePort, theWsdlPath);
    }

    // org.objectweb.type_test.types.ChoiceOfChoice

    protected boolean equals(ChoiceOfChoice x, ChoiceOfChoice y) {
        if (x.getVarInt() != null && y.getVarInt() != null) {
            return x.getVarInt().equals(y.getVarInt());
        }
        if (x.getVarFloat() != null && y.getVarFloat() != null) {
            return x.getVarFloat().equals(y.getVarFloat());
        }
        if (x.getVarOtherInt() != null && y.getVarOtherInt() != null) {
            return x.getVarOtherInt().equals(y.getVarOtherInt());
        }
        if (x.getVarString() != null && y.getVarString() != null) {
            return x.getVarString().equals(y.getVarString());
        }
        return false;
    }
    
    // XXX - ri generated code flattened - no nested choice
    public void testChoiceOfChoice() throws Exception {
        //ChoiceOfChoice.ChoiceOfChoice1 bx = new ChoiceOfChoice.ChoiceOfChoice1();
        //ChoiceOfChoice.ChoiceOfChoice2 by = new ChoiceOfChoice.ChoiceOfChoice2();
        //x.setChoiceOfChoice1(bx);
        //yOrig.setChoiceOfChoice2(by);

        ChoiceOfChoice x = new ChoiceOfChoice();
        ChoiceOfChoice yOrig = new ChoiceOfChoice();
        x.setVarFloat(3.14f);
        yOrig.setVarString("y456");

        Holder<ChoiceOfChoice> y = new Holder<ChoiceOfChoice>(yOrig);
        Holder<ChoiceOfChoice> z = new Holder<ChoiceOfChoice>();

        ChoiceOfChoice ret;
        if (testDocLiteral) {
            ret = docClient.testChoiceOfChoice(x, y, z);
        } else {
            ret = rpcClient.testChoiceOfChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testChoiceOfChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testChoiceOfChoice(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testChoiceOfChoice(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.ChoiceOfSeq

    protected boolean equals(ChoiceOfSeq x, ChoiceOfSeq y) {
        if (x.getVarFloat() != null && x.getVarInt() != null 
            && y.getVarFloat() != null && y.getVarInt() != null) {
            return x.getVarInt().equals(y.getVarInt())
                && x.getVarFloat().compareTo(y.getVarFloat()) == 0;
        }
        if (x.getVarOtherInt() != null && y.getVarOtherInt() != null 
            && x.getVarString() != null && y.getVarString() != null) {
            return x.getVarOtherInt().equals(y.getVarOtherInt())
                && x.getVarString().equals(y.getVarString());
        }
        return false;
    }

    // XXX - ri generated code is flattened - no nested sequence
    public void testChoiceOfSeq() throws Exception {
        ChoiceOfSeq x = new ChoiceOfSeq();
        x.setVarInt(123);
        x.setVarFloat(3.14f);

        ChoiceOfSeq yOrig = new ChoiceOfSeq();
        yOrig.setVarOtherInt(456);
        yOrig.setVarString("y456");

        Holder<ChoiceOfSeq> y = new Holder<ChoiceOfSeq>(yOrig);
        Holder<ChoiceOfSeq> z = new Holder<ChoiceOfSeq>();

        ChoiceOfSeq ret;
        if (testDocLiteral) {
            ret = docClient.testChoiceOfSeq(x, y, z);
        } else {
            ret = rpcClient.testChoiceOfSeq(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testChoiceOfSeq(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testChoiceOfSeq(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testChoiceOfSeq(): Incorrect return value", equals(x, ret));
        }
    }

    //  org.objectweb.type_test.types.DerivedStructBaseStruct

    protected boolean equals(DerivedStructBaseStruct x, DerivedStructBaseStruct y) {
        return equals((SimpleStruct)x, (SimpleStruct)y)
            && (x.getVarFloatExt() == y.getVarFloatExt())
            && (x.getVarStringExt().equals(y.getVarStringExt()))
            && (x.getAttrString1().equals(y.getAttrString1()))
            && (x.getAttrString2().equals(y.getAttrString2()));
    }
    
    public void testDerivedStructBaseStruct() throws Exception {
        DerivedStructBaseStruct x = new DerivedStructBaseStruct();
        //Base
        x.setVarFloat(3.14f);
        x.setVarInt(new BigInteger("42"));
        x.setVarString("BaseStruct-x");
        x.setVarAttrString("BaseStructAttr-x");
        //Derived
        x.setVarFloatExt(-3.14f);
        x.setVarStringExt("DerivedStruct-x");
        x.setAttrString1("DerivedAttr1-x");
        x.setAttrString2("DerivedAttr2-x");

        DerivedStructBaseStruct yOrig = new DerivedStructBaseStruct();
        //Base
        yOrig.setVarFloat(-9.14f);
        yOrig.setVarInt(new BigInteger("10"));
        yOrig.setVarString("BaseStruct-y");
        yOrig.setVarAttrString("BaseStructAttr-y");
        //Derived
        yOrig.setVarFloatExt(1.414f);
        yOrig.setVarStringExt("DerivedStruct-y");
        yOrig.setAttrString1("DerivedAttr1-y");
        yOrig.setAttrString2("DerivedAttr2-y");

        Holder<DerivedStructBaseStruct> y = new Holder<DerivedStructBaseStruct>(yOrig);
        Holder<DerivedStructBaseStruct> z = new Holder<DerivedStructBaseStruct>();
        DerivedStructBaseStruct ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedStructBaseStruct(x, y, z);
        } else {
            ret = rpcClient.testDerivedStructBaseStruct(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testDerivedStructBaseStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testDerivedStructBaseStruct(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testDerivedStructBaseStruct(): Incorrect return value", equals(x, ret));
        }
    }
    
    //  org.objectweb.type_test.types.DerivedStructBaseChoice

    protected boolean equals(DerivedStructBaseChoice x, DerivedStructBaseChoice y) {
        return equals((SimpleChoice)x, (SimpleChoice)y)
            && (Float.compare(x.getVarFloatExt(), y.getVarFloatExt()) == 0)
            && (x.getVarStringExt().equals(y.getVarStringExt()))
            && (x.getAttrString().equals(y.getAttrString()));
    }
    
    public void testDerivedStructBaseChoice() throws Exception {
        DerivedStructBaseChoice x = new DerivedStructBaseChoice();
        //Base
        x.setVarString("BaseChoice-x");
        //Derived
        x.setVarFloatExt(-3.14f);
        x.setVarStringExt("DerivedStruct-x");
        x.setAttrString("DerivedAttr-x");

        DerivedStructBaseChoice yOrig = new DerivedStructBaseChoice();
        //Base
        yOrig.setVarFloat(-9.14f);
        //Derived
        yOrig.setVarFloatExt(1.414f);
        yOrig.setVarStringExt("DerivedStruct-y");
        yOrig.setAttrString("DerivedAttr-y");

        Holder<DerivedStructBaseChoice> y = new Holder<DerivedStructBaseChoice>(yOrig);
        Holder<DerivedStructBaseChoice> z = new Holder<DerivedStructBaseChoice>();
        DerivedStructBaseChoice ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedStructBaseChoice(x, y, z);
        } else {
            ret = rpcClient.testDerivedStructBaseChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testDerivedStructBaseChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testDerivedStructBaseChoice(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testDerivedStructBaseChoice(): Incorrect return value", equals(x, ret));
        }
    }
    
    //  org.objectweb.type_test.types.DerivedChoiceBaseStruct

    protected boolean equals(DerivedChoiceBaseStruct x, DerivedChoiceBaseStruct y) {
        boolean isEquals = x.getAttrString().equals(y.getAttrString());
        if (x.getVarStringExt() != null && y.getVarStringExt() != null) {
            isEquals &= x.getVarStringExt().equals(y.getVarStringExt());
        } else {
            isEquals &= x.getVarFloatExt() != null && y.getVarFloatExt() != null
                && x.getVarFloatExt().compareTo(y.getVarFloatExt()) == 0;
        }
        return isEquals && equals((SimpleStruct)x, (SimpleStruct)y);
    }
    
    public void testDerivedChoiceBaseStruct() throws Exception {
        DerivedChoiceBaseStruct x = new DerivedChoiceBaseStruct();
        //Base
        x.setVarFloat(3.14f);
        x.setVarInt(new BigInteger("42"));
        x.setVarString("BaseStruct-x");
        x.setVarAttrString("BaseStructAttr-x");
        //Derived
        x.setVarStringExt("DerivedChoice-x");
        x.setAttrString("DerivedAttr-x");

        DerivedChoiceBaseStruct yOrig = new DerivedChoiceBaseStruct();
        // Base
        yOrig.setVarFloat(-9.14f);
        yOrig.setVarInt(new BigInteger("10"));
        yOrig.setVarString("BaseStruct-y");
        yOrig.setVarAttrString("BaseStructAttr-y");
        // Derived
        yOrig.setVarFloatExt(1.414f);
        yOrig.setAttrString("DerivedAttr-y");

        Holder<DerivedChoiceBaseStruct> y = new Holder<DerivedChoiceBaseStruct>(yOrig);
        Holder<DerivedChoiceBaseStruct> z = new Holder<DerivedChoiceBaseStruct>();
        DerivedChoiceBaseStruct ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedChoiceBaseStruct(x, y, z);
        } else {
            ret = rpcClient.testDerivedChoiceBaseStruct(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testDerivedChoiceBaseStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testDerivedChoiceBaseStruct(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testDerivedChoiceBaseStruct(): Incorrect return value", equals(x, ret));
        }
    }
    
    // org.objectweb.type_test.types.DerivedChoiceBaseArray

    protected boolean equals(DerivedChoiceBaseArray x, DerivedChoiceBaseArray y) {
        boolean isEquals = x.getAttrStringExt().equals(y.getAttrStringExt());
        if (x.getVarFloatExt() != null && y.getVarFloatExt() != null) {
            isEquals &= x.getVarFloatExt().compareTo(y.getVarFloatExt()) == 0;
        } else {
            isEquals &= x.getVarStringExt() != null && y.getVarStringExt() != null
                && x.getVarStringExt().equals(y.getVarStringExt());
        }
        return isEquals && equals((UnboundedArray)x, (UnboundedArray)y);
    }
    
    public void testDerivedChoiceBaseArray() throws Exception {
        DerivedChoiceBaseArray x = new DerivedChoiceBaseArray();
        //Base
        x.getItem().addAll(Arrays.asList("AAA", "BBB", "CCC"));
        //Derived
        x.setVarStringExt("DerivedChoice-x");
        x.setAttrStringExt("DerivedAttr-x");

        DerivedChoiceBaseArray yOrig = new DerivedChoiceBaseArray();
        //Base
        yOrig.getItem().addAll(Arrays.asList("XXX", "YYY", "ZZZ"));
        //Derived
        yOrig.setVarFloatExt(1.414f);
        yOrig.setAttrStringExt("DerivedAttr-y");

        Holder<DerivedChoiceBaseArray> y = new Holder<DerivedChoiceBaseArray>(yOrig);
        Holder<DerivedChoiceBaseArray> z = new Holder<DerivedChoiceBaseArray>();
        DerivedChoiceBaseArray ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedChoiceBaseArray(x, y, z);
        } else {
            ret = rpcClient.testDerivedChoiceBaseArray(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testDerivedChoiceBaseArray(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testDerivedChoiceBaseArray(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testDerivedChoiceBaseArray(): Incorrect return value",
                       equals(ret, x));
        }
    }

    // org.objectweb.type_test.types.DerivedChoiceBaseChoice
    
    protected boolean equals(DerivedChoiceBaseChoice x, DerivedChoiceBaseChoice y) {
        boolean isEquals = x.getAttrString().equals(y.getAttrString());
        if (x.getVarStringExt() != null && y.getVarStringExt() != null) {
            isEquals &= x.getVarStringExt().equals(y.getVarStringExt());
        } else {
            isEquals &= x.getVarFloatExt() != null && y.getVarFloatExt() != null
                && x.getVarFloatExt().compareTo(y.getVarFloatExt()) == 0;
        }
        return isEquals && equals((SimpleChoice)x, (SimpleChoice)y);
    }
    
    public void testDerivedChoiceBaseChoice() throws Exception {
        DerivedChoiceBaseChoice x = new DerivedChoiceBaseChoice();
        //Base
        x.setVarString("BaseChoice-x");
        //Derived
        x.setVarStringExt("DerivedChoice-x");
        x.setAttrString("DerivedAttr-x");

        DerivedChoiceBaseChoice yOrig = new DerivedChoiceBaseChoice();
        //Base
        yOrig.setVarFloat(-9.14f);
        //Derived
        yOrig.setVarFloatExt(1.414f);
        yOrig.setAttrString("DerivedAttr-y");

        Holder<DerivedChoiceBaseChoice> y = new Holder<DerivedChoiceBaseChoice>(yOrig);
        Holder<DerivedChoiceBaseChoice> z = new Holder<DerivedChoiceBaseChoice>();
        DerivedChoiceBaseChoice ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedChoiceBaseChoice(x, y, z);
        } else {
            ret = rpcClient.testDerivedChoiceBaseChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testDerivedChoiceBaseChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testDerivedChoiceBaseChoice(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testDerivedChoiceBaseChoice(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.DerivedNoContent

    protected boolean equals(DerivedNoContent x, DerivedNoContent y) {
        return equals((SimpleStruct)x, (SimpleStruct)y)
            && x.getVarAttrString().equals(y.getVarAttrString());
    }
    
    public void testDerivedNoContent() throws Exception {
        DerivedNoContent x = new DerivedNoContent();
        x.setVarFloat(3.14f);
        x.setVarInt(new BigInteger("42"));
        x.setVarString("BaseStruct-x");
        x.setVarAttrString("BaseStructAttr-x");

        DerivedNoContent yOrig = new DerivedNoContent();
        yOrig.setVarFloat(1.414f);
        yOrig.setVarInt(new BigInteger("13"));
        yOrig.setVarString("BaseStruct-y");
        yOrig.setVarAttrString("BaseStructAttr-y");

        Holder<DerivedNoContent> y = new Holder<DerivedNoContent>(yOrig);
        Holder<DerivedNoContent> z = new Holder<DerivedNoContent>();
        DerivedNoContent ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedNoContent(x, y, z);
        } else {
            ret = rpcClient.testDerivedNoContent(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testDerivedNoContent(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testDerivedNoContent(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testDerivedNoContent(): Incorrect return value", equals(ret, x));
        }
    }

    // org.objectweb.type_test.types.DerivedStructBaseEmpty

    protected boolean equals(DerivedStructBaseEmpty x, DerivedStructBaseEmpty y) {
        return (x.getVarFloatExt() == y.getVarFloatExt())
            && (x.getVarStringExt().equals(y.getVarStringExt()))
            && (x.getAttrString().equals(y.getAttrString()));
    }
    
    public void testDerivedStructBaseEmpty() throws Exception {
        DerivedStructBaseEmpty x = new DerivedStructBaseEmpty();
        //Derived
        x.setVarFloatExt(-3.14f);
        x.setVarStringExt("DerivedStruct-x");
        x.setAttrString("DerivedAttr-x");

        DerivedStructBaseEmpty yOrig = new DerivedStructBaseEmpty();
        //Derived
        yOrig.setVarFloatExt(1.414f);
        yOrig.setVarStringExt("DerivedStruct-y");
        yOrig.setAttrString("DerivedAttr-y");

        Holder<DerivedStructBaseEmpty> y = new Holder<DerivedStructBaseEmpty>(yOrig);
        Holder<DerivedStructBaseEmpty> z = new Holder<DerivedStructBaseEmpty>();
        DerivedStructBaseEmpty ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedStructBaseEmpty(x, y, z);
        } else {
            ret = rpcClient.testDerivedStructBaseEmpty(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testDerivedStructBaseEmpty(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testDerivedStructBaseEmpty(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testDerivedStructBaseEmpty(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.DerivedEmptyBaseEmptyAll

    public void testDerivedEmptyBaseEmptyAll() throws Exception {
        DerivedEmptyBaseEmptyAll x = new DerivedEmptyBaseEmptyAll();
        DerivedEmptyBaseEmptyAll yOrig = new DerivedEmptyBaseEmptyAll();
        Holder<DerivedEmptyBaseEmptyAll> y = new Holder<DerivedEmptyBaseEmptyAll>(yOrig);
        Holder<DerivedEmptyBaseEmptyAll> z = new Holder<DerivedEmptyBaseEmptyAll>();
        DerivedEmptyBaseEmptyAll ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedEmptyBaseEmptyAll(x, y, z);
        } else {
            ret = rpcClient.testDerivedEmptyBaseEmptyAll(x, y, z);
        }
        // not much to check
        assertNotNull(y.value);
        assertNotNull(z.value);
        assertNotNull(ret);
    }

    // org.objectweb.type_test.types.DerivedEmptyBaseEmptyChoice

    public void testDerivedEmptyBaseEmptyChoice() throws Exception {
        DerivedEmptyBaseEmptyChoice x = new DerivedEmptyBaseEmptyChoice();
        DerivedEmptyBaseEmptyChoice yOrig = new DerivedEmptyBaseEmptyChoice();
        Holder<DerivedEmptyBaseEmptyChoice> y = new Holder<DerivedEmptyBaseEmptyChoice>(yOrig);
        Holder<DerivedEmptyBaseEmptyChoice> z = new Holder<DerivedEmptyBaseEmptyChoice>();
        DerivedEmptyBaseEmptyChoice ret;
        if (testDocLiteral) {
            ret = docClient.testDerivedEmptyBaseEmptyChoice(x, y, z);
        } else {
            ret = rpcClient.testDerivedEmptyBaseEmptyChoice(x, y, z);
        }
        // not much to check
        assertNotNull(y.value);
        assertNotNull(z.value);
        assertNotNull(ret);
    }

    // org.objectweb.type_test.types.RestrictedChoiceBaseChoice

    protected boolean equals(RestrictedChoiceBaseChoice x, RestrictedChoiceBaseChoice y) {
        if (x.getVarFloat() != null && y.getVarFloat() != null) {
            return x.getVarFloat().compareTo(y.getVarFloat()) == 0;
        } else {
            return x.getVarInt() != null && y.getVarInt() != null
                && x.getVarInt().compareTo(y.getVarInt()) == 0;
        }
    }
    
    public void testRestrictedChoiceBaseChoice() throws Exception {
        RestrictedChoiceBaseChoice x = new RestrictedChoiceBaseChoice();
        x.setVarInt(12);

        RestrictedChoiceBaseChoice yOrig = new RestrictedChoiceBaseChoice();
        yOrig.setVarFloat(-9.14f);

        Holder<RestrictedChoiceBaseChoice> y = new Holder<RestrictedChoiceBaseChoice>(yOrig);
        Holder<RestrictedChoiceBaseChoice> z = new Holder<RestrictedChoiceBaseChoice>();

        RestrictedChoiceBaseChoice ret;
        if (testDocLiteral) {
            ret = docClient.testRestrictedChoiceBaseChoice(x, y, z);
        } else {
            ret = rpcClient.testRestrictedChoiceBaseChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testRestrictedChoiceBaseChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testRestrictedChoiceBaseChoice(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testRestrictedChoiceBaseChoice(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.ComplexTypeWithAttributeGroup

    protected boolean equals(ComplexTypeWithAttributeGroup x,
                             ComplexTypeWithAttributeGroup y) {
        return x.getAttrInt().compareTo(y.getAttrInt()) == 0
            && x.getAttrString().equals(y.getAttrString());
    }
    
    public void testComplexTypeWithAttributeGroup() throws Exception {
        ComplexTypeWithAttributeGroup x = new ComplexTypeWithAttributeGroup();
        x.setAttrInt(new BigInteger("123"));
        x.setAttrString("x123");
        ComplexTypeWithAttributeGroup yOrig = new ComplexTypeWithAttributeGroup();
        yOrig.setAttrInt(new BigInteger("456"));
        yOrig.setAttrString("x456");

        Holder<ComplexTypeWithAttributeGroup> y = new Holder<ComplexTypeWithAttributeGroup>(yOrig);
        Holder<ComplexTypeWithAttributeGroup> z = new Holder<ComplexTypeWithAttributeGroup>();
        ComplexTypeWithAttributeGroup ret;
        if (testDocLiteral) {
            ret = docClient.testComplexTypeWithAttributeGroup(x, y, z);
        } else {
            ret = rpcClient.testComplexTypeWithAttributeGroup(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testComplexTypeWithAttributeGroup(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testComplexTypeWithAttributeGroup(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testComplexTypeWithAttributeGroup(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.ComplexTypeWithAttributeGroup1

    protected boolean equals(ComplexTypeWithAttributeGroup1 x,
                             ComplexTypeWithAttributeGroup1 y) {
        return x.getAttrInt().compareTo(y.getAttrInt()) == 0
            && x.getAttrFloat().compareTo(y.getAttrFloat()) == 0
            && x.getAttrString().equals(y.getAttrString());
    }
    
    public void testComplexTypeWithAttributeGroup1() throws Exception {
        ComplexTypeWithAttributeGroup1 x = new ComplexTypeWithAttributeGroup1();
        x.setAttrInt(new BigInteger("123"));
        x.setAttrString("x123");
        x.setAttrFloat(new Float(3.14f));
        ComplexTypeWithAttributeGroup1 yOrig = new ComplexTypeWithAttributeGroup1();
        yOrig.setAttrInt(new BigInteger("456"));
        yOrig.setAttrString("x456");
        yOrig.setAttrFloat(new Float(6.28f));

        Holder<ComplexTypeWithAttributeGroup1> y = new Holder<ComplexTypeWithAttributeGroup1>(yOrig);
        Holder<ComplexTypeWithAttributeGroup1> z = new Holder<ComplexTypeWithAttributeGroup1>();
        ComplexTypeWithAttributeGroup1 ret;
        if (testDocLiteral) {
            ret = docClient.testComplexTypeWithAttributeGroup1(x, y, z);
        } else {
            ret = rpcClient.testComplexTypeWithAttributeGroup1(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testComplexTypeWithAttributeGroup1(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testComplexTypeWithAttributeGroup1(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testComplexTypeWithAttributeGroup1(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.SequenceWithGroupSeq
    protected boolean equals(SequenceWithGroupSeq x, SequenceWithGroupSeq y) {
        return x.getVarInt() == y.getVarInt()
            && Float.compare(x.getVarFloat(), y.getVarFloat()) == 0
            && x.getVarString().equals(y.getVarString())
            && x.getVarOtherInt() == y.getVarOtherInt()
            && Float.compare(x.getVarOtherFloat(), y.getVarOtherFloat()) == 0
            && x.getVarOtherString().equals(y.getVarOtherString());
    }
    
    // XXX - ri generated code is flattened - no nested structs
    public void testSequenceWithGroupSeq() throws Exception {
        //BatchElementsSeq x1 = new BatchElementsSeq();
        //BatchElementsSeq y1 = new BatchElementsSeq();
        SequenceWithGroupSeq x = new SequenceWithGroupSeq();
        x.setVarInt(100);         
        x.setVarString("hello");
        x.setVarFloat(1.1f); 
        x.setVarOtherInt(11);
        x.setVarOtherString("world");
        x.setVarOtherFloat(10.1f);
        SequenceWithGroupSeq yOrig = new SequenceWithGroupSeq();
        yOrig.setVarInt(11);
        yOrig.setVarString("world");
        yOrig.setVarFloat(10.1f);
        yOrig.setVarOtherInt(100);
        yOrig.setVarOtherString("hello");
        yOrig.setVarOtherFloat(1.1f);

        Holder<SequenceWithGroupSeq> y = new Holder<SequenceWithGroupSeq>(yOrig);
        Holder<SequenceWithGroupSeq> z = new Holder<SequenceWithGroupSeq>();

        SequenceWithGroupSeq ret;
        if (testDocLiteral) {
            ret = docClient.testSequenceWithGroupSeq(x, y, z);
        } else {
            ret = rpcClient.testSequenceWithGroupSeq(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testSequenceWithGroupSeq(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testSequenceWithGroupSeq(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testSequenceWithGroupSeq(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.SequenceWithGroupChoice

    protected boolean equals(SequenceWithGroupChoice x, SequenceWithGroupChoice y) {
        if (x.getVarInt() != null && y.getVarInt() != null) {
            if (x.getVarInt().compareTo(y.getVarInt()) != 0) {
                return false;
            }
        } else if (x.getVarFloat() != null && y.getVarFloat() != null) {
            if (x.getVarFloat().compareTo(y.getVarFloat()) != 0) {
                return false;
            }
        } else if (x.getVarString() != null && y.getVarString() != null) {
            if (!x.getVarString().equals(y.getVarString())) {
                return false;
            }
        } else {
            return false;
        }
        if (x.getVarOtherInt() != null && y.getVarOtherInt() != null) {
            if (x.getVarOtherInt().compareTo(y.getVarOtherInt()) != 0) {
                return false;
            }
        } else if (x.getVarOtherFloat() != null && y.getVarOtherFloat() != null) {
            if (x.getVarOtherFloat().compareTo(y.getVarOtherFloat()) != 0) {
                return false;
            }
        } else if (x.getVarOtherString() != null && y.getVarOtherString() != null) {
            return x.getVarOtherString().equals(y.getVarOtherString());
        } else {
            return false;
        }
        return true;
    }
    
    // XXX - ri generated code is flattened - no nested choice
    public void testSequenceWithGroupChoice() throws Exception {
        //BatchElementsChoice x1 = new BatchElementsChoice();
        //BatchElementsChoice y1 = new BatchElementsChoice();

        SequenceWithGroupChoice x = new SequenceWithGroupChoice();
        x.setVarFloat(1.1f);
        x.setVarOtherString("world");
        SequenceWithGroupChoice yOrig = new SequenceWithGroupChoice();
        yOrig.setVarOtherFloat(2.2f);
        yOrig.setVarString("world");

        Holder<SequenceWithGroupChoice> y = new Holder<SequenceWithGroupChoice>(yOrig);
        Holder<SequenceWithGroupChoice> z = new Holder<SequenceWithGroupChoice>();

        SequenceWithGroupChoice ret;
        if (testDocLiteral) {
            ret = docClient.testSequenceWithGroupChoice(x, y, z);
        } else {
            ret = rpcClient.testSequenceWithGroupChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testSequenceWithGroupChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testSequenceWithGroupChoice(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testSequenceWithGroupChoice(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.SequenceWithGroups

    protected boolean equals(SequenceWithGroups x, SequenceWithGroups y) {
        if (x.getVarOtherInt() != null && y.getVarOtherInt() != null) {
            if (x.getVarOtherInt().compareTo(y.getVarOtherInt()) != 0) {
                return false;
            }
        } else if (x.getVarOtherFloat() != null && y.getVarOtherFloat() != null) {
            if (x.getVarOtherFloat().compareTo(y.getVarOtherFloat()) != 0) {
                return false;
            }
        } else if (x.getVarOtherString() != null && y.getVarOtherString() != null) {
            if (!x.getVarOtherString().equals(y.getVarOtherString())) {
                return false;
            }
        } else {
            return false;
        }
        return x.getVarInt() == y.getVarInt()
            && Float.compare(x.getVarFloat(), y.getVarFloat()) == 0
            && x.getVarString().equals(y.getVarString());
    }
    
    // XXX - ri generated code is flattened - no nested struct/choice
    public void testSequenceWithGroups() throws Exception {
        //BatchElementsSeq x1 = new BatchElementsSeq();
        //BatchElementsSeq y1 = new BatchElementsSeq();
        //BatchElementsChoice x2 = new BatchElementsChoice();
        //BatchElementsChoice y2 = new BatchElementsChoice();
        SequenceWithGroups x = new SequenceWithGroups();
        x.setVarInt(100);
        x.setVarString("hello");
        x.setVarFloat(1.1f);
        x.setVarOtherFloat(1.1f);

        SequenceWithGroups yOrig = new SequenceWithGroups();
        yOrig.setVarInt(11);
        yOrig.setVarString("world");
        yOrig.setVarFloat(10.1f);
        yOrig.setVarOtherString("world");

        Holder<SequenceWithGroups> y = new Holder<SequenceWithGroups>(yOrig);
        Holder<SequenceWithGroups> z = new Holder<SequenceWithGroups>();

        SequenceWithGroups ret;
        if (testDocLiteral) {
            ret = docClient.testSequenceWithGroups(x, y, z);
        } else {
            ret = rpcClient.testSequenceWithGroups(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testSequenceWithGroups(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testSequenceWithGroups(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testSequenceWithGroups(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.SequenceWithOccuringGroup

    protected boolean equals(SequenceWithOccuringGroup x, SequenceWithOccuringGroup y) {
        return equalsFloatIntStringList(x.getBatchElementsSeq(), y.getBatchElementsSeq());
    }
    
    // XXX - ri generated code is flattened - no nested structs
    public void testSequenceWithOccuringGroup() throws Exception {
        //BatchElementsSeq bx1 = new BatchElementsSeq();
        //BatchElementsSeq bx2 = new BatchElementsSeq();
        SequenceWithOccuringGroup x = new SequenceWithOccuringGroup();
        x.getBatchElementsSeq().add(100);
        x.getBatchElementsSeq().add("hello");
        x.getBatchElementsSeq().add(1.1f);

        SequenceWithOccuringGroup yOrig = new SequenceWithOccuringGroup();
        yOrig.getBatchElementsSeq().add(200);
        yOrig.getBatchElementsSeq().add("world");
        yOrig.getBatchElementsSeq().add(2.2f);

        Holder<SequenceWithOccuringGroup> y = new Holder<SequenceWithOccuringGroup>(yOrig);
        Holder<SequenceWithOccuringGroup> z = new Holder<SequenceWithOccuringGroup>();

        SequenceWithOccuringGroup ret;
        if (testDocLiteral) {
            ret = docClient.testSequenceWithOccuringGroup(x, y, z);
        } else {
            ret = rpcClient.testSequenceWithOccuringGroup(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testGroupDirectlyInComplexType(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testGroupDirectlyInComplexType(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testGroupDirectlyInComplexType(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.GroupDirectlyInComplexType

    protected boolean equals(GroupDirectlyInComplexType x, GroupDirectlyInComplexType y) {
        return x.getVarInt() == y.getVarInt() 
            && x.getVarString().equals(y.getVarString())
            && Float.compare(x.getVarFloat(), y.getVarFloat()) == 0
            && x.getAttr1().equals(y.getAttr1());
    }
    
    // XXX - ri generated code is flattened - no nested struct
    public void testGroupDirectlyInComplexType() throws Exception {
        //BatchElementsSeq x1 = new BatchElementsSeq();
        //BatchElementsSeq y1 = new BatchElementsSeq();

        GroupDirectlyInComplexType x = new GroupDirectlyInComplexType();
        x.setVarInt(100);
        x.setVarString("hello");
        x.setVarFloat(1.1f);
        x.setAttr1(new Integer(1));
        GroupDirectlyInComplexType yOrig = new GroupDirectlyInComplexType();
        yOrig.setVarInt(11);
        yOrig.setVarString("world");
        yOrig.setVarFloat(10.1f);
        yOrig.setAttr1(new Integer(2)); 

        Holder<GroupDirectlyInComplexType> y = new Holder<GroupDirectlyInComplexType>(yOrig);
        Holder<GroupDirectlyInComplexType> z = new Holder<GroupDirectlyInComplexType>();

        GroupDirectlyInComplexType ret;
        if (testDocLiteral) {
            ret = docClient.testGroupDirectlyInComplexType(x, y, z);
        } else {
            ret = rpcClient.testGroupDirectlyInComplexType(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testGroupDirectlyInComplexType(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testGroupDirectlyInComplexType(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testGroupDirectlyInComplexType(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.ComplexTypeWithAttributes

    protected boolean equals(ComplexTypeWithAttributes x, ComplexTypeWithAttributes y) {
        return x.getAttrInt().equals(y.getAttrInt())
            && x.getAttrString().equals(y.getAttrString());
    }

    public void testComplexTypeWithAttributes() throws Exception {
        ComplexTypeWithAttributes x = new ComplexTypeWithAttributes();
        x.setAttrInt(new BigInteger("123"));
        x.setAttrString("x123");
        ComplexTypeWithAttributes yOrig = new ComplexTypeWithAttributes();
        yOrig.setAttrInt(new BigInteger("456"));
        yOrig.setAttrString("x456");

        Holder<ComplexTypeWithAttributes> y = new Holder<ComplexTypeWithAttributes>(yOrig);
        Holder<ComplexTypeWithAttributes> z = new Holder<ComplexTypeWithAttributes>();
        ComplexTypeWithAttributes ret;
        if (testDocLiteral) {
            ret = docClient.testComplexTypeWithAttributes(x, y, z);
        } else {
            ret = rpcClient.testComplexTypeWithAttributes(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testComplexTypeWithAttributes(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testComplexTypeWithAttributes(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testComplexTypeWithAttributes(): Incorrect return value", equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.StructWithAny

    public void assertEqualsStructWithAny(StructWithAny a, StructWithAny b) throws Exception {
        assertEquals("StructWithAny names don't match", a.getName(), b.getName());
        assertEquals("StructWithAny addresses don't match", a.getAddress(), b.getAddress());
        assertEquals(a.getAny(), b.getAny());
    }
    
    public void assertEquals(Element elA, Element elB) throws Exception {
        if (elA instanceof SOAPElement && elB instanceof SOAPElement) {
            SOAPElement soapA = (SOAPElement)elA;
            SOAPElement soapB = (SOAPElement)elB;
            assertEquals("StructWithAny soap element names don't match",
                soapA.getElementName(), soapB.getElementName());
            assertEquals("StructWithAny soap element text nodes don't match",
                soapA.getValue(), soapB.getValue());
            
            Iterator itExp = soapA.getChildElements();
            Iterator itGen = soapB.getChildElements();
            while (itExp.hasNext()) {
                if (!itGen.hasNext()) {
                    fail("Incorrect number of child elements inside any");
                }
                Object objA = itExp.next();         
                Object objB = itGen.next();
                if (objA instanceof SOAPElement) {
                    if (objB instanceof SOAPElement) {
                        assertEquals((SOAPElement)objA, (SOAPElement)objB);
                    } else {
                        fail("No matching soap element.");
                    }
                }
            }
        }
    }
    
    public void testStructWithAny() throws Exception {
        StructWithAny swa = new StructWithAny();
        swa.setName("Name");
        swa.setAddress("Some Address");

        StructWithAny yOrig = new StructWithAny();
        yOrig.setName("Name2");
        yOrig.setAddress("Some Other Address");

        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement x = factory.createElement("hello", "foo", "http://some.url.com");
        x.addNamespaceDeclaration("foo", "http://some.url.com");
        x.addTextNode("This is the text of the node");

        SOAPElement x2 = factory.createElement("hello2", "foo", "http://some.url.com");
        x2.addNamespaceDeclaration("foo", "http://some.url.com");
        x2.addTextNode("This is the text of the node for the second struct");

        swa.setAny(x);
        yOrig.setAny(x2);

        Holder<StructWithAny> y = new Holder<StructWithAny>(yOrig);
        Holder<StructWithAny> z = new Holder<StructWithAny>();

        StructWithAny ret;
        if (testDocLiteral) {
            ret = docClient.testStructWithAny(swa, y, z);
        } else {
            ret = rpcClient.testStructWithAny(swa, y, z);
        }
        if (!perfTestOnly) {
            assertEqualsStructWithAny(swa, y.value);
            assertEqualsStructWithAny(yOrig, z.value);
            assertEqualsStructWithAny(swa, ret);
        }
    }

    // org.objectweb.type_test.types.StructWithAnyArray

    public void assertEqualsStructWithAnyArray(StructWithAnyArray a, StructWithAnyArray b) throws Exception {
        assertEquals("StructWithAny names don't match", a.getName(), b.getName());
        assertEquals("StructWithAny addresses don't match", a.getAddress(), b.getAddress());

        List<Element> ae = a.getAny();
        List<Element> be = b.getAny();
        
        assertEquals("StructWithAny soap element lengths don't match", ae.size(), be.size());
        for (int i = 0; i < ae.size(); i++) {
            assertEquals(ae.get(i), be.get(i));
        }
    }

    public void testStructWithAnyArray() throws Exception {
        StructWithAnyArray swa = new StructWithAnyArray();
        swa.setName("Name");
        swa.setAddress("Some Address");

        StructWithAnyArray yOrig = new StructWithAnyArray();
        yOrig.setName("Name2");
        yOrig.setAddress("Some Other Address");

        SOAPFactory factory = SOAPFactory.newInstance();
        SOAPElement x = factory.createElement("hello", "foo", "http://some.url.com");
        x.addNamespaceDeclaration("foo", "http://some.url.com");
        x.addTextNode("This is the text of the node");

        SOAPElement x2 = factory.createElement("hello2", "foo", "http://some.url.com");
        x2.addNamespaceDeclaration("foo", "http://some.url.com");
        x2.addTextNode("This is the text of the node for the second struct");

        swa.getAny().add(x);
        yOrig.getAny().add(x2);

        Holder<StructWithAnyArray> y = new Holder<StructWithAnyArray>(yOrig);
        Holder<StructWithAnyArray> z = new Holder<StructWithAnyArray>();

        StructWithAnyArray ret;
        if (testDocLiteral) {
            ret = docClient.testStructWithAnyArray(swa, y, z);
        } else {
            ret = rpcClient.testStructWithAnyArray(swa, y, z);
        }
        if (!perfTestOnly) {
            assertEqualsStructWithAnyArray(swa, y.value);
            assertEqualsStructWithAnyArray(yOrig, z.value);
            assertEqualsStructWithAnyArray(swa, ret);
        }
    }

    public void testStructWithAnyStrict() throws Exception {
        // XXX - only added to the soap typetest
    }

    public void testStructWithAnyArrayLax() throws Exception {
        // XXX - only added to the soap typetest
    }

    // org.objectweb.type_test.types.IDTypeAttribute

    protected boolean equalsIDTypeAttribute(IDTypeAttribute x, IDTypeAttribute y) {
        return equalsNilable(x.getId(), y.getId());
    }

    public void testIDTypeAttribute() throws Exception {
        IDTypeAttribute x = new IDTypeAttribute();
        x.setId("x123");
        IDTypeAttribute yOrig = new IDTypeAttribute();
        //yOrig.setId("x456");

        Holder<IDTypeAttribute> y = new Holder<IDTypeAttribute>(yOrig);
        Holder<IDTypeAttribute> z = new Holder<IDTypeAttribute>();
        IDTypeAttribute ret;
        if (testDocLiteral) {
            ret = docClient.testIDTypeAttribute(x, y, z);
        } else {
            ret = rpcClient.testIDTypeAttribute(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testIDTypeAttribute(): Incorrect value for inout param",
                       equalsIDTypeAttribute(x, y.value));
            assertTrue("testIDTypeAttribute(): Incorrect value for out param",
                       equalsIDTypeAttribute(yOrig, z.value));
            assertTrue("testIDTypeAttribute(): Incorrect return value",
                       equalsIDTypeAttribute(x, ret));
        }
    }

    // org.objectweb.type_test.types.MultipleOccursSequenceInSequence
    protected boolean equals(MultipleOccursSequenceInSequence x, MultipleOccursSequenceInSequence y) {
        int size = x.getValue().size();
        if (size != y.getValue().size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (x.getValue().get(i).compareTo(y.getValue().get(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    // XXX - ri generated code is flattened - no nested struct
    public void testMultipleOccursSequenceInSequence() throws Exception {
        //MultipleOccursSequenceInSequence.MultipleOccursSequenceInSequence1 x1 =
        //    new MultipleOccursSequenceInSequence.MultipleOccursSequenceInSequence1();
        //MultipleOccursSequenceInSequence.MultipleOccursSequenceInSequence1 y1 =
        //    new MultipleOccursSequenceInSequence.MultipleOccursSequenceInSequence1();
        MultipleOccursSequenceInSequence x = new MultipleOccursSequenceInSequence();
        x.getValue().add(new BigInteger("32"));
        MultipleOccursSequenceInSequence yOriginal = new MultipleOccursSequenceInSequence();
        yOriginal.getValue().add(new BigInteger("3200"));

        Holder<MultipleOccursSequenceInSequence> y =
            new Holder<MultipleOccursSequenceInSequence>(yOriginal);
        Holder<MultipleOccursSequenceInSequence> z =
            new Holder<MultipleOccursSequenceInSequence>();

        MultipleOccursSequenceInSequence ret;
        if (testDocLiteral) {
            ret = docClient.testMultipleOccursSequenceInSequence(x, y, z);
        } else {
            ret = rpcClient.testMultipleOccursSequenceInSequence(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testMultipleOccursSequenceInSequence(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testMultipleOccursSequenceInSequence(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testMultipleOccursSequenceInSequence(): Incorrect return value",
                       equals(x, ret));
        }
    }
 
    // org.objectweb.type_test.types.StructWithBinary;
    
    protected boolean equals(StructWithBinary x, StructWithBinary y) {
        return Arrays.equals(x.getBase64(), y.getBase64())
            && Arrays.equals(x.getHex(), y.getHex());
    }
    
    public void testStructWithBinary() throws Exception {
        StructWithBinary x = new StructWithBinary();
        x.setBase64("base64Binary_x".getBytes());
        x.setHex("hexBinary_x".getBytes());

        StructWithBinary yOriginal = new StructWithBinary();
        yOriginal.setBase64("base64Binary_y".getBytes());
        yOriginal.setHex("hexBinary_y".getBytes());

        Holder<StructWithBinary> y = new Holder<StructWithBinary>(yOriginal);
        Holder<StructWithBinary> z = new Holder<StructWithBinary>();

        StructWithBinary ret;
        if (testDocLiteral) {
            ret = docClient.testStructWithBinary(x, y, z);
        } else {
            ret = rpcClient.testStructWithBinary(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testStructWithBinary(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testStructWithBinary(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testStructWithBinary(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.ChoiceWithBinary;

    protected boolean equals(ChoiceWithBinary x, ChoiceWithBinary y) {
        if (x.getBase64() != null && y.getBase64() != null) {
            return Arrays.equals(x.getBase64(), y.getBase64());
        } else {
            return x.getHex() != null && y.getHex() != null
                && Arrays.equals(x.getHex(), y.getHex());
        }
    }

    public void testChoiceWithBinary() throws Exception {
        ChoiceWithBinary x = new ChoiceWithBinary();
        x.setBase64("base64Binary_x".getBytes());

        ChoiceWithBinary yOriginal = new ChoiceWithBinary();
        yOriginal.setHex("hexBinary_y".getBytes());

        Holder<ChoiceWithBinary> y = new Holder<ChoiceWithBinary>(yOriginal);
        Holder<ChoiceWithBinary> z = new Holder<ChoiceWithBinary>();

        ChoiceWithBinary ret;
        if (testDocLiteral) {
            ret = docClient.testChoiceWithBinary(x, y, z);
        } else {
            ret = rpcClient.testChoiceWithBinary(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testChoiceWithBinary(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testChoiceWithBinary(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testChoiceWithBinary(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.ChoiceWithGroupChoice;

    protected boolean equals(ChoiceWithGroupChoice x, ChoiceWithGroupChoice y) {
        if (x.getVarFloat() != null  && y.getVarFloat() != null) {
            return x.getVarFloat().compareTo(y.getVarFloat()) == 0;
        }
        if (x.getVarInt() != null  && y.getVarInt() != null) {
            return x.getVarInt().compareTo(y.getVarInt()) == 0;
        }
        if (x.getVarString() != null  && y.getVarString() != null) {
            return x.getVarString().equals(y.getVarString());
        }
        if (x.getVarOtherFloat() != null  && y.getVarOtherFloat() != null) {
            return x.getVarOtherFloat().compareTo(y.getVarOtherFloat()) == 0;
        }
        if (x.getVarOtherInt() != null  && y.getVarOtherInt() != null) {
            return x.getVarOtherInt().compareTo(y.getVarOtherInt()) == 0;
        }
        if (x.getVarOtherString() != null  && y.getVarOtherString() != null) {
            return x.getVarOtherString().equals(y.getVarOtherString());
        }
        return false;
    }

    // XXX - Generated code flattens nested choice
    public void testChoiceWithGroupChoice() throws Exception {
        //BatchElementsChoice x1 = new BatchElementsChoice();
        //BatchElementsChoice y1 = new BatchElementsChoice();

        ChoiceWithGroupChoice x = new ChoiceWithGroupChoice();
        x.setVarFloat(1.1f);
        ChoiceWithGroupChoice yOrig = new ChoiceWithGroupChoice();
        yOrig.setVarOtherString("world");

        Holder<ChoiceWithGroupChoice> y = new Holder<ChoiceWithGroupChoice>(yOrig);
        Holder<ChoiceWithGroupChoice> z = new Holder<ChoiceWithGroupChoice>();

        ChoiceWithGroupChoice ret;
        if (testDocLiteral) {
            ret = docClient.testChoiceWithGroupChoice(x, y, z);
        } else {
            ret = rpcClient.testChoiceWithGroupChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testChoiceWithGroupChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testChoiceWithGroupChoice(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testChoiceWithGroupChoice(): Incorrect return value",
                       equals(x, ret));
        }
    }
    
    // org.objectweb.type_test.types.ChoiceWithGroupSeq;

    protected boolean equals(ChoiceWithGroupSeq x, ChoiceWithGroupSeq y) {
        if (x.getVarInt() != null && x.getVarFloat() != null
            && x.getVarString() != null) {
            if (x.getVarInt().compareTo(y.getVarInt()) != 0) {
                return false;
            }
            if (x.getVarFloat().compareTo(y.getVarFloat()) != 0) {
                return false;
            }
            return x.getVarString().equals(y.getVarString());
        }
        if (x.getVarOtherInt() != null && x.getVarOtherFloat() != null
            && x.getVarOtherString() != null) {
            if (x.getVarOtherInt().compareTo(y.getVarOtherInt()) != 0) {
                return false;
            }
            if (x.getVarOtherFloat().compareTo(y.getVarOtherFloat()) != 0) {
                return false;
            }
            return x.getVarOtherString().equals(y.getVarOtherString());
        }
        return false;
    }
    
    // XXX - Generated code flattens nested structs
    public void testChoiceWithGroupSeq() throws Exception {
        //BatchElementsSeq x1 = new BatchElementsSeq();
        //BatchElementsSeq y1 = new BatchElementsSeq();

        ChoiceWithGroupSeq x = new ChoiceWithGroupSeq();
        x.setVarInt(100);
        x.setVarString("hello");
        x.setVarFloat(1.1f);
        ChoiceWithGroupSeq yOrig = new ChoiceWithGroupSeq();
        yOrig.setVarOtherInt(11);
        yOrig.setVarOtherString("world");
        yOrig.setVarOtherFloat(10.1f);

        Holder<ChoiceWithGroupSeq> y = new Holder<ChoiceWithGroupSeq>(yOrig);
        Holder<ChoiceWithGroupSeq> z = new Holder<ChoiceWithGroupSeq>();

        ChoiceWithGroupSeq ret;
        if (testDocLiteral) {
            ret = docClient.testChoiceWithGroupSeq(x, y, z);
        } else {
            ret = rpcClient.testChoiceWithGroupSeq(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testChoiceWithGroupSeq(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testChoiceWithGroupSeq(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testChoiceWithGroupSeq(): Incorrect return value",
                       equals(x, ret));
        }
    }
    
    // org.objectweb.type_test.types.ChoiceWithGroups;

    protected boolean equals(ChoiceWithGroups x, ChoiceWithGroups y) {
        if (x.getVarInt() != null && x.getVarString() != null
            && x.getVarFloat() != null) {
            if (x.getVarInt().compareTo(y.getVarInt()) == 0 
                && x.getVarString().equals(y.getVarString())
                && x.getVarFloat().compareTo(y.getVarFloat()) == 0) {
                return true;
            }
            return false;
        } else {
            if (x.getVarOtherFloat() != null && y.getVarOtherFloat() != null) {
                return x.getVarOtherFloat().compareTo(y.getVarOtherFloat()) == 0;
            }
            if (x.getVarOtherInt() != null && y.getVarOtherInt() != null) {
                return x.getVarOtherInt().compareTo(y.getVarOtherInt()) == 0;
            }
            if (x.getVarOtherString() != null && y.getVarOtherString() != null) {
                return x.getVarOtherString().equals(y.getVarOtherString());
            }
            return false;
        }
    }
    
    // XXX - Generated code flattens nested structs
    public void testChoiceWithGroups() throws Exception {
        //BatchElementsSeq x1 = new BatchElementsSeq();
        //BatchElementsChoice y1 = new BatchElementsChoice();

        ChoiceWithGroups x = new ChoiceWithGroups();
        x.setVarInt(100);
        x.setVarString("hello");
        x.setVarFloat(1.1f);
        ChoiceWithGroups yOrig = new ChoiceWithGroups();
        yOrig.setVarOtherString("world");

        Holder<ChoiceWithGroups> y = new Holder<ChoiceWithGroups>(yOrig);
        Holder<ChoiceWithGroups> z = new Holder<ChoiceWithGroups>();

        ChoiceWithGroups ret;
        if (testDocLiteral) {
            ret = docClient.testChoiceWithGroups(x, y, z);
        } else {
            ret = rpcClient.testChoiceWithGroups(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testChoiceWithGroups(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testChoiceWithGroups(): Incorrect value for out param",
                       equals(yOrig, z.value));
            assertTrue("testChoiceWithGroups(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.ExtBase64Binary;

    protected boolean equals(ExtBase64Binary x, ExtBase64Binary y) {
        return x.getId() == y.getId() && Arrays.equals(x.getValue(), y.getValue());
    }

    public void testExtBase64Binary() throws Exception {
        ExtBase64Binary x1 = new ExtBase64Binary();
        x1.setValue("base64a".getBytes());
        x1.setId(1);

        ExtBase64Binary y1 = new ExtBase64Binary();
        y1.setValue("base64b".getBytes());
        y1.setId(2);

        Holder<ExtBase64Binary> y1Holder = new Holder<ExtBase64Binary>(y1);
        Holder<ExtBase64Binary> z1 = new Holder<ExtBase64Binary>();
        ExtBase64Binary ret;
        if (testDocLiteral) {
            ret = docClient.testExtBase64Binary(x1, y1Holder, z1);
        } else {
            ret = rpcClient.testExtBase64Binary(x1, y1Holder, z1);
        }

        if (!perfTestOnly) {
            assertTrue("testExtBase64Binary(): Incorrect value for inout param",
                       equals(x1, y1Holder.value));
            assertTrue("testExtBase64Binary(): Incorrect value for out param",
                       equals(y1, z1.value));
            assertTrue("testExtBase64Binary(): Incorrect return value", equals(x1, ret));
        }
    }

    public void testOccuringAll() throws Exception {
        // XXX - BUG #6761
    }

    // XXX - There's not much difference between the OccuringStruct tests.
    // org.objectweb.type_test.types.OccurringStruct;

    protected boolean equals(OccuringStruct x, OccuringStruct y) {
        if (!equalsNilable(x.getVarAttrib(), y.getVarAttrib())) {
            return false;
        }
        return equalsFloatIntStringList(x.getVarFloatAndVarIntAndVarString(),
                                        y.getVarFloatAndVarIntAndVarString());
    }

    protected boolean equalsFloatIntStringList(List<Serializable> xList,
                                               List<Serializable> yList) {
        int size = xList.size();
        if (size != yList.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (xList.get(i) instanceof Float && yList.get(i) instanceof Float) {
                Float fx = (Float)xList.get(i);
                Float fy = (Float)yList.get(i);
                if (fx.compareTo(fy) != 0) {
                    return false;
                }
            } else if (xList.get(i) instanceof Integer && yList.get(i) instanceof Integer) {
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
    
    public void testOccuringStruct() throws Exception {
        OccuringStruct x = new OccuringStruct();
        List<Serializable> theList = x.getVarFloatAndVarIntAndVarString(); 
        theList.add(new Integer(0));
        theList.add(1.14f);
        theList.add("x1");
        theList.add(new Integer(1));
        theList.add(11.14f);
        theList.add("x2");
        x.setVarAttrib("x_attr");

        OccuringStruct yOriginal = new OccuringStruct();
        theList = yOriginal.getVarFloatAndVarIntAndVarString();
        theList.add(3.14f);
        theList.add("y");
        theList.add(new Integer(42));
        yOriginal.setVarAttrib("y_attr");

        Holder<OccuringStruct> y = new Holder<OccuringStruct>(yOriginal);
        Holder<OccuringStruct> z = new Holder<OccuringStruct>();

        OccuringStruct ret;
        if (testDocLiteral) {
            ret = docClient.testOccuringStruct(x, y, z);
        } else {
            ret = rpcClient.testOccuringStruct(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testOccuringStruct(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testOccuringStruct(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testOccuringStruct(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.OccurringStruct1;
    
    protected boolean equals(OccuringStruct1 x, OccuringStruct1 y) {
        return equalsFloatIntStringList(x.getVarFloatAndVarIntAndVarString(),
                                        y.getVarFloatAndVarIntAndVarString());
    }
    
    public void testOccuringStruct1() throws Exception {
        OccuringStruct1 x = new OccuringStruct1();
        List<Serializable> theList = x.getVarFloatAndVarIntAndVarString(); 
        theList.add(1);
        theList.add(2);
        theList.add("x1");

        OccuringStruct1 yOriginal = new OccuringStruct1();
        theList = yOriginal.getVarFloatAndVarIntAndVarString();
        theList.add("y");
        theList.add(11);
        theList.add(22);

        Holder<OccuringStruct1> y = new Holder<OccuringStruct1>(yOriginal);
        Holder<OccuringStruct1> z = new Holder<OccuringStruct1>();

        OccuringStruct1 ret;
        if (testDocLiteral) {
            ret = docClient.testOccuringStruct1(x, y, z);
        } else {
            ret = rpcClient.testOccuringStruct1(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testOccuringStruct1(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testOccuringStruct1(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testOccuringStruct1(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.OccurringStruct2;
    
    protected boolean equals(OccuringStruct2 x, OccuringStruct2 y) {
        if (Float.compare(x.getVarFloat(), y.getVarFloat()) != 0) {
            return false;
        }
        List<Serializable> xList = x.getVarIntAndVarString();
        List<Serializable> yList = y.getVarIntAndVarString();
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

    // XXX - Generated code is flattened (no nested struct)
    public void testOccuringStruct2() throws Exception {
        OccuringStruct2 x = new OccuringStruct2();
        x.setVarFloat(1.14f);
        List<Serializable> theList = x.getVarIntAndVarString();
        theList.add(0);
        theList.add("x1");
        theList.add(1);
        theList.add("x2");

        OccuringStruct2 yOriginal = new OccuringStruct2();
        yOriginal.setVarFloat(3.14f);
        theList = yOriginal.getVarIntAndVarString();
        theList.add(42);
        theList.add("y");

        Holder<OccuringStruct2> y = new Holder<OccuringStruct2>(yOriginal);
        Holder<OccuringStruct2> z = new Holder<OccuringStruct2>();

        OccuringStruct2 ret;
        if (testDocLiteral) {
            ret = docClient.testOccuringStruct2(x, y, z);
        } else {
            ret = rpcClient.testOccuringStruct2(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testOccuringStruct2(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testOccuringStruct2(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testOccuringStruct2(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.OccurringChoice;

    protected boolean equals(OccuringChoice x, OccuringChoice y) {
        if (!equalsNilable(x.getVarAttrib(), y.getVarAttrib())) {
            return false;
        }
        return equalsFloatIntStringList(x.getVarFloatOrVarIntOrVarString(),
                                        y.getVarFloatOrVarIntOrVarString());
    }
    
    public void testOccuringChoice() throws Exception {
        OccuringChoice x = new OccuringChoice();
        List<Serializable> theList = x.getVarFloatOrVarIntOrVarString();
        theList.add(0);
        theList.add(1.14f);
        theList.add("x1");
        theList.add(1);
        theList.add(11.14f);
        theList.add("x2");
        x.setVarAttrib("x_attr");

        OccuringChoice yOriginal = new OccuringChoice();
        theList = yOriginal.getVarFloatOrVarIntOrVarString();
        theList.add(3.14f);
        theList.add("y");
        theList.add(42);
        yOriginal.setVarAttrib("y_attr");

        Holder<OccuringChoice> y = new Holder<OccuringChoice>(yOriginal);
        Holder<OccuringChoice> z = new Holder<OccuringChoice>();

        OccuringChoice ret;
        if (testDocLiteral) {
            ret = docClient.testOccuringChoice(x, y, z);
        } else {
            ret = rpcClient.testOccuringChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testOccuringChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testOccuringChoice(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testOccuringChoice(): Incorrect return value",
                       equals(x, ret));
        }

        theList.add(52);
        theList.add(4.14f);
        theList.add("y2");

        y = new Holder<OccuringChoice>(yOriginal);
        z = new Holder<OccuringChoice>();

        if (testDocLiteral) {
            ret = docClient.testOccuringChoice(x, y, z);
        } else {
            ret = rpcClient.testOccuringChoice(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testOccuringChoice(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testOccuringChoice(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testOccuringChoice(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.OccurringChoice1;
    
    protected boolean equals(OccuringChoice1 x, OccuringChoice1 y) {
        List<Comparable> xList = x.getVarFloatOrVarInt();
        List<Comparable> yList = y.getVarFloatOrVarInt();
        int size = xList.size();
        if (size != yList.size()) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (xList.get(i) instanceof Integer && yList.get(i) instanceof Integer) {
                Integer xi = (Integer)xList.get(i);
                Integer yi = (Integer)yList.get(i);
                if (xi.compareTo(yi) != 0) {
                    return false;
                }
            }
            if (xList.get(i) instanceof Float && yList.get(i) instanceof Float) {
                Float xf = (Float)xList.get(i);
                Float yf = (Float)yList.get(i);
                if (xf.compareTo(yf) != 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public void testOccuringChoice1() throws Exception {
        OccuringChoice1 x = new OccuringChoice1();
        List<Comparable> theList = x.getVarFloatOrVarInt();
        theList.add(0);
        theList.add(new Float(1.14f));
        theList.add(1);
        theList.add(new Float(11.14f));
        // leave y empty
        OccuringChoice1 yOriginal = new OccuringChoice1();

        Holder<OccuringChoice1> y = new Holder<OccuringChoice1>(yOriginal);
        Holder<OccuringChoice1> z = new Holder<OccuringChoice1>();

        OccuringChoice1 ret;
        if (testDocLiteral) {
            ret = docClient.testOccuringChoice1(x, y, z);
        } else {
            ret = rpcClient.testOccuringChoice1(x, y, z);
        }
        if (!perfTestOnly) {
            assertTrue("testOccuringChoice1(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testOccuringChoice1(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testOccuringChoice1(): Incorrect return value",
                       equals(x, ret));
        }
    }

    // org.objectweb.type_test.types.OccurringChoice2;
    protected boolean equals(OccuringChoice2 x, OccuringChoice2 y) {
        if (x.getVarString() != null && !x.getVarString().equals(y.getVarString())) {
            return false;
        }
        if (x.getVarInt() != null && x.getVarInt() != y.getVarInt()) {
            return false;
        }
        return true;
    }

    // XXX - generated code is flattened - no nested choice
    public void testOccuringChoice2() throws Exception {
        //OccuringChoice2.OccuringChoice21 x1 = new OccuringChoice2.OccuringChoice21();
        //OccuringChoice2.OccuringChoice21 y1 = new OccuringChoice2.OccuringChoice21();
        OccuringChoice2 x = new OccuringChoice2();
        x.setVarString("x1");
        OccuringChoice2 yOriginal = new OccuringChoice2();
        yOriginal.setVarString("y1");

        Holder<OccuringChoice2> y = new Holder<OccuringChoice2>(yOriginal);
        Holder<OccuringChoice2> z = new Holder<OccuringChoice2>();
        OccuringChoice2 ret;
        if (testDocLiteral) {
            ret = docClient.testOccuringChoice2(x, y, z);
        } else {
            ret = rpcClient.testOccuringChoice2(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testOccuringChoice2(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testOccuringChoice2(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testOccuringChoice2(): Incorrect return value",
                       equals(x, ret));
        }

        x = new OccuringChoice2();
        yOriginal = new OccuringChoice2();
        yOriginal.setVarString("y1");

        y = new Holder<OccuringChoice2>(yOriginal);
        z = new Holder<OccuringChoice2>();
        if (testDocLiteral) {
            ret = docClient.testOccuringChoice2(x, y, z);
        } else {
            ret = rpcClient.testOccuringChoice2(x, y, z);
        }

        if (!perfTestOnly) {
            assertTrue("testOccuringChoice2(): Incorrect value for inout param",
                       equals(x, y.value));
            assertTrue("testOccuringChoice2(): Incorrect value for out param",
                       equals(yOriginal, z.value));
            assertTrue("testOccuringChoice2(): Incorrect return value",
                       equals(x, ret));
        }
    }
}
