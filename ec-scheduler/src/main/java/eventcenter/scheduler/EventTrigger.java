package eventcenter.scheduler;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import eventcenter.api.EventInfo;

/**
 * @author JackyLIU
 *
 */
public abstract class EventTrigger implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8370747123396351850L;
	
	protected List<TriggerFilter> filters;

	/**
	 * 执行的次数
	 */
	private volatile AtomicLong executed = new AtomicLong(0L);

	private transient Logger logger;

	protected Logger getLogger(){
		if(null == logger){
			logger = Logger.getLogger(this.getClass());
		}
		return logger;
	}

	public List<TriggerFilter> getFilters() {
		return filters;
	}

	public void setFilters(List<TriggerFilter> filters) {
		this.filters = filters;
	}
	
	/**
	 * 在注册到计划任务之前的一层过滤，如果直接返回 {@link FilterReceipt#Rejected}或者 {@link FilterReceipt#Stop}，那么此事件将不会加入到计划任务中
	 * @param eventInfo
	 * @return
	 */
	public FilterReceipt preSchedule(EventInfo eventInfo){
		if(null == filters || filters.size() == 0) {
			return FilterReceipt.Scheduled;
		}

		for(TriggerFilter filter : filters){
			try{
				FilterReceipt receipt = filter.preSchedule(eventInfo, this);
				if(receipt != FilterReceipt.Scheduled) {
					return receipt;
				}
			}catch(Exception e){
				getLogger().error(e.getMessage(), e);
				// 抛出异常那么将返回Stop回执
				return FilterReceipt.Stop;
			}
		}
		
		return FilterReceipt.Scheduled;
	}
	
	/**
	 * 这个过滤器在计划任务触发Job之后，并在发送事件之前执行过滤，这里将会执行一次过滤，通过FilterReceipt控制事件是否需要发送，如果返回{@link FilterReceipt#Scheduled}
	 * 那么表示按照正常计划执行，那么将会触发事件，如果返回 {@link FilterReceipt#Rejected}此次任务将不会触发事件，那么依然按照计划任务等待执行下一次计划，如果返回 {@value FilterReceipt#Changed}
	 * 那么计划任务将改变行程安排，不会触发事件，如果返回 {@link FilterReceipt#Stop}将停止这个计划任务
	 * @param eventInfo
	 * @param result
	 * @return
	 */
	public FilterReceipt filter(EventInfo eventInfo){
		// 累加执行次数
		executed.incrementAndGet();
		if(null == filters || filters.size() == 0) {
			return FilterReceipt.Scheduled;
		}

		for(TriggerFilter filter : filters){
			try{
				FilterReceipt receipt = filter.filter(eventInfo, this);
				if(receipt != FilterReceipt.Scheduled) {
					return receipt;
				}
			}catch(Exception e){
				getLogger().error(e.getMessage(), e);
				// 抛出异常那么将返回Stop回执
				return FilterReceipt.Stop;
			}
		}

		return FilterReceipt.Scheduled;
	}

	/**
	 * 获取当前执行的次数
	 * @return
	 */
	public Long getExecuted(){
		return executed.get();
	}

	
}
