package eventcenter.monitor.server.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 自定义事件索引的配置类
 * Created by liumingjian on 16/2/24.
 */
public class IndexConfig implements Serializable {
    private static final long serialVersionUID = -4059404132693554502L;

    private String id;

    /**
     * 事件中心分组
     */
    private String group;

    /**
     * 事件名称，如果需要匹配一批事件，可以使用通配符 *，例如trade.*，那么trade.consign, trade.update都会被使用这个配置
     */
    private String eventName;

    /**
     * <pre>
     * 事件源的args参数的索引名称，例如事件源为new Object[]{user, message}，两个参数，那么这里传输到服务端变成JSON的字符串，如下：
     * [{
     *     'userName':'Jacky',
     *     'age':20
     * },{
     *     'id':'1',
     *     'content':'hello world'
     * }]
     * 那么设置索引的值为:[0].userName,[1].id,那么事件监控的Collection，将会创建为这两个对象创建这个索引到custom字段中，如果参数是个数组，
     * 可以使用[0][].id之类的符号描述，使用[]这个中括号
     * </pre>
     */
    private String argsIndexes;

    /**
     * <pre>
     * 结果索引配置，如果result是一个对象，可以直接使用对象内的属性名称作为建立索引的名称，例如
     * {
     *     'userName':'Jacky',
     *     'age':20,
     *     'userId':'100001'
     * }
     * 那么可以设置为userName,userId 使用,分割
     * 如果result是一个同类型的数组，如果需要将数组内的某个字段都写入到索引中，可以使用[].userId
     * </pre>
     */
    private String resultIndexes;

    private Date created;

    private Date modified;

    /**
     * 判断eventName设置是否使用了通配符
     */
    private boolean wildcard;

    /**
     * 为了方便找到使用通配符的事件名称，这里会在匹配的时候，如果匹配成功，将会插入这个完整的事件名称到这个表中，并将matched变量设置为true，并将设置有wildcard属性的配置的id和这个配置关联
     */
    private String wildcardId;

    /**
     * 如果使用的是通配符匹配的事件名称，那么会插入一条配置数据，并设置这个matched为true
     */
    private boolean matched;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * <pre>
     * 事件源的args参数的索引名称，例如事件源为new Object[]{user, message}，两个参数，那么这里传输到服务端变成JSON的字符串，如下：
     * [{
     *     'userName':'Jacky',
     *     'age':20
     * },{
     *     'id':'1',
     *     'content':'hello world'
     * }]
     * 那么设置索引的值为:[0].userName,[1].id,那么事件监控的Collection，将会创建为这两个对象创建这个索引到custom字段中，如果参数是个数组，
     * 可以使用[0][].id之类的符号描述，使用[]这个中括号
     * </pre>
     * @return
     */
    public String getArgsIndexes() {
        return argsIndexes;
    }

    /**
     * <pre>
     * 事件源的args参数的索引名称，例如事件源为new Object[]{user, message}，两个参数，那么这里传输到服务端变成JSON的字符串，如下：
     * [{
     *     'userName':'Jacky',
     *     'age':20
     * },{
     *     'id':'1',
     *     'content':'hello world'
     * }]
     * 那么设置索引的值为:[0].userName,[1].id,那么事件监控的Collection，将会创建为这两个对象创建这个索引到custom字段中，如果参数是个数组，
     * 可以使用[0][].id之类的符号描述，使用[]这个中括号
     * </pre>
     * @param argsIndexes
     */
    public void setArgsIndexes(String argsIndexes) {
        this.argsIndexes = argsIndexes;
    }

    /**
     * <pre>
     * 结果索引配置，如果result是一个对象，可以直接使用对象内的属性名称作为建立索引的名称，例如
     * {
     *     'userName':'Jacky',
     *     'age':20,
     *     'userId':'100001'
     * }
     * 那么可以设置为userName,userId 使用,分割
     * 如果result是一个同类型的数组，如果需要将数组内的某个字段都写入到索引中，可以使用[].userId
     * </pre>
     * @return
     */
    public String getResultIndexes() {
        return resultIndexes;
    }

    /**
     * <pre>
     * 结果索引配置，如果result是一个对象，可以直接使用对象内的属性名称作为建立索引的名称，例如
     * {
     *     'userName':'Jacky',
     *     'age':20,
     *     'userId':'100001'
     * }
     * 那么可以设置为userName,userId 使用,分割
     * 如果result是一个同类型的数组，如果需要将数组内的某个字段都写入到索引中，可以使用[].userId
     * </pre>
     * @param resultIndexes
     */
    public void setResultIndexes(String resultIndexes) {
        this.resultIndexes = resultIndexes;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }

    public String getWildcardId() {
        return wildcardId;
    }

    public void setWildcardId(String wildcardId) {
        this.wildcardId = wildcardId;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    /**
     * 事件中心分组
     * @return
     */
    public String getGroup() {
        return group;
    }

    /**
     * 事件中心分组
     * @param group
     */
    public void setGroup(String group) {
        this.group = group;
    }
}
