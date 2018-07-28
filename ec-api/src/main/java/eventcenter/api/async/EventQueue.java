package eventcenter.api.async;

import eventcenter.api.EventSourceBase;

import java.io.Closeable;


/**
 * 事件队列
 * @author JackyLIU
 *
 */
public interface EventQueue extends Closeable{

	/**
	 * 将元素设置到队列中
	 * @return
	 */
	void offer(EventSourceBase evt) throws QueueException;
	
	/**
	 * 将元素设置到队列中
	 * @param timeout
	 * @return
	 */
	void offer(EventSourceBase evt, long timeout);
	
	/**
	 * 从队列获取事件源
	 */
	EventSourceBase transfer();
	
	/**
	 * 等待并从队列获取事件源，如果在指定的时间未返回，则超时
	 * @param timeout
	 * @return
	 */
	EventSourceBase transfer(long timeout);
	
	/**
	 * 队列中的元素数量
	 * @return
	 */
	int enqueueSize();
	
	/**
	 * 队列是否空闲
	 * @return
	 */
	boolean isEmpty();
	
	/**
	 * 设置消息监听器
	 * @param listener
	 */
	void setMessageListener(MessageListener listener);
}
