package eventcenter.monitor;

import java.util.*;

/**
 * Created by liumingjian on 16/3/30.
 */
public abstract class AbstractMonitorDataCodec implements MonitorDataCodec {

    /**
     * if collection or map 's element is collection or map, it would deep into till to maxLevel
     */
    protected int maxLevel = 2;

    /**
     * 将一个非{@link Collection}、{@link Object[]}和{@link Map}类型的对象，转换成为{@link Map}结构的数据
     * @param data
     * @return
     */
    protected abstract Map<String, Object> codecElement(Object data);

    /**
     * 将一个非{@link Collection}、{@link Object[]}和{@link Map}类型的对象，转换成为{@link Map}结构的数据
     * @param data
     * @return
     */
    @Override
    public Object codec(String eventName, Object data) {
        return codec(eventName, data, 0);
    }

    protected Object codec(String eventName, final Object data, int level){
        if(null == data){
            return null;
        }
        if(!(data instanceof Collection || data instanceof Map || data instanceof Object[])){
            Map<String, Object> result = codecElement(data);
            if(null == result)
                return data;
            return result;
        }
        if(level > maxLevel)
            return data;

        if(data instanceof Collection){
            return codecCollection(eventName, (Collection)data, ++level);
        }
        if(data instanceof Object[]){
            return codecArray(eventName, (Object[])data, ++level);
        }
        Map<Object, Object> map = (Map<Object, Object>)data;
        Set<Object> keys = map.keySet();
        Map<String, Object> result = new HashMap<String, Object>();
        ++level;
        for(Object key : keys){
            Object val = map.get(key);
            if(null == val)
                continue;
            String _key = key==null?"null":key.toString();
            if(val instanceof Collection){
                result.put(_key, codecCollection(eventName, (Collection) val, level));
                continue;
            }
            if(val instanceof Map){
                result.put(_key, codec(eventName, val, level));
                continue;
            }
            if(val instanceof Object[]){
                result.put(_key, codecArray(eventName, (Object[]) val, level));
                continue;
            }
            Map<String, Object> codec = codecElement(val);
            if(null == codec){
                result.put(_key, val);
            }else{
                result.put(_key, codec);
            }
        }
        return result;
    }

    @Override
    public Collection<Object> codecCollection(String eventName, Collection<?> collection) {
        return codecCollection(eventName, collection, 0);
    }

    protected Collection<Object> codecCollection(String eventName, final Collection<?> collection, int level) {
        if(null == collection)
            return null;
        Collection<Object> result = new ArrayList<Object>(collection.size() + 1);
        //++level;
        for(Object obj : collection){
            Object val = codec(eventName, obj, level);
            if(null == val){
                result.add(obj);
                continue ;
            }
            result.add(val);
        }
        return result;
    }

    protected Collection<Object> codecArray(String eventName, final Object[] array, int level){
        if(null == array)
            return null;
        Collection<Object> result = new ArrayList<Object>(array.length + 1);
        //++level;
        for(Object obj : array){
            Object val = codec(eventName, obj, level);
            if(null == val){
                result.add(obj);
                continue ;
            }
            result.add(val);
        }
        return result;
    }

    public static class Builder {
        private Map<String, Object> result = new HashMap<String, Object>();

        public Builder append(String key, Object value){
            result.put(key, value);
            return this;
        }

        public Map<String, Object> build(){
            return result;
        }
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
}
