package eventcenter.builder.spring.schema;

import com.alibaba.dubbo.config.AbstractInterfaceConfig;
import com.alibaba.dubbo.config.AbstractReferenceConfig;
import com.alibaba.dubbo.config.AbstractServiceConfig;
import eventcenter.builder.AggregatorContainerBuilder;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.LevelDBContainerBuilder;
import eventcenter.builder.MonitorConfig;
import eventcenter.builder.dubbo.*;
import eventcenter.builder.dubbo.*;
import eventcenter.builder.monitor.log.LogMonitorConfigBuilder;
import eventcenter.builder.monitor.mixing.MixingMonitorConfigBuilder;
import eventcenter.builder.monitor.mysql.MysqlMonitorConfigBuilder;
import eventcenter.builder.saf.SimpleSAFPolicyBuilder;
import eventcenter.builder.saf.leveldb.LeveldbSAFPolicyBuilder;
import eventcenter.builder.spring.EventCenterFactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by liumingjian on 2017/9/27.
 */
public class EventCenterBeanDefinitionParser implements BeanDefinitionParser{

    static final String FACTORY_BEAN_NAME = "eventCenterBuilder";

    static final String FACTORY_BEAN_METHOD = "build";

    static final String ELEMENT_QUEUE = "queue";

    static final String ELEMENT_SIMPLE_QUEUE_CONTAINER = "simpleQueueContainer";

    static final String ELEMENT_LEVELDB_QUEUE_CONTAINER = "leveldbQueueContainer";

    static final String ELEMENT_AGGREGATOR = "aggregator";

    static final String ELEMENT_AGGREGATOR_MULTI = "multi";

    static final String ELEMENT_AGGREGATOR_THREADPOOL_INFO = "threadPoolInfo";

    static final String ELEMENT_DUBBO = "dubbo";

    static final String ELEMENT_DUBBO_PUBLISH = "dubboPublish";

    static final String ELEMENT_DUBBO_SUBSCRIBE = "dubboSubscribe";

    static final String ELEMENT_DUBBO_PUBLISH_GROUP = "dubboPublishGroup";

    static final String ELEMENT_EVENT_TRANSMISSION = "eventTransmission";

    static final String ELEMENT_SAF = "saf";

    static final String ELEMENT_SAF_SIMPLE = "simpleSaf";

    static final String ELEMENT_SAF_LEVELDB = "leveldbSaf";

    static final String ELEMENT_LOG_MONITOR = "logMonitor";

    static final String ELEMENT_MYSQL_MONITOR = "mysqlMonitor";

    static final String ELEMENT_MIXING_MONITOR = "mixingMonitor";

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition(EventCenterFactoryBean.class);
        beanDefinition.setLazyInit(true);
        beanDefinition.setInitMethodName("startup");
        beanDefinition.setDestroyMethodName("shutdown");
        String id = element.getAttribute("id");
        if(!StringUtils.hasText(id)){
            id = "eventCenter";
        }

