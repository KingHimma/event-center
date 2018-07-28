package eventcenter.remote;

import java.util.Map;

/**
 * 用于动态创建{@link EventTransmission}接口的工厂，比如会用于{@link EventPublisher}接口实现中，用于自动发现，并动态创建出{@link EventTransmission}
 * @author JackyLIU
 *
 */
public interface EventTransmissionFactory {

	/**
	 * 根据发布端的{@link EventTransmission},根据args参数创建出
	 * @param subscriberGroup
	 * @param params
	 * @return
	 */
	EventTransmission create(SubscriberGroup subscriberGroup, Map<String, Object> params);
	
	void destroy(EventTransmission eventTransmission);
}
