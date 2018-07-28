package eventcenter.leveldb;

import eventcenter.api.CommonEventSource;

import java.io.Serializable;

/**
 * it wrapper EventSourceBase and a txn id
 * @author JackyLIU
 *
 */
public class EventSourceWrapper extends CommonEventSource implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7204507292488957404L;

	private final String txnId;
	
	public EventSourceWrapper(String txnId, CommonEventSource evt){
		super(evt.getSource(), evt.getEventId(), evt.getEventName(), evt.getArgs(), evt.getResult(), evt.getMdcValue());
		this.txnId = txnId;
	}

	public String getTxnId() {
		return txnId;
	}
}
