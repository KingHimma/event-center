package eventcenter.tutorial.section1_5.manager;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 请先启动这个示例，然后再启动{@link eventcenter.tutorial.section1_5.boss.BossMain}示例。这里使用的通讯发现协议是multicast方式，如果出现了"Can't assign requested address"，请在JVM启动参数内添加：-Djava.net.preferIPv4Stack=true
 * @author liumingjian
 * @date 2018/7/30
 **/
public class ManagerMain {

    public static void main(String[] args){
        // 初始化spring
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/tutorial/section1_5/spring-manager-ec.xml");
        System.out.println("静静的等待老板的任务");
    }
}
