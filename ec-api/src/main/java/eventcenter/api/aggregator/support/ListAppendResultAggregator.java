package eventcenter.api.aggregator.support;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.aggregator.ListenerConsumedResult;
import eventcenter.api.aggregator.ListenersConsumedResult;
import eventcenter.api.aggregator.ResultAggregator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 将多个结果集添加到总的数组中并返回
 * @author JackyLIU
 *
 * @param <T>
 */
public class ListAppendResultAggregator<T> implements ResultAggregator<List<T>> {

	@Override
	public Object exceptionHandler(EventListener listener,
								   CommonEventSource source, Exception e) {
		// 子类应该实现此方法
		return new ArrayList<Object>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> aggregate(ListenersConsumedResult result) {
		List<T> list = new ArrayList<T>();
		for(ListenerConsumedResult consumedResult : result.getResults()){
			Object obj = consumedResult.getResult();
			if(!(obj instanceof Collection)){
				list.add((T)obj);
			}else{
				list.addAll((Collection<T>)obj);
			}
			
		}
		return list;
	}

}
