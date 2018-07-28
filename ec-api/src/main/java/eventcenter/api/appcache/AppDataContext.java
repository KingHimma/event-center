package eventcenter.api.appcache;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * cache some application's data into disk, it would be reload by startup,e.g. using zk to cache load offline address.
 * it would be start up to reload offline address into queue.
 * Created by liumingjian on 15/12/13.
 */
public class AppDataContext {

    public static final String SYSTEM_PROPERTY_PATH = "ec.appdata.path";

    public static final String SYSTEM_PROPERTY_APPDATA_NAME = "ec.appdata.name";

    public static final String PATH_NAME = ".eventcenter";

    private static AppDataContext self;

    List<String> fileList;

    File path;

    private final Logger logger = Logger.getLogger(this.getClass());

    AppDataContext(){}

    String getDataPath(){
        String path = _getDataPath();
        String appName = System.getProperty(SYSTEM_PROPERTY_APPDATA_NAME, "df");
        return new StringBuilder(path).append(File.separator).append(appName).toString();
    }

    private String _getDataPath(){
        String path = System.getProperty(SYSTEM_PROPERTY_PATH);
        if(null != path && !"".equals(path.trim())) {
            return path;
        }

        path = System.getProperty("user.home");
        if(!isEmpty(path)) {
            return new StringBuilder(filterPath(path)).append(PATH_NAME).toString();
        }

        return new StringBuilder(filterPath(System.getProperty("user.dir"))).append(PATH_NAME).toString();
    }

    private String filterPath(String path){
        if(isEmpty(path)) {
            return path;
        }

        if(path.substring(path.length() - 1, path.length()).equals(File.separator)) {
            return path;
        }

        return new StringBuilder(path).append(File.separator).toString();

    }

    private boolean isEmpty(String v){
        return null == v || "".equals(v.trim());
    }

    void init() throws IOException {
        String dataPath = getDataPath();
        logger.info(new StringBuilder("load ec-app-data:path:").append(dataPath));
        path = new File(dataPath);
        if(!path.exists()){
            path.mkdirs();
        }

        // load path files
        fileList = initListFiles(path);
    }

    List<String> initListFiles(File path) throws IOException {
        File listFile = buildFile("list");
        List<String> files = new ArrayList<String>();
        if(!listFile.exists()){
            listFile.createNewFile();
            return files;
        }

        BufferedReader reader = new BufferedReader(new FileReader(listFile));
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                files.add(line);
            }
        }finally{
            reader.close();
        }
        return files;
    }

    public static synchronized AppDataContext getInstance() throws IOException {
        if(null == self){
            self = new AppDataContext();
            self.init();
        }
        return self;
    }

    /**
     * create app data
     * @param name
     * @return
     */
    public Properties createData(String name) throws IOException {
        File file = buildFile(name);
        Properties props = new Properties();
        if(file.exists()){
            FileReader fr = new FileReader(file);
            try {
                props.load(fr);
                return props;
            }finally {
                fr.close();
            }
        }
        saveData(file, props);
        return props;
    }

    public void saveData(String name, Properties props) throws IOException {
        saveData(buildFile(name), props);
    }

    File buildFile(String name){
        return new File(new StringBuilder(path.getPath()).append(File.separator).append(name).toString());
    }

    void saveData(File file, Properties props) throws IOException {
        FileWriter fw = new FileWriter(file);
        try {
            props.store(fw, "");
        }finally {
            fw.close();
        }
    }
}
