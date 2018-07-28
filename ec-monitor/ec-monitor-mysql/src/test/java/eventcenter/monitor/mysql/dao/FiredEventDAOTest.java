package eventcenter.monitor.mysql.dao;

import eventcenter.api.EventInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * Created by liumingjian on 2017/4/6.
 */
public class FiredEventDAOTest extends DAOTestBase{

    FiredEventDAO firedEventDAO;

    @Before
    public void setUp() throws Exception {
        firedEventDAO = (FiredEventDAO) init(FiredEventDAO.class);
    }

    @After
    public void tearDown() throws Exception {
        destroy();
    }

    @Test
    public void testSave() throws Exception {
        firedEventDAO.save(UUID.randomUUID().toString(), this, new EventInfo("test").setMdcValue(UUID.randomUUID().toString()));
    }
}