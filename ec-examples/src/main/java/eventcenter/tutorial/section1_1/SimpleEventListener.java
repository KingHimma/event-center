package eventcenter.tutorial.section1_1;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.annotation.ListenerBind;

/**
 * 这里需要添加一个注解，用于描述这个监听器所监听的事件，可以支持多个事件同时监听
 */
@ListenerBind("example.saidHello")
public class SimpleEventListener implements EventListener {

    @Override
    public void onObserved(CommonEventSource source) {
        // source中传递了事件触发时所带的参数，这里可以原封不动的按照顺序取出
        System.out.println("Hello " + source.getArg(0, String.class));
    }
}
