package eventcenter.api;

import java.io.IOException;

/**
 * 这个默认的实现，主要是包装了真正需要被调用的监听器
 * @author JackyLIU
 *
 */
public class DefaultDelayEventListener implements DelayableEventListener {

	protected final EventListener eventListener;
	
	protected final long delayTime;
	
	private final Object lock = new Object();
	
	private volatile boolean closed = false;
	
	public DefaultDelayEventListener(EventListener eventListener, long delayTime){
		this.eventListener = eventListener;
		this.delayTime = delayTime;
	}
	
	@Override
	public void onObserved(CommonEventSource source) {
		closed = false;
		if(delayTime > 0){
			synchronized (lock) {
				try {
					lock.wait(delayTime);
				} catch (InterruptedException e) {
					
				}
			}
			if(closed == true)
				return ;
		}
		this.eventListener.onObserved(source);
	}
	
	@Override
	public void close() throws IOException {
		closed = true;
		synchronized (lock) {
			lock.notifyAll();
		}
	}

	@Override
	public long getDelayTime() {
		return delayTime;
	}

}
