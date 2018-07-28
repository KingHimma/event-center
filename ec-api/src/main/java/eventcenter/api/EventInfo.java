package eventcenter.api;

import java.io.Serializable;
import java.util.UUID;

/**
 * 事件信息
 * @author JackyLIU
 *
 */
public class EventInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6273218077724637964L;

	/**
	 * 事件编号,默认使用uuid
	 */
	private String id;
	
	/**
	 * 事件名称
	 */
	private String name;
	
	/**
	 * 是否异步执行
	 */
	private boolean async = true;
	
	/**
	 * 是否延迟执行
	 */
	private long delay = 0;

	/**
	 * 用于跟踪日志数据
	 */
	private String mdcValue;
	
	/**
	 * 监控的事件点的方法的参数
	 */
	private Object[] args;
	
	public EventInfo(){}
	
	public EventInfo(String name){
		if(null == name || "".equals(name))
			throw new IllegalArgumentException("事件名称不能为空！");
		this.name = name;
		this.id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public EventInfo setId(String id) {
		this.id = id;
		return this;
	}

	public EventInfo setName(String name) {
		this.name = name;
		return this;
	}

	public String getName() {
		return name;
	}

	public boolean isAsync() {
		return async;
	}

	public EventInfo setAsync(boolean async) {
		this.async = async;
		return this;
	}

	public long getDelay() {
		return delay;
	}

	public EventInfo setDelay(long delay) {
		this.delay = delay;
		return this;
	}

	public Object[] getArgs() {
		return args;
	}

	public EventInfo setArgs(Object[] args) {
		this.args = args;
		return this;
	}

	public String getMdcValue() {
		return mdcValue;
	}

	public EventInfo setMdcValue(String mdcValue) {
		this.mdcValue = mdcValue;
		return this;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{name:").append(name)
			.append(",id:").append(id).append(",async:")
			.append(async).append(",delay:").append(delay)
			.append("}");
		return sb.toString();
	}
}
