package eventcenter.leveldb;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventConsumedStatus;

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

	private final CommonEventSource wrapper;
	
	public EventSourceWrapper(String txnId, CommonEventSource evt){
		super(evt.getSource(), evt.getEventId(), evt.getEventName(), evt.getArgs(), null, null);
		this.txnId = txnId;
		this.wrapper = evt;
	}

	public CommonEventSource getWrapper(){
		return this.wrapper;
	}

	@Override
	public Object getResult() {
		return wrapper.getResult();
	}

	@Override
	public <T> T getResult(Class<T> type) {
		return wrapper.getResult(type);
	}

	@Override
	public void pushArg(Object arg) {
		wrapper.pushArg(arg);
	}

	@Override
	public Object getSyncResult() {
		return wrapper.getSyncResult();
	}

	@Override
	public String getMdcValue() {
		return wrapper.getMdcValue();
	}

	@Override
	public void setMdcValue(String mdcValue) {
		wrapper.setMdcValue(mdcValue);
	}

	@Override
	public void setSyncResult(Object syncResult) {
		wrapper.setSyncResult(syncResult);
	}

	@Override
	public EventConsumedStatus getStatus() {
		return wrapper.getStatus();
	}

	@Override
	public void setStatus(EventConsumedStatus status) {
		wrapper.setStatus(status);
	}

	public String getTxnId() {
		return txnId;
	}
}
