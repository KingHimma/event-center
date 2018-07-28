package eventcenter.leveldb.tx;

import eventcenter.api.EventCenter;
import eventcenter.api.EventContainer;
import eventcenter.api.support.DefaultEventCenter;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Random;

/**
 * 压力测试
 * Created by liumingjian on 2017/2/28.
 */
public class LoadMain {

    public static void main(String[] args) {
        //org.apache.log4j.xml.DOMConfigurator.configure(LoadMain.class.getResource("/spring/leveldb/tx/log4j.xml"));
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/leveldb/tx/spring-ec.xml");

        ExampleService es = ctx.getBean(ExampleService.class);		// 获取业务方法

        DefaultEventCenter eventCenter = (DefaultEventCenter)ctx.getBean(EventCenter.class);
        EventContainer container = eventCenter.getAsyncContainer();
        final int count = 0;
        Random random = new Random();
        final int maxWait = 100;
        for(int i = 0;i < count;i++){
            int wait = random.nextInt(maxWait);
            es.annotationFireEvent("Hello", wait);// 调用业务方法，调用成功之后，将会触发事件
        }
        System.out.println("总共发送事件:" + count + ", countOfQueueBuffer:" + container.countOfQueueBuffer() + ",countOfLiveThread:" + container.countOfLiveThread() + ",queueSize:" + container.queueSize());
    }
}
