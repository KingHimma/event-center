package eventcenter.monitor;

import java.util.List;

/**
 * 监控数据的存储器
 *
 * @author liumingjian
 * @date 16/2/15
 */
public interface InfoStorage {

    /**
     * 打开存储器
     */
    void open() throws Exception;

    /**
     * 关闭存储器
     */
    void close() throws Exception;

    /**
     * 批量保存监控信息
     * @param infos
     */
    void pushEventInfos(List<MonitorEventInfo> infos);

    /**
     * 保存单个监控信息
     * @param info
     */
    void pushEventInfo(MonitorEventInfo info);

    /**
     * 将监控信息按照时间从早到晚推出来，推出之后，磁盘将会删除这些数据
     * @param maxSize
     * @return
     */
    List<MonitorEventInfo> popEventInfos(int maxSize);

    /**
     * 推出最早保存的监控的事件数据
     * @return
     */
    MonitorEventInfo popEventInfo();

    /**
     * 保存事件中心节点信息
     * @param nodeInfo
     */
    void saveNodeInfo(NodeInfo nodeInfo);

    /**
     * 获取事件中心节点信息
     * @return
     */
    NodeInfo queryNodeInfo();
}
