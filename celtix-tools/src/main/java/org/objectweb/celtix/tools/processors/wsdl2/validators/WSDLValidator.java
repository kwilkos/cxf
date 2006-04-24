package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.util.*;
import javax.wsdl.Definition;

import org.objectweb.celtix.common.util.StringUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;

public class WSDLValidator extends AbstractValidator {

    private final List<AbstractValidator> validators = new ArrayList<AbstractValidator>();
    
    public WSDLValidator(Definition definition) {
        super(definition);
    }

    public WSDLValidator(Definition definition, ProcessorEnvironment pe) {
        super(definition, pe);
    }

    public boolean isValid() {
        String schemaDir = getSchemaDir();
        if (!StringUtils.isEmpty(schemaDir)) {
            SchemaWSDLValidator schemaValidator =
                new SchemaWSDLValidator(schemaDir,
                                        (String)env.get(ToolConstants.CFG_WSDLURL),
                                        null,
                                        false);
            
            if (!schemaValidator.isValid()) {
                return false;
            }
        }
        
        validators.add(new UniqueBodyPartsValidator(this.def));
        validators.add(new WSIBPValidator(this.def));
        validators.add(new MIMEBindingValidator(this.def));
        validators.add(new XMLFormatValidator(this.def));

        for (AbstractValidator validator : validators) {
            if (!validator.isValid()) {
                addErrorMessage(validator.getErrorMessage());
                return false;
            }
        }
        return true;
    }

    public String getSchemaDir() {
        return System.getProperty("CELTIX_SCHEMA_DIR");
    }
}
