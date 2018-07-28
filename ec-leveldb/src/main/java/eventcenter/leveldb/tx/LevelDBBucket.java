package eventcenter.leveldb.tx;

import eventcenter.api.utils.SerializeUtils;
import eventcenter.leveldb.LevelDBPage;
import eventcenter.remote.utils.StringHelper;
import org.apache.log4j.Logger;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.WriteBatch;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * LevelDBBucket中包含多个数据页，将数据离散存储
 * Created by liumingjian on 2016/12/26.
 */
public class LevelDBBucket implements Serializable {

    private static final long serialVersionUID = -3011861468370113368L;

    static final int DEFAULT_PAGE_CAPACITY = 10;
    /**
     * bucket编号
     */
    private final String id;

    /**
     * bucket对应多个pageId
     */
    private List<String> pageIds;

    /**
     * pages的缓存，对于一些小桶的数据可以直接缓存到内存中，并同步刷新数据到磁盘
     */
    BucketPageCache pagesCache;

    /**
     * 是否开启页面缓存,默认不开启
     */
    private final boolean openCache;

    private final DB db;

    /**
     * 队列名称
     */
    private final String queueName;

    /**
     * 当前页面的数量
     */
    private int pageCount = 0;

    /**
     * 加载当前页的数据
     */
    private LevelDBPage currentPage;

    /**
     * 最大的页面数
     */
    private int maxCountOfPage = 20;

    /**
     * 每个页面的最大容量
     */
    private int maxCapacityOfPage = DEFAULT_PAGE_CAPACITY;

    /**
     * 每页的数量
     */
    private int[] pageCounts;

    /**
     * bucket的最大容量
     */
    private int capacityOfBucket;

    /**
     * 是否初始化过
     */
    private volatile boolean init = false;

    /**
     * 当前读取的页数
     */
    Integer readPageNo;

    private final ReentrantLock writeLock = new ReentrantLock();

    private final Logger logger = Logger.getLogger(this.getClass());

    LevelDBBucket(String id, DB db, Profile profile){
        this(id, db, profile.getQueueName(), profile.isOpenCache());
        if(null != profile.getMaxCountOfPage()){
            this.maxCountOfPage = profile.getMaxCountOfPage();
        }
    }

    LevelDBBucket(String id, DB db, String queueName, boolean openCache){
        this.db = db;
        this.id = id;
        this.queueName = queueName;
        this.openCache = openCache;
    }

    LevelDBBucket(String id, DB db, String queueName){
        this(id, db, queueName, false);
    }

    /**
     * 开启bucket
     */
    public synchronized void open() throws IOException {
        if(init)
            return ;
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try{
            loadPageCount();
            if(openCache){
                loadPages();
            }
            loadCapacityOfBucket();
            loadPageCounts();
            loadCurrentPage();
            loadReadPageNo();
        }finally{
            init = true;
            lock.unlock();
        }
    }

    private void loadPageCounts() throws IOException {
        pageCounts = new int[maxCountOfPage];
        for(int i = 0;i < maxCountOfPage;i++){
            if(i > pageCount){
                pageCounts[i] = 0;
                continue;
            }
            final String pageNo = String.valueOf(i + 1);
            LevelDBPage page = getPage(pageNo);
            if(null == page){
                logger.warn("bucket[" + id + "]'s page[" + pageNo + "] is null");
                pageCounts[i] = 0;
                continue;
            }
            pageCounts[i] = page.getIndexes().size();
        }
    }

