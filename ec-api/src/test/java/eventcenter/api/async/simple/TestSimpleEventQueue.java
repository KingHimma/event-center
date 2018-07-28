package eventcenter.api.async.simple;

import eventcenter.api.CommonEventSource;
import eventcenter.api.async.MessageListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestSimpleEventQueue {

	private SimpleEventQueue queue;
	
	@Before
	public void setUp(){
		queue = new SimpleEventQueue();
	}
	
	@Test
	public void testWithNonListener(){
		queue.offer(new CommonEventSource(this, "test", "test", null, null, null));
		
		CommonEventSource element = queue.transfer();
		Assert.assertNotNull(element);
	}
	
	@Test
	public void testWithListener() throws InterruptedException{
		SampleMessageListener listener = new SampleMessageListener();
		queue.setMessageListener(listener);
		queue.offer(new CommonEventSource(this, "test", "test", null, null, null));
		Thread.sleep(2);
		Assert.assertNotNull(listener.getMessage());
	}
	
	class SampleMessageListener implements MessageListener {
		
		private CommonEventSource message;
		
		@Override
		public void onMessage(CommonEventSource message) {
			this.message = message;
		}

		public CommonEventSource getMessage() {
			return message;
		}
		
		
	}
}
