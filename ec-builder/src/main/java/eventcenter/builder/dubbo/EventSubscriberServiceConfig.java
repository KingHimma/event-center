package eventcenter.builder.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.AbstractServiceConfig;
import com.alibaba.dubbo.config.MethodConfig;
import com.alibaba.dubbo.config.ProviderConfig;
import com.alibaba.dubbo.config.ServiceConfig;
import eventcenter.remote.EventSubscriber;
import eventcenter.remote.EventTransmission;
import eventcenter.remote.utils.StringHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class EventSubscriberServiceConfig extends AbstractServiceConfig {
    private static final long serialVersionUID = 1435544645200451405L;

    private Class<?>            interfaceClass;

    // 服务名称
    private String              path;

    private ProviderConfig provider;

    private final List<URL> urls = new ArrayList<URL>();

    protected Integer checkHealthTimeout;

    private ServiceConfig<EventSubscriber> eventSubscriberConfig;

    private ServiceConfig<EventTransmission> eventTransmissionConfig;

    public EventSubscriberServiceConfig() {

    }

    void load(eventcenter.remote.subscriber.EventSubscriber eventSubscriber, boolean loadSubscriber){
        eventTransmissionConfig = new ServiceConfig<EventTransmission>();
        copyServiceConfig(eventTransmissionConfig);
        loadEventTransmission();
        eventTransmissionConfig.setInterface(EventTransmission.class);
        eventTransmissionConfig.setRef(eventSubscriber);
        eventTransmissionConfig.export();

        if(loadSubscriber) {
            eventSubscriberConfig = new ServiceConfig<EventSubscriber>();
            copyServiceConfig(eventSubscriberConfig);
            eventSubscriberConfig.setInterface(EventSubscriber.class);
            eventSubscriberConfig.setRef(eventSubscriber);
            eventSubscriberConfig.export();
        }
    }

    void loadEventTransmission(){
        List<MethodConfig> methodConfigs = new ArrayList<MethodConfig>(2);
        MethodConfig m1 = new MethodConfig();
        m1.setName("asyncTransmission");
        m1.setAsync(true);
        m1.setReturn(false);
        methodConfigs.add(m1);
        MethodConfig m2 = new MethodConfig();
        m2.setName("checkHealth");
        m2.setTimeout(checkHealthTimeout == null ? 1000 : checkHealthTimeout);
        methodConfigs.add(m2);
        eventTransmissionConfig.setMethods(methodConfigs);
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public ProviderConfig getProvider() {
        return provider;
    }

    public void setProvider(ProviderConfig provider) {
        this.provider = provider;
    }

    public List<URL> getUrls() {
        return urls;
    }

    public Integer getCheckHealthTimeout() {
        return checkHealthTimeout;
    }

    public void setCheckHealthTimeout(Integer checkHealthTimeout) {
        this.checkHealthTimeout = checkHealthTimeout;
    }

    private void copyServiceConfig(ServiceConfig<?> dest){
        if(null != interfaceClass) {
            dest.setInterface(interfaceClass);
        }
        if(StringHelper.isNotEmpty(path)) {
            dest.setPath(path);
        }
        if(null != provider) {
            dest.setProvider(provider);
        }
        if(null != accesslog) {
            dest.setAccesslog(accesslog);
        }
        if(null != actives) {
            dest.setActives(actives);
        }
        if(null != application) {
            dest.setApplication(application);
        }
        if(null != async) {
            dest.setAsync(async);
        }
        if(StringHelper.isNotEmpty(cache)) {
            dest.setCache(cache);
        }
        if(null != getCallbacks()) {
            dest.setCallbacks(getCallbacks());
        }
        if(StringHelper.isNotEmpty(cluster)) {
            dest.setCluster(cluster);
        }
        if(null != connections) {
            dest.setConnections(connections);
        }
        if(null != delay) {
            dest.setDelay(delay);
        }
        if(null != deprecated) {
            dest.setDeprecated(deprecated);
        }
        if(StringHelper.isNotEmpty(document)) {
            dest.setDocument(document);
        }
        if(null != dynamic) {
            dest.setDynamic(dynamic);
        }
        if(null != getExecutes()) {
            dest.setExecutes(getExecutes());
        }
        if(StringHelper.isNotEmpty(filter)) {
            dest.setFilter(filter);
        }
        if(StringHelper.isNotEmpty(group)) {
            dest.setGroup(group);
        }
        if(StringHelper.isNotEmpty(id)) {
            dest.setId(id);
        }
        if(StringHelper.isNotEmpty(layer)) {
            dest.setLayer(layer);
        }
        if(StringHelper.isNotEmpty(listener)) {
            dest.setListener(listener);
        }
        if(StringHelper.isNotEmpty(loadbalance)) {
            dest.setLoadbalance(loadbalance);
        }
        if(StringHelper.isNotEmpty(merger)) {
            dest.setMerger(merger);
        }
        if(StringHelper.isNotEmpty(mock)) {
            dest.setMock(mock);
        }
        if(null != module) {
            dest.setModule(module);
        }
        if(null != monitor) {
            dest.setMonitor(monitor);
        }
        if(StringHelper.isNotEmpty(onconnect)) {
            dest.setOnconnect(onconnect);
        }
        if(StringHelper.isNotEmpty(ondisconnect)) {
            dest.setOndisconnect(ondisconnect);
        }
        if(StringHelper.isNotEmpty(owner)) {
            dest.setOwner(owner);
        }
        if(null != parameters) {
            dest.setParameters(parameters);
        }
        if(StringHelper.isNotEmpty(proxy)) {
            dest.setProxy(proxy);
        }
        if(null != isRegister()) {
            dest.setRegister(isRegister());
        }
        if(null != registries && registries.size() > 0){
            dest.setRegistries(registries);
        }
        if(null != retries) {
            dest.setRetries(retries);
        }
        if(StringHelper.isNotEmpty(getScope())) {
            dest.setScope(getScope());
        }
        if(null != sent) {
            dest.setSent(sent);
        }
        if(StringHelper.isNotEmpty(stub)) {
            dest.setStub(stub);
        }
        if(null != timeout) {
            dest.setTimeout(timeout);
        }
        if(StringHelper.isNotEmpty(token)) {
            dest.setToken(token);
        }
        if(StringHelper.isNotEmpty(validation)) {
            dest.setValidation(validation);
        }
        if(StringHelper.isNotEmpty(version)) {
            dest.setVersion(version);
        }
        if(null != weight) {
            dest.setWeight(weight);
        }
        if(null != protocols){
            dest.setProtocols(protocols);
        }
    }
}
