package org.objectweb.celtix.tools.processors.wsdl2.validators;

import com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import com.sun.org.apache.xerces.internal.util.SymbolTable;

public class SchemaSymbolTable extends SymbolTable {

    public SchemaSymbolTable() {
        super.addSymbol(SchemaSymbols.ATTVAL_ANYSIMPLETYPE);
        super.addSymbol(SchemaSymbols.ATTVAL_ANYTYPE);
        super.addSymbol(SchemaSymbols.ATTVAL_ANYURI);
        super.addSymbol(SchemaSymbols.ATTVAL_BASE64BINARY);
        super.addSymbol(SchemaSymbols.ATTVAL_BOOLEAN);

        super.addSymbol(SchemaSymbols.ATTVAL_BYTE);
        super.addSymbol(SchemaSymbols.ATTVAL_COLLAPSE);
        super.addSymbol(SchemaSymbols.ATTVAL_DATE);
        super.addSymbol(SchemaSymbols.ATTVAL_DATETIME);
        super.addSymbol(SchemaSymbols.ATTVAL_DAY);

        super.addSymbol(SchemaSymbols.ATTVAL_DECIMAL);
        super.addSymbol(SchemaSymbols.ATTVAL_DOUBLE);
        super.addSymbol(SchemaSymbols.ATTVAL_DURATION);
        super.addSymbol(SchemaSymbols.ATTVAL_ENTITIES);
        super.addSymbol(SchemaSymbols.ATTVAL_ENTITY);

        super.addSymbol(SchemaSymbols.ATTVAL_EXTENSION);
        super.addSymbol(SchemaSymbols.ATTVAL_FALSE);
        super.addSymbol(SchemaSymbols.ATTVAL_FALSE_0);
        super.addSymbol(SchemaSymbols.ATTVAL_FLOAT);
        super.addSymbol(SchemaSymbols.ATTVAL_HEXBINARY);

        super.addSymbol(SchemaSymbols.ATTVAL_ID);
        super.addSymbol(SchemaSymbols.ATTVAL_IDREF);
        super.addSymbol(SchemaSymbols.ATTVAL_IDREFS);
        super.addSymbol(SchemaSymbols.ATTVAL_INT);
        super.addSymbol(SchemaSymbols.ATTVAL_INTEGER);

        super.addSymbol(SchemaSymbols.ATTVAL_LANGUAGE);
        super.addSymbol(SchemaSymbols.ATTVAL_LAX);
        super.addSymbol(SchemaSymbols.ATTVAL_LIST);
        super.addSymbol(SchemaSymbols.ATTVAL_LONG);
        super.addSymbol(SchemaSymbols.ATTVAL_MONTH);

        super.addSymbol(SchemaSymbols.ATTVAL_MONTHDAY);
        super.addSymbol(SchemaSymbols.ATTVAL_NAME);
        super.addSymbol(SchemaSymbols.ATTVAL_NCNAME);
        super.addSymbol(SchemaSymbols.ATTVAL_NEGATIVEINTEGER);
        super.addSymbol(SchemaSymbols.ATTVAL_NMTOKEN);
        addSymbols();
   

    }
    
    
    private void addSymbols() {
        super.addSymbol(SchemaSymbols.ATTVAL_NMTOKENS);
        super.addSymbol(SchemaSymbols.ATTVAL_NONNEGATIVEINTEGER);
        super.addSymbol(SchemaSymbols.ATTVAL_POSITIVEINTEGER);
        super.addSymbol(SchemaSymbols.ATTVAL_POUNDALL);
        super.addSymbol(SchemaSymbols.ATTVAL_PRESERVE);

        super.addSymbol(SchemaSymbols.ATTVAL_PROHIBITED);
        super.addSymbol(SchemaSymbols.ATTVAL_REPLACE);
        super.addSymbol(SchemaSymbols.ATTVAL_REQUIRED);
        super.addSymbol(SchemaSymbols.ATTVAL_RESTRICTION);
        super.addSymbol(SchemaSymbols.ATTVAL_SHORT);

        super.addSymbol(SchemaSymbols.ATTVAL_SKIP);
        super.addSymbol(SchemaSymbols.ATTVAL_STRICT);
        super.addSymbol(SchemaSymbols.ATTVAL_STRING);
        super.addSymbol(SchemaSymbols.ATTVAL_SUBSTITUTION);
        super.addSymbol(SchemaSymbols.ATTVAL_TIME);

        super.addSymbol(SchemaSymbols.ATTVAL_TOKEN);
        super.addSymbol(SchemaSymbols.ATTVAL_TRUE);
        super.addSymbol(SchemaSymbols.ATTVAL_TRUE_1);
        super.addSymbol(SchemaSymbols.ATTVAL_TWOPOUNDANY);
        super.addSymbol(SchemaSymbols.ATTVAL_TWOPOUNDLOCAL);

        super.addSymbol(SchemaSymbols.ATTVAL_TWOPOUNDOTHER);
        super.addSymbol(SchemaSymbols.ATTVAL_TWOPOUNDTARGETNS);
        super.addSymbol(SchemaSymbols.ATTVAL_UNBOUNDED);
        super.addSymbol(SchemaSymbols.ATTVAL_UNION);
        super.addSymbol(SchemaSymbols.ATTVAL_UNQUALIFIED);

        super.addSymbol(SchemaSymbols.ATTVAL_UNSIGNEDBYTE);
        super.addSymbol(SchemaSymbols.ATTVAL_UNSIGNEDINT);
        super.addSymbol(SchemaSymbols.ATTVAL_UNSIGNEDLONG);
        super.addSymbol(SchemaSymbols.ATTVAL_UNSIGNEDSHORT);
    }

}
