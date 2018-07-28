package eventcenter.api.async;

import org.junit.Assert;
import org.junit.Test;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventCenterConfig;
import eventcenter.api.EventSourceBase;
import eventcenter.api.async.simple.SimpleEventQueue;

public class TestQueueEventContainer {

	@Test
	public void test() {
		EventQueue queue = new SimpleEventQueue();
		EventCenterConfig config = new EventCenterConfig();
		QueueEventContainer container = new SampleQueueEventContainer(config, queue);
		
		container.send(new CommonEventSource(this, "test", "test", null, null, null));
		
		EventSourceBase qe = queue.transfer();
		Assert.assertNotNull(qe);
		CommonEventSource source = (CommonEventSource)qe;
		Assert.assertEquals("test", source.getEventName());
		Assert.assertEquals("test", source.getEventId());
	}
	
	class SampleQueueEventContainer extends QueueEventContainer{
		
		public SampleQueueEventContainer(EventCenterConfig config, EventQueue queue) {
			super(config, queue);
		}

		@Override
		public void startup() throws Exception {
			
		}
		
		@Override
		public void shutdown() throws Exception {
			
		}
		
		@Override
		public boolean isIdle() {
			return false;
		}


		@Override
		public boolean isPersisted() {
			return false;
		}

		@Override
		public int countOfMaxConcurrent() {
			return 0;
		}

		@Override
		public int countOfLiveThread() {
			return 0;
		}

		@Override
		public int countOfQueueBuffer() {
			return 0;
		}
	}

}
