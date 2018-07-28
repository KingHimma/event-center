package eventcenter.remote;

import eventcenter.api.Remotable;

import java.io.Serializable;
import java.util.Date;

/**
 * 事件目标者，用于发送远程事件的target
 * @author JackyLIU
 *
 */
public class Target implements Serializable, Remotable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5715461371980160136L;

	private String targetClass;

	/**
	 * 生产端的（或者是发送端的）事件中心的身份ID
	 */
	private String nodeId;

	/**
	 * target创建时间
	 */
	private Date created = new Date();
	
	public Target(){
		
	}
	
	public Target(String targetClass){
		this.targetClass = targetClass;
	}

	public String getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(String targetClass) {
		this.targetClass = targetClass;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public Date getCreated() {
		return created;
	}
}
