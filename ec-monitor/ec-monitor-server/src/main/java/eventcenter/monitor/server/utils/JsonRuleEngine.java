package eventcenter.monitor.server.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import eventcenter.remote.utils.ExpiryMap;

/**
 * 解析JSON规则表达式的引擎
 * Created by liumingjian on 16/2/26.
 */
public class JsonRuleEngine {

    private String separatorChar = " ";

    /**
     * 自动将解析Resolver缓存起来
     */
    private boolean autoCache = true;

    /**
     * 默认缓存两个小时
     */
    private long cacheExpired = 2 * 3600 * 1000;

    /**
     * 缓存key
     */
    private final static ExpiryMap<String, JsonRuleResolver> cache = new ExpiryMap<String, JsonRuleResolver>();

    public JsonRuleEngine(){

    }

    /**
     * <pre>
     *     从JSON数据中，根据rule规则获取JSON的值，并使用seperatorChar设置的分隔符号拼接成字符串
     *     "id" 取JSON根级下的id字段
     *     "[].id" 取JSON数组的所有元素的下的id字段
     *     "[0].id" 取JSON数组的第0个下标的id字段
     *     "classroom.student[0].topics[].name" 取JSON根下的classroom字段的第0个student的对象，并获取这个student下所有topics数组元素的name值
     *     "id,[0].id" 支持多个解析，使用','逗号分隔
     * </pre>
     * @param json
     * @param rule
     * @return
     */
    public String resolve(JSON json, String rule){
        if(null == json)
            throw new IllegalArgumentException("json argument can't be null");
        if(null == rule || "".equals(rule))
            return "";

        String _rule = rule.trim();
        StringBuilder sb = new StringBuilder();
        JsonRule[] jsonRules = findResolver(_rule).getJsonRules();
        for(JsonRule jsonRule : jsonRules){
            _resolve(sb, json, jsonRule);
        }
        return sb.toString();
    }

    void _resolve(StringBuilder sb, JSON json, JsonRule jsonRule){
        if(jsonRule.isArray()){
            if(!(json instanceof JSONArray))
                return ;
            JSONArray ja = (JSONArray)json;
            if(jsonRule.getEmbedded() == null){
                appendString(sb, findJsonArray(ja, jsonRule.getArrayIndex(), jsonRule.getField()));
                return ;
            }
            if(null != jsonRule.getArrayIndex()){
                JSONObject jo = (JSONObject)ja.get(jsonRule.getArrayIndex());
                if(null == jo || !jo.containsKey(jsonRule.getField()))
                    return ;
                _resolve(sb, (JSON)jo.get(jsonRule.getField()), jsonRule.getEmbedded());
            }else{
                for(int i = 0;i < ja.size();i++){
                    JSONObject jo = (JSONObject)ja.get(i);
                    if(null == jo || !jo.containsKey(jsonRule.getField()))
                        continue;
                    _resolve(sb, (JSON)jo.get(jsonRule.getField()), jsonRule.getEmbedded());
                }
            }
            return ;
        }
        // 对象
        if(!(json instanceof JSONObject))
            return ;
        JSONObject jo = (JSONObject)json;
        if(jsonRule.getEmbedded() == null){
            appendString(sb, jo.getString(jsonRule.getField()));
            return ;
        }
        if(null == jo || !jo.containsKey(jsonRule.getField()))
            return ;
        _resolve(sb, (JSON)jo.get(jsonRule.getField()), jsonRule.getEmbedded());
    }

    String findJsonArray(JSONArray ja, Integer arrayIndex, String field){
        if(null != arrayIndex){
            if(ja.size() > arrayIndex){
                JSONObject jo = ja.getJSONObject(arrayIndex);
                if(null == jo || !jo.containsKey(field))
                    return "";
                return jo.getString(field);
            }
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i < ja.size();i++){
            JSONObject jo = (JSONObject)ja.get(i);
            if(null == jo || !jo.containsKey(field)){
                continue;
            }
            appendString(sb, jo.getString(field));
        }
        return sb.toString();
    }

    StringBuilder appendString(StringBuilder sb, String v){
        if(null == v || "".equals(v.trim()))
            return sb;
        if(sb.length() == 0){
            sb.append(v);
        }else{
            sb.append(separatorChar).append(v);
        }
        return sb;
    }

    JsonRuleResolver findResolver(String rule){
        if(!cache.isStart()){
            cache.startup();
        }

        if(cache.containKey(rule)){
            return cache.get(rule).getValue();
        }

        JsonRuleResolver resolver = new JsonRuleResolver(rule);
        cache.put(rule, cacheExpired, resolver);
        return resolver;
    }

    public String getSeparatorChar() {
        return separatorChar;
    }

    public void setSeparatorChar(String separatorChar) {
        this.separatorChar = separatorChar;
    }

    public boolean isAutoCache() {
        return autoCache;
    }

    public void setAutoCache(boolean autoCache) {
        this.autoCache = autoCache;
    }

    public long getCacheExpired() {
        return cacheExpired;
    }

    public void setCacheExpired(long cacheExpired) {
        this.cacheExpired = cacheExpired;
    }

    public static ExpiryMap<String, JsonRuleResolver> getCache() {
        return cache;
    }
}
