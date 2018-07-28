package eventcenter.monitor;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 监控服务端
 * Created by liumingjian on 16/2/23.
 */
public class MonitorServerMain {

    public static void main(String[] args) throws InterruptedException {
        org.apache.log4j.BasicConfigurator.configure();
        System.setProperty("dubbo.registry.address", "zookeeper://localhost:2181");
        System.setProperty("dubbo.application.name", "ec-dubbo-monitor-server");
        new ClassPathXmlApplicationContext("/spring/monitor/spring-ec-monitor-server.xml");

        System.out.println("监控服务端启动成功");
        Thread.sleep(1000000);
    }
}
