package eventcenter.leveldb.tx;

import org.fusesource.leveldbjni.JniDBFactory;
import org.fusesource.leveldbjni.internal.NativeDB;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by liumingjian on 2016/12/26.
 */
public class TestLevelDBBucket {

    DB db;

    JniDBFactory factory = new JniDBFactory();

    File dir = new File(System.getProperty("user.dir") + File.separator + "target" + File.separator + "leveldbtx");

    Options options = new Options();

    static{
        NativeDB.LIBRARY.load();
    }

    @Before
    public void setUp() throws Exception {
        options.createIfMissing(true);
        db = factory.open(dir, options);
    }

    @After
    public void tearDown() throws Exception {
        if(null == db)
            return ;
        db.close();
        factory.destroy(dir, options);
    }

    @Test
    public void testBuilder1() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(100).openCache(true);
        LevelDBBucket bucket = builder.build();
        Assert.assertEquals("test", bucket.getQueueName());
        Assert.assertEquals(100, bucket.getMaxCountOfPage());
        Assert.assertEquals(100 ,bucket.getPageCount());
        Assert.assertEquals(true, bucket.isOpenCache());
        Assert.assertEquals(bucket.getPageCount(), bucket.pagesCache.size());
        Assert.assertNotNull(bucket.getCurrentPage());
        Assert.assertEquals(1L, bucket.getCurrentPage().getNo());
        Assert.assertNotNull(bucket.getId());
        Assert.assertEquals(0, bucket.count());
        Assert.assertEquals(1000, bucket.capacity());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilder2() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).maxCountOfPage(100).openCache(true);
        builder.build();
    }

    @Test
    public void testBuilder3() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(100).openCache(true);
        LevelDBBucket bucket = builder.build();
        String bucketId = bucket.getId();
        db.close();

        // 重新打开
        setUp();
        builder = new LevelDBBucket.Builder(db).id(bucketId).queueName("test").maxCountOfPage(100).openCache(true);
        bucket = builder.build();
        Assert.assertEquals(bucketId, bucket.getId());
        Assert.assertEquals(0, bucket.count());
        Assert.assertEquals(1000, bucket.capacity());
    }

    @Test
    public void testSavePageIndex1() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(100).openCache(false);
        LevelDBBucket bucket = builder.build();
        WriteBatch writeBatch = db.createWriteBatch();
        String index = UUID.randomUUID().toString();
        try {
            bucket.savePageIndex(writeBatch, index);
        }finally{
            db.write(writeBatch);
        }
        Assert.assertEquals(1, bucket.count());
        Assert.assertEquals(1, bucket.getCurrentPage().getNo());

        db.close();
        // 重新打开
        setUp();
        builder = new LevelDBBucket.Builder(db).id(bucket.getId()).queueName("test").maxCountOfPage(100).openCache(false);
        bucket = builder.build();
        Assert.assertEquals(1, bucket.count());
        Assert.assertEquals(1, bucket.getCurrentPage().getNo());
        Assert.assertEquals(index, bucket.getCurrentPage().getIndexes().get(0));
    }

    @Test
    public void testSavePageIndex2() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(100).openCache(false);
        LevelDBBucket bucket = builder.build();
        WriteBatch writeBatch = db.createWriteBatch();
        final int count = bucket.getMaxCapacityOfPage() + 1;
        try {
            for(int i = 0;i < count;i++) {
                bucket.savePageIndex(writeBatch, UUID.randomUUID().toString());
            }
        }finally{
            db.write(writeBatch);
        }
        Assert.assertEquals(count, bucket.count());
        Assert.assertEquals(2, bucket.getCurrentPage().getNo());

        db.close();
        // 重新打开
        setUp();
        builder = new LevelDBBucket.Builder(db).id(bucket.getId()).queueName("test").maxCountOfPage(100).openCache(false);
        bucket = builder.build();
        Assert.assertEquals(count, bucket.count());
        Assert.assertEquals(2, bucket.getPageCount());
        Assert.assertEquals(2, bucket.getCurrentPage().getNo());
        Assert.assertEquals(1, bucket.getCurrentPage().getIndexes().size());
    }

    /**
     * 测试是否会满，未开启缓冲
     * @throws Exception
     */
    @Test(expected = BucketFullException.class)
    public void testSavePageIndex3() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(10).openCache(false);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage() + 1;
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                bucket.savePageIndex(writeBatch, UUID.randomUUID().toString());
            }finally{
                db.write(writeBatch);
            }
        }
    }

    /**
     * 测试是否会满，开启缓冲
     * @throws Exception
     */
    @Test(expected = BucketFullException.class)
    public void testSavePageIndex4() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(10).openCache(true);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage() + 1;
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                bucket.savePageIndex(writeBatch, UUID.randomUUID().toString());
            }finally{
                db.write(writeBatch);
            }
        }
    }

    @Test
    public void testPop1() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(10).openCache(false);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = bucket.pop(writeBatch);
                Assert.assertEquals(allIndex.get(i), id);
            }finally{
                db.write(writeBatch);
            }
            if(i%bucket.getMaxCapacityOfPage() == 0){
                Assert.assertEquals(i/bucket.getMaxCapacityOfPage(), bucket.readPageNo - 1);
            }
        }

    }

    @Test
    public void testPop2() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(10).openCache(false);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }
        WriteBatch writeBatch = db.createWriteBatch();
        try {
            List<String> popIndex = bucket.pop(writeBatch, count);
            Assert.assertArrayEquals(allIndex.toArray(new String[allIndex.size()]), popIndex.toArray(new String[popIndex.size()]));
        }finally{
            db.write(writeBatch);
        }
    }

    @Test
    public void testPop3() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(10).openCache(false);
        LevelDBBucket bucket = builder.build();
        WriteBatch wb = db.createWriteBatch();
        try {
            Assert.assertNull(bucket.pop(wb));
        }finally {
            db.write(wb);
        }
        wb = db.createWriteBatch();
        try {
            Assert.assertTrue(bucket.pop(wb, 10000).isEmpty());
        }finally {
            db.write(wb);
        }
    }

    @Test
    public void testPop4() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(2).openCache(true);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = bucket.pop(writeBatch);
                Assert.assertEquals(allIndex.get(i), id);
            }finally{
                db.write(writeBatch);
            }
            if(i%bucket.getMaxCapacityOfPage() == 0){
                Assert.assertEquals(i/bucket.getMaxCapacityOfPage(), bucket.readPageNo - 1);
            }
        }

    }

    @Test
    public void testPop5() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(10).openCache(true);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }
        WriteBatch writeBatch = db.createWriteBatch();
        try {
            List<String> popIndex = bucket.pop(writeBatch, count);
            Assert.assertArrayEquals(allIndex.toArray(new String[allIndex.size()]), popIndex.toArray(new String[popIndex.size()]));
        }finally{
            db.write(writeBatch);
        }
    }

    @Test
    public void testPop6() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(10).openCache(true);
        LevelDBBucket bucket = builder.build();
        WriteBatch wb = db.createWriteBatch();
        try {
            Assert.assertNull(bucket.pop(wb));
        }finally {
            db.write(wb);
        }
        wb = db.createWriteBatch();
        try {
            Assert.assertTrue(bucket.pop(wb, 10000).isEmpty());
        }finally {
            db.write(wb);
        }
    }

    @Test
    public void testPop7() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(2).openCache(false);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }
        final int popCount = count/2;
        for(int i = 0;i < popCount;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = bucket.pop(writeBatch);
                Assert.assertEquals(allIndex.get(i), id);
            }finally{
                db.write(writeBatch);
            }
        }
        allIndex.removeAll(allIndex.subList(0, popCount));

        for(int i = 0;i < popCount;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }

        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = bucket.pop(writeBatch);
                Assert.assertEquals(allIndex.get(i), id);
            }finally{
                db.write(writeBatch);
            }
        }

        Assert.assertEquals(0, bucket.count());
    }

    @Test
    public void testPopByIndex1() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(10).openCache(true);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }

        for(String index : allIndex){
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                Assert.assertEquals(index, bucket.popByIndex(writeBatch, index));
            }finally{
                db.write(writeBatch);
            }
        }
    }

    @Test(expected = IllegalAccessException.class)
    public void testPopByIndex2() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(2).openCache(false);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }

        for(String index : allIndex){
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                bucket.popByIndex(writeBatch, index);
            }finally{
                db.write(writeBatch);
            }
        }
    }

    @Test
    public void testPopByIndex3() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(1).openCache(true);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }

        final int firstPopCount = count/2;
        int popCount = 0;
        for(String index : allIndex){
            if(popCount >= firstPopCount)
                break;
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                bucket.popByIndex(writeBatch, index);
            }finally{
                db.write(writeBatch);
                popCount++;
            }
        }

        for(int i = 0;i < firstPopCount;i++){
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                bucket.pop(writeBatch);
            }finally{
                db.write(writeBatch);
            }
        }

        Assert.assertEquals(0, bucket.count());
    }

    @Test
    public void testPopByIndex4() throws Exception {
        LevelDBBucket.Builder builder = new LevelDBBucket.Builder(db).queueName("test").maxCountOfPage(1).openCache(true);
        LevelDBBucket bucket = builder.build();
        final int count = bucket.getMaxCapacityOfPage() * bucket.getMaxCountOfPage();
        final List<String> allIndex = new ArrayList<String>(count + 1);
        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }

        final int firstPopCount = count/2;
        int popCount = 0;
        for(String index : allIndex){
            if(popCount >= firstPopCount)
                break;
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                bucket.popByIndex(writeBatch, index);
            }finally{
                db.write(writeBatch);
                popCount++;
            }
        }

        for(int i = 0;i < firstPopCount;i++){
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                String id = UUID.randomUUID().toString();
                allIndex.add(id);
                bucket.savePageIndex(writeBatch, id);
            }finally{
                db.write(writeBatch);
            }
        }

        for(int i = 0;i < count;i++) {
            WriteBatch writeBatch = db.createWriteBatch();
            try {
                bucket.pop(writeBatch);
            }finally{
                db.write(writeBatch);
            }
        }

        Assert.assertEquals(0, bucket.count());
    }
}