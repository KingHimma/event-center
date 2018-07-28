package eventcenter.api.appcache;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

/**
 * 每个事件中心都会有一个身份编号，如果应用是首次启动，那么将会使用这个身份编号
 * Created by liumingjian on 16/2/18.
 */
public class IdentifyContext {
    public static final String FILE_NAME = "identifyId";

    public static final String PROP_KEY_ID = "id";

    public static String getId() throws IOException {
        Properties data = AppDataContext.getInstance().createData(FILE_NAME);
        if(data.containsKey(PROP_KEY_ID)){
            return data.getProperty(PROP_KEY_ID);
        }

        String id = UUID.randomUUID().toString();
        data.put(PROP_KEY_ID, id);
        AppDataContext.getInstance().saveData(FILE_NAME, data);
        return id;
    }
}
