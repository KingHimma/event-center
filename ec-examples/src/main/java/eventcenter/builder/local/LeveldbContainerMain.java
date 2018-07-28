package eventcenter.builder.local;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.ExampleService;
import eventcenter.builder.InitBuilder;
import eventcenter.builder.LevelDBContainerBuilder;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class LeveldbContainerMain {

    public static void main(String[] args) throws Exception {
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                .addEventListeners(InitBuilder.buildEventListeners())
                .queueContainerFactory(
                        new LevelDBContainerBuilder()
                                .maximumPoolSize(10)
                                .corePoolSize(1)
                                .openTxn(true).build()
                ).build();
        eventCenter.startup();
        ExampleService es = new ExampleService();
        es.setEventCenter(eventCenter);
        es.manualFireEvent("1", 1);
        Thread.sleep(1000);
        eventCenter.shutdown();
    }
}
