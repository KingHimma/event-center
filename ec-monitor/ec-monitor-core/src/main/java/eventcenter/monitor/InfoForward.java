package eventcenter.monitor;

import java.util.List;

/**
 * 节点信息推送接口，节点信息包含两部分，第一部分是节点信息的变更，第二部分是事件消费的回执信息推送
 *
 * @author liumingjian
 * @date 16/2/15
 */
public interface InfoForward {

    /**
     * 推送事件中心节点信息
     * @param info
     */
    void forwardNodeInfo(NodeInfo info);

    /**
     * 推送已消费的事件信息
     * @param infos
     */
    void forwardEventInfo(List<MonitorEventInfo> infos);
}
