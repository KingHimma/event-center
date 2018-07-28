package eventcenter.builder.local;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.ExampleService;
import eventcenter.builder.InitBuilder;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class Main1 {

    public static void main(String[] args) throws Exception {
        DefaultEventCenter eventCenter = new EventCenterBuilder().addEventListeners(InitBuilder.buildEventListeners()).build();
        eventCenter.startup();
        ExampleService es = new ExampleService();
        es.setEventCenter(eventCenter);
        es.manualFireEvent("1", 1);
        Thread.sleep(1000);
        eventCenter.shutdown();
    }
}
