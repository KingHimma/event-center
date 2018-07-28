package eventcenter.remote.saf;

import eventcenter.remote.EventPublisher;

/**
 * 为{@link EventPublisher}提供动态加载和卸载订阅者的功能
 * @author JackyLIU
 *
 */
public interface PublishGroupChangeable {

	/**
	 * 离线的有效期为多长时，则关闭离线队列，单位毫秒
	 * @return
	 */
	int getExpiryOffline();
	
	/**
	 * 设置事件推送接口
	 * @param eventForward
	 */
	void setForwardAndStorePolicy(EventForward eventForward, StoreAndForwardPolicy policy);
}
