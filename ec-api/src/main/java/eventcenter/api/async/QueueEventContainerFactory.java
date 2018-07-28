package eventcenter.api.async;

import eventcenter.api.EventCenterConfig;

/**
 * 创建异步事件容器的工厂
 * @author JackyLIU
 *
 */
public interface QueueEventContainerFactory {

	/**
	 * 创建容器
	 * @return
	 */
	QueueEventContainer createContainer(EventCenterConfig config);
}
