package eventcenter.api.aggregator.simple;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.EventSourceBase;
import eventcenter.api.aggregator.AggregatorEventListener;
import eventcenter.api.aggregator.AggregatorEventSource;
import eventcenter.api.aggregator.ListenerExceptionHandler;
import eventcenter.api.aggregator.ListenersConsumedResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

/**
 * Created by liumingjian on 2016/10/20.
 */
public class TestSimpleAggregatorMultiContainer {

    SimpleAggregatorMultiContainer multiContainer;

    public TestSimpleAggregatorMultiContainer(){
        multiContainer = new SimpleAggregatorMultiContainer();
        multiContainer.getThreadPoolInfos().add(AggregatorThreadPoolInfo.buildDefault("test2"));
        multiContainer.getThreadPoolInfos().add(AggregatorThreadPoolInfo.buildDefault("test3,test4"));
        multiContainer.start();
    }

    @Test
    public void testStartAndClose() throws Exception {
        Assert.assertEquals(3, multiContainer.eventThreadPoolCache.size());
        Assert.assertEquals(2, multiContainer.multiThreadPools.size());
    }

    @Test
    public void testExecuteListeners1() throws InterruptedException {
        final Long delay = 2000L;
        AggregatorEventListener listener1 = new AggregatorListener1(delay);
        AggregatorEventListener listener2 = new AggregatorListener2(delay);
        AggregatorEventSource source = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test1", new Object[]{"Hello", "World"}, null, null));
        long start = System.currentTimeMillis();
        ListenersConsumedResult result = multiContainer.executeListeners(Arrays.asList(listener1, listener2), source, new ListenerExceptionHandler() {
            @Override
            public Object handle(EventListener listener, EventSourceBase source, Exception e) {
                return null;
            }
        });
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue(took <= delay + 50);
        Assert.assertEquals(2, result.getResults().size());
        Assert.assertEquals(2, multiContainer.threadPool.getCompletedTaskCount());
    }

    @Test
    public void testExecuteListeners2() throws InterruptedException {
        final Long delay = 2000L;
        AggregatorEventListener listener1 = new AggregatorListener1(delay);
        AggregatorEventListener listener2 = new AggregatorListener2(delay);
        AggregatorEventSource source = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World"}, null, null));
        long start = System.currentTimeMillis();
        ListenersConsumedResult result = multiContainer.executeListeners(Arrays.asList(listener1, listener2), source, new ListenerExceptionHandler() {
            @Override
            public Object handle(EventListener listener, EventSourceBase source, Exception e) {
                return null;
            }
        });
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue(took <= delay + 50);
        Assert.assertEquals(2, result.getResults().size());
        Assert.assertEquals(2, multiContainer.eventThreadPoolCache.get("test2").getCompletedTaskCount());
    }

    @Test
    public void testExecuteListeners3() throws InterruptedException {
        final Long delay = 2000L;
        AggregatorEventListener listener1 = new AggregatorListener1(delay);
        AggregatorEventListener listener2 = new AggregatorListener2(delay);
        AggregatorEventSource source = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test3", new Object[]{"Hello", "World"}, null, null));
        long start = System.currentTimeMillis();
        ListenersConsumedResult result = multiContainer.executeListeners(Arrays.asList(listener1, listener2), source, new ListenerExceptionHandler() {
            @Override
            public Object handle(EventListener listener, EventSourceBase source, Exception e) {
                return null;
            }
        });
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue(took <= delay + 50);
        Assert.assertEquals(2, result.getResults().size());
        Assert.assertEquals(2, multiContainer.eventThreadPoolCache.get("test3").getCompletedTaskCount());
    }

    @Test
    public void testExecuteListeners4() throws InterruptedException {
        final Long delay = 2000L;
        AggregatorEventListener listener1 = new AggregatorListener1(delay);
        AggregatorEventListener listener2 = new AggregatorListener2(delay);
        AggregatorEventSource source = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test4", new Object[]{"Hello", "World"}, null, null));
        long start = System.currentTimeMillis();
        ListenersConsumedResult result = multiContainer.executeListeners(Arrays.asList(listener1, listener2), source, new ListenerExceptionHandler() {
            @Override
            public Object handle(EventListener listener, EventSourceBase source, Exception e) {
                return null;
            }
        });
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue(took <= delay + 50);
        Assert.assertEquals(2, result.getResults().size());
        Assert.assertEquals(2, multiContainer.eventThreadPoolCache.get("test4").getCompletedTaskCount());
    }

