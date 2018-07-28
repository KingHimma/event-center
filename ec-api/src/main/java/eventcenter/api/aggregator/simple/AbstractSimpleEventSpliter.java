package eventcenter.api.aggregator.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eventcenter.api.EventInfo;
import eventcenter.api.aggregator.EventSpliter;
import eventcenter.api.EventInfo;

/**
 * 简化split方法实现
 * @author JackyLIU
 *
 */
public abstract class AbstractSimpleEventSpliter implements EventSpliter {

	@Override
	public List<EventInfo> split(Object target, EventInfo eventInfo) {
		if(eventInfo.getArgs() == null || eventInfo.getArgs().length == 0)
			return Arrays.asList(eventInfo);
		
		List<Object> splitArgs = splitArgs(eventInfo.getArgs());
		List<EventInfo> list = new ArrayList<EventInfo>();
		for(Object splitArg : splitArgs){
			EventInfo subEI = new EventInfo(eventInfo.getName()).setArgs(new Object[]{splitArg}).setId(eventInfo.getId());
			list.add(subEI);
		}
		return list;
	}

	/**
	 * 将eventInfo中的参数分割成多个参数列表
	 * @param args
	 * @return
	 */
	abstract protected List<Object> splitArgs(Object[] args);
}
