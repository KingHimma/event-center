package eventcenter.builder.springschema.monitor;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class LogMonitorSubscriberMain {

    public static void main(String[] args) throws IOException {
        org.apache.log4j.BasicConfigurator.configure();
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/monitor/spring-ec-log-subscriber.xml");
        System.out.println("启动成功，正在监听数据");
    }
}
