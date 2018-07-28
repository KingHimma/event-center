package eventcenter.leveldb.tx;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by liumingjian on 2017/3/1.
 */
public class EventIdContext {

    private static AtomicInteger consumedCount = new AtomicInteger(0);

    public static int increaseAndGet(){
        return consumedCount.incrementAndGet();
    }
}
