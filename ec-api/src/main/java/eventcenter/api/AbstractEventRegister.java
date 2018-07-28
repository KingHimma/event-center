package eventcenter.api;


/**
 * 使用了Spring的容器进行管理EventListener
 * @author JackyLIU
 *
 */
public abstract class AbstractEventRegister implements EventRegister {
	
	protected EventListener[] eventListeners;

	@Override
	public EventListener[] getEventListeners() {
		return eventListeners;
	}

	public void setEventListeners(EventListener[] eventListeners) {
		this.eventListeners = eventListeners;
	}

}
