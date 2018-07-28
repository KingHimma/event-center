package eventcenter.remote;


/**
 * 远程事件订阅器
 * @author JackyLIU
 *
 */
public interface EventSubscriber {
	
	/**
	 * 事件传输器的编号，每个实例的编号都是唯一的
	 * @return
	 */
	String getId();

	SubscriberGroup getSubscriberGroup(String group);
}
