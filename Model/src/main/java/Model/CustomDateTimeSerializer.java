package Model;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public class CustomDateTimeSerializer extends StdSerializer<OffsetDateTime> {

    public CustomDateTimeSerializer() {
        this(null);
    }
    public CustomDateTimeSerializer(Class<OffsetDateTime> t) {
        super(t);
    }

    static String serialize(OffsetDateTime value)
    {
        if(value.getOffset().getTotalSeconds()==0)
            return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss").format(value);
        else
            return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(value);
    }

    @Override
    public void serialize
            (OffsetDateTime value, JsonGenerator gen, SerializerProvider arg2)
            throws IOException {

        var sdt = serialize(value);
        gen.writeString(sdt);
    }
}

