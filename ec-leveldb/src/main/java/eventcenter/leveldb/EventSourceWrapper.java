package eventcenter.leveldb;

import java.io.Serializable;

import eventcenter.api.EventSourceBase;

/**
 * it wrapper EventSourceBase and a txn id
 * @author JackyLIU
 *
 */
public class EventSourceWrapper extends EventSourceBase implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7204507292488957404L;

	private final String txnId;
	
	private final EventSourceBase evt;
	
	public EventSourceWrapper(String txnId, EventSourceBase evt){
		super(evt.getSource(), evt.getEventId(), evt.getEventName(), evt.getMdcValue());
		this.txnId = txnId;
		this.evt = evt;
	}

	public String getTxnId() {
		return txnId;
	}

	public EventSourceBase getEvt() {
		return evt;
	}
}
