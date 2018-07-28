package eventcenter.monitor.client.leveldb;

import eventcenter.api.utils.IdWorker;
import eventcenter.api.utils.SerializeUtils;
import eventcenter.monitor.*;
import org.apache.log4j.Logger;
import org.iq80.leveldb.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * 使用了LevelDB存储监控数据
 * Created by liumingjian on 16/2/15.
 */
public class LeveldbInfoStorage implements InfoStorage {

    static final String PREFIX_EVENT_INFO = "ei";

    static final String KEY_NODE_INFO = "node_info";

    private String name;

    /**
     * 数据库目录
     */
    private File dirPath;

    private Integer blockRestartInterval;

    private Integer blockSize;

    private Long cacheSize;

    private Boolean useSnappyCompression;

    private Integer maxOpenFiles;

    private Boolean paranoidChecks;

    private Boolean verifyChecksums;

    private Integer writeBufferSize;

    private Integer batchDeleteSize = 1000;

    protected DB db;

    protected final Logger logger = Logger.getLogger(this.getClass());

    protected IdWorker idWorker;

    protected final ReentrantLock readLock = new ReentrantLock();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getDirPath() {
        if (null == dirPath) {
            String path = new StringBuilder(System.getProperty("user.home")).append(File.separator).append(".ecmonitor").toString();
            dirPath = new File(path);
        }
        return dirPath;
    }

    public void setDirPath(File dirPath) {
        this.dirPath = dirPath;
    }

    public Integer getBlockRestartInterval() {
        return blockRestartInterval;
    }

    public void setBlockRestartInterval(Integer blockRestartInterval) {
        this.blockRestartInterval = blockRestartInterval;
    }

