package eventcenter.builder.dubbo;

import eventcenter.api.appcache.AppDataContext;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.InitBuilder;

import java.io.File;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class AutowiredSubscriberWithoutSpringMain1 {

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
        EventSubscriberServiceConfig serviceConfig = new EventSubscriberServiceConfig();
        serviceConfig.setVersion("ec-builder-test-1.0");
        DubboConfigContext.getInstance()
                .applicationName("ec-sample-publisher")
                .registryProtocol("zookeeper")
                .registryAddress("localhost:2181")
                .protocolHost("127.0.0.1")
                .protocolName("dubbo")
                .protocolPort(7078)
                .groupName("test");
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                .addEventListeners(InitBuilder.buildEventListeners())
                .subscriber(new DubboSubscriberConfigBuilder()
                        .eventSubscriberServiceConfig(serviceConfig)
                        .addSubscriber("example.manual").build()).build();
        eventCenter.startup();
        System.out.println("等待监听");
        Thread.sleep(2000000);
        eventCenter.shutdown();
    }
}
