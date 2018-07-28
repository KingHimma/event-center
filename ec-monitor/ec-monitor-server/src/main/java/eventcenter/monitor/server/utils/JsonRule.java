package eventcenter.monitor.server.utils;

import java.io.Serializable;

/**
 * 用于解析 JSON字符串的格式，通过这个规则表达式可以获取到JSON内某个字段的值
 * Created by liumingjian on 16/2/26.
 */
public class JsonRule implements Serializable {

    private static final long serialVersionUID = -1859500249031516029L;

    /**
     * 是否为JSON数组
     */
    private boolean array;

    /**
     * 如果 {@link #array}为true，那么可以指定为取某个下标的值
     */
    private Integer arrayIndex;

    /**
     * 如果 {@link #array}为false，那么可以指定某个JSON的field值
     */
    private String field;

    /**
     * 如果某个field值是嵌入的，例如 child.children[0]，那么需要使用这个embedded对象
     */
    private JsonRule embedded;

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public Integer getArrayIndex() {
        return arrayIndex;
    }

    public void setArrayIndex(Integer arrayIndex) {
        this.arrayIndex = arrayIndex;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public JsonRule getEmbedded() {
        return embedded;
    }

    public void setEmbedded(JsonRule embedded) {
        this.embedded = embedded;
    }
}
