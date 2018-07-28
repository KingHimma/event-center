package eventcenter.remote.publisher;

import eventcenter.api.EventInfo;
import eventcenter.api.appcache.AppDataContext;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.Target;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

public class TestAsyncFireRemoteEventsPolicy {

	public TestAsyncFireRemoteEventsPolicy(){
		System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
	}

	/*@Test
	public void test() throws InterruptedException {
		PublishEventCenter ec = Mockito.mock(PublishEventCenter.class);
		AbstractFireRemoteEventsPolicy policy = Mockito.spy(new AsyncFireRemoteEventsPolicy(ec));
		EventTransmission publisherGroup = Mockito.mock(EventTransmission.class);
		List<PublisherGroup> list = new ArrayList<PublisherGroup>();
		PublisherGroup group = new PublisherGroup(publisherGroup);
		group.setRemoteEvents("test");
		list.add(group);
		group = new PublisherGroup(publisherGroup);
		group.setRemoteEvents("test");
		list.add(group);
		group = new PublisherGroup(publisherGroup);
		group.setRemoteEvents("test");
		list.add(group);

		EventInfo ei = new EventInfo("test");
		Target target = new Target(this.getClass().getName());
		final AtomicInteger count = new AtomicInteger();
		Mockito.doAnswer(new Answer<Object>(){
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Thread.sleep(500);
				count.incrementAndGet();
				return null;
			}
		}).when(publisherGroup).asyncTransmission(target, ei, null);
		
		long start = System.currentTimeMillis();
		policy.fireRemoteEvents(list, target, ei, null);
		boolean flag = true;
		while(flag){
			if(count.get() >= 3)
				break;
			Thread.sleep(1);
		}
		long took = (System.currentTimeMillis() - start);
		System.out.println("消耗时间：" + (System.currentTimeMillis() - start) + " ms.");
		Assert.assertEquals(true, took < 650);
		
		Mockito.verify(publisherGroup, Mockito.atMost(3)).asyncTransmission(target, ei, null);
		
		Mockito.doThrow(new RuntimeException("test")).when(publisherGroup).asyncTransmission(target, ei, null);
		policy.fireRemoteEvents(list, target, ei, null);
		Thread.sleep(10);
		Mockito.verify(policy, Mockito.atMost(3)).handleAsyncTransmissionException(Mockito.any(Exception.class), Mockito.any(PublisherGroup.class), 
				Mockito.any(Target.class), Mockito.any(EventInfo.class), Mockito.any(Object.class));
	}*/

	@Test
	public void testAsyncTransmission(){
		PublishEventCenter ec = Mockito.mock(PublishEventCenter.class);
		AbstractFireRemoteEventsPolicy policy = Mockito.spy(new AsyncFireRemoteEventsPolicy(ec));
		Target target = new Target(this.getClass().getName());
		policy.asyncTransmissionDirectly(Mockito.mock(EventTransmission.class), null, target, Mockito.mock(EventInfo.class), null);
		Assert.assertTrue(target.getNodeId() != null && !"".equals(target.getNodeId().trim()));
	}

}
