package eventcenter.api;

/**
 * 事件发送时的运行容器，例如同步执行容器异步执行容器，计划任务执行容器
 * @author JackyLIU
 *
 */
public interface EventContainer {

	/**
	 * 发送事件
	 * @param source
	 * @return
	 */
	Object send(CommonEventSource source);

	/**
	 * 容器是否支持持久化，如果事件堆积的比较多，容器将会把未执行的事件放置在持久化队列中保存
	 * @return
	 */
	boolean isPersisted();

	/**
	 * 缓存队列所缓存的事件数量
	 * @return
	 */
	int queueSize();

	/**
	 * 容器中最高的线程并发数
	 * @return
	 */
	int countOfMaxConcurrent();

	/**
	 * 容器中正在运行的线程并发数
	 * @return
	 */
	int countOfLiveThread();

	/**
	 * 获取容器中缓冲的队列容量
	 * @return
	 */
	int countOfQueueBuffer();
}
