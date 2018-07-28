package eventcenter.remote.saf;

import eventcenter.api.async.EventQueue;

/**
 * 远程发送事件的Store And Forward策略接口，Store用于存储事件，提高事件准确发送到远程端
 * 的概率，本身dubbo已经提供了retry和failover的机制，但是还缺少离线机制，SAF正是为了提高远程端离线
 * 状态下，事件能够缓存起来，并准实时的将事件发送到远程端执行
 * @author JackyLIU
 *
 */
public interface StoreAndForwardPolicy {

	/**
	 * <p>是否在发生异常时存储事件，如果返回true表示，事件只在发送失败的时候存储到死信队列中，由forward接口
	 * watch dog监控远程端是否连接成功，然后发送不断推送离线事件到远程端。
	 * <p>如果为false，那么事件总是在发送前存储，并由forward接口不断的从队列中取出数据向远程段推送事件
	 * @return
	 */
	boolean storeOnSendFail();
	
	/**
	 * 创建存储事件的队列
	 * @param groupName 分组名称
	 * @return
	 */
	EventQueue createEventQueue(String groupName);
	
	/**
	 * 创建事件推送接口
	 * @return
	 */
	EventForward createEventForward();
}
