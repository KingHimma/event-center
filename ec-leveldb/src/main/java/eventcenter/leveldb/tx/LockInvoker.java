package eventcenter.leveldb.tx;

/**
 * Created by liumingjian on 2017/3/1.
 */
public interface LockInvoker<T> {
    T lockAndInvoke() throws Exception;
}
