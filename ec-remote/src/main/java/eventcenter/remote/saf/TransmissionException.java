package eventcenter.remote.saf;

import eventcenter.api.EventSourceBase;

/**
 * 发送到服务端事件异常，此异常将会抛出出错的{@link IEventQueueElement}对象
 * @author JackyLIU
 *
 */
public class TransmissionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3122451712667496344L;

	private final EventSourceBase evt;
	
	public TransmissionException(Exception e, EventSourceBase evt){
		super(e);
		this.evt = evt;
	}

	public EventSourceBase getEvt() {
		return evt;
	}
}
