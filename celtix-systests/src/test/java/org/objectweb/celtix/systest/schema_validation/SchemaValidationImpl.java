package org.objectweb.celtix.systest.schema_validation;

import java.io.Serializable;
import java.util.List;

import javax.jws.WebService;

import org.objectweb.schema_validation.SchemaValidation;
import org.objectweb.schema_validation.types.ComplexStruct;
import org.objectweb.schema_validation.types.OccuringStruct;

@WebService(serviceName = "SchemaValidationService", 
            portName = "SoapPort",
            endpointInterface = "org.objectweb.schema_validation.SchemaValidation",
            targetNamespace = "http://objectweb.org/schema_validation")
public class SchemaValidationImpl implements SchemaValidation {

    public boolean setComplexStruct(ComplexStruct in) {
        return true;
    }

    public boolean setOccuringStruct(OccuringStruct in) {
        return true;
    }
    
    public ComplexStruct getComplexStruct(String in) {
        ComplexStruct complexStruct = new ComplexStruct();
        complexStruct.setElem1(in + "-one");
        // Don't initialize a member of the structure.  Validation should throw
        // an exception.
        // complexStruct.setElem2(in + "-two");
        complexStruct.setElem3(in.length());
        return complexStruct;
    }

    public OccuringStruct getOccuringStruct(String in) {
        OccuringStruct occuringStruct = new OccuringStruct();
        // Populate the list in the wrong order.  Validation should throw
        // an exception.
        List<Serializable> floatIntStringList = occuringStruct.getVarFloatAndVarIntAndVarString();
        floatIntStringList.add(in + "-two");
        floatIntStringList.add(new Integer(2));
        floatIntStringList.add(new Float(2.5f));
        return occuringStruct;
    }

}
