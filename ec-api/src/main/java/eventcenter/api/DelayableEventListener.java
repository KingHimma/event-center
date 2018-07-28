package eventcenter.api;

import java.io.Closeable;

/**
 * 可延长化调用
 * @author JackyLIU
 *
 */
public interface DelayableEventListener extends EventListener, Closeable {

	/**
	 * 获取延长时间
	 * @return
	 */
	long getDelayTime();
}
