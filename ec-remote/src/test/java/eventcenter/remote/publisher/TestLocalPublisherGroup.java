package eventcenter.remote.publisher;

import eventcenter.api.EventInfo;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class TestLocalPublisherGroup {

	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		EventInfo eventInfo = new EventInfo("test");
		PublishEventCenter ec = Mockito.spy(new PublishEventCenter());
		Mockito.when(ec.getFireRemoteEventsPolicy()).thenReturn(Mockito.mock(AbstractFireRemoteEventsPolicy.class));
		Mockito.when(ec._superFireEvent(this, eventInfo, null)).thenReturn(null);
		
		List<PublisherGroup> groups = new ArrayList<PublisherGroup>();
		PublisherGroup group1 = new PublisherGroup(null);
		group1.setRemoteEvents("test");
		groups.add(group1);
		LocalPublisherGroup group2 = new LocalPublisherGroup();
		group2.setRemoteEvents("test");
		groups.add(group2);
		
		Mockito.when(ec.getPublisherGroups()).thenReturn(groups);
		
		ec.fireEvent(this, eventInfo, null);
		
		Mockito.verify(ec, Mockito.atLeastOnce()).fireRemoteEvent(Mockito.anyList(), Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class), Mockito.anyBoolean());
		Mockito.verify(ec, Mockito.atLeastOnce())._superFireEvent(this, eventInfo, null);
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void test2() {
		EventInfo eventInfo = new EventInfo("test");
		PublishEventCenter ec = Mockito.spy(new PublishEventCenter());
		Mockito.when(ec.getFireRemoteEventsPolicy()).thenReturn(Mockito.mock(AbstractFireRemoteEventsPolicy.class));
		Mockito.when(ec._superFireEvent(this, eventInfo, null)).thenReturn(null);
		
		List<PublisherGroup> groups = new ArrayList<PublisherGroup>();
		PublisherGroup group1 = new PublisherGroup(null);
		group1.setRemoteEvents("test");
		groups.add(group1);
		PublisherGroup group2 = new PublisherGroup(null);
		group2.setRemoteEvents("test2");
		groups.add(group2);
		
		Mockito.when(ec.getPublisherGroups()).thenReturn(groups);
		
		ec.fireEvent(this, eventInfo, null);
		
		Mockito.verify(ec, Mockito.atLeastOnce()).fireRemoteEvent(Mockito.anyList(), Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class), Mockito.anyBoolean());
		Mockito.verify(ec, Mockito.atLeast(0))._superFireEvent(this, eventInfo, null);
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void test3() {
		EventInfo eventInfo = new EventInfo("test");
		PublishEventCenter ec = Mockito.spy(new PublishEventCenter());
		Mockito.when(ec.getFireRemoteEventsPolicy()).thenReturn(Mockito.mock(AbstractFireRemoteEventsPolicy.class));
		Mockito.when(ec._superFireEvent(this, eventInfo, null)).thenReturn(null);
		
		List<PublisherGroup> groups = new ArrayList<PublisherGroup>();
		
		Mockito.when(ec.getPublisherGroups()).thenReturn(groups);
		
		ec.fireEvent(this, eventInfo, null);
		
		Mockito.verify(ec, Mockito.atLeast(0)).fireRemoteEvent(Mockito.anyList(), Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class), Mockito.anyBoolean());
		Mockito.verify(ec, Mockito.atLeast(1))._superFireEvent(this, eventInfo, null);
	}
	

	@SuppressWarnings("unchecked")
	@Test
	public void test4() {
		EventInfo eventInfo = new EventInfo("test");
		PublishEventCenter ec = Mockito.spy(new PublishEventCenter());
		Mockito.when(ec.getFireRemoteEventsPolicy()).thenReturn(Mockito.mock(AbstractFireRemoteEventsPolicy.class));
		Mockito.when(ec._superFireEvent(this, eventInfo, null)).thenReturn(null);
		
		List<PublisherGroup> groups = new ArrayList<PublisherGroup>();
		PublisherGroup group1 = new PublisherGroup(null);
		group1.setRemoteEvents("test1");
		groups.add(group1);
		PublisherGroup group2 = new PublisherGroup(null);
		group2.setRemoteEvents("test2");
		groups.add(group2);
		
		Mockito.when(ec.getPublisherGroups()).thenReturn(groups);
		
		ec.fireEvent(this, eventInfo, null);
		
		Mockito.verify(ec, Mockito.atLeast(0)).fireRemoteEvent(Mockito.anyList(), Mockito.any(Object.class), Mockito.any(EventInfo.class), Mockito.any(Object.class), Mockito.anyBoolean());
		Mockito.verify(ec, Mockito.atLeast(1))._superFireEvent(this, eventInfo, null);
	}

}
