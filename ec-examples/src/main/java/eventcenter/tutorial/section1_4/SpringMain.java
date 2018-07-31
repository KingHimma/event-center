package eventcenter.tutorial.section1_4;

import eventcenter.api.EventCenter;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author liumingjian
 * @date 2018/7/30
 **/
public class SpringMain {

    public static void main(String[] args) throws InterruptedException {
        // 初始化spring
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/tutorial/section1_4/spring-ec.xml");
        EventCenter eventCenter = ctx.getBean(EventCenter.class);
        // 接着开始触发事件，打一个招呼
        eventCenter.fireEvent("Main", "example.saidHello", "World");
        // 跟着观察下控制台的输出，这里暂停500ms
        Thread.sleep(500);
        ctx.close();
    }
}
