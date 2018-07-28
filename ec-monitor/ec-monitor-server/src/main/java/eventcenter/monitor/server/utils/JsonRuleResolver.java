package eventcenter.monitor.server.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonRule解析器，支持的表达式如下：
 * <pre>
 *     "id" 取JSON根级下的id字段
 *     "[]id" 取JSON数组的所有元素的下的id字段
 *     "[0]petshop.dog.name" 取JSON数组的第0个下标为petshop对象下的dog对象下的name属性
 *     "classroom.student.[0]topics.[]name" 取JSON根下的classroom字段的第0个student的对象，并获取这个student下所有topics数组元素的name值
 *     "id,[0]id" 支持多个解析，使用','逗号分隔
 * </pre>
 * Created by liumingjian on 16/2/26.
 */
public class JsonRuleResolver {
    private final static Pattern RG_ARRAY = Pattern.compile("\\[(\\d*)\\](\\w+)");

    private String rule;

    private JsonRule[] jsonRules;

    JsonRuleResolver(){

    }

    JsonRuleResolver(String rule){
        if(null == rule || "".equals(rule))
            throw new IllegalArgumentException("rule argument can't be null");
        this.rule = rule;
        jsonRules = resolve(rule);
    }

    JsonRule[] resolve(String rule){
        String[] ruleRegex = rule.split(",");
        JsonRule[] jsonRules = new JsonRule[ruleRegex.length];
        for(int i = 0;i < ruleRegex.length;i++){
            jsonRules[i] = resolveOne(ruleRegex[i]);
        }
        return jsonRules;
    }

    JsonRule resolveOne(String rule){
        String[] levels = rule.split("\\.");
        JsonRule root = new JsonRule();
        resolveLevel(root, levels[0]);
        JsonRule prev = root;
        for(int i = 1;i < levels.length;i++){
            JsonRule child = new JsonRule();
            prev.setEmbedded(child);
            resolveLevel(child, levels[i]);
            prev = child;
        }
        return root;
    }

    void resolveLevel(JsonRule rule, String level){
        Matcher m = RG_ARRAY.matcher(level);
        if(!m.matches()){
            rule.setField(level);
            return ;
        }
        rule.setArray(true);
        String groupValue = m.group(1);
        String field = m.group(2);
        rule.setField(field);
        if(null == groupValue || "".equals(groupValue)){
            return ;
        }
        rule.setArrayIndex(Integer.parseInt(groupValue));
    }

    public String getRule() {
        return rule;
    }

    public JsonRule[] getJsonRules() {
        return jsonRules;
    }
}
