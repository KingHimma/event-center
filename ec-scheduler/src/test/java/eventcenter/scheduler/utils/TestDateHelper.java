package eventcenter.scheduler.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class TestDateHelper {

	@Test
	public void testAddSecond() throws ParseException {
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-11-07 10:34:31");
		
		Date value = DateHelper.add(date, 1, TimeUnit.SECONDS);
		Calendar c = Calendar.getInstance();
		c.setTime(value);
		Assert.assertEquals(32, c.get(Calendar.SECOND));
	}

	@Test
	public void testAddMinute() throws ParseException {
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-11-07 10:34:31");
		
		Date value = DateHelper.add(date, 1, TimeUnit.MINUTES);
		Calendar c = Calendar.getInstance();
		c.setTime(value);
		Assert.assertEquals(35, c.get(Calendar.MINUTE));
	}

	@Test
	public void testAddHour() throws ParseException {
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-11-07 10:34:31");
		
		Date value = DateHelper.add(date, 1, TimeUnit.HOURS);
		Calendar c = Calendar.getInstance();
		c.setTime(value);
		Assert.assertEquals(11, c.get(Calendar.HOUR));
	}

	@Test
	public void testAddDay() throws ParseException {
		Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-11-07 10:34:31");
		
		Date value = DateHelper.add(date, 1, TimeUnit.DAYS);
		Calendar c = Calendar.getInstance();
		c.setTime(value);
		Assert.assertEquals(8, c.get(Calendar.DAY_OF_MONTH));
	}
}
