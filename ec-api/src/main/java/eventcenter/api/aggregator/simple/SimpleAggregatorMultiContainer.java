package eventcenter.api.aggregator.simple;

import eventcenter.api.CommonEventSource;
import eventcenter.api.aggregator.AggregatorEventListener;
import eventcenter.api.aggregator.ListenerExceptionHandler;
import eventcenter.api.aggregator.ListenersConsumedResult;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 支持多个并发运行的容器，可以指定不同的事件到不同的线程池中，如果未指定的，则使用默认的线程池
 * Created by liumingjian on 2016/10/20.
 */
public class SimpleAggregatorMultiContainer extends SimpleAggregatorContainer {

    private volatile boolean open = false;

    private List<AggregatorThreadPoolInfo> threadPoolInfos;

    /**
     * it created by {@link #threadPoolInfos}
     */
    protected Set<ThreadPoolExecutor> multiThreadPools;

    /**
     * 事件名称所对应的线程池
     */
    protected Map<String, ThreadPoolExecutor> eventThreadPoolCache;

    /**
     * 构造函数配置默认容器的参数
     * @param corePoolSize
     * @param maximumPoolSize
     */
    public SimpleAggregatorMultiContainer(int corePoolSize, int maximumPoolSize) {
        super(corePoolSize, maximumPoolSize);
    }

    /**
     * 构造函数配置默认容器的参数
     * @param threadPool
     */
    public SimpleAggregatorMultiContainer(ThreadPoolExecutor threadPool) {
        super(threadPool);
    }

    /**
     * 构造函数配置默认容器的参数
     */
    public SimpleAggregatorMultiContainer() {
        super(createDefaultThreadPool(0,100));
    }

    @PostConstruct
    public void start(){
        if(open) {
            return ;
        }

        if(null == threadPoolInfos || threadPoolInfos.size() == 0){
            logger.warn("it didn't set threadPoolInfos, all aggregator event would use default thread pool");
            open = true;
            return ;
        }

        for(AggregatorThreadPoolInfo poolInfo : threadPoolInfos){
            validateThreadPoolInfo(poolInfo);
        }

        multiThreadPools = new HashSet<ThreadPoolExecutor>(threadPoolInfos.size() + 1);
        eventThreadPoolCache = new HashMap<String, ThreadPoolExecutor>();
        for(AggregatorThreadPoolInfo poolInfo : threadPoolInfos){
            ThreadPoolExecutor executor = createExecutor(poolInfo);
            String[] names = poolInfo.getEventNames().split(",");
            for(String name : names){
                eventThreadPoolCache.put(name.trim(), executor);
            }
            multiThreadPools.add(executor);
        }
        open = true;
    }

    void validateThreadPoolInfo(AggregatorThreadPoolInfo poolInfo){
        if(poolInfo.getCorePoolSize() < 0) {
            throw new IllegalArgumentException("corePoolSize parameter must be more or equal than 0");
        }
        if(poolInfo.getMaximumPoolSize() <= 0) {
            throw new IllegalArgumentException("maximumPoolSize parameter must be more than 0");
        }
        if(poolInfo.getEventNames() == null || "".equals(poolInfo.getEventNames().trim())) {
            throw new IllegalArgumentException("eventNames parameter can't be empty");
        }
    }

    ThreadPoolExecutor createExecutor(AggregatorThreadPoolInfo poolInfo){
        return createDefaultThreadPool(poolInfo.getCorePoolSize(), poolInfo.getMaximumPoolSize());
    }

    @Override
    @PreDestroy
    public void close(){
        if(!open) {
            return ;
        }

        super.close();
        for(ThreadPoolExecutor executor : multiThreadPools){
            executor.shutdownNow();
        }
        open = false;
    }

    public List<AggregatorThreadPoolInfo> getThreadPoolInfos() {
        if(null == threadPoolInfos) {
            threadPoolInfos = new ArrayList<AggregatorThreadPoolInfo>();
        }
        return threadPoolInfos;
    }

    public void setThreadPoolInfos(List<AggregatorThreadPoolInfo> threadPoolInfos) {
        this.threadPoolInfos = threadPoolInfos;
    }

    @Override
    public ListenersConsumedResult executeListenerSources(AggregatorEventListener listener, List<CommonEventSource> sources, ListenerExceptionHandler handler) throws InterruptedException {
        if(!open) {
            start();
        }
        final String eventName = sources.get(0).getEventName();
        // default use default thread pool
        if(!eventThreadPoolCache.containsKey(eventName)) {
            return super.executeListenerSources(listener, sources, handler);
        }

        return executeListenerSources(listener, sources, handler, eventThreadPoolCache.get(eventName));
    }

    @Override
    public ListenersConsumedResult executeListeners(List<AggregatorEventListener> listeners, CommonEventSource source, ListenerExceptionHandler handler) throws InterruptedException {
        if(!open) {
            start();
        }
        final String eventName = source.getEventName();
        if(!eventThreadPoolCache.containsKey(eventName)) {
            return super.executeListeners(listeners, source, handler);
        }

        return executeListeners(listeners, source, handler, eventThreadPoolCache.get(eventName));
    }
}
