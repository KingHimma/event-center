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
public class ReceivedEventDAOTest  extends DAOTestBase {

    ReceivedEventDAO dao;

    @Before
    public void setUp() throws Exception {
        dao = (ReceivedEventDAO)super.init(ReceivedEventDAO.class);
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
        mei.setStart(new Date());
        mei.setEventId(UUID.randomUUID().toString());
        mei.setEventName("test");
        mei.setDelay(1000L);
        mei.setFromNodeId(UUID.randomUUID().toString());
        mei.setMdcValue(UUID.randomUUID().toString());
        dao.save(mei);
    }
}