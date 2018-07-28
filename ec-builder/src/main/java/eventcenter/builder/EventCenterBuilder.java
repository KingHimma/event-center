package eventcenter.builder;

import eventcenter.api.*;
import eventcenter.api.aggregator.AggregatorContainer;
import eventcenter.api.annotation.ListenerBind;
import eventcenter.api.async.QueueEventContainerFactory;
import eventcenter.api.async.simple.SimpleQueueEventContainerFactory;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.monitor.mysql.MysqlMonitorConfig;
import eventcenter.remote.publisher.PublishEventCenter;
import eventcenter.remote.saf.StoreAndForwardPolicy;
import eventcenter.remote.saf.SAFPublishEventCenter;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 事件中心构建器
 * Created by liumingjian on 2017/8/31.
 */
public class EventCenterBuilder {

    protected EventCenterConfig eventCenterConfig;

    protected List<EventFireFilter> eventFireFilters;

    protected AggregatorContainer aggregatorContainer;

    protected PublisherConfig publisherConfig;

    protected SubscriberConfig subscriberConfig;

    protected MonitorConfig monitorConfig;

    protected StoreAndForwardPolicy safPolicy;

    protected CommonEventListenerConfig commonEventListenerConfig;

    protected ApplicationContext applicationContext;

    EventCenterConfig getEventCenterConfig() {
        if(null == eventCenterConfig){
            eventCenterConfig = new EventCenterConfig();
        }
        return eventCenterConfig;
    }

    CommonEventListenerConfig getCommonEventListenerConfig(){
        if(null == commonEventListenerConfig){
            commonEventListenerConfig = new CommonEventListenerConfig();
        }
        return commonEventListenerConfig;
    }

    public List<EventFireFilter> getEventFireFilters() {
        if(null == eventFireFilters){
            eventFireFilters = new ArrayList<EventFireFilter>();
        }
        return eventFireFilters;
    }

    public EventCenterBuilder applicationContext(ApplicationContext applicationContext){
        this.applicationContext = applicationContext;
        return this;
    }

    /**
     * 添加事件中心的日志跟踪编号，log4j中有个mdc的功能，能够在一个线程中打印的日志中添加mdc的值，这些日志打印出的mdc值将保持一致，方便排查问题
     * @param openLoggerMdc
     * @return
     */
    public EventCenterBuilder openLoggerMdc(Boolean openLoggerMdc){
        getEventCenterConfig().setOpenLoggerMdc(openLoggerMdc);
        return this;
    }

    /**
     * 从log4j中的mdc取出具体需要跟踪的值的名称
     * @param loggerMdcField
     * @return
     */
    public EventCenterBuilder loggerMdcField(String loggerMdcField){
        getEventCenterConfig().setLoggerMdcField(loggerMdcField);
        return this;
    }

    /**
     * 构建{@link eventcenter.api.async.simple.SimpleQueueEventContainer}，此容器使用内存作为事件中心容器的队列
     * @param corePoolSize 线程池核心线程数
     * @return
     */
    public EventCenterBuilder simpleQueueContainer(Integer corePoolSize){
        return simpleQueueContainer(corePoolSize, null, null);
    }

    /**
     * 构建{@link eventcenter.api.async.simple.SimpleQueueEventContainer}，此容器使用内存作为事件中心容器的队列
     * @param corePoolSize 线程池核心线程数
     * @param queueCapacity 队列容量
     * @return
     */
    public EventCenterBuilder simpleQueueContainer(Integer corePoolSize, Integer queueCapacity){
        return simpleQueueContainer(corePoolSize, queueCapacity, null);
    }

    /**
     * 构建{@link eventcenter.api.async.simple.SimpleQueueEventContainer}，此容器使用内存作为事件中心容器的队列
     * @param corePoolSize 线程池核心线程数
     * @param queueCapacity 队列容量
     * @param maximumPoolSize 线程池最大线程数
     * @return
     */
    public EventCenterBuilder simpleQueueContainer(Integer corePoolSize, Integer queueCapacity, Integer maximumPoolSize){
        SimpleQueueEventContainerFactory factory = new SimpleQueueEventContainerFactory();
        factory.setCorePoolSize(corePoolSize);
        factory.setQueueCapacity(queueCapacity);
        factory.setMaximumPoolSize(maximumPoolSize);
        return queueContainerFactory(factory);
    }

    /**
     * 使用自定义的队列事件容器工厂构建{@link eventcenter.api.async.simple.SimpleQueueEventContainer}
     * @param factory
     * @return
     */
    public EventCenterBuilder queueContainerFactory(QueueEventContainerFactory factory){
        getEventCenterConfig().setQueueEventContainerFactory(factory);
        return this;
    }

    /**
     * 添加过滤器，非全局的过滤器，所以需要设置eventName
     * @param eventName
     * @param listenerFilter
     * @return
     */
    public EventCenterBuilder addListenerFilter(String eventName, ListenerFilter listenerFilter){
        getEventCenterConfig().addFilter(eventName, listenerFilter);
        return this;
    }

