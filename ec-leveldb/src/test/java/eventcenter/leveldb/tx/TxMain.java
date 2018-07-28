package eventcenter.leveldb.tx;

/**
 * Created by liumingjian on 2017/1/3.
 */
public class TxMain {

    public static void main(String[] agrs) throws Exception {
        TestLevelDBContainerWithTx test = new TestLevelDBContainerWithTx();
        test.setUp();

        //test.fireEvents(10);
    }
}
