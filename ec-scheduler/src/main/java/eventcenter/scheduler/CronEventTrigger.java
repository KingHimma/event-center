package eventcenter.scheduler;

/**
 * 使用cron表达式设置计划任务
 * @author JackyLIU
 *
 */
public class CronEventTrigger extends EventTrigger {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4868124106097287515L;

	private String cron;

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}
	
	@Override
	public String toString(){
		return new StringBuilder("cron:").append(cron).toString();
	}
}
