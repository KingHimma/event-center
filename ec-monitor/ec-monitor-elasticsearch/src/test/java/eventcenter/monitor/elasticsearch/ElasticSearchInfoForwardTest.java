package eventcenter.monitor.elasticsearch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by liumingjian on 2016/10/24.
 */
public class ElasticSearchInfoForwardTest {

    ElasticSearchInfoForward forward;

    @Before
    public void setUp() throws Exception {
        forward = new ElasticSearchInfoForward();
        forward.setElasticHost("http://elas.back.damaijia.com");
        forward.startup();
    }

    @After
    public void tearDown() throws Exception {
        forward.shutdown();
    }

    @Test
    public void test(){

    }
}