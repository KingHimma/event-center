package eventcenter.api.aggregator;

import java.io.Serializable;

/**
 * 单个监听器消费事件的结果信息
 * @author JackyLIU
 *
 */
public class ListenerConsumedResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1883318174161064242L;

	/**
	 * 监听器消费的时间
	 */
	private long took;
	
	/**
	 * 监听器类名
	 */
	private Class<?> listenerType;
	
	/**
	 * 监听器消费后的返回结果
	 */
	private Object result;
	
	/**
	 * listener运行是否出现异常
	 */
	private boolean error;

	public long getTook() {
		return took;
	}

	public void setTook(long took) {
		this.took = took;
	}

	public Class<?> getListenerType() {
		return listenerType;
	}

	public void setListenerType(Class<?> listenerType) {
		this.listenerType = listenerType;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}
}
