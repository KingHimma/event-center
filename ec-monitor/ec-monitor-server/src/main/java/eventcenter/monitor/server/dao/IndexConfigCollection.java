package eventcenter.monitor.server.dao;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import eventcenter.monitor.server.model.IndexConfig;
import eventcenter.monitor.server.model.IndexConfigs;
import eventcenter.remote.utils.ExpiryMap;
import eventcenter.remote.utils.StringWildcard;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * 这个集合配置了，{@link EventInfoCollection}中创建事件内容的自定义索引
 * Created by liumingjian on 16/2/24.
 */
@Repository
public class IndexConfigCollection extends MongodbCollection {

    private final String collectionName = "indexConfig";

    /**
     * 缓存过期时间
     */
    private long cacheExpiry = 600 * 1000;

    final Map<String, ExpiryMap.ExpiryValue<IndexConfig>> cache = Collections.synchronizedMap(new HashMap<String, ExpiryMap.ExpiryValue<IndexConfig>>());

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public void createIndexes() {

    }

    /**
     * 保存自定义索引配置
     * @param config
     */
    public void save(IndexConfig config){
        if(config.getId() == null){
            insert(config);
            return ;
        }
        update(config);
    }

    public IndexConfigs search(String group, Integer pageNo, Integer pageSize){
        return IndexConfigs.build((int)searchCount(group),
                _search(group, pageNo, pageSize));
    }

