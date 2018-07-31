package eventcenter.tutorial.section1_4;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.leveldb.LevelDBContainerFactory;

/**
 * 增加了leveldb的事件中心运行容器的配置
 * @author liumingjian
 * @date 2018/7/29
 **/
public class BuilderMain {

    public static void main(String[] args) throws Exception {
        // 配置LevelDB的运行容器
        LevelDBContainerFactory levelDBContainerFactory = new LevelDBContainerFactory();
        // 初始化运行容器中的线程池的最小活跃线程数
        levelDBContainerFactory.setCorePoolSize(1);
        // 初始化运行容器中的线程池的最大活跃线程数
        levelDBContainerFactory.setMaximumPoolSize(Runtime.getRuntime().availableProcessors());
        // 加强事件运行时的数据一致性，防止系统异常导致事件丢失的问题，默认为false
        levelDBContainerFactory.setOpenTxn(true);
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                // 这里需要将监听器注入进来，后续章节会介绍如何自动注入监听器
                .addEventListener(new SimpleEventListener())
                // 将leveldb的容器注入进来
                .queueContainerFactory(levelDBContainerFactory)
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
