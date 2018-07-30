package eventcenter.tutorial.section1_1;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;

/**
 * @author liumingjian
 * @date 2018/7/29
 **/
public class ExampleMain {

    public static void main(String[] args) throws Exception {
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                // 这里需要将监听器注入进来，后续章节会介绍如何自动注入监听器
                .addEventListener(new SimpleEventListener())
                .build();
        // 启动事件中心容器
        eventCenter.startup();
        // 接着开始触发事件，打一个招呼
        eventCenter.fireEvent("Main", "example.saidHello", "World");
        // 跟着观察下控制台的输出，这里暂停500ms
        Thread.sleep(500);
        // 最后关闭掉容器
        eventCenter.shutdown();
    }
}
