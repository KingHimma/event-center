package eventcenter.monitor.elasticsearch;

import com.google.gson.*;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * Created by liumingjian on 2016/10/13.
 */
public class DateTimeTypeConverter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    // No need for an InstanceCreator since DateTime provides a no-args constructor
    @Override
    public JsonElement serialize(Date src, Type srcType, JsonSerializationContext context) {
        DateTime date = new DateTime(src);
        return new JsonPrimitive(date.toString());
    }

    @Override
    public Date deserialize(JsonElement json, Type type, JsonDeserializationContext context)
        throws JsonParseException {
        String value = json.getAsString();
        if(value.contains("+08:00")){
            return ISODateTimeFormat.dateTime().parseDateTime(value).toDate();
        }
        return ISODateTimeFormat.dateTimeNoMillis().parseDateTime(value).toDate();

    }
}

