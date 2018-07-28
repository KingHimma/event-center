package eventcenter.remote.saf;

import eventcenter.api.CommonEventSource;

/**
 * 发送到服务端事件异常
 * @author JackyLIU
 *
 */
public class TransmissionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3122451712667496344L;

	private final CommonEventSource evt;
	
	public TransmissionException(Exception e, CommonEventSource evt){
		super(e);
		this.evt = evt;
	}

	public CommonEventSource getEvt() {
		return evt;
	}
}
