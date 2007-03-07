package org.apache.cxf.aegis.type.basic;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;

import org.apache.cxf.aegis.Context;
import org.apache.cxf.aegis.DatabindingException;
import org.apache.cxf.aegis.type.Type;
import org.apache.cxf.aegis.util.date.XsDateTimeFormat;
import org.apache.cxf.aegis.xml.MessageReader;
import org.apache.cxf.aegis.xml.MessageWriter;

/**
 * Type for the Time class which serializes to an xs:time.
 * 
 * @author <a href="mailto:dan@envoisolutions.com">Dan Diephouse</a>
 */
public class TimestampType extends Type {
    private static XsDateTimeFormat format = new XsDateTimeFormat();

    @Override
    public Object readObject(MessageReader reader, Context context) throws DatabindingException {
        String value = reader.getValue();

        if (value == null) {
            return null;
        }

        try {
            Calendar c = (Calendar)format.parseObject(value);
            return new Timestamp(c.getTimeInMillis());
        } catch (ParseException e) {
            throw new DatabindingException("Could not parse xs:dateTime: " + e.getMessage(), e);
        }
    }

    @Override
    public void writeObject(Object object, MessageWriter writer, Context context) {
        Calendar c = Calendar.getInstance();
        c.setTime((Timestamp)object);
        writer.writeValue(format.format(c));
    }
}
