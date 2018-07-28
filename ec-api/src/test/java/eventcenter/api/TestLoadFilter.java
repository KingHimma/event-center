package eventcenter.api;

import eventcenter.api.annotation.EventFilterable;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liumingjian on 2016/11/17.
 */
public class TestLoadFilter {

    @Test
    public void test1() throws Exception {
        EventCenterConfig config = new EventCenterConfig();
        Map<String, EventFilter> filterMap = new HashMap<String, EventFilter>();
        filterMap.put("testFilter", new TestFilter());
        config.loadFilter(filterMap);
        Assert.assertEquals(1, config.getModuleFilters().size());
        Assert.assertEquals(config.getModuleFilters().get(0), filterMap.get("testFilter"));
    }

    class TestFilter implements EventFilter {

    }

    @EventFilterable(value="test")
    class AddFilterableFilter implements ListenerFilter {

        @Override
        public boolean before(EventListener listener, EventSourceBase evt) {
            return true;
        }

        @Override
        public void after(ListenerReceipt receipt) {

        }
    }
}
