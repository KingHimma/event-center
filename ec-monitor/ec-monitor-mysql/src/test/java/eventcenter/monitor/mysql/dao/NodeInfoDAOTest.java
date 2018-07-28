package eventcenter.monitor.mysql.dao;

import eventcenter.monitor.NodeInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.UUID;

/**
 * Created by liumingjian on 2017/4/7.
 */
public class NodeInfoDAOTest extends DAOTestBase {

    NodeInfoDAO nodeInfoDAO;

    @Before
    public void setUp() throws Exception {
        nodeInfoDAO = (NodeInfoDAO) init(NodeInfoDAO.class);
    }

    @After
    public void tearDown() throws Exception {
        destroy();
    }

    @Test
    public void testSave() throws Exception {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setId(UUID.randomUUID().toString());
        nodeInfo.setGroup("test");
        nodeInfo.setHost("127.0.0.1");
        nodeInfo.setName("test");
        nodeInfoDAO.save(nodeInfo);
        Thread.sleep(2000);
        nodeInfo.setStart(new Date());
        nodeInfoDAO.save(nodeInfo);
    }
}