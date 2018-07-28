package eventcenter.builder.springschema.local;

import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.ExampleService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author liumingjian
 * @date 2018/5/2
 **/
public class LeveldbContainerMain {

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();
        @SuppressWarnings("resource")
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/spring/builder/schema/local/spring-ec-leveldb.xml");
        DefaultEventCenter eventCenter = ctx.getBean(DefaultEventCenter.class);
        ExampleService es = ctx.getBean(ExampleService.class);
        es.manualFireEvent("1", 1);
        Thread.sleep(1000);
        eventCenter.shutdown();
    }
}
