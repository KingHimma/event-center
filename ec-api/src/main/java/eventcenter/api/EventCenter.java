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
	 * 触发事件，和方法{@link #fireEvent(Object, EventInfo)}类似，这个方法只需要传递target，事件名称和参数
	 * @param target
	 * @param eventName
	 * @param args
	 * @return
	 */
	Object fireEvent(Object target, String eventName, Object... args);

	/**
	 * 触发事件，将事件通知给对应的事件订阅者
	 * @param target 触发事件的目标
	 * @param eventInfo 事件体，包含了事件名称，事件参数，事件编号
	 * @return
	 */
	Object fireEvent(Object target, EventInfo eventInfo);
	
	/**
	 * 触发事件，将事件通知给对应的事件订阅者
	 * @param target 触发事件的目标
	 * @param eventInfo 事件体，包含了事件名称，事件参数，事件编号
	 * @param result 事件触发之前或者之后产生的数据
	 * @return 通过listener返回数据
	 */
	Object fireEvent(Object target, EventInfo eventInfo, Object result);
}
