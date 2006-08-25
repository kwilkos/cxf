package org.objectweb.celtix.tools.processors.wsdl2.validators;

import java.util.*;
import javax.wsdl.Definition;

import org.objectweb.celtix.common.util.StringUtils;
import org.objectweb.celtix.tools.common.ProcessorEnvironment;
import org.objectweb.celtix.tools.common.ToolConstants;
import org.objectweb.celtix.tools.common.ToolException;

public class WSDL11Validator extends AbstractValidator {

    private final List<AbstractValidator> validators = new ArrayList<AbstractValidator>();

    public WSDL11Validator(Definition definition) {
        super(definition);
    }

    public WSDL11Validator(Definition definition, ProcessorEnvironment pe) {
        super(definition, pe);
    }

    public boolean isValid() throws ToolException {
        boolean isValid = true;
        String schemaDir = getSchemaDir();
        if (!StringUtils.isEmpty(schemaDir)) {

            String[] schemas = (String[])env.get(ToolConstants.CFG_SCHEMA_URL);
            
            SchemaValidator schemaValidator = new SchemaValidator(schemaDir, (String)env
                .get(ToolConstants.CFG_WSDLURL), schemas, false);

            if (!schemaValidator.isValid()) {              
                this.addErrorMessage(schemaValidator.getErrorMessage());
                isValid = false;
                throw new ToolException(this.getErrorMessage());
               
            } else {
                this.def = schemaValidator.def;
            }
        } else {
            throw new ToolException("Schema dir should be defined before validate wsdl");
        }

        validators.add(new UniqueBodyPartsValidator(this.def));
        validators.add(new WSIBPValidator(this.def));
        validators.add(new MIMEBindingValidator(this.def));
        validators.add(new XMLFormatValidator(this.def));
        
        for (AbstractValidator validator : validators) {
            if (!validator.isValid()) {             
                addErrorMessage(validator.getErrorMessage());
                isValid = false;
                throw new ToolException(this.getErrorMessage());
            }
        }
        
        return isValid;
    }

    public String getSchemaDir() {
        String dir = "";
        if (env.get(ToolConstants.CFG_SCHEMA_DIR) == null) {
            dir = System.getProperty(ToolConstants.CELTIX_SCHEMA_DIR);
        } else {
            dir = (String)env.get(ToolConstants.CFG_SCHEMA_DIR);
        }
        return dir;
    }
}
