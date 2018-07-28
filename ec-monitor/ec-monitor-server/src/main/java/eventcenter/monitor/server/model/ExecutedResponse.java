package eventcenter.monitor.server.model;

import java.io.Serializable;

/**
 * Created by liumingjian on 16/2/25.
 */
public class ExecutedResponse implements Serializable {

    private static final long serialVersionUID = 8699897964927377658L;

    private Long qTime;

    private int result;

    private Object data;

    private String errorMsg;

    public Long getQTime() {
        return qTime;
    }

    public void setQTime(Long qTime) {
        this.qTime = qTime;
    }

    public int getResult() {
		return result;
	}

	public void setResult(int result) {
		this.result = result;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public static ExecutedResponse buildSuccess(Long qTime, Object result){
        return build(qTime, 1, null, result);
    }

    public static ExecutedResponse buildError(Long qTime, String errorMsg){
        return build(qTime, 2, errorMsg, null);
    }

    public static ExecutedResponse build(Long qTime, int result, String errorMsg, Object data){
        ExecutedResponse response = new ExecutedResponse();
        response.setQTime(qTime);
        response.setResult(result);
        response.setData(data);
        response.setErrorMsg(errorMsg);
        return response;
    }
}
