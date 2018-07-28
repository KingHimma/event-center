package eventcenter.scheduler;

/**
 * 过滤器的回执状态
 * @author JackyLIU
 *
 */
public enum FilterReceipt {

	/**
	 * 按照正常计划执行
	 */
	Scheduled,
	
	/**
	 * 拒绝此次执行任务，只拒绝这一次，计划任务依然正常按照行程执行
	 */
	Rejected,
	
	/**
	 * 停止这个计划任务
	 */
	Stop,
	
	/**
	 * 改变了计划安排，将重新安排计划任务
	 */
	Changed
}
