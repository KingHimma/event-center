package eventcenter.builder.dubbo;

import eventcenter.api.appcache.AppDataContext;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.ExampleService;
import eventcenter.builder.InitBuilder;

import java.io.File;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class PublisherWithoutSpringMain1 {

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        System.setProperty(AppDataContext.SYSTEM_PROPERTY_PATH, "." + File.separator + "target");
        EventTransmissionReferenceConfig referenceConfig = new EventTransmissionReferenceConfig();
        referenceConfig.setVersion("ec-builder-test-1.0");
        DubboConfigContext.getInstance().applicationName("ec-sample-publisher").registryProtocol("zookeeper").registryAddress("localhost:2181").groupName("test");
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                .addEventListeners(InitBuilder.buildEventListeners())
                .publisher(new DubboPublisherConfigBuilder()
                        .addLocalPublisherGroup("example.manual")
                        .addPublisherGroup(new DubboPublisherGroupBuilder()
                                .eventTransmission(referenceConfig)
                                .remoteEvents("example.manual"))
                        .build()).build();
        eventCenter.startup();
        ExampleService es = new ExampleService();
        es.setEventCenter(eventCenter);
        es.manualFireEvent("1", 1);
        Thread.sleep(1000);
        eventCenter.shutdown();
    }
}
