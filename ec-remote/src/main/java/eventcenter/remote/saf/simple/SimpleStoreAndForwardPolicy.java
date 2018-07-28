package eventcenter.remote.saf.simple;

import eventcenter.api.async.EventQueue;
import eventcenter.api.async.simple.SimpleEventQueue;
import eventcenter.remote.saf.EventForward;
import eventcenter.remote.saf.StoreAndForwardPolicy;
import eventcenter.remote.saf.StoreAndForwardPolicy;

/**
 * 简单高效的SAF策略实现
 * @author JackyLIU
 *
 */
public class SimpleStoreAndForwardPolicy implements StoreAndForwardPolicy {

	private boolean isStoreOnSendFail = true;
	
	/**
	 * 队列的事件元素最大的容量
	 */
	private int queueCapacity = SimpleEventQueue.DEFAULT_QUEUE_CAPACITY;
	
	/**
	 * 检查远程端的健康值的间隔，默认为60000毫秒
	 */
	private Long checkInterval;
	
	/**
	 * 此版本默认在发送失败时进行存储
	 * @return
	 */
	@Override
	public boolean storeOnSendFail() {
		return isStoreOnSendFail;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public void setStoreOnSendFail(boolean isStoreOnSendFail) {
		this.isStoreOnSendFail = isStoreOnSendFail;
	}

	public Long getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(Long checkInterval) {
		this.checkInterval = checkInterval;
	}

	@Override
	public EventQueue createEventQueue(String groupName) {
		return new SimpleEventQueue(queueCapacity);
	}

	@Override
	public EventForward createEventForward() {
		SimpleEventForward forward = new SimpleEventForward(isStoreOnSendFail);
		if(null != checkInterval)
			forward.setCheckInterval(checkInterval);
		
		return forward;
	}

}
