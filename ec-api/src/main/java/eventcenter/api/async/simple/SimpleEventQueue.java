package eventcenter.api.async.simple;

import eventcenter.api.CommonEventSource;
import eventcenter.api.async.EventQueue;
import eventcenter.api.async.MessageListener;
import eventcenter.api.async.QueueException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 此版本将异步事件发送到队列，然后由后台开启线程池，处理队列中的消息，队列使用的是{@link LinkedBlockingQueue}
 * @author JackyLIU
 *
 */
public class SimpleEventQueue implements EventQueue {
	
	public static final int DEFAULT_QUEUE_CAPACITY = Integer.MAX_VALUE;
	
	private final LinkedBlockingQueue<CommonEventSource> queue;
	
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private volatile boolean closed = false;
	
	private MessageListener messageListener;
	
	/**
	 * 默认读取队列元素的超时时间
	 */
	private long transferTimeout = 200;
	
	private volatile boolean openListener = false;
	
	private MonitorQueue monitorQueue;
	
	public SimpleEventQueue(){
		this(DEFAULT_QUEUE_CAPACITY);
	}
	
	public SimpleEventQueue(int queueCapacity){
		queue = new LinkedBlockingQueue<CommonEventSource>(queueCapacity);
	}

	@Override
	public void close() throws IOException {
		closed = true;
		try {
			closeListener(false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		queue.clear();
		logger.info("SimpleEventQueue队列关闭");
	}

	@Override
	public void offer(CommonEventSource element) {
		boolean result = queue.offer(element);
		if(!result){
			logger.warn("enqueue failure, may be queue is in full size.");
		}
	}

	@Override
	public void offer(CommonEventSource element, long timeout) {
		try {
			boolean result = queue.offer(element, timeout, TimeUnit.MILLISECONDS);
			if(!result){
				logger.warn("enqueue failure, may be queue is in full size.");
			}
		} catch (InterruptedException e) {
			throw new QueueException(e);
		}
	}

	@Override
	public CommonEventSource transfer() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			throw new QueueException(e);
		}
	}

	/**
	 * 简单版的不支持超时设置
	 */
	@Override
	public CommonEventSource transfer(long timeout) {
		if(closed) {
			return null;
		}
		try {
			return queue.take();
		} catch (InterruptedException e) {
			throw new QueueException(e);
		}
	}

	public MessageListener getMessageListener() {
		return messageListener;
	}

	@Override
	public void setMessageListener(MessageListener listener) {
		this.messageListener = listener;
		openListener();		
	}
	
	protected void openListener(){
		if(openListener){
			try{
				closeListener(true);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		openListener = true;
		monitorQueue = new MonitorQueue();
		Thread thread = new Thread(monitorQueue, "simple-queue-monitor");
		thread.start();
	}
	
	protected void closeListener(boolean needSleep) throws InterruptedException{
		if(monitorQueue == null) {
			return ;
		}
		if(openListener){
			openListener = false;
			if(needSleep) {
				Thread.sleep(transferTimeout);
			}
		}
	}

	/**
	 * 监听队列
	 * @author JackyLIU
	 *
	 */
	class MonitorQueue implements Runnable{
		@Override
		public void run() {
			while(!closed && openListener){
				try{
					final CommonEventSource evt = transfer(transferTimeout);
					if(null == evt) {
						continue;
					}

					messageListener.onMessage(evt);
					if(logger.isDebugEnabled()){
						logger.debug(new StringBuilder("执行异步任务:").append(evt));
					}
				}catch(Exception e){
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	@Override
	public int enqueueSize() {
		return queue.size();
	}

	@Override
	public boolean isEmpty() {
		return queue.isEmpty();
	}
}