    /**
     * 全局的事件过滤器
     * @param listenerFilter
     * @return
     */
    public EventCenterBuilder addGlobleFilter(ListenerFilter listenerFilter){
        getEventCenterConfig().getGlobalFilters().add(listenerFilter);
        return this;
    }

    /**
     * 事件中心调用fireEvent之前，将会调用此过滤器，这里会阻塞住fireEvent，所以实现IEventFireFilter需要尽可能的减少调用延迟
     * @param filter
     * @return
     */
    public EventCenterBuilder addEventFireFilter(EventFireFilter filter){
        getEventFireFilters().add(filter);
        return this;
    }

    public EventCenterBuilder addEventListener(String eventName, EventListener listener){
        List<EventListener> listeners = getCommonEventListenerConfig().getListeners().get(eventName);
        if(null == listeners){
            listeners = new ArrayList<EventListener>();
            getCommonEventListenerConfig().getListeners().put(eventName, listeners);
        }
        listeners.add(listener);
        return this;
    }

    public EventCenterBuilder addEventListener(EventListener listener){
        ListenerBind bind = listener.getClass().getAnnotation(ListenerBind.class);
        if(null == bind){
            throw new IllegalArgumentException("please input eventName or tag ListenerBind on EventListener");
        }
        String[] eventNames = bind.value().split(",");
        for(String eventName : eventNames){
            addEventListener(eventName.trim(), listener);
        }
        return this;
    }

    public EventCenterBuilder addEventListeners(List<EventListener> listeners){
        for(EventListener listener : listeners){
            addEventListener(listener);
        }
        return this;
    }

    /**
     * 构建并发聚合容器，可以使用{@link AggregatorContainerBuilder}进行构建
     * @param container
     * @return
     */
    public EventCenterBuilder aggregatorContainer(AggregatorContainer container){
        this.aggregatorContainer = container;
        return this;
    }

    /**
     * 构建线程池的并发聚合容器
     * @param corePoolSize
     * @param maximumPoolSize
     * @return
     */
    public EventCenterBuilder simpleAggregatorContainer(Integer corePoolSize, Integer maximumPoolSize){
        this.aggregatorContainer = new AggregatorContainerBuilder().simpleAggregatorContainer(corePoolSize, maximumPoolSize).build();
        return this;
    }

    /**
     * 构建线程池的并发聚合容器
     * @param executor
     * @return
     */
    public EventCenterBuilder simpleAggregatorContainer(ThreadPoolExecutor executor){
        this.aggregatorContainer = new AggregatorContainerBuilder().simpleAggregatorContainer(executor).build();
        return this;
    }

    public EventCenterBuilder safPolicy(StoreAndForwardPolicy policy){
        this.safPolicy = policy;
        return this;
    }

    public EventCenterBuilder publisher(PublisherConfig config){
        this.publisherConfig = config;
        return this;
    }

    public EventCenterBuilder subscriber(SubscriberConfig config){
        this.subscriberConfig = config;
        return this;
    }

    public EventCenterBuilder monitor(MonitorConfig config){
        this.monitorConfig = config;
        return this;
    }

    public DefaultEventCenter build() throws Exception {
        DefaultEventCenter eventCenter;
        if(null == this.publisherConfig){
            // 构建DefaultEventCenter
            eventCenter = new DefaultEventCenter();
        }else if(null == this.safPolicy){
            eventCenter = new PublishEventCenter();
        }else{
            eventCenter = new SAFPublishEventCenter();
            ((SAFPublishEventCenter)eventCenter).setSafPolicy(safPolicy);
        }
        if(null != applicationContext){
            eventCenter.setApplicationContext(applicationContext);
        }
        getEventCenterConfig().getModuleFilters().addAll(this.getEventFireFilters());
        eventCenter.setEcConfig(getEventCenterConfig());
        eventCenter.setAggregatorContainer(aggregatorContainer);
        if(null != commonEventListenerConfig){
            eventCenter.getEcConfig().setEventListenerConfig(commonEventListenerConfig);
        }
        if(null != this.subscriberConfig){
            this.subscriberConfig.load(eventCenter);
        }
        if(null != this.publisherConfig){
            this.publisherConfig.load((PublishEventCenter)eventCenter);
        }
        if(null != this.monitorConfig){
            initMonitorConfig();
            this.monitorConfig.load(eventCenter, this.subscriberConfig != null);
        }
        //eventCenter.startup();
        return eventCenter;
    }

    void initMonitorConfig(){
        if(null == applicationContext){
            return ;
        }
        if(!(this.monitorConfig instanceof MysqlMonitorConfig)){
            return ;
        }
        MysqlMonitorConfig mysqlMonitorConfig = (MysqlMonitorConfig)this.monitorConfig;
        if(!StringUtils.hasText(mysqlMonitorConfig.getDataSourceBeanId())){
            return ;
        }
        mysqlMonitorConfig.setDataSource(this.applicationContext.getBean(mysqlMonitorConfig.getDataSourceBeanId(), DataSource.class));
    }
}
