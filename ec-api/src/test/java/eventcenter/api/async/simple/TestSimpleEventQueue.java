package eventcenter.api.async.simple;

import eventcenter.api.async.MessageListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventSourceBase;
import eventcenter.api.async.MessageListener;

public class TestSimpleEventQueue {

	private SimpleEventQueue queue;
	
	@Before
	public void setUp(){
		queue = new SimpleEventQueue();
	}
	
	@Test
	public void testWithNonListener(){
		queue.offer(new CommonEventSource(this, "test", "test", null, null, null));
		
		EventSourceBase element = queue.transfer();
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
		
		private EventSourceBase message;
		
		@Override
		public void onMessage(EventSourceBase message) {
			this.message = message;
		}

		public EventSourceBase getMessage() {
			return message;
		}
		
		
	}
}
