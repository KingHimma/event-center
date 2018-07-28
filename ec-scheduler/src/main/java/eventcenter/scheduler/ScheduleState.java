package eventcenter.scheduler;

import java.io.Serializable;
import java.util.Date;

/**
 * 计划任务执行状况
 * @author JackyLIU
 *
 */
public class ScheduleState implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3110612713434992911L;

	/**
	 * 上一次触发任务的时间
	 */
	private Date previousFireTime;
	
	/**
	 * 计划任务实际触发的时间，例如，计划定在12:00:00，但由于服务器比较繁忙，导致在12:00:03执行
	 */
	private Date startTime;
	
	/**
	 * 下一次触发任务的时间
	 */
	private Date nextFireTime;
	
	private Date endTime;

	public Date getPreviousFireTime() {
		return previousFireTime;
	}

	public void setPreviousFireTime(Date previousFireTime) {
		this.previousFireTime = previousFireTime;
	}

	public Date getNextFireTime() {
		return nextFireTime;
	}

	public void setNextFireTime(Date nextFireTime) {
		this.nextFireTime = nextFireTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	
}
