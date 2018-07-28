package eventcenter.monitor.server.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.annotation.Resource;
import java.util.List;

/**
 * Mongodb的访问存储集合的基类
 * Created by liumingjian on 16/2/19.
 */
public abstract class MongodbCollection {

    protected final Logger logger = Logger.getLogger(this.getClass());

    @Resource
    protected MongoDatabase db;

    protected MongoCollection<Document> getCollection(){
        return db.getCollection(getCollectionName());
    }

    protected void insertOne(Document doc){
        getCollection().insertOne(doc);
    }

    protected void insertMany(List<? extends Document> list){
        getCollection().insertMany(list);
    }

    protected UpdateResult updateOne(Bson filter, Bson update){
        return getCollection().updateOne(filter, update);
    }

    protected UpdateResult updateMany(Bson filter, Bson update) {
        return getCollection().updateMany(filter, update);
    }

    protected DeleteResult deleteMany(Bson filter){
        return getCollection().deleteMany(filter);
    }

    protected DeleteResult deleteOne(Bson filter){
        return getCollection().deleteOne(filter);
    }

    protected FindIterable<Document> find(Bson filter){
        return getCollection().find(filter);
    }

    public MongoDatabase getDb() {
        return db;
    }

    public void setDb(MongoDatabase db) {
        this.db = db;
    }

    public abstract String getCollectionName();

    public abstract void createIndexes();

    protected boolean isEmpty(String value){
        return null == value || "".equals(value);
    }

    protected boolean isNotEmpty(String value){
        return !isEmpty(value);
    }
}
