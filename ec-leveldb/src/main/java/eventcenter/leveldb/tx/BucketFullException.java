package eventcenter.leveldb.tx;

/**
 * If bucket size is full, it would throw the exception
 * Created by liumingjian on 2016/12/27.
 */
public class BucketFullException extends Exception{

    private static final long serialVersionUID = 614097270220034002L;

    public BucketFullException(String id){
        super(new StringBuilder("bucket[").append(id).append("] is full").toString());
    }
}
