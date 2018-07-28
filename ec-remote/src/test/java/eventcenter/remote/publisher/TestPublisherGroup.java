package eventcenter.remote.publisher;

import eventcenter.remote.EventTransmission;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Created by liumingjian on 16/8/24.
 */
public class TestPublisherGroup {

    PublisherGroup group;

    @Before
    public void setUp() throws Exception {
        group = new PublisherGroup(mock(EventTransmission.class));
        group.setGroupName("test");
        group.setRemoteEvents("staff.*,company.registry,stock.adjustment,sandbox.build,sku.bind,item.bind,item.unbind.force,sku.unbind.force,item.change,sku.change,user.active,user.unactive,bridge.item.sync,trade.buyerpay,trade.sellership,trade.refund.statuschange,items.update,skus.update,tmc.trade.memoModified,tmc.trade.changeAddress,staff.bindshop,workOrder.continue.send.goods,module.open.validate");
        group.setRemoteUrl("127.0.0.0.1:8080");
    }

    @Test
    public void testIsRemoteEvent() throws Exception {
        Assert.assertEquals(true, group.isRemoteEvent("skus.update"));
        Assert.assertEquals(true, group.isRemoteEvent("items.update"));
        Assert.assertEquals(true, group.isRemoteEvent("items.update"));
        Assert.assertEquals(true, group.isRemoteEvent("items.update"));

    }
}