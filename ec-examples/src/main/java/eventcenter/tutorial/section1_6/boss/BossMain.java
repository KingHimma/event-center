package eventcenter.tutorial.section1_6.boss;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 由于使用的是multicast，所以先启动监听端也就是manager，然后再启动这个示例，看控制台的提示，然后关闭掉manager，配置中配置了10秒钟的重试，10秒之后，manager就能够接收到事件。
 * 这里使用的通讯发现协议是multicast方式，如果出现了"Can't assign requested s"，请在JVM启动参数内添加：-Djava.net.preferIPv4Stack=true
 * @author liumingjian
 * @date 2018/7/30
 **/
public class BossMain {

    public static void main(String[] args) throws InterruptedException {
        System.err.println("启动之前，请先确保manager启动着，由于使用了multicast协议，需要监听端启动着，不然会发生阻塞，生产环境使用尽可能使用zookeeper或者redis的发现机制");
        // 初始化spring
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/tutorial/section1_6/spring-boss-ec.xml");
        System.out.println("请关闭掉manager");
        Thread.sleep(10000);
        // 将老板请出来
        Boss boss = ctx.getBean(Boss.class);
        // 老板开始要准备实现一个小目标
        boss.submitTask("年底完成2亿销售额");
        System.out.println("然后再打开manager");
    }
}
