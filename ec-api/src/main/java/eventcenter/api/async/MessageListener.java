package eventcenter.api.async;

import eventcenter.api.EventSourceBase;

/**
 * 监听{@link EventQueue}的消息事件，当队列有消息时，将会推送消息到监听端
 * @author JackyLIU
 *
 */
public interface MessageListener {

	void onMessage(EventSourceBase evt);
}
