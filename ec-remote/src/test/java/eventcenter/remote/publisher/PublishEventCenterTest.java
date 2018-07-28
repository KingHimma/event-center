package eventcenter.remote.publisher;

import eventcenter.api.EventInfo;
import eventcenter.remote.EventTransmission;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * Created by liumingjian on 16/8/24.
 */
public class PublishEventCenterTest {

    PublishEventCenter publishEventCenter;

    @Before
    public void setUp() throws Exception {
        publishEventCenter = spy(new PublishEventCenter());
        publishEventCenter.setPublisherGroups(Arrays.asList(buildGroup("erp-gray2_erp-caigou-gray2-dubbo", "module.open.validate,module.open"),
                buildGroup("erp-gray2_erp-index-gray2-dubbo", "address.sync,module.open.result"),
                buildGroup("erp-gray2_finance-app-1", "trade.resend.db.before"),
                buildGroup("erp-gray2_erp-trade-stage-dubbo", "staff.*,company.registry,stock.adjustment,sandbox.build,sku.bind,item.bind,item.unbind.force,sku.unbind.force,item.change,sku.change,user.active,user.unactive,bridge.item.sync,trade.buyerpay,trade.sellership,trade.refund.statuschange,items.update,skus.update,tmc.trade.memoModified,tmc.trade.changeAddress,staff.bindshop,workOrder.continue.send.goods,module.open.validate"),
                buildGroup("erp-gray2_erp-wms-gray2-dubbo", "items.inactive,skus.delete,items.delete,trade.unaudit,trade.audit,trade.autoaudit,stock.goods_section.changeitem,stock.goods_section.changeitem_num,stock.goods_section.minus_stock,stock.goods_section.unlock,goods_section.retry,goods_section.excel_delete,goodsSection.addBatch,trade.merge.split.stock,backend.goods_section.minus_return_stocks,trade.stockfix.lock,module.open.validate,module.open"),
                buildLocalGroup(null, "*")));
        //publishEventCenter.startup();
        doNothing().when(publishEventCenter).fireRemoteEvent(anyList(), any(), any(EventInfo.class), any(), Mockito.anyBoolean());
        doReturn(null).when(publishEventCenter)._superFireEvent(any(), any(EventInfo.class), any());
    }

    @Test
    public void test__fireEvent1() throws Exception {
        publishEventCenter.__fireEvent(this, new EventInfo("items.update").setArgs(new Object[]{"test"}), null);
        publishEventCenter.__fireEvent(this, new EventInfo("items.update").setArgs(new Object[]{"test"}), null);
        publishEventCenter.__fireEvent(this, new EventInfo("combine.stock.change").setArgs(new Object[]{"test"}), null);
        publishEventCenter.__fireEvent(this, new EventInfo("skus.update").setArgs(new Object[]{"test"}), null);
        publishEventCenter.__fireEvent(this, new EventInfo("items.update").setArgs(new Object[]{"test"}), null);
        publishEventCenter.__fireEvent(this, new EventInfo("skus.update").setArgs(new Object[]{"test"}), null);
        publishEventCenter.__fireEvent(this, new EventInfo("items.update").setArgs(new Object[]{"test"}), null);
        publishEventCenter.__fireEvent(this, new EventInfo("staff.active").setArgs(new Object[]{"test"}), null);
        verify(publishEventCenter, atLeast(7)).fireRemoteEvent(anyList(), any(), any(EventInfo.class), any(), Mockito.anyBoolean());
        verify(publishEventCenter, atLeast(8))._superFireEvent(any(), any(EventInfo.class), any());
    }

    PublisherGroup buildGroup(String groupName, String remoteEvents){
        PublisherGroup group = new PublisherGroup(mock(EventTransmission.class));
        group.setGroupName(groupName);
        group.setRemoteEvents(remoteEvents);
        return group;
    }

    LocalPublisherGroup buildLocalGroup(String groupName, String remoteEvents){
        LocalPublisherGroup group = new LocalPublisherGroup();
        group.setGroupName(groupName);
        group.setRemoteEvents(remoteEvents);
        return group;
    }
}