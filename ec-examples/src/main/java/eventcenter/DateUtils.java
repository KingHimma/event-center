package eventcenter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

	public static String getNowDate(){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		return format.format(new Date());
	}
}
