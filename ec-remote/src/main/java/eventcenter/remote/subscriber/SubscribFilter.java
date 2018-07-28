package eventcenter.remote.subscriber;

import eventcenter.api.EventInfo;
import eventcenter.api.EventFilter;
import eventcenter.remote.Target;

/**
 * filter of subscriber when received event from publisher, it would filter event
 *
 * @author liumingjian
 * @date 2016/11/18
 */
public interface SubscribFilter extends EventFilter {

    /**
     * it will record when it received event
     * @param target
     * @param eventInfo
     * @param result
     * @return if return false, it wouldn't put event to queue
     */
    boolean afterReceived(Target target, EventInfo eventInfo, Object result);
}
