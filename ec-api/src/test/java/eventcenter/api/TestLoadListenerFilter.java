package eventcenter.api;

import eventcenter.api.annotation.EventFilterable;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * test load listener filters in {@link EventCenterConfig}
 * Created by liumingjian on 16/1/26.
 */
public class TestLoadListenerFilter {

    @Test
    public void test1(){
        EventCenterConfig config = new EventCenterConfig();
        Map<String, ListenerFilter> filterMap = new HashMap<String, ListenerFilter>();
        filterMap.put("addFilterableFilter", new AddFilterableFilter());
        config.loadListenerFilter(filterMap);
        Assert.assertTrue(config.getListenerFilters().containsKey("test"));
    }

    @Test
    public void test2(){
        EventCenterConfig config = new EventCenterConfig();
        Map<String, ListenerFilter> filterMap = new HashMap<String, ListenerFilter>();
        filterMap.put("addMultiFilterableFilter", new AddMultiFilterableFilter());
        config.loadListenerFilter(filterMap);
        Assert.assertTrue(config.getListenerFilters().containsKey("test1"));
        Assert.assertTrue(config.getListenerFilters().containsKey("test2"));
        Assert.assertTrue(config.getListenerFilters().containsKey("test3"));
    }

    @Test
    public void test3(){
        EventCenterConfig config = new EventCenterConfig();
        Map<String, ListenerFilter> filterMap = new HashMap<String, ListenerFilter>();
        filterMap.put("addGlobalFilter", new AddGlobalFilter());
        config.loadListenerFilter(filterMap);
        Assert.assertTrue(config.getListenerFilters().size() == 0);
        Assert.assertEquals(1, config.getGlobalFilters().size());
    }

    @Test
    public void test4(){
        EventCenterConfig config = new EventCenterConfig();
        Map<String, ListenerFilter> filterMap = new HashMap<String, ListenerFilter>();
        filterMap.put("nonFilterableFilter", new NonFilterableFilter());
        config.loadListenerFilter(filterMap);
        Assert.assertTrue(config.getListenerFilters().size() == 0);
        Assert.assertEquals(0, config.getGlobalFilters().size());
    }

    public class NonFilterableFilter implements ListenerFilter {

        @Override
        public boolean before(EventListener listener, CommonEventSource evt) {
            return true;
        }

        @Override
        public void after(ListenerReceipt receipt) {

        }
    }

    @EventFilterable(value="test")
    class AddFilterableFilter implements ListenerFilter {

        @Override
        public boolean before(EventListener listener, CommonEventSource evt) {
            return true;
        }

        @Override
        public void after(ListenerReceipt receipt) {

        }
    }

    @EventFilterable(value="test1,test2,test3")
    public class AddMultiFilterableFilter implements ListenerFilter {

        @Override
        public boolean before(EventListener listener, CommonEventSource evt) {
            return true;
        }

        @Override
        public void after(ListenerReceipt receipt) {

        }
    }

    @EventFilterable(isGlobal = true)
    public class AddGlobalFilter implements ListenerFilter {

        @Override
        public boolean before(EventListener listener, CommonEventSource evt) {
            return true;
        }

        @Override
        public void after(ListenerReceipt receipt) {

        }
    }
}
