package eventcenter.tutorial.section1_5.manager.listeners;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.annotation.ListenerBind;
import eventcenter.tutorial.section1_5.manager.Manager;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 这个是管理员监听器，目前监听了老板的任务，注意：这个类需要加上Spring Scan的注解，例如{@link Component}注解是必要的
 * @author liumingjian
 * @date 2018/7/29
 **/
@Component
@ListenerBind("boss.task.submit")
public class ManagerEventListener implements EventListener {

    @Resource
    Manager manager;

    @Override
    public void onObserved(CommonEventSource source) {
        // 这里和section1_2不同的是，boss的name是通过result传递过来
        manager.manageTask(source.getResult(String.class), source.getArg(0, String.class));
    }
}
