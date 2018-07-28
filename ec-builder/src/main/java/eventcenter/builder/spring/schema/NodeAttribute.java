package eventcenter.builder.spring.schema;

import org.w3c.dom.Node;

/**
 * Created by liumingjian on 2017/9/30.
 */
public class NodeAttribute {

    private final Node node;

    public NodeAttribute(Node node){
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public String getAttribute(String key){
        Node item = node.getAttributes().getNamedItem(key);
        return null==item?null:item.getNodeValue();
    }

    public Long getAttributeLong(String key){
        String value = getAttribute(key);
        return null == value ? null : Long.parseLong(value);
    }

    public Integer getAttributeInteger(String key){
        String value = getAttribute(key);
        return null == value ? null : Integer.parseInt(value);
    }

    public Boolean getAttribuetBoolean(String key){
        String value = getAttribute(key);
        return null == value ? null : Boolean.parseBoolean(value);
    }
}
