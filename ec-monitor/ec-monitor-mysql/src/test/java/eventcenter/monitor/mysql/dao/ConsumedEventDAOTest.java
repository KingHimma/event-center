package eventcenter.monitor.mysql.dao;

import eventcenter.monitor.MonitorEventInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liumingjian on 2017/4/10.
 */
public class ConsumedEventDAOTest extends DAOTestBase {

    ConsumedEventDAO dao;

    @Before
    public void setUp() throws Exception {
        dao = (ConsumedEventDAO)super.init(ConsumedEventDAO.class);
    }

    @After
    public void tearDown() throws Exception {
        super.destroy();
    }

    @Test
    public void testSave() throws Exception {
        MonitorEventInfo mei = new MonitorEventInfo();
        mei.setNodeId(UUID.randomUUID().toString());
        mei.setCreated(new Date());
        mei.setConsumed(new Date());
        mei.setEventId(UUID.randomUUID().toString());
        mei.setEventName("test");
        mei.setDelay(1000L);
        mei.setFromNodeId(UUID.randomUUID().toString());
        mei.setMdcValue(UUID.randomUUID().toString());
        mei.setSuccess(true);
        mei.setTook(100L);
        mei.setListenerClazz(this.getClass().getName());
        dao.save(mei);
    }
}