package eventcenter.monitor.server.mongo;

import com.mongodb.DBRef;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import static com.mongodb.client.model.Filters.*;

/**
 * Created by liumingjian on 16/2/19.
 */
public class TestMongodbClientFactory {

    MongodbClientFactory factory;

    final String databaseName = "test";

    @Before
    public void setUp() throws Exception {
        factory = new MongodbClientFactory();
        factory.setDatabaseName(databaseName);
    }

    @After
    public void tearDown() throws Exception {
        factory.destroy();

    }

    @Test
    public void testCreate(){
        MongoDatabase db = factory.initConnections();
        MongoCollection<Document> collection = db.getCollection("test2");

        collection.insertOne(new Document().append("name", "Jacky LIU").append("created", new Date()).append("child",
                new Document().append("name", "YUTAO LIU")));
    }

    @Test
    public void testCreate2(){
        MongoDatabase db = factory.initConnections();
        MongoCollection<Document> collection = db.getCollection("test2");
        String id = "100001";
        /*collection.updateOne(eq("_id", id), new Document("$set", new Document().append("_id", id).append("name", "Jacky LIU2").append("created", new Date()).append("child",
                new Document().append("name", "YUTAO LIU"))));*/

        id = UUID.randomUUID().toString();
        UpdateResult updateResult = collection.replaceOne(eq("_id", id),
                new Document().append("name", "Jacky LIU3").append("created", new Date()).append("child",
                        new Document().append("name", "YUTAO LIU")));
    }

    @Test
    public void testDbRef(){
        MongoDatabase db = factory.initConnections();
        MongoCollection<Document> collection = db.getCollection("classroom");
        final String id = UUID.randomUUID().toString();
        collection.insertOne(new Document().append("_id", id).append("classroom", "02-02"));

        MongoCollection<Document> collection2 = db.getCollection("student");
        collection2.insertOne(new Document().append("name", "YUTAO").append("age", 5).append("classroom", new DBRef("classroom", id)));


    }

    class ComboObj implements Serializable{

        private static final long serialVersionUID = -5874246479516797339L;

        private UserInfo userInfo;

        public UserInfo getUserInfo() {
            return userInfo;
        }

        public void setUserInfo(UserInfo userInfo) {
            this.userInfo = userInfo;
        }
    }

    class UserInfo implements Serializable {
        private static final long serialVersionUID = 7436797311000214520L;
        private String name;

        private UserInfo child;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public UserInfo getChild() {
            return child;
        }

        public void setChild(UserInfo child) {
            this.child = child;
        }
    }
}
