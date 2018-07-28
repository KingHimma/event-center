package eventcenter.monitor.server.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import java.util.List;

/**
 * 创建mongodb连接的工厂
 * Created by liumingjian on 16/2/19.
 */
public class MongodbClientFactory {

    private MongoClient mongoClient;

    private MongoDatabase db;

    private String host;

    private Integer port;

    private String databaseName;

    private MongoClientOptions options;

    private List<MongoCredential> credentials;

    public MongoDatabase initConnections(){
        if(null == databaseName || "".equals(databaseName.trim()))
            throw new IllegalArgumentException("please set databaseName arguments");
        if(null != db)
            return db;

        initMongoClient();
        db = mongoClient.getDatabase(databaseName);
        return db;
    }

    protected void initMongoClient(){
        ServerAddress address = null;
        if(!isEmpty(host)){
            if(null == port)
                address = new ServerAddress(host);
            else
                address = new ServerAddress(host, port);
        }else{
            address = new ServerAddress();
        }
        if(null == options){
            options = new MongoClientOptions.Builder().build();
        }
        if(null == credentials){
            mongoClient = new MongoClient(address, options);
        }else{
            mongoClient = new MongoClient(address, credentials, options);
        }
    }

    public void destroy(){
        if(null == mongoClient)
            return ;
        mongoClient.close();
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public MongoClientOptions getOptions() {
        return options;
    }

    public void setOptions(MongoClientOptions options) {
        this.options = options;
    }

    public List<MongoCredential> getCredentials() {
        return credentials;
    }

    public void setCredentials(List<MongoCredential> credentials) {
        this.credentials = credentials;
    }

    protected boolean isEmpty(String s){
        return s == null || "".equals(s);
    }
}
