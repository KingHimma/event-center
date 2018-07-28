package eventcenter.builder;

import eventcenter.api.aggregator.AggregatorContainer;
import eventcenter.api.aggregator.simple.AggregatorThreadPoolInfo;
import eventcenter.api.aggregator.simple.SimpleAggregatorContainer;
import eventcenter.api.aggregator.simple.SimpleAggregatorMultiContainer;
import eventcenter.remote.utils.StringHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 构建{@link AggregatorContainer}并发聚合容器
 * Created by liumingjian on 2017/9/1.
 */
public class AggregatorContainerBuilder {

    protected Integer corePoolSize;

    protected Integer maximumPoolSize;

    protected ThreadPoolExecutor executor;

    private List<AggregatorThreadPoolInfo> infos;

    public List<AggregatorThreadPoolInfo> getInfos() {
        if(null == infos){
            infos = new ArrayList<AggregatorThreadPoolInfo>();
        }
        return infos;
    }

    public void setInfos(List<AggregatorThreadPoolInfo> infos) {
        this.infos = infos;
    }

    /**
     * 构建并发聚合的容器，使用线程池的并发聚合容器
     * @param corePoolSize 线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @return
     */
    public AggregatorContainerBuilder simpleAggregatorContainer(Integer corePoolSize, Integer maximumPoolSize){
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        return this;
    }

    /**
     * 构建并发聚合的容器，使用线程池的并发聚合容器
     * @param executor 线程池实例
     * @return
     */
    public AggregatorContainerBuilder simpleAggregatorContainer(ThreadPoolExecutor executor){
        this.executor = executor;
        return this;
    }

    /**
     * 构建多个事件放在独立的线程池中
     * @param eventNames 多个事件请使用','逗号分隔
     * @param corePoolSize
     * @param maximumPoolSize
     * @return
     */
    public AggregatorContainerBuilder threadPoolInfo(String eventNames, Integer corePoolSize, Integer maximumPoolSize){
        if(StringHelper.isEmpty(eventNames)){
            throw new IllegalArgumentException("please set eventNames");
        }
        AggregatorThreadPoolInfo info = new AggregatorThreadPoolInfo();
        info.setEventNames(eventNames);
        info.setCorePoolSize(corePoolSize);
        info.setMaximumPoolSize(maximumPoolSize);
        getInfos().add(info);
        return this;
    }

    public AggregatorContainer build(){
        if(getInfos().size() > 0){
            SimpleAggregatorMultiContainer container;
            if(null != executor) {
                container = new SimpleAggregatorMultiContainer(executor);
            }else if(corePoolSize != null){
                container = new SimpleAggregatorMultiContainer(corePoolSize, maximumPoolSize);
            }else{
                container = new SimpleAggregatorMultiContainer();
            }
            container.setThreadPoolInfos(getInfos());
            return container;
        }
        SimpleAggregatorContainer container;
        if(null != executor){
            container = new SimpleAggregatorContainer(executor);
        }else if(corePoolSize != null){
            container = new SimpleAggregatorContainer(corePoolSize, maximumPoolSize);
        }else{
            container = new SimpleAggregatorContainer();
        }
        return container;
    }
}