    /**
     * it should be invoked after method of {@link #loadPageCounts()}
     * @throws IOException
     */
    private void loadCurrentPage() throws IOException {
        if(this.openCache){
            this.currentPage = getPageInner(1);
            WriteBatch wb = db.createWriteBatch();
            try {
                getIdlePage(wb);
            }catch(BucketFullException e){
                logger.error(e.getMessage());
            }
            return ;
        }
        this.currentPage = getPage(String.valueOf(this.pageCount));
        if(this.currentPage == null){
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                this.currentPage = nextPage(writeBatch);
            }finally{
                db.write(writeBatch);
                writeBatch.close();
            }
        }
    }

    private void loadReadPageNo() throws IOException {
        this.readPageNo = get(db, buildReadPageNoKey(), Integer.class);
        if(null == this.readPageNo){
            WriteBatch wb = db.createWriteBatch();
            this.readPageNo = 1;
            try {
                saveReadPageNo(wb, this.readPageNo);
            } finally {
                db.write(wb);
                wb.close();
            }
        }
    }

    private void loadCapacityOfBucket(){
        capacityOfBucket = this.maxCountOfPage * this.maxCapacityOfPage;
    }

    /**
     * 加载页面数据，用于开启openCache的模式
     */
    private void loadPages() throws IOException {
        pagesCache = new BucketPageCache(this.maxCountOfPage + 1);
        WriteBatch wb = db.createWriteBatch();
        try {
            for (int i = 1; i <= maxCountOfPage; i++) {
                LevelDBPage page = getPage(String.valueOf(i));
                if (null == page) {
                    page = createPage(i);
                    savePage(wb, page);
                    pageCount++;
                    savePageCount(wb, pageCount);
                }
                pagesCache.addPage(page);
            }
        }finally{
            db.write(wb);
            wb.close();
        }
    }

    private void loadPageCount() throws IOException {
        Integer value = get(db, buildBucketPageCount(id), Integer.class);
        if(null == value)
            return ;
        this.pageCount = value;
    }

    private LevelDBPage nextPage(WriteBatch wb) throws IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try{
            this.pageCount++;
            LevelDBPage page = createPage(this.pageCount);
            savePage(wb, page);
            savePageCount(wb, this.pageCount);
            return page;
        }finally{
            lock.unlock();
        }
    }

    private LevelDBPage createPage(int pageNo){
        LevelDBPage page = new LevelDBPage();
        page.setNo(pageNo);
        page.setIndexes(new ArrayList<String>());
        return page;
    }

    /**
     * 根据page中的index进行索引弹出，这个必须要开启openCache才能使用这个功能
     * @param wb
     * @param index
     * @return
     * @throws IllegalAccessException if openCache didn't open, it would be thrown
     */
    public String popByIndex(WriteBatch wb, String index) throws IllegalAccessException, IOException {
        if(!this.openCache)
            throw new IllegalAccessException("it needs open cache");

        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            // find index from cache
            LevelDBPage page = pagesCache.removeIndex(index);
            if (null == page)
                return null;

            savePage(wb, page);
            pageCounts[(int)page.getNo() - 1] = page.getIndexes().size();
            return index;
        }finally{
            lock.unlock();
        }
    }

    /**
     * 根据page中的index值取出索引数据，并删除page中的索引数据
     * @return
     */
    public String pop(WriteBatch wb) throws IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            List<String> values = pop(wb, 1);
            if (values == null || values.size() == 0)
                return null;
            return values.get(0);
        }finally{
            lock.unlock();
        }
    }

    /**
     * 弹出索引数据，并删除page中的索引数据
     * @param count
     * @return
     */
    public List<String> pop(WriteBatch wb, int count) throws IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        List<String> indexes = new ArrayList<String>(count + 1);
        try{
            int readPageNoSnapshot = this.readPageNo;
            this.readPageNo = pop(indexes, wb, this.readPageNo, count, count(), 0);
            if(this.readPageNo != readPageNoSnapshot){
                saveReadPageNo(wb, this.readPageNo);
            }
        }finally {
            lock.unlock();
        }
        return indexes;
    }

    private int pop(List<String> indexes, WriteBatch wb, int readPageNo, int count, int totalCount, int loopCount) throws IOException {
        if(count == 0 || totalCount == 0 || loopCount >= this.maxCountOfPage)
            return readPageNo;
        int pageCount = this.pageCounts[readPageNo - 1];
        if(pageCount == 0){
            // 换一页
            readPageNo = increaseReadPageNo(readPageNo);
            pop(indexes, wb,readPageNo,  count, totalCount, ++loopCount);
        }
        LevelDBPage page = getPageInner(readPageNo);
        int actualCount = 0;
        int leftCount = 0;
        if(count <= pageCount){
            actualCount = count;
        }else{
            actualCount = pageCount;
            leftCount = count - pageCount;
        }

        try{
            List<String> subList = page.getIndexes().subList(0, actualCount);
            indexes.addAll(subList);
            page.getIndexes().removeAll(subList);
            totalCount -= actualCount;
        }catch(IndexOutOfBoundsException e){
            logger.warn("bucket[" + id + "] page no :" + readPageNo + " only left:" + page.getIndexes().size() + ", it needs " + actualCount);
            totalCount -= page.getIndexes().size();
            indexes.addAll(page.getIndexes());
            page.getIndexes().clear();
        }
        this.pageCounts[readPageNo - 1] = page.getIndexes().size();
        if(actualCount == pageCount){
            readPageNo = increaseReadPageNo(readPageNo);
        }
        savePage(wb, page);
        if(leftCount == 0){
            return readPageNo;
        }
        return pop(indexes, wb, readPageNo, leftCount, totalCount, ++loopCount);
    }

    /**
     * 将page页的一条索引值插入到bucket的当前页中，如果当前页满了，则需要查找到空闲的一张页面，如果空闲页面也找不到，那么就扩展一张新的页。如果页面数量超过最大阀值，则覆盖掉最早的一条数据
     * @param index
     */
    public Integer savePageIndex(WriteBatch wb, String index) throws BucketFullException, IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try{
            if(isFull())
                throw new BucketFullException(id);
            LevelDBPage idlePage = getIdlePage(wb);
            if(this.openCache){
                pagesCache.updateIndex(idlePage, index);
            }else {
                idlePage.getIndexes().add(index);
            }
            savePage(wb, idlePage);
            increaseCount(wb, (int) idlePage.getNo());
            return (int)idlePage.getNo();
        }finally{
            lock.unlock();
        }
    }

    public void saveReadPageNo(WriteBatch wb, Integer pageNo) throws IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try{
            set(wb, buildReadPageNoKey(), pageNo);
        }finally{
            lock.unlock();
        }
    }

    private void increaseCount(WriteBatch wb, int pageNo){
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try{
            if(pageCounts[pageNo - 1] == this.maxCapacityOfPage){
                logger.warn("bucket[" + id + "] page[" + pageNo + "]'s count is reach max capacity of page:" + this.maxCapacityOfPage + ", page count can't be increased");
                return ;
            }
            pageCounts[pageNo - 1] += 1;
        }finally{
            lock.unlock();
        }
    }

    private void decreaseCount(int pageNo){
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try{
            if(pageCounts[pageNo - 1] == 0){
                logger.warn("bucket[" + id + "] page[" + pageNo + "]'s count is 0, page count can't be decreased");
                return ;
            }
            pageCounts[pageNo - 1] -= 1;
        }finally{
            lock.unlock();
        }
    }

    private int increaseReadPageNo(int pageNo){
        if(pageNo == this.maxCountOfPage)
            return 1;
        return ++pageNo;
    }

    private void savePageCount(WriteBatch wb, int count) throws IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            set(wb, buildBucketPageCount(id), count);
        }finally{
            lock.unlock();
        }
    }

    private void savePage(WriteBatch wb, LevelDBPage page) throws IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            set(wb, buildPageId(String.valueOf(page.getNo())), page);
        }finally{
            lock.unlock();
        }
    }

    /**
     * 按照pageNo的顺序，不断往下查找，直到最后一页时，再重新从第一页开始
     * @param wb
     * @return
     * @throws BucketFullException
     * @throws IOException
     */
    private LevelDBPage getIdlePage(WriteBatch wb) throws BucketFullException, IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try {
            // first check current page is full
            if(currentPage.getIndexes().size() < this.maxCapacityOfPage){
                return currentPage;
            }
            int pageNo = 0;
            int scanPageNo = (int)currentPage.getNo();
            for (; scanPageNo <= pageCounts.length; scanPageNo++) {
                if (pageCounts[scanPageNo-1] < this.maxCapacityOfPage) {
                    pageNo = scanPageNo;
                    break;
                }
            }
            if(currentPage.getNo() == 1 && pageNo == 0)
                throw new BucketFullException(id);
            if(pageNo == 0){
                // 重新从第一页开始扫描
                scanPageNo = 1;
                for(; scanPageNo < currentPage.getNo();scanPageNo++){
                    if (pageCounts[scanPageNo-1] < this.maxCapacityOfPage) {
                        pageNo = scanPageNo;
                        break;
                    }
                }
                if(pageNo == currentPage.getNo())
                    throw new BucketFullException(id);
            }
            if(this.openCache){
                this.currentPage = this.pagesCache.getPageByNo(pageNo);
            }else{
                LevelDBPage page = getPage(String.valueOf(pageNo));
                if(null == page){
                    page = createPage(pageNo);
                    savePage(wb, page);
                    this.pageCount++;
                    savePageCount(wb, this.pageCount);
                }
                this.currentPage = page;
            }
            return this.currentPage;
        }finally{
            lock.unlock();
        }
    }

    public LevelDBPage getCurrentPage() {
        return currentPage;
    }

    public boolean isFull(){
        return count() >= capacity();
    }

    /**
     * 获取total count of bucket
     * @return
     */
    public int count(){
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try{
            int count = 0;
            for(int pageCount : pageCounts){
                count += pageCount;
            }
            return count;
        }finally {
            lock.unlock();
        }
    }

    /**
     * max capacity of bucket
     * @return
     */
    public int capacity(){
        return capacityOfBucket;
    }

    /**
     * 传入pageId，pageId值一般从1开始
     * @param pageId
     * @return 如果pageId找不到则返回空
     */
    private LevelDBPage getPage(String pageId) throws IOException {
        return get(db, buildPageId(pageId), LevelDBPage.class);
    }

    /**
     * 这个方法会判断openCache是否开启，如果开启了，则从pageCach中获取
     * @param pageId
     * @return
     */
    public LevelDBPage getPageInner(int pageId) throws IOException {
        if(openCache){
            return this.pagesCache.getPageByNo(pageId);
        }
        return getPage(String.valueOf(pageId));
    }

    /**
     * 遍历整个bucket中的page数据，并将每页中的index，传递到iterator中
     * @param iterator
     */
    public void iterateIndex(IndexIterator iterator) throws IOException {
        for (int pageNo = 1; pageNo <= getPageCount(); pageNo++) {
            LevelDBPage snapshotPage = clonePage(pageNo);
            if(null == snapshotPage)
                return ;
            for (String index : snapshotPage.getIndexes()) {
                try {
                    iterator.iterateIndex(index, pageNo);
                } catch (Throwable e) {
                    logger.error("iterating page index error:" + e.getMessage(), e);
                }
            }
        }
    }

    private LevelDBPage clonePage(int pageNo) throws IOException {
        final ReentrantLock lock = this.writeLock;
        lock.lock();
        try{
            LevelDBPage page = getPageInner(pageNo);
            if (null == page)
                return null;

            if (page.getIndexes() == null || page.getIndexes().size() == 0)
                return null;

            return page.clone();
        }finally {
            lock.unlock();
        }
    }

    protected String buildReadPageNoKey(){
        return new StringBuffer(buildBucketKey(id)).append("_rc").toString();
    }

    protected static String buildBucketKey(String id){
        return new StringBuilder("bucket_").append(id).toString();
    }

    protected static String buildBucketPageCount(String id){
        return new StringBuffer(buildBucketKey(id)).append("_pageCount").toString();
    }

    protected String buildPageId(String pageNo){
        return wrapperKey(new StringBuilder(id).append("_page_").append(pageNo).toString());
    }

    protected String wrapperKey(String key){
        return new StringBuilder(queueName).append("_").append(key).toString();
    }

    public DB getDb() {
        return db;
    }

    public boolean isOpenCache() {
        return openCache;
    }

    public String getQueueName() {
        return queueName;
    }

    public int getMaxCountOfPage() {
        return maxCountOfPage;
    }

    public void setMaxCountOfPage(int maxCountOfPage) {
        if(maxCountOfPage < 10){
            throw new IllegalArgumentException("maxCountOfPage parameter can't be lower than 10");
        }
        this.maxCountOfPage = maxCountOfPage;
    }

    public int getMaxCapacityOfPage() {
        return maxCapacityOfPage;
    }

    public int getPageCount() {
        return pageCount;
    }

    public String getId() {
        return id;
    }

    static <T> T get(DB db, String key, Class<T> type) throws IOException {
        byte[] data = db.get(key.getBytes());
        Serializable obj = SerializeUtils.unserialize(data);
        if(null == obj)
            return null;
        return type.cast(obj);
    }

    static void delete(DB db, String key){
        db.delete(key.getBytes());
    }

    static void delete(WriteBatch wb, String key){
        wb.delete(key.getBytes());
    }

    static void set(DB db, String key, Serializable obj) throws IOException {
        db.put(key.getBytes(), SerializeUtils.serialize(obj));
    }

    static void set(WriteBatch wb, String key, Serializable obj) throws IOException {
        wb.put(key.getBytes(), SerializeUtils.serialize(obj));
    }

    private static Profile loadProfile(DB db, String id) throws IOException {
        String bucketKey = buildBucketKey(id);
        return get(db, bucketKey, Profile.class);
    }

    /**
     * bucket配置信息
     */
    public static class Profile implements Serializable{

        private static final long serialVersionUID = -2216005382888211890L;

        private Integer maxCountOfPage;

        private String queueName;

        private boolean openCache = false;

        public Integer getMaxCountOfPage() {
            return maxCountOfPage;
        }

        public void setMaxCountOfPage(Integer maxCountOfPage) {
            this.maxCountOfPage = maxCountOfPage;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public boolean isOpenCache() {
            return openCache;
        }

        public void setOpenCache(boolean openCache) {
            this.openCache = openCache;
        }
    }

    /**
     * 构造LevelDBBucket
     */
    public static class Builder{
        private final DB db;

        /**
         * 队列名称
         */
        private String queueName;

        private String id;

        private Profile profile = new Profile();

        public Builder(DB db){
            this.db = db;
        }

        /**
         * 传入id，如果已经知道bucket id，可以直接传入，builder会进行加载
         * @param id
         * @return
         */
        public Builder id(String id){
            this.id = id;
            return this;
        }

        /**
         * 队列名称，如果是创建bucket，可以不传入id，但是id和queueName必须有一个不为空
         * @param queueName
         * @return
         */
        public Builder queueName(String queueName){
            this.queueName = queueName;
            return this;
        }

        public Builder openCache(boolean openCache){
            this.profile.setOpenCache(openCache);
            return this;
        }

        public Builder maxCountOfPage(Integer maxCountOfPage){
            this.profile.setMaxCountOfPage(maxCountOfPage);
            return this;
        }

        public LevelDBBucket build() throws IOException {
            if(StringHelper.isEmpty(id) && StringHelper.isEmpty(queueName))
                throw new IllegalArgumentException("please set id or queueName parameter");
            LevelDBBucket bucket = null;
            if(StringHelper.isNotEmpty(id)){
                Profile snapshotProfile = loadProfile(db, id);
                if(null != snapshotProfile) {
                    if (null != profile.getMaxCountOfPage()) {
                        snapshotProfile.setMaxCountOfPage(profile.getMaxCountOfPage());
                    }
                    this.profile = snapshotProfile;
                }
            }else{
                profile.setQueueName(queueName);
            }
            if(StringUtils.isEmpty(id)){
                id = generateId();
                // save into leveldb
                set(db, buildBucketKey(id), profile);
            }else if(StringUtils.isEmpty(profile.getQueueName())){
                // 如果id不为空，但是从leveldb中load不出来，并且queueName也为空，那么这个Bucket是无法创建传来
                throw new IllegalArgumentException("bucket id can't be found and queueName is empty, please input queueName parameter");
            }
            bucket = new LevelDBBucket(id, db, profile);
            bucket.open();
            return bucket;
        }

        /**
         * 自动生成bucket id编号
         * @return
         */
        private String generateId(){
            return UUID.randomUUID().toString();
        }
    }
}
