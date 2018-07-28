package eventcenter.builder.local;

import eventcenter.api.*;
import eventcenter.api.support.DefaultEventCenter;
import eventcenter.builder.EventCenterBuilder;
import eventcenter.builder.ExampleService;
import eventcenter.builder.InitBuilder;

/**
 * Created by liumingjian on 2017/9/5.
 */
public class FilterMain {

    public static void main(String[] args) throws Exception {
        DefaultEventCenter eventCenter = new EventCenterBuilder()
                .addEventListeners(InitBuilder.buildEventListeners())
                .addGlobleFilter(new ListenerFilter() {
                    @Override
                    public boolean before(EventListener listener, EventSourceBase evt) {
                        System.out.println("我是全局过滤前置器:" + evt.getSource()==null?"":evt.getSource().getClass());
                        return true;
                    }

                    @Override
                    public void after(ListenerReceipt receipt) {
                        System.out.println("我是全局过滤后置器,took:" + receipt.getTook() + " ms");
                    }
                })
                .addListenerFilter("example.manual", new ListenerFilter() {
                    @Override
                    public boolean before(EventListener listener, EventSourceBase evt) {
                        System.out.println("我是单个过滤前置器:" + evt.getSource()==null?"":evt.getSource().getClass());
                        return true;
                    }

                    @Override
                    public void after(ListenerReceipt receipt) {
                        System.out.println("我是单个过滤后置器,took:" + receipt.getTook() + " ms");
                    }
                })
                .addEventFireFilter(new EventFireFilter() {
                    @Override
                    public void onFired(Object target, EventInfo eventInfo, Object result) {
                        System.out.println(target.getClass() + "准备触发事件:" + eventInfo.getName());
                    }
                }).build();
        eventCenter.startup();
        ExampleService es = new ExampleService();
        es.setEventCenter(eventCenter);
        es.manualFireEvent("1", 1);
        Thread.sleep(1000);
        eventCenter.shutdown();
    }
}