    @Test
    public void testExecuteListenerSources1() throws InterruptedException {
        final Long delay = 1000L;
        AggregatorEventListener listener3 = new AggregatorListener3(delay);
        EventSourceBase source1 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test1", new Object[]{"Hello", "World", 1}, null, null));
        EventSourceBase source2 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test1", new Object[]{"Hello", "World", 2}, null, null));
        EventSourceBase source3 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test1", new Object[]{"Hello", "World", 3}, null, null));

        long start = System.currentTimeMillis();
        ListenersConsumedResult result = multiContainer.executeListenerSources(listener3, Arrays.asList(source1, source2, source3), new ListenerExceptionHandler() {
            @Override
            public Object handle(EventListener listener, EventSourceBase source, Exception e) {
                return null;
            }
        });
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue(took <= delay + 50);
        Assert.assertEquals(3, result.getResults().size());
        Assert.assertEquals(3, multiContainer.threadPool.getCompletedTaskCount());
    }

    @Test
    public void testExecuteListenerSources2() throws InterruptedException {
        final Long delay = 1000L;
        AggregatorEventListener listener3 = new AggregatorListener3(delay);
        EventSourceBase source1 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 1}, null, null));
        EventSourceBase source2 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 2}, null, null));
        EventSourceBase source3 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test2", new Object[]{"Hello", "World", 3}, null, null));

        long start = System.currentTimeMillis();
        ListenersConsumedResult result = multiContainer.executeListenerSources(listener3, Arrays.asList(source1, source2, source3), new ListenerExceptionHandler() {
            @Override
            public Object handle(EventListener listener, EventSourceBase source, Exception e) {
                return null;
            }
        });
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue(took <= delay + 50);
        Assert.assertEquals(3, result.getResults().size());
        Assert.assertEquals(3, multiContainer.eventThreadPoolCache.get("test2").getCompletedTaskCount());
    }

    @Test
    public void testExecuteListenerSources3() throws InterruptedException {
        final Long delay = 1000L;
        AggregatorEventListener listener3 = new AggregatorListener3(delay);
        EventSourceBase source1 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test3", new Object[]{"Hello", "World", 1}, null, null));
        EventSourceBase source2 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test3", new Object[]{"Hello", "World", 2}, null, null));
        EventSourceBase source3 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test3", new Object[]{"Hello", "World", 3}, null, null));

        long start = System.currentTimeMillis();
        ListenersConsumedResult result = multiContainer.executeListenerSources(listener3, Arrays.asList(source1, source2, source3), new ListenerExceptionHandler() {
            @Override
            public Object handle(EventListener listener, EventSourceBase source, Exception e) {
                return null;
            }
        });
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue(took <= delay + 50);
        Assert.assertEquals(3, result.getResults().size());
        Assert.assertEquals(3, multiContainer.eventThreadPoolCache.get("test3").getCompletedTaskCount());
    }

    @Test
    public void testExecuteListenerSources4() throws InterruptedException {
        final Long delay = 1000L;
        AggregatorEventListener listener3 = new AggregatorListener3(delay);
        EventSourceBase source1 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test4", new Object[]{"Hello", "World", 1}, null, null));
        EventSourceBase source2 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test4", new Object[]{"Hello", "World", 2}, null, null));
        EventSourceBase source3 = new AggregatorEventSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test4", new Object[]{"Hello", "World", 3}, null, null));

        long start = System.currentTimeMillis();
        ListenersConsumedResult result = multiContainer.executeListenerSources(listener3, Arrays.asList(source1, source2, source3), new ListenerExceptionHandler() {
            @Override
            public Object handle(EventListener listener, EventSourceBase source, Exception e) {
                return null;
            }
        });
        long took = System.currentTimeMillis() - start;
        Assert.assertTrue(took <= delay + 50);
        Assert.assertEquals(3, result.getResults().size());
        Assert.assertEquals(3, multiContainer.eventThreadPoolCache.get("test4").getCompletedTaskCount());
    }

    class AggregatorListener1 implements AggregatorEventListener {

        final Long delay;

        public AggregatorListener1(Long delay){
            if(null == delay){
                this.delay = 0L;
            }else{
                this.delay = delay;
            }
        }

        @Override
        public void onObserved(EventSourceBase source) {
            AggregatorEventSource evt = (AggregatorEventSource)source;

            if(this.delay > 0L){
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 将执行的结果放入到结果中
            evt.putResult(this, Thread.currentThread().getName());
        }

    }

    class AggregatorListener2 implements AggregatorEventListener {

        final Long delay;

        public AggregatorListener2(Long delay){
            if(null == delay){
                this.delay = 0L;
            }else{
                this.delay = delay;
            }
        }

        @Override
        public void onObserved(EventSourceBase source) {
            AggregatorEventSource evt = (AggregatorEventSource)source;
            if(this.delay > 0L){
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            evt.putResult(this, Thread.currentThread().getName());
        }

    }

    class AggregatorListener3 implements AggregatorEventListener {

        final Long delay;

        public AggregatorListener3(Long delay){
            if(null == delay){
                this.delay = 0L;
            }else{
                this.delay = delay;
            }
        }

        @Override
        public void onObserved(EventSourceBase source) {
            AggregatorEventSource evt = (AggregatorEventSource)source;
            if(this.delay > 0L){
                try {
                    Thread.sleep(this.delay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            evt.putResult(this, Thread.currentThread().getName());
        }

    }
}