package eventcenter.builder.spring.schema;

import eventcenter.api.EventCenter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by liumingjian on 2017/9/27.
 */
public class SpringMain {

    public static void main(String[] args){
        org.apache.log4j.BasicConfigurator.configure();
        ApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:spring/spring-simple.xml");
        String[] beanDefinitionNames = ctx.getBeanDefinitionNames();
        EventCenter eventCenter = ctx.getBean("eventCenter", EventCenter.class);
        System.out.println("取出eventCenter成功");
    }
}
