package eventcenter.builder.dubbo;

import eventcenter.api.appcache.AppDataContext;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.InitBuilder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class SubscriberMain1 {

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/dubbo/spring-ec-subscriber.xml");
        EventSubscriberServiceConfig serviceConfig = new EventSubscriberServiceConfig();
        serviceConfig.setVersion("ec-builder-test-1.0");
        DubboConfigContext.getInstance().load(ctx).groupName("test");
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                .addEventListeners(InitBuilder.buildEventListeners())
                .subscriber(new DubboSubscriberConfigBuilder()
                        .eventSubscriberServiceConfig(serviceConfig).build()).build();
        eventCenter.startup();
        System.out.println("等待监听");
        Thread.sleep(2000000);
        eventCenter.shutdown();
    }
}
