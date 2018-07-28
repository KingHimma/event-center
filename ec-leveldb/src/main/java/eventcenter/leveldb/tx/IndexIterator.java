package eventcenter.leveldb.tx;

/**
 * 实现查询{@link LevelDBBucket}中的数据的
 *
 * @author liumingjian
 * @date 2016/12/29
 */
public interface IndexIterator {

    void iterateIndex(String index, int pageNo) throws Exception;
}
