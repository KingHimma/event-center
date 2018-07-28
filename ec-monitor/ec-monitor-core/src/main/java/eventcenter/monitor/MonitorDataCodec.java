package eventcenter.monitor;

import java.util.Collection;

/**
 * 用于解析监控的事件的args和result的类型，将他转换成字符串存储起来
 *
 * @author liumingjian
 * @date 16/3/30
 */
public interface MonitorDataCodec {

    /**
     * 将data转换为key-value的形式对象
     * @param data
     * @return
     */
    Object codec(String eventName, Object data);

    /**
     * 将数组类型的集合进行转换
     * @param collection
     * @return
     */
    Collection<Object> codecCollection(String eventName, Collection<?> collection);
}
