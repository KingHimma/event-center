package eventcenter.remote.saf;

import eventcenter.api.async.EventQueue;
import eventcenter.remote.publisher.AbstractFireRemoteEventsPolicy;
import eventcenter.remote.publisher.PublisherGroup;
import eventcenter.remote.publisher.AbstractFireRemoteEventsPolicy;
import eventcenter.remote.publisher.PublisherGroup;

import java.io.IOException;
import java.util.Map;

/**
 * 远程事件推送接口，将离线或者死信队列中的事件推送到远程段
 * @author JackyLIU
 *
 */
public interface EventForward {
	
	/**
	 * <p>启动事件推送服务，启动之后，将会监控远程服务是否健康，并适时从本地离线队列中取出事件推送到远程端
	 * <p>传入参数monitors的目的是为了能够让看门狗程序监控远程端的服务健康状态，一个IEventTransmission对应着一个事件队列
	 */
	void startup(Map<PublisherGroup, EventQueue> monitors);
	
	/**
	 * 关闭事件推送服务
	 */
	void shutdown();
	
	/**
	 * 服务是否启动
	 * @return
	 */
	boolean isStartup();
	
	Map<PublisherGroup, EventQueue> getMonitors();
	
	/**
	 * 添加监听器，如果添加成功，将返回true，添加时，将会开启监控
	 * @param publisherGroup
	 * @param eventQueue
	 */
	boolean addMonitor(PublisherGroup publisherGroup, EventQueue eventQueue);
	
	/**
	 * 删除监听器，如果删除成功，将返回true
	 * @param publisherGroup
	 */
	boolean removeMonitor(PublisherGroup publisherGroup) throws IOException;

	/**
	 * 设置发送到远程消费端的策略
	 * @param policy
	 */
	void setFireRemoteEventsPolicy(AbstractFireRemoteEventsPolicy policy);
}
