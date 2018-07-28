package eventcenter.monitor;

import eventcenter.api.ListenerReceipt;

/**
 * 客户端的监控器，用于收集各种监控数据
 *
 * @author liumingjian
 * @date 16/2/15
 */
public interface ControlMonitor {

    /**
     * 启动监控器
     */
    void startup();

    /**
     * 关闭监控器
     */
    void shutdown();

    /**
     * 保存事件消费完成之后的回执信息
     * @param mei
     */
    void saveEventInfo(MonitorEventInfo mei);

    /**
     * 这个可以直接保存{@link ListenerReceipt}，并且可以通过{@link AbstractMonitorDataCodec}的实现类，将event.args或者result转换成相应的字符串，以便减少存储
     * @param receipt
     */
    void saveListenerReceipt(ListenerReceipt receipt);

    /**
     * 获取当前事件中心的节点信息
     * @param includeDetail 是否包含详细信息，主要包括stat、queueSize、等等有关容器方面的数据
     * @return
     */
    NodeInfo queryNodeInfo(boolean includeDetail);

    /**
     * 查看缓存队列所缓存的事件数量
     * @return
     */
    int queueSize();

    /**
     * 查看事件消费容器中最高的线程并发数
     * @return
     */
    int countOfMaxConcurrent();

    /**
     * 查看事件消费容器中正在运行的线程并发数
     * @return
     */
    int countOfLiveThread();

    /**
     * 获取线程池中的阻塞队列的元素数量
     * @return
     */
    int countOfQueueBuffer();
}
