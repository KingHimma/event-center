package eventcenter.scheduler.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public final class DateHelper {

	/**
	 * 对date时间增加数值
	 * @param date
	 * @param timeUnit
	 * @return
	 */
	public static Date add(Date date, long value, TimeUnit timeUnit){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		if(timeUnit == TimeUnit.MICROSECONDS){
			c.add(Calendar.MILLISECOND, (int)value);
		}else if(timeUnit == TimeUnit.SECONDS){
			c.add(Calendar.SECOND, (int)value);			
		}else if(timeUnit == TimeUnit.MINUTES){
			c.add(Calendar.MINUTE, (int)value);			
		}else if(timeUnit == TimeUnit.HOURS){
			c.add(Calendar.HOUR, (int)value);			
		}else if(timeUnit == TimeUnit.DAYS){
			c.add(Calendar.DAY_OF_YEAR, (int)value);			
		}
		return c.getTime();
	}
}
