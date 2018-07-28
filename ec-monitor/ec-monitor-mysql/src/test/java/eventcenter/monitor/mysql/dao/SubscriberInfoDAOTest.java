package eventcenter.monitor.mysql.dao;

import eventcenter.remote.SubscriberGroup;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by liumingjian on 2017/4/11.
 */
public class SubscriberInfoDAOTest extends DAOTestBase<SubscriberInfoDAO> {

    SubscriberInfoDAO dao;

    @Before
    public void setUp() throws Exception {
        dao = init(SubscriberInfoDAO.class);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSave() throws Exception {
        SubscriberGroup group = new SubscriberGroup();
        group.setName("test");
        group.setGroupName("test");
        group.setRemoteEvents("staff.*,warehouse.insert,trade.audit");
        dao.save(group);
    }
}