package eventcenter.builder.spring.schema;

import eventcenter.api.EventCenter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

/**
 * Created by liumingjian on 2017/9/27.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-simple.xml")
public class EventCenterBeanDefinitionParserTest {

    @Resource
    EventCenter eventCenter;

    public EventCenterBeanDefinitionParserTest(){
        org.apache.log4j.BasicConfigurator.configure();
    }

    @Test
    public void test(){

    }
}