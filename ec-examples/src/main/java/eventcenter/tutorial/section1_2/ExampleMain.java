package eventcenter.tutorial.section1_2;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 这个示例程序演示了{@link Boss}和{@link Manager}如何通过事件进行交互，并使用Spring的配置初始化事件中心组件
 * @author liumingjian
 * @date 2018/7/29
 **/
public class ExampleMain {

    public static void main(String[] args) throws InterruptedException {
        // 初始化spring
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/tutorial/section1_2/spring-ec.xml");
        // 将老板请出来
        Boss boss = ctx.getBean(Boss.class);
        // 老板开始要准备实现一个小目标
        boss.submitTask("年底完成2亿销售额");
        // 开始悄悄等待管理层的响应和安排了
        Thread.sleep(500);

        ctx.close();
    }
}
