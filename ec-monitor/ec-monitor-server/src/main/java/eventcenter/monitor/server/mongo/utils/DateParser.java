package eventcenter.monitor.server.mongo.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用于mongodb的时间解析
 * Created by liumingjian on 16/2/19.
 */
public class DateParser {

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static String format(Date date){
        return dateFormat.format(date);
    }

    public static Date parse(String date) throws ParseException {
        return dateFormat.parse(date);
    }
}
