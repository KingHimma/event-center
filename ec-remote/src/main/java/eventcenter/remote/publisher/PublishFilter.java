package eventcenter.remote.publisher;

import eventcenter.api.EventInfo;
import eventcenter.api.EventFilter;
import eventcenter.remote.Target;

/**
 * 发布事件过滤器，其中对发送远程事件进行过滤
 *
 * @author liumingjian
 * @date 2016/11/17
 */
public interface PublishFilter extends EventFilter {

    /**
     * execute filter afterSend event sent to remote
     * @param group
     * @param target
     * @param eventInfo
     * @param result
     * @param e if sent fails, it would record exception
     * @return if return false, it means next filters dosen't to be execute
     */
    boolean afterSend(PublisherGroup group, Target target, EventInfo eventInfo, Object result, Exception e);
}
