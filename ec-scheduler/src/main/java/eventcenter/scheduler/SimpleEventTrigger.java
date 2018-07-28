package eventcenter.scheduler;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 可以设置简单一些的计划任务参数
 * @author JackyLIU
 *
 */
public class SimpleEventTrigger extends EventTrigger {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3740893626549573801L;

	private Date startAt;
	
	/**
	 * 延迟执行，单位默认为分钟，可以设置timeUnit参数控制单位
	 */
	private Long delay;
	
	/**
	 * 间隔时间，需要配合intervalTimeUnit一起使用，如果不设置，那么默认为分钟
	 */
	private Long interval;
	
	/**
	 * interval间隔时间的单位
	 */
	private TimeUnit timeUnit;
	
	/**
	 * 是否一直循环执行，如果这个设置为false，也可以设置endAt参数，计划任务停止时间
	 */
	private Boolean foreverRepeat;
	
	private Date endAt;
	
	/**
	 * 执行次数
	 */
	private Integer repeatCount;

	public Date getStartAt() {
		return startAt;
	}

	public void setStartAt(Date startAt) {
		this.startAt = startAt;
	}

	public Long getDelay() {
		return delay;
	}

	public void setDelay(Long delay) {
		this.delay = delay;
	}

	public Long getInterval() {
		return interval;
	}

	public void setInterval(Long interval) {
		this.interval = interval;
	}

	public TimeUnit getTimeUnit() {
		if(null == timeUnit)
			timeUnit = TimeUnit.MINUTES;
		return timeUnit;
	}

	public void setTimeUnit(TimeUnit timeUnit) {
		this.timeUnit = timeUnit;
	}

	public Boolean getForeverRepeat() {
		return foreverRepeat;
	}

	public void setForeverRepeat(Boolean foreverRepeat) {
		this.foreverRepeat = foreverRepeat;
	}

	public Date getEndAt() {
		return endAt;
	}

	public void setEndAt(Date endAt) {
		this.endAt = endAt;
	}

	public Integer getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(Integer repeatCount) {
		this.repeatCount = repeatCount;
	}
	
	
	
	
}
