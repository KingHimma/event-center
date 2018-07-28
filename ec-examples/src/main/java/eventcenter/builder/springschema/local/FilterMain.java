package eventcenter.builder.springschema.local;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.ExampleService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class FilterMain {

    public static void main(String[] args) throws Exception {
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/local/spring-ec-filter.xml");
        DefaultEventCenter eventCenter = ctx.getBean(DefaultEventCenter.class);
        ExampleService es = ctx.getBean(ExampleService.class);
        es.annotationFireEvent("hello world", 1);
        Thread.sleep(1000);
        es.manualFireEvent("hello2", 2);
        Thread.sleep(1000);
        eventCenter.shutdown();
    }
}
