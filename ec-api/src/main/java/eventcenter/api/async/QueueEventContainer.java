package eventcenter.api.async;

import eventcenter.api.*;
import eventcenter.api.EventContainer;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 异步事件发送容器
 * @author JackyLIU
 */
public abstract class QueueEventContainer implements EventContainer {
	
	protected final EventQueue queue;

	protected final EventCenterConfig config;

	protected ListenerCache listenerCache = ListenerCache.get();
	
	protected final Logger logger = Logger.getLogger(this.getClass());
	
	public QueueEventContainer(EventCenterConfig config, EventQueue queue){
		this.queue = queue;
		this.config = config;
	}
	
	public EventQueue getQueue() {
		return queue;
	}
	
	/**
	 * 启动异步队列事件容器
	 */
	public abstract void startup() throws Exception;
	
	/**
	 * 停止异步队列事件队列容器
	 */
	public abstract void shutdown() throws Exception;
	
	/**
	 * 容器是否空闲
	 * @return
	 */
	public abstract boolean isIdle();
	
	@Override
	public Object send(EventSourceBase source) {
		queue.offer(source);
		return null;
	}
	
	protected EventRegister getEventRegister(EventSourceBase evt){
		return config.getEventRegisters().get(evt.getEventName());
	}
	
	protected List<EventListener> findAsyncEventListeners(EventSourceBase message){
		EventRegister register = getEventRegister(message);
		if(null == register){
			logger.warn("can't find register for event:" + message.getEventName());
			return new ArrayList<EventListener>(1);
		}
		return listenerCache.findAsyncEventListeners(register, message.getEventName());
	}

	@Override
	public int queueSize() {
		return queue.enqueueSize();
	}
}
