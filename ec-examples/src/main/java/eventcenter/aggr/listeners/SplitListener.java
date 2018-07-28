package eventcenter.aggr.listeners;

import eventcenter.api.CommonEventSource;
import eventcenter.api.aggregator.AggregatorEventListener;
import eventcenter.api.aggregator.AggregatorEventSource;
import eventcenter.api.annotation.ListenerBind;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 拆分之后的事件源的监听器
 * @author JackyLIU
 *
 */
@Component
@ListenerBind("test2")
public class SplitListener implements AggregatorEventListener {

	@Override
	public void onObserved(CommonEventSource source) {
		AggregatorEventSource evt = (AggregatorEventSource)source;
		List<Integer> list = evt.getArgList(0, Integer.class);
		// 给每个元素加1
		List<Integer> result = new ArrayList<Integer>(list.size());
		for(Integer i : list){
			result.add(i + 1);
		}
		
		// 然后将结果集设置到
		evt.putResult(this, result);
	}

}
