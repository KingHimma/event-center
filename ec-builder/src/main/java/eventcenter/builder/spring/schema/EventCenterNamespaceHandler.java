package eventcenter.builder.spring.schema;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Created by liumingjian on 2017/9/27.
 */
public class EventCenterNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("conf", new EventCenterBeanDefinitionParser());
    }
}
