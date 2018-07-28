package eventcenter.scheduler;

import java.io.Serializable;

/**
 * 添加计划任务回执信息，可以通过回执信息取消计划任务
 * @author JackyLIU
 *
 */
public class ScheduleReceipt implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1965826551756660143L;

	private String id;
	
	private boolean success;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	@Override
	public String toString(){
		return new StringBuilder().append("id=").append(id).append(",success=").append(success).toString();
	}
}
