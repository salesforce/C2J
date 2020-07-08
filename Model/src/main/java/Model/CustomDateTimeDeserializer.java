package Model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;


public class CustomDateTimeDeserializer extends StdDeserializer<OffsetDateTime> {

    public CustomDateTimeDeserializer() {
        this(null);
    }

    public CustomDateTimeDeserializer(Class<?> vc) {
        super(vc);
    }


    static DateTimeFormatter dtFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;//DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZHH:mm");
    static SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    static {
        dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateTimeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    static OffsetDateTime deserializeOffsetDateTime(String date) {
        try {
            return OffsetDateTime.parse(date);
        } catch (Exception pe) {
            return null;
        }
    }

    static OffsetDateTime deserializeLocalDateTime(String date) {
        try {
            return LocalDateTime.parse(date, dtFormatter).toInstant(ZoneOffset.UTC).atOffset(ZoneOffset.UTC);
        } catch (Exception pe) {
            return null;
        }
    }

    static OffsetDateTime deserializeUsingFormatter(String date, SimpleDateFormat formatter) {
        try {
            return formatter.parse(date).toInstant()
                    .atOffset(ZoneOffset.UTC);
        } catch (Exception pe) {
            return null;
        }
    }

    public static OffsetDateTime deserialize(String date) {

        var t = deserializeOffsetDateTime(date);
        if (t != null)
            return t;

        t = deserializeLocalDateTime(date);
        if (t != null)
            return t;
        t = deserializeUsingFormatter(date, dateTimeFormatter);
        if (t != null)
            return t;
        t = deserializeUsingFormatter(date, dateFormatter);
        if (t != null)
            return t;

        throw new RuntimeException(String.format("Can't deserialize %s", date));
    }


    @Override
    public OffsetDateTime deserialize(JsonParser jsonparser, DeserializationContext context)
            throws IOException {

        String date = jsonparser.getText();
        var dt = deserialize(date);
        return dt;
    }
}