package eventcenter.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by liumingjian on 16/1/26.
 */
public class TestEventListenerTask {

    EventListenerTask task;

    int beforeFilterExec;

    int afterFilterExec;

    int listenerExec;

    boolean filterThrow;

    boolean listenerThrow;

    boolean receiptIsExcep;

    SampleListener listener;

    CommonEventSource evt;

    @Before
    public void setUp(){
        EventCenterConfig config = new EventCenterConfig();
        config.getGlobalFilters().add(new SampleFilter());
        config.getListenerFilters().put("test", Arrays.asList((ListenerFilter) new SampleFilter()));
        ConfigContext.setEventCenterConfig(config);

        listener = new SampleListener();
        evt = new CommonEventSource(this, UUID.randomUUID().toString(), "test", null ,null, null);
        task = new EventListenerTask(listener, evt);

        beforeFilterExec = 0;
        afterFilterExec = 0;
        listenerExec = 0;

        filterThrow = false;
        listenerThrow = false;
        receiptIsExcep = false;
    }

    @Test
    public void test1(){
        task.run();
        Assert.assertEquals(2, beforeFilterExec);
        Assert.assertEquals(2, afterFilterExec);
        Assert.assertEquals(1, listenerExec);
    }

    @Test
    public void test2(){
        SampleListener listener = new SampleListener();
        CommonEventSource evt = new CommonEventSource(this, UUID.randomUUID().toString(), "test1", null ,null, null);
        task = new EventListenerTask(listener, evt);
        task.run();
        Assert.assertEquals(1, beforeFilterExec);
        Assert.assertEquals(1, afterFilterExec);
        Assert.assertEquals(1, listenerExec);
    }

    @Test
    public void test3(){
        filterThrow = true;
        task.run();
        Assert.assertEquals(0, beforeFilterExec);
        Assert.assertEquals(0, afterFilterExec);
        Assert.assertEquals(1, listenerExec);
    }

    @Test
    public void test4(){
        listenerThrow = true;
        task.run();
        Assert.assertEquals(2, beforeFilterExec);
        Assert.assertEquals(2, afterFilterExec);
        Assert.assertEquals(0, listenerExec);
        Assert.assertEquals(true, receiptIsExcep);
    }

    @Test
    public void test5(){
        ConfigContext.getConfig().getListenerFilters().put("test1", Arrays.asList((ListenerFilter) new SampleFilter2()));
        SampleListener listener = new SampleListener();
        CommonEventSource evt = new CommonEventSource(this, UUID.randomUUID().toString(), "test1", null ,null, null);
        task = new EventListenerTask(listener, evt);
        filterThrow = true;
        task.run();
        Assert.assertEquals(1, beforeFilterExec);
        Assert.assertEquals(1, afterFilterExec);
        Assert.assertEquals(1, listenerExec);
    }

    @Test
    public void test6(){
        ConfigContext.getConfig().getListenerFilters().put("test1", Arrays.asList((ListenerFilter) new SampleFilter3()));
        SampleListener listener = new SampleListener();
        CommonEventSource evt = new CommonEventSource(this, UUID.randomUUID().toString(), "test1", null ,null, null);
        task = new EventListenerTask(listener, evt);
        filterThrow = true;
        task.run();
        Assert.assertEquals(1, beforeFilterExec);
        Assert.assertEquals(0, afterFilterExec);
        Assert.assertEquals(0, listenerExec);
    }

    @Test
    public void testListenerExecuted1(){
        ListenerExecuted listenerExecuted = Mockito.mock(ListenerExecuted.class);
        task.setListenerExecuted(listenerExecuted);
        test1();
        Mockito.verify(listenerExecuted, Mockito.atLeastOnce()).afterExecuted(evt, listener, null);
    }

    @Test
    public void testListenerExecuted2(){
        ListenerExecuted listenerExecuted = Mockito.mock(ListenerExecuted.class);
        SampleListener listener = new SampleListener();
        CommonEventSource evt = new CommonEventSource(this, UUID.randomUUID().toString(), "test1", null ,null, null);
        task = new EventListenerTask(listener, evt);
        task.setListenerExecuted(listenerExecuted);
        task.run();
        Assert.assertEquals(1, beforeFilterExec);
        Assert.assertEquals(1, afterFilterExec);
        Assert.assertEquals(1, listenerExec);
        Mockito.verify(listenerExecuted, Mockito.atLeastOnce()).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
        Mockito.verify(listenerExecuted, Mockito.atMost(1)).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
    }

    @Test
    public void testListenerExecuted3(){
        ListenerExecuted listenerExecuted = Mockito.mock(ListenerExecuted.class);
        task.setListenerExecuted(listenerExecuted);
        test3();
        Mockito.verify(listenerExecuted, Mockito.atLeastOnce()).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
        Mockito.verify(listenerExecuted, Mockito.atMost(1)).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
    }

    @Test
    public void testListenerExecuted4(){
        ListenerExecuted listenerExecuted = Mockito.mock(ListenerExecuted.class);
        task.setListenerExecuted(listenerExecuted);
        test4();
        Mockito.verify(listenerExecuted, Mockito.atLeastOnce()).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
        Mockito.verify(listenerExecuted, Mockito.atMost(1)).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
    }

    @Test
    public void testListenerExecuted5(){
        ListenerExecuted listenerExecuted = Mockito.mock(ListenerExecuted.class);
        ConfigContext.getConfig().getListenerFilters().put("test1", Arrays.asList((ListenerFilter) new SampleFilter2()));
        SampleListener listener = new SampleListener();
        CommonEventSource evt = new CommonEventSource(this, UUID.randomUUID().toString(), "test1", null ,null, null);
        task = new EventListenerTask(listener, evt);
        task.setListenerExecuted(listenerExecuted);
        filterThrow = true;
        task.run();
        Assert.assertEquals(1, beforeFilterExec);
        Assert.assertEquals(1, afterFilterExec);
        Assert.assertEquals(1, listenerExec);
        Mockito.verify(listenerExecuted, Mockito.atLeastOnce()).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
        Mockito.verify(listenerExecuted, Mockito.atMost(1)).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
    }

    @Test
    public void testListenerExecuted6(){
        ListenerExecuted listenerExecuted = Mockito.mock(ListenerExecuted.class);
        ConfigContext.getConfig().getListenerFilters().put("test1", Arrays.asList((ListenerFilter) new SampleFilter3()));
        SampleListener listener = new SampleListener();
        CommonEventSource evt = new CommonEventSource(this, UUID.randomUUID().toString(), "test1", null ,null, null);
        task = new EventListenerTask(listener, evt);
        task.setListenerExecuted(listenerExecuted);
        filterThrow = true;
        task.run();
        Assert.assertEquals(1, beforeFilterExec);
        Assert.assertEquals(0, afterFilterExec);
        Assert.assertEquals(0, listenerExec);
        Mockito.verify(listenerExecuted, Mockito.atLeastOnce()).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
        Mockito.verify(listenerExecuted, Mockito.atMost(1)).afterExecuted(Mockito.any(CommonEventSource.class), Mockito.any(EventListener.class), Mockito.any(Throwable.class));
    }

    class SampleListener implements EventListener {

        @Override
        public void onObserved(CommonEventSource source) {
            if(listenerThrow)
                throw new IllegalArgumentException();
            listenerExec++;
        }
    }

    class SampleFilter implements ListenerFilter {

        @Override
        public boolean before(EventListener listener, CommonEventSource evt) {
            if(filterThrow)
                throw new IllegalArgumentException();
            beforeFilterExec++;
            return true;
        }

        @Override
        public void after(ListenerReceipt receipt) {
            if(filterThrow)
                throw new IllegalArgumentException();
            afterFilterExec++;
            if(!receipt.isSuccess()){
                receiptIsExcep = true;
            }
        }
    }

    class SampleFilter2 implements ListenerFilter {

        @Override
        public boolean before(EventListener listener, CommonEventSource evt) {
            beforeFilterExec++;
            return true;
        }

        @Override
        public void after(ListenerReceipt receipt) {
            afterFilterExec++;
        }
    }

    class SampleFilter3 implements ListenerFilter {

        @Override
        public boolean before(EventListener listener, CommonEventSource evt) {
            beforeFilterExec++;
            return false;
        }

        @Override
        public void after(ListenerReceipt receipt) {
            afterFilterExec++;
        }
    }
}
