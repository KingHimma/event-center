package eventcenter.tutorial.section1_5.boss;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 启动这个示例之前，需要先启动{@link eventcenter.tutorial.section1_5.manager.ManagerMain}示例程序。这里使用的通讯发现协议是multicast方式，如果出现了"Can't assign requested address"，请在JVM启动参数内添加：-Djava.net.preferIPv4Stack=true
 * @author liumingjian
 * @date 2018/7/30
 **/
public class BossMain {

    public static void main(String[] args) throws InterruptedException {
        // 初始化spring
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/tutorial/section1_5/spring-boss-ec.xml");
        // 将老板请出来
        Boss boss = ctx.getBean(Boss.class);
        // 老板开始要准备实现一个小目标
        boss.submitTask("年底完成2亿销售额");
        // 开始悄悄等待管理层的响应和安排了
        Thread.sleep(500);

        ctx.close();
    }
}
