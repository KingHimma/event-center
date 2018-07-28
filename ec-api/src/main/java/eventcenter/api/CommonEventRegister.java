package eventcenter.api;

/**
 * 通用的事件注册者，他将会自动创建{@link CommonEventSource}返回出来
 * @author JackyLIU
 *
 */
public class CommonEventRegister extends AbstractEventRegister {

	@Override
	public EventSourceBase createEventSource(Object source, String id, String eventName, Object[] args,
			Object result, String mdcValue) {
		return new CommonEventSource(source, id, eventName, args, result, mdcValue);
	}
}