        // 构建builder
        beanDefinition.getPropertyValues().add("builder", createEventCenterBuilder(element));
        parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        return beanDefinition;
    }

    EventCenterBuilder createEventCenterBuilder(Element element){
        EventCenterBuilder builder = new EventCenterBuilder();
        String openLoggerMdc = element.getAttribute("openLoggerMdc");
        String loggerMdcField = element.getAttribute("loggerMdcField");
        String group = element.getAttribute("group");
        if(StringUtils.hasText(openLoggerMdc)){
            builder.openLoggerMdc(Boolean.parseBoolean(openLoggerMdc));
        }
        if(StringUtils.hasText(loggerMdcField)){
            builder.loggerMdcField(loggerMdcField);
        }
        if(StringUtils.hasText(group)){
            DubboConfigContext.getInstance().groupName(group);
        }
        NodeList nodeList = element.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++){
            Node node = nodeList.item(i);
            if(ELEMENT_QUEUE.equals(node.getLocalName())){
                builder = createQueueContainer(builder, node);
            }else if(ELEMENT_AGGREGATOR.equals(node.getLocalName())){
                builder = createAggregator(builder, node);
            }else if(ELEMENT_DUBBO.equals(node.getLocalName())){
                builder = createDubbo(builder, node);
            }else if(ELEMENT_SAF.equals(node.getLocalName())){
                builder = createSaf(builder, node);
            }else if(ELEMENT_LOG_MONITOR.equals(node.getLocalName())){
                builder.monitor(createLogMonitor(builder, node));
            }else if(ELEMENT_MYSQL_MONITOR.equals(node.getLocalName())){
                builder.monitor(createMysqlMonitor(builder, node));
            }else if(ELEMENT_MIXING_MONITOR.equals(node.getLocalName())){
                builder.monitor(createMixingMonitor(builder, node));
            }
        }
        return builder;
    }

    EventCenterBuilder createDubbo(EventCenterBuilder builder, Node node){
        String applicationName = getAttribute(node, "applicationName");
        String applicationOwner = getAttribute(node, "applicationOwner");
        String protocolName = getAttribute(node, "protocolName");
        String protocolHost = getAttribute(node, "protocolHost");
        String protocolPort = getAttribute(node, "protocolPort");
        String registryProtocol = getAttribute(node, "registryProtocol");
        String registryAddress = getAttribute(node, "registryAddress");
        String registryUsername = getAttribute(node, "registryUsername");
        String registryPassword = getAttribute(node, "registryPassword");
        if(StringUtils.hasText(applicationName)){
            DubboConfigContext.getInstance().applicationName(applicationName);
        }
        if(StringUtils.hasText(applicationOwner)){
            DubboConfigContext.getInstance().applicationOwner(applicationOwner);
        }
        if(StringUtils.hasText(protocolName)){
            DubboConfigContext.getInstance().protocolName(protocolName);
        }
        if(StringUtils.hasText(protocolHost)){
            DubboConfigContext.getInstance().protocolHost(protocolHost);
        }
        if(StringUtils.hasText(protocolPort)){
            DubboConfigContext.getInstance().protocolPort(Integer.parseInt(protocolPort));
        }
        if(StringUtils.hasText(registryProtocol)){
            DubboConfigContext.getInstance().registryProtocol(registryProtocol);
        }
        if(StringUtils.hasText(registryAddress)){
            DubboConfigContext.getInstance().registryAddress(registryAddress);
        }
        if(StringUtils.hasText(registryUsername)){
            DubboConfigContext.getInstance().registryUsername(registryUsername);
        }
        if(StringUtils.hasText(registryPassword)){
            DubboConfigContext.getInstance().registryPassword(registryPassword);
        }
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++){
            Node subNode = nodeList.item(i);
            if(ELEMENT_DUBBO_PUBLISH.equals(subNode.getLocalName())){
                builder.publisher(createDubboPublish(subNode).build());
            }else if(ELEMENT_DUBBO_SUBSCRIBE.equals(subNode.getLocalName())){
                builder.subscriber(createDubboSubscriberConfig(subNode).build());
            }
        }
        return builder;
    }

    DubboPublisherConfigBuilder createDubboPublish(Node node){
        DubboPublisherConfigBuilder builder = new DubboPublisherConfigBuilder();
        String localEventNames = getAttribute(node, "localEventNames");
        String group = getAttribute(node, "group");
        String asyncFireRemote = getAttribute(node, "asyncFireRemote");
        String copySendUnderSameVersion = getAttribute(node, "copySendUnderSameVersion");
        String devMode = getAttribute(node, "devMode");
        String expiryOffline = getAttribute(node, "expiryOffline");
        String subscriberAutowired = getAttribute(node, "subscriberAutowired");
        if(StringUtils.hasText(localEventNames)){
            builder.addLocalPublisherGroup(localEventNames);
        }
        if(StringUtils.hasText(group)){
            builder.groupName(group);
        }
        if(StringUtils.hasText(asyncFireRemote)){
            builder.asyncFireRemote(Boolean.parseBoolean(asyncFireRemote));
        }
        if(StringUtils.hasText(copySendUnderSameVersion)){
            builder.copySendUnderSameVersion(Boolean.parseBoolean(copySendUnderSameVersion));
        }
        if(StringUtils.hasText(devMode)){
            builder.devMode(Boolean.parseBoolean(devMode));
        }
        if(StringUtils.hasText(expiryOffline)){
            builder.expiryOffline(Long.parseLong(expiryOffline));
        }
        if(StringUtils.hasText(subscriberAutowired)){
            builder.subscriberAutowired(Boolean.parseBoolean(subscriberAutowired));
        }
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++) {
            Node subNode = nodeList.item(i);
            if(ELEMENT_DUBBO_PUBLISH_GROUP.equals(subNode.getLocalName())){
                builder.addPublisherGroup(createDubboPublisherGroup(subNode));
            }
        }
        return builder;
    }

    DubboPublisherGroupBuilder createDubboPublisherGroup(Node node){
        DubboPublisherGroupBuilder builder = new DubboPublisherGroupBuilder();
        String group = getAttribute(node, "group");
        String remoteEvents = getAttribute(node, "remoteEvents");
        if(StringUtils.hasText(group)){
            builder.groupName(group);
        }
        if(StringUtils.hasText(remoteEvents)){
            builder.remoteEvents(remoteEvents);
        }
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++){
            Node subNode = nodeList.item(i);
            if(ELEMENT_EVENT_TRANSMISSION.equals(subNode.getLocalName())){
                builder.eventTransmission(buildEventTransmissionReferenceConfig(subNode));
            }
        }
        return builder;
    }

    DubboSubscriberConfigBuilder createDubboSubscriberConfig(Node node){
        DubboSubscriberConfigBuilder builder = new DubboSubscriberConfigBuilder();
        String subscribeEvents = getAttribute(node, "eventNames");
        String dubboVersion = getAttribute(node, "version");
        if(StringUtils.hasText(subscribeEvents)){
            builder.addSubscriber(subscribeEvents);
        }
        if(StringUtils.hasText(dubboVersion)){
            EventSubscriberServiceConfig serviceConfig = new EventSubscriberServiceConfig();
            serviceConfig.setVersion(dubboVersion);
            return builder.eventSubscriberServiceConfig(serviceConfig);
        }
        if(node.getChildNodes() == null || node.getChildNodes().getLength() == 0){
            throw new IllegalArgumentException("please set dubboSubscriberServiceConfig node in dubboSubscribe");
        }

        return builder.eventSubscriberServiceConfig(buildEventSubscriberServiceConfig(node.getFirstChild()));
    }

    EventSubscriberServiceConfig buildEventSubscriberServiceConfig(Node node){
        EventSubscriberServiceConfig config = new EventSubscriberServiceConfig();
        copyAbstractServiceConfig(config, new NodeAttribute(node));
        String checkHealthTimeout = getAttribute(node, "checkHealthTimeout");
        if(StringUtils.hasText(checkHealthTimeout)){
            config.setCheckHealthTimeout(Integer.parseInt(checkHealthTimeout));
        }
        return config;
    }

    void copyAbstractInterfaceConfig(AbstractInterfaceConfig config, NodeAttribute a){
        config.setActives(a.getAttributeInteger("actives"));
        config.setCache(a.getAttribute("cache"));
        config.setCallbacks(a.getAttributeInteger("callbacks"));
        config.setCluster(a.getAttribute("cluster"));
        config.setConnections(a.getAttributeInteger("connections"));
        config.setFilter(a.getAttribute("filter"));
        config.setId(a.getAttribute("id"));
        config.setLayer(a.getAttribute("layer"));
        config.setListener(a.getAttribute("listener"));
        config.setLoadbalance(a.getAttribute("localbalance"));
        config.setMerger(a.getAttribute("merger"));
        config.setMock(a.getAttribuetBoolean("mock"));
        config.setMonitor(a.getAttribute("monitor"));
        config.setOnconnect(a.getAttribute("onconnect"));
        config.setOndisconnect(a.getAttribute("ondisconnect"));
        config.setOwner(a.getAttribute("owner"));
        config.setProxy(a.getAttribute("proxy"));
        config.setRetries(a.getAttributeInteger("retries"));
        config.setScope(a.getAttribute("scope"));
        config.setSent(a.getAttribuetBoolean("sent"));
        config.setStub(a.getAttribuetBoolean("stub"));
        config.setTimeout(a.getAttributeInteger("timeout"));
        config.setValidation(a.getAttribute("validation"));
    }

    void copyAbstractServiceConfig(AbstractServiceConfig config, NodeAttribute a){
        copyAbstractInterfaceConfig(config, a);
        config.setVersion(a.getAttribute("version"));
        config.setGroup(a.getAttribute("group"));
        config.setDeprecated(a.getAttribuetBoolean("deprecated"));
        config.setDelay(a.getAttributeInteger("deplay"));
        config.setExport(a.getAttribuetBoolean("export"));
        config.setWeight(a.getAttributeInteger("weight"));
        config.setDocument(a.getAttribute("document"));
        config.setDynamic(a.getAttribuetBoolean("dynamic"));
        config.setToken(a.getAttribuetBoolean("token"));
        config.setAccesslog(a.getAttribuetBoolean("accessLog"));
        config.setExecutes(a.getAttributeInteger("executes"));
    }

    void copyAbstractReferenceConfig(AbstractReferenceConfig config, NodeAttribute a){
        copyAbstractInterfaceConfig(config, a);
        config.setAsync(a.getAttribuetBoolean("async"));
        config.setCheck(a.getAttribuetBoolean("check"));
        config.setGeneric(a.getAttribuetBoolean("generic"));
        config.setInit(a.getAttribuetBoolean("init"));
        config.setReconnect(a.getAttribute("reconnect"));
        config.setSticky(a.getAttribuetBoolean("stricky"));
        config.setVersion(a.getAttribute("version"));
    }

    EventTransmissionReferenceConfig buildEventTransmissionReferenceConfig(Node node){
        EventTransmissionReferenceConfig config = new EventTransmissionReferenceConfig();
        NodeAttribute a = new NodeAttribute(node);
        config.setClient(a.getAttribute("client"));
        config.setProtocol(a.getAttribute("protocol"));
        config.setUrl(a.getAttribute("url"));
        copyAbstractReferenceConfig(config, a);
        // 校验下eventTransmission的属性是否正确
        // eventTransmission下必须要设置versions或者url
        if((StringUtils.isEmpty(config.getVersion()) || "0.0.0".equals(config.getVersion())) && StringUtils.isEmpty(config.getUrl())){
            throw new IllegalArgumentException("please set version or url in node of eventTransmission");
        }
        return config;
    }

    EventCenterBuilder createAggregator(EventCenterBuilder builder, Node node){
        AggregatorContainerBuilder containerBuilder = new AggregatorContainerBuilder();
        String corePoolSize = getAttribute(node, "corePoolSize");
        String maximumPoolSize = getAttribute(node, "maximumPoolSize");
        containerBuilder.simpleAggregatorContainer(Integer.parseInt(corePoolSize), Integer.parseInt(maximumPoolSize));
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++) {
            Node subNode = nodeList.item(i);
            if(ELEMENT_AGGREGATOR_MULTI.equals(subNode.getLocalName())){
                containerBuilder = createAggregatorThreadPoolInfos(containerBuilder, subNode);
            }
        }
        builder.aggregatorContainer(containerBuilder.build());
        return builder;
    }

    AggregatorContainerBuilder createAggregatorThreadPoolInfos(AggregatorContainerBuilder builder, Node node){
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++) {
            Node subNode = nodeList.item(i);
            if(ELEMENT_AGGREGATOR_THREADPOOL_INFO.equals(subNode.getLocalName())){
                builder = createAggregatorThreadPoolInfo(builder, subNode);
            }
        }
        return builder;
    }

    AggregatorContainerBuilder createAggregatorThreadPoolInfo(AggregatorContainerBuilder builder, Node node){
        String eventNames = getAttribute(node, "eventNames");
        String corePoolSize = getAttribute(node, "corePoolSize");
        String maximumPoolSize = getAttribute(node, "maximumPoolSize");
        builder.threadPoolInfo(eventNames, Integer.parseInt(corePoolSize), Integer.parseInt(maximumPoolSize));
        return builder;
    }

    EventCenterBuilder createSaf(EventCenterBuilder builder, Node node){
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++) {
            Node subNode = nodeList.item(i);
            if(ELEMENT_SAF_SIMPLE.equals(subNode.getLocalName())){
                builder = buildSimpleSaf(builder, subNode);
            }else if(ELEMENT_SAF_LEVELDB.equals(subNode.getLocalName())){
                builder = buildLeveldbSaf(builder, subNode);
            }
        }
        return builder;
    }

    EventCenterBuilder buildSimpleSaf(EventCenterBuilder builder, Node node){
        SimpleSAFPolicyBuilder policyBuilder = new SimpleSAFPolicyBuilder();
        String storeOnSendFail = getAttribute(node, "storeOnSendFail");
        String checkInterval = getAttribute(node, "checkInterval");
        String queueCapacity = getAttribute(node, "queueCapacity");
        if(StringUtils.hasText(storeOnSendFail)){
            policyBuilder.storeOnSendFail(Boolean.parseBoolean(storeOnSendFail));
        }
        if(StringUtils.hasText(checkInterval)){
            policyBuilder.checkInterval(Long.parseLong(checkInterval));
        }
        if(StringUtils.hasText(queueCapacity)){
            policyBuilder.queueCapacity(Integer.parseInt(queueCapacity));
        }
        return builder.safPolicy(policyBuilder.build());
    }

    EventCenterBuilder buildLeveldbSaf(EventCenterBuilder builder, Node node){
        LeveldbSAFPolicyBuilder policyBuilder = new LeveldbSAFPolicyBuilder();
        String storeOnSendFail = getAttribute(node, "storeOnSendFail");
        String checkInterval = getAttribute(node, "checkInterval");
        String path = getAttribute(node, "path");
        String readLimitSize = getAttribute(node, "readLimitSize");
        String levelDBName = getAttribute(node, "levelDBName");
        String houseKeepingInterval = getAttribute(node, "houseKeepingInterval");
        if(StringUtils.hasText(storeOnSendFail)){
            policyBuilder.storeOnSendFail(Boolean.parseBoolean(storeOnSendFail));
        }
        if(StringUtils.hasText(checkInterval)){
            policyBuilder.checkInterval(Long.parseLong(checkInterval));
        }
        if(StringUtils.hasText(path)){
            policyBuilder.path(path);
        }
        if(StringUtils.hasText(readLimitSize)){
            policyBuilder.readLimitSize(Integer.parseInt(readLimitSize));
        }
        if(StringUtils.hasText(levelDBName)){
            policyBuilder.levelDBName(levelDBName);
        }
        if(StringUtils.hasText(houseKeepingInterval)){
            policyBuilder.houseKeepingInterval(Long.parseLong(houseKeepingInterval));
        }
        return builder.safPolicy(policyBuilder.build());
    }

    EventCenterBuilder createQueueContainer(EventCenterBuilder builder, Node node){
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++) {
            Node subNode = nodeList.item(i);
            if(ELEMENT_SIMPLE_QUEUE_CONTAINER.equals(subNode.getLocalName())){
                builder = createSimpleQueueContainer(builder, subNode);
            }else if(ELEMENT_LEVELDB_QUEUE_CONTAINER.equals(subNode.getLocalName())){
                builder = createLeveldbQueueContainer(builder, subNode);
            }
        }
        return builder;
    }

    EventCenterBuilder createSimpleQueueContainer(EventCenterBuilder builder, Node node){
        String corePoolSize = getAttribute(node, "corePoolSize");
        String queueCapacity = getAttribute(node, "queueCapacity");
        String maximumPoolSize = getAttribute(node, "maximumPoolSize");
        if(StringUtils.hasText(corePoolSize) && StringUtils.hasText(queueCapacity) && StringUtils.hasText(maximumPoolSize)){
            builder.simpleQueueContainer(Integer.parseInt(corePoolSize), Integer.parseInt(queueCapacity), Integer.parseInt(maximumPoolSize));
        }else if(StringUtils.hasText(corePoolSize) && StringUtils.hasText(queueCapacity)){
            builder.simpleQueueContainer(Integer.parseInt(corePoolSize), Integer.parseInt(queueCapacity));
        }else if(StringUtils.hasText(corePoolSize)){
            builder.simpleQueueContainer(Integer.parseInt(corePoolSize));
        }
        return builder;
    }

    EventCenterBuilder createLeveldbQueueContainer(EventCenterBuilder builder, Node node){
        String corePoolSize = getAttribute(node, "corePoolSize");
        String maximumPoolSize = getAttribute(node, "maximumPoolSize");
        String checkInterval = getAttribute(node, "checkInterval");
        String path = getAttribute(node, "path");
        String levelDBName = getAttribute(node, "levelDBName");
        String readLimitSize = getAttribute(node, "readLimitSize");
        String openTxn = getAttribute(node, "openTxn");
        String keepAliveTime = getAttribute(node, "keepAliveTime");
        String blockingQueueFactor = getAttribute(node, "blockingQueueFactor");
        String loopQueueInterval = getAttribute(node, "loopQueueInterval");
        String openLevelDbLog = getAttribute(node, "openLevelDbLog");

        LevelDBContainerBuilder containerBuilder = new LevelDBContainerBuilder();
        if(StringUtils.hasText(corePoolSize)){
            containerBuilder.corePoolSize(Integer.parseInt(corePoolSize));
        }
        if(StringUtils.hasText(maximumPoolSize)){
            containerBuilder.maximumPoolSize(Integer.parseInt(maximumPoolSize));
        }
        if(StringUtils.hasText(path)){
            containerBuilder.path(path);
        }
        if (StringUtils.hasText(checkInterval)){
            containerBuilder.checkInterval(Long.parseLong(checkInterval));
        }
        if(StringUtils.hasText(levelDBName)){
            containerBuilder.levelDBName(levelDBName);
        }
        if(StringUtils.hasText(readLimitSize)){
            containerBuilder.readLimitSize(Integer.parseInt(readLimitSize));
        }
        if(StringUtils.hasText(openTxn)){
            containerBuilder.openTxn(Boolean.parseBoolean(openTxn));
        }
        if(StringUtils.hasText(keepAliveTime)){
            containerBuilder.keepAliveTime(Integer.parseInt(keepAliveTime));
        }
        if(StringUtils.hasText(blockingQueueFactor)){
            containerBuilder.blockingQueueFactor(Integer.parseInt(blockingQueueFactor));
        }
        if(StringUtils.hasText(loopQueueInterval)){
            containerBuilder.loopQueueInterval(Long.parseLong(loopQueueInterval));
        }
        if(StringUtils.hasText(openLevelDbLog)){
            containerBuilder.openLevelDbLog(Boolean.parseBoolean(openLevelDbLog));
        }
        return builder.queueContainerFactory(containerBuilder.build());
    }

    MonitorConfig createLogMonitor(EventCenterBuilder builder, Node node){
        LogMonitorConfigBuilder configBuilder = new LogMonitorConfigBuilder();
        String nodeName = getAttribute(node, "nodeName");
        String saveEventData = getAttribute(node, "saveEventData");
        String heartbeatInterval = getAttribute(node, "heartbeatInterval");
        if(StringUtils.hasText(nodeName)){
            configBuilder.nodeName(nodeName);
        }
        if(StringUtils.hasText(saveEventData)){
            configBuilder.saveEventData(Boolean.parseBoolean(saveEventData));
        }
        if(StringUtils.hasText(heartbeatInterval)){
            configBuilder.heartbeatInterval(Long.parseLong(heartbeatInterval));
        }
        return configBuilder.build();
    }

    MonitorConfig createMysqlMonitor(EventCenterBuilder builder, Node node){
        MysqlMonitorConfigBuilder configBuilder = new MysqlMonitorConfigBuilder();
        String nodeName = getAttribute(node, "nodeName");
        String saveEventData = getAttribute(node, "saveEventData");
        String heartbeatInterval = getAttribute(node, "heartbeatInterval");
        String dataSourceBeanId = getAttribute(node, "dataSourceBeanId");
        if(StringUtils.hasText(nodeName)){
            configBuilder.nodeName(nodeName);
        }
        if(StringUtils.hasText(saveEventData)){
            configBuilder.saveEventData(Boolean.parseBoolean(saveEventData));
        }
        if(StringUtils.hasText(heartbeatInterval)){
            configBuilder.heartbeatInterval(Long.parseLong(heartbeatInterval));
        }
        if(StringUtils.hasText(dataSourceBeanId)){
            configBuilder.dataSourceBeanId(dataSourceBeanId);
        }
        return configBuilder.build();
    }

    MonitorConfig createMixingMonitor(EventCenterBuilder builder, Node node){
        MixingMonitorConfigBuilder configBuilder = new MixingMonitorConfigBuilder();
        String nodeName = getAttribute(node, "nodeName");
        String saveEventData = getAttribute(node, "saveEventData");
        String heartbeatInterval = getAttribute(node, "heartbeatInterval");
        if(StringUtils.hasText(nodeName)){
            configBuilder.nodeName(nodeName);
        }
        if(StringUtils.hasText(saveEventData)){
            configBuilder.saveEventData(Boolean.parseBoolean(saveEventData));
        }
        if(StringUtils.hasText(heartbeatInterval)){
            configBuilder.heartbeatInterval(Long.parseLong(heartbeatInterval));
        }
        NodeList nodeList = node.getChildNodes();
        for(int i = 0;i < nodeList.getLength();i++) {
            Node subNode = nodeList.item(i);
            if(ELEMENT_LOG_MONITOR.equals(subNode.getLocalName())){
                configBuilder.addMonitorConfig(createLogMonitor(builder, subNode));
            }else if(ELEMENT_MYSQL_MONITOR.equals(subNode.getLocalName())){
                configBuilder.addMonitorConfig(createMysqlMonitor(builder, subNode));
            }
        }
        return configBuilder.build();
    }

    String getAttribute(Node node, String key){
        Node namedItem = node.getAttributes().getNamedItem(key);
        if(null == namedItem) {
            return null;
        }
        return namedItem.getNodeValue();
    }
}
