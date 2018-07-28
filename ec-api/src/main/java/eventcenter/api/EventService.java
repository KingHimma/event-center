package eventcenter.api;

/**
 * 有关于事件的组件服务，如果事件中心是通过第三方的消息队列服务或者消息相关的中间件实现，那么这些组件应该要托管在事件中心里，有便于管理组件服务的生命周期
 *
 * @author liumingjian
 * @date 16/7/28
 */
public interface EventService {

    /**
     * 启动服务组件
     */
    void startup(EventCenterConfig config) throws Exception;

    /**
     * 关闭服务组件
     */
    void shutdown() throws Exception;
}
