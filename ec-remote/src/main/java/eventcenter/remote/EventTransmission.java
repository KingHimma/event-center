package eventcenter.remote;

import eventcenter.api.EventInfo;

import java.io.Serializable;

/**
 * 事件传递接口，将一个事件从一个点转移到另一个点，并在另一个点触发事件。事件传递
 * 只支持异步事件
 * @author JackyLIU
 *
 */
public interface EventTransmission {
	
	/**
	 * 检查远程端是否健康
	 * @return
	 */
	public boolean checkHealth();

	/**
	 * <p>异步传递事件，将事件传递到另一个点，方法不会阻塞执行，传递成功之后将会立即返回。
	 * <p>eventInfo中的args和result都必须实现{@link Serializable}接口，否则传递的过程会因为序列化问题导致抛出异常
	 * @return
	 */
	public void asyncTransmission(Target target, EventInfo eventInfo, Object result);
}
