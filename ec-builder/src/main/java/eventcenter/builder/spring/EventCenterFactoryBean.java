package eventcenter.builder.spring;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.dubbo.DubboConfigContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Created by liumingjian on 2017/9/27.
 */
public class EventCenterFactoryBean implements FactoryBean<DefaultEventCenter>, ApplicationContextAware {

    private EventCenterBuilder builder;

    private ApplicationContext applicationContext;

    private DefaultEventCenter eventCenter;

    public EventCenterFactoryBean(){

    }

    public EventCenterBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(EventCenterBuilder builder) {
        this.builder = builder;
    }

    // 初始化
    private void init(){
        if(null == applicationContext) {
            return ;
        }
        this.builder.applicationContext(applicationContext);
        DubboConfigContext.getInstance().load(applicationContext);
    }

    @Override
    public DefaultEventCenter getObject() throws Exception {
        if(null != eventCenter){
            return eventCenter;
        }
        init();
        eventCenter = this.builder.build();
        eventCenter.startup();
        return eventCenter;
    }

    @Override
    public Class<DefaultEventCenter> getObjectType() {
        return DefaultEventCenter.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void startup() throws Exception {
        if(null == eventCenter){
            return ;
        }
        eventCenter.startup();
    }

    public void shutdown() throws Exception {
        if(null == eventCenter){
            return ;
        }
        eventCenter.shutdown();

    }
}