    private List<IndexConfig> _search(String group, Integer pageNo, Integer pageSize){
        Document filter = buildCondition(group);
        if(null != pageNo){
            filter.append("pageNo", pageNo);
        }
        if(null != pageSize){
            filter.append("pageSize", pageSize);
        }
        FindIterable<Document> find = find(filter);
        if(null != pageNo && null != pageSize){
            find.skip((pageNo - 1) * pageSize);
        }
        if(null != pageSize){
            find.limit(pageSize);
        }
        final List<IndexConfig> result = new ArrayList<IndexConfig>();
        find.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                result.add(toIndexConfig(document));
            }
        });
        return result;
    }

    private Document buildCondition(String group){
        Document filter = new Document();
        filter.append("matched", false);
        if(isNotEmpty(group)){
            filter.append("group", group);
        }
        return filter;
    }

    private long searchCount(String group){
        Document filter = buildCondition(group);
        return getCollection().count(filter);
    }

    /**
     * 根据事件的名称查找
     * @param eventName
     * @return
     */
    public IndexConfig queryByEventName(String eventName, String group){
        if(isEmpty(eventName))
            throw new IllegalArgumentException("please set eventName argument");
        String cacheKey = buildCacheKey(eventName, group);
        if(cache.containsKey(cacheKey)){
            if(!checkExpiry(cache.get(cacheKey)))
                return cache.get(cacheKey).getValue();
            cache.remove(cacheKey);
        }

        IndexConfig config = _queryByEventName(eventName,group);
        if(null != config && isNotEmpty(config.getWildcardId())){
            config = queryById(config.getWildcardId());
        }
        cache.put(cacheKey, ExpiryMap.ExpiryValue.build(cacheExpiry, config));
        return config;
    }

    String buildCacheKey(String eventName, String group){
        return isEmpty(group)?eventName:new StringBuilder(group).append("_").append(eventName).toString();
    }

    /**
     * 判断config的修改时间是否已经超过了设置的cacheExpiry的过期时长
     * @param config
     * @return
     */
    protected boolean checkExpiry(ExpiryMap.ExpiryValue<IndexConfig> config){
        Date now = new Date();
        return (now.getTime() - config.getCreated().getTime()) >= cacheExpiry;
    }

    protected IndexConfig _queryByEventName(String eventName, String group){
        final List<IndexConfig> result = new ArrayList<IndexConfig>(2);
        Document filter = new Document("eventName", eventName);
        if(isNotEmpty(group)){
            filter.append("group", group);
        }
        find(filter).forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                result.add(toIndexConfig(document));
            }
        });
        if(result.size() > 0){
            return result.get(0);
        }
        // 使用模糊匹配
        List<IndexConfig> wildcardConfigs = queryByWildcard();
        if(wildcardConfigs.size() == 0)
            return null;

        IndexConfig matched = null;
        for(IndexConfig config : wildcardConfigs){
            if(match(eventName, config.getEventName())){
                matched = config;
                break;
            }
        }

        if(null != matched && !matched.equals(eventName)){
            IndexConfig matchedConfig = createMatchedConfig(eventName, matched.getId(), group);
            insert(matchedConfig);
        }
        return matched;
    }

    protected boolean match(String source, String pattern){
        return StringWildcard.wildMatch(pattern, source);
    }

    protected IndexConfig createMatchedConfig(String eventName, String wildcardId, String group){
        IndexConfig config = new IndexConfig();
        config.setEventName(eventName);
        config.setMatched(true);
        config.setWildcardId(wildcardId);
        config.setGroup(group);
        return config;
    }

    /**
     * 查询使用了通配符的配置
     * @return
     */
    protected List<IndexConfig> queryByWildcard(){
        final List<IndexConfig> result = new ArrayList<IndexConfig>();
        find(new Document("wildcard", true)).forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                result.add(toIndexConfig(document));
            }
        });
        return result;
    }

    /**
     * 根据事件配置的ID查找
     * @param id
     * @return
     */
    public IndexConfig queryById(String id){
        FindIterable<Document> result = find(new Document("_id", id));
        final List<IndexConfig> configs = new ArrayList<IndexConfig>(2);
        result.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                configs.add(toIndexConfig(document));
            }
        });
        return configs.size() == 0?null:configs.get(0);
    }

    public long deleteById(String id){
        if(isEmpty(id))
            throw new IllegalArgumentException("please set id argument");
        IndexConfig origin = queryById(id);
        if(origin == null)
            throw new IllegalArgumentException("can't find data with id:" + id);

        long deleteCount = 0;
        deleteCount = deleteOne(new Document("_id", id)).getDeletedCount();
        if(origin.isWildcard()){
            deleteCount += deleteByWildcardId(id);
        }
        cache.clear();
        return deleteCount;
    }

    protected long deleteByWildcardId(String id){
        return deleteMany(new Document("wildcardId", id)).getDeletedCount();
    }

    private void insert(IndexConfig config){
        if(isEmpty(config.getEventName()))
            throw new IllegalArgumentException("please set config.eventName arguments");
        if(!config.isMatched()){
            if(isEmpty(config.getArgsIndexes()) && isEmpty(config.getResultIndexes()))
                throw new IllegalArgumentException("please set config.argsIndexes or config.resultIndexes arguments");
        }else{
            if(isEmpty(config.getWildcardId()))
                throw new IllegalArgumentException("please set config.wildcardId if config is matched");
        }
        config.setId(UUID.randomUUID().toString());
        config.setCreated(new Date());
        config.setModified(new Date());
        config.setWildcard(isWildcard(config.getEventName()));
        insertOne(toDocument(config));
    }

    boolean isWildcard(String eventName){
        return eventName.contains("*");
    }

    void update(IndexConfig config){
        IndexConfig origin = queryById(config.getId());
        if(origin.isWildcard() && isNotEmpty(config.getEventName())){
            boolean isWildcard = isWildcard(config.getEventName());
            if(!isWildcard || !origin.getEventName().equals(config.getEventName())){
                deleteByWildcardId(origin.getId());
            }
            config.setWildcard(isWildcard);
        }else{
            config.setWildcard(origin.isWildcard());
        }
        cache.clear();
        updateOne(new Document("_id", config.getId()), toDocument(config));
    }

    private Document toDocument(IndexConfig config){
        Document doc = new Document();
        if(isNotEmpty(config.getId())){
            doc.append("_id", config.getId());
        }
        if(isNotEmpty(config.getEventName())){
            doc.append("eventName", config.getEventName());
        }
        if(isNotEmpty(config.getArgsIndexes())){
            doc.append("argsIndexes", config.getArgsIndexes());
        }
        if(isNotEmpty(config.getResultIndexes())){
            doc.append("resultIndexes", config.getResultIndexes());
        }
        if(config.getCreated() != null){
            doc.append("created", config.getCreated());
        }
        if(config.getModified() != null){
            doc.append("modified", config.getModified());
        }
        if(isNotEmpty(config.getGroup())){
            doc.append("group", config.getGroup());
        }
        doc.append("matched",config.isMatched());
        doc.append("wildcard", config.isWildcard());
        if(isNotEmpty(config.getWildcardId())){
            doc.append("wildcardId", config.getWildcardId());
        }
        return doc;
    }

    private IndexConfig toIndexConfig(Document doc){
        IndexConfig config = new IndexConfig();
        config.setId(doc.getString("_id"));
        config.setModified(doc.getDate("modified"));
        config.setCreated(doc.getDate("created"));
        config.setArgsIndexes(doc.getString("argsIndexes"));
        config.setResultIndexes(doc.getString("resultIndexes"));
        config.setEventName(doc.getString("eventName"));
        config.setWildcard(doc.getBoolean("wildcard"));
        config.setMatched(doc.getBoolean("matched"));
        config.setWildcardId(doc.getString("wildcardId"));
        config.setGroup(doc.getString("group"));
        return config;
    }

    public long getCacheExpiry() {
        return cacheExpiry;
    }

    public void setCacheExpiry(long cacheExpiry) {
        this.cacheExpiry = cacheExpiry;
    }
}
