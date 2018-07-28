package eventcenter.api.aggregator;

import eventcenter.api.EventInfo;
import eventcenter.api.EventInfo;

import java.util.List;

/**
 * 将事件源的参数或者结果，拆分成多个数组，用于拆分一个大的数组到多个数量较小的数组中，然后放入到聚合并发运行的容器中并发执行。
 * @author JackyLIU
 *
 */
public interface EventSpliter {

	/**
	 * 将EventSourceBase拆分成多个EventSourceBase，其中EventSourceBase中的事件名称和ID必须要相同
	 * @param target
	 * @param eventInfo
	 * @return
	 */
	List<EventInfo> split(Object target, EventInfo eventInfo);
}
