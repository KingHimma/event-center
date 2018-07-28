package eventcenter.api.aggregator.simple;

import java.io.Serializable;

/**
 * 用于创建 {@link SimpleAggregatorMultiContainer}容器所需要的指定事件的容器配置。此线程池默认使用{@link java.util.concurrent.SynchronousQueue}
 * 作为线程池的阻塞队列
 * Created by liumingjian on 2016/10/20.
 */
public class AggregatorThreadPoolInfo implements Serializable{
    private static final long serialVersionUID = 7984830283599423759L;

    private String eventNames;

    private Integer corePoolSize = 0;

    private Integer maximumPoolSize = 100;

    public String getEventNames() {
        return eventNames;
    }

    public void setEventNames(String eventNames) {
        this.eventNames = eventNames;
    }

    /**
     * 线程池初始化的核心数量
     * @return
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * 线程池初始化的核心数量
     * @param corePoolSize
     */
    public void setCorePoolSize(Integer corePoolSize) {
        if(null == corePoolSize)
            return ;
        this.corePoolSize = corePoolSize;
    }

    /**
     * 线程池的最大数量
     * @return
     */
    public Integer getMaximumPoolSize() {
        return maximumPoolSize;
    }

    /**
     * 线程池的最大数量
     * @param maximumPoolSize
     */
    public void setMaximumPoolSize(Integer maximumPoolSize) {
        if(null == maximumPoolSize)
            return ;
        this.maximumPoolSize = maximumPoolSize;
    }

    public static AggregatorThreadPoolInfo buildDefault(String eventNames){
        AggregatorThreadPoolInfo info = new AggregatorThreadPoolInfo();
        info.setEventNames(eventNames);
        return info;
    }

    public static AggregatorThreadPoolInfo build(String eventNames, int corePoolSize, int maximumPoolSize){
        AggregatorThreadPoolInfo info = new AggregatorThreadPoolInfo();
        info.setEventNames(eventNames);
        info.setCorePoolSize(corePoolSize);
        info.setMaximumPoolSize(maximumPoolSize);
        return info;
    }
}
