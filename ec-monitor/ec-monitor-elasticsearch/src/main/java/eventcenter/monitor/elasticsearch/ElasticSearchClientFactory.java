package eventcenter.monitor.elasticsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

import java.io.Serializable;
import java.util.Date;

/**
 * 连接ElasticSearch的基本配置
 * Created by liumingjian on 2016/10/13.
 */
public class ElasticSearchClientFactory implements Serializable {

    private String elasticHost;

    private Integer connTimeout;

    private Integer readTimeout;

    HttpClientConfig buildConfig(){
        HttpClientConfig.Builder builder = new HttpClientConfig.Builder(elasticHost).multiThreaded(true);
        if(null != connTimeout){
            builder.connTimeout(connTimeout);
        }
        if(null != readTimeout){
            builder.readTimeout(readTimeout);
        }
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateTimeTypeConverter()).create();
        builder.gson(gson);
        return builder.build();
    }

    /**
     * 建立Elastic Search客户端
     * @return
     */
    public JestClient createClient(){
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(buildConfig());
        return factory.getObject();
    }

    public String getElasticHost() {
        return elasticHost;
    }

    public void setElasticHost(String elasticHost) {
        this.elasticHost = elasticHost;
    }

    public Integer getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(Integer connTimeout) {
        this.connTimeout = connTimeout;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }
}
