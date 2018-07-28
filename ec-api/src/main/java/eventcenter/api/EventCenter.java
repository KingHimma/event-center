package eventcenter.api;

/**
 * 事件中心接口
 * @author JackyLIU
 *
 */
public interface EventCenter {

	/**
	 * 通过事件名称，查找到事件注册者
	 * @param name
	 * @return
	 */
	EventRegister findEventRegister(String name);
	
	/**
	 * 触发事件，将事件通知给对应的事件订阅者
	 * @param target 触发事件的目标
	 * @param eventInfo 包含了事件名称
	 * @param result 事件触发之前产生的数据
	 * @return 通过listener返回数据
	 */
	Object fireEvent(Object target, EventInfo eventInfo, Object result);
}