    public Integer getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Integer blockSize) {
        this.blockSize = blockSize;
    }

    public Long getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(Long cacheSize) {
        this.cacheSize = cacheSize;
    }

    public Boolean getUseSnappyCompression() {
        return useSnappyCompression;
    }

    public void setUseSnappyCompression(Boolean useSnappyCompression) {
        this.useSnappyCompression = useSnappyCompression;
    }

    public Integer getMaxOpenFiles() {
        return maxOpenFiles;
    }

    public void setMaxOpenFiles(Integer maxOpenFiles) {
        this.maxOpenFiles = maxOpenFiles;
    }

    public Boolean getParanoidChecks() {
        return paranoidChecks;
    }

    public void setParanoidChecks(Boolean paranoidChecks) {
        this.paranoidChecks = paranoidChecks;
    }

    public Boolean getVerifyChecksums() {
        return verifyChecksums;
    }

    public void setVerifyChecksums(Boolean verifyChecksums) {
        this.verifyChecksums = verifyChecksums;
    }

    public Integer getWriteBufferSize() {
        return writeBufferSize;
    }

    public void setWriteBufferSize(Integer writeBufferSize) {
        this.writeBufferSize = writeBufferSize;
    }

    public Integer getBatchDeleteSize() {
        return batchDeleteSize;
    }

    public void setBatchDeleteSize(Integer batchDeleteSize) {
        this.batchDeleteSize = batchDeleteSize;
    }

    protected Options createOptions() {
        Options options = new Options();
        if (null != blockRestartInterval)
            options.blockRestartInterval(blockRestartInterval);
        if (null != blockSize)
            options.blockSize(blockSize);
        if (null != cacheSize)
            options.cacheSize(cacheSize);
        if (null != useSnappyCompression && useSnappyCompression.booleanValue())
            options.compressionType(CompressionType.SNAPPY);
        if (null != maxOpenFiles)
            options.maxOpenFiles(maxOpenFiles);
        if (null != paranoidChecks)
            options.paranoidChecks(paranoidChecks);
        if (null != verifyChecksums)
            options.verifyChecksums(verifyChecksums);
        if (null != writeBufferSize)
            options.writeBufferSize(writeBufferSize);
        return options;
    }

    @Override
    public void open() throws Exception{
        if(db != null)
            return ;
        idWorker = new IdWorker(1);
        Options options = createOptions();
        File dirPath = getDirPath();
        if(!dirPath.exists()){
            dirPath.mkdirs();
        }
        db = factory.open(dirPath, options);
        if(logger.isDebugEnabled()){
            logger.debug("open leveldb for info storage success, path:" + dirPath.getCanonicalPath());
        }
    }

    @Override
    public void close() throws Exception{
        if(db == null)
            return ;
        // TODO 关闭时还需要判断当前是否正在popEventInfo数据
        db.close();
        db = null;
        if(logger.isDebugEnabled()){
            logger.debug("closed leveldb for info storage success");
        }
    }

    protected void assertDbOpen(){
        if(null == db)
            new MonitorException("leveldb didn't open");
    }

    @Override
    public void pushEventInfos(List<MonitorEventInfo> infos) {
        assertDbOpen();
        WriteBatch batch = db.createWriteBatch();
        try {
            for (MonitorEventInfo info : infos) {
                String id = String.valueOf(idWorker.nextId());
                try {
                    batch.put(generateEventInfoKey(id).getBytes(), SerializeUtils.serialize(info));
                } catch (IOException e) {
                    throw new MonitorStorageException(e);
                }
            }
        }finally {
            db.write(batch);
            try {
                batch.close();
            } catch (IOException e) {
                throw new MonitorStorageException(e);
            }
        }

    }

    @Override
    public void pushEventInfo(MonitorEventInfo info) {
        assertDbOpen();
        pushEventInfos(Arrays.asList(info));
    }

    @Override
    public List<MonitorEventInfo> popEventInfos(int maxSize) {
        assertDbOpen();
        List<MonitorEventInfo> list = new ArrayList<MonitorEventInfo>(maxSize + 1);
        final ReentrantLock lock = readLock;
        lock.lock();
        WriteBatch batch = db.createWriteBatch();
        try{
            DBIterator iterator = db.iterator();
            iterator.seekToFirst();
            int index = 0;
            try {
                while (index < maxSize && iterator.hasNext()) {
                    Map.Entry<byte[], byte[]> element = iterator.next();
                    try {
                        String key = new String(element.getKey());
                        if (key.indexOf(PREFIX_EVENT_INFO) == -1)
                            continue;
                        list.add((MonitorEventInfo) SerializeUtils.unserialize(element.getValue()));
                        index++;
                        batch.delete(element.getKey());
                    } catch (IOException e) {
                        throw new MonitorStorageException(e);
                    }
                }
            } finally {
                try {
                    iterator.close();
                } catch (IOException e) {
                    logger.error(new StringBuilder("when close leveldb iterator happened error:").append(e.getMessage()), e);
                }
            }

            db.write(batch);
        }finally{
            try{
                batch.close();
            }catch(Exception e){
                logger.error(new StringBuilder("when close leveldb writeBatch happened error:").append(e.getMessage()), e);
            }
            lock.unlock();
        }
        return list;
    }

    @Override
    public MonitorEventInfo popEventInfo() {
        assertDbOpen();
        List<MonitorEventInfo> list = popEventInfos(1);
        if(list.size() == 0)
            return null;
        return list.get(0);
    }

    @Override
    public void saveNodeInfo(NodeInfo nodeInfo) {
        assertDbOpen();
        try {
            db.put(KEY_NODE_INFO.getBytes(), SerializeUtils.serialize(nodeInfo));
        } catch (IOException e) {
            throw new MonitorStorageException(e);
        }
    }

    @Override
    public NodeInfo queryNodeInfo() {
        assertDbOpen();
        byte[] bytes = db.get(KEY_NODE_INFO.getBytes());
        if(null == bytes || bytes.length == 0)
            return null;
        try {
            return (NodeInfo)SerializeUtils.unserialize(bytes);
        } catch (IOException e) {
            throw new MonitorStorageException(e);
        }
    }

    /**
     * 生成存储在leveldb中的key，他会加载一个前缀
     * @param key
     * @return
     */
    protected String generateKey(String prefix, String key){
        return new StringBuilder(prefix).append("_").append(key).toString();
    }

    protected String generateEventInfoKey(String key){
        return generateKey(PREFIX_EVENT_INFO, key);
    }
}
