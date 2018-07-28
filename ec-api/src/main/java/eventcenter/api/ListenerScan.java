package eventcenter.api;

import eventcenter.api.annotation.ListenerBind;

import java.util.List;

/**
 * 监听器扫描仪，能够扫描到包中的{@link EventListener}接口，这个要求实现{@link EventListener}的实现类中，标记{@link ListenerBind}注解
 * @author JackyLIU
 *
 */
public interface ListenerScan {

	EventCenterConfig getEventCenterConfig();
	
	List<EventListener> getEventListener();
}
