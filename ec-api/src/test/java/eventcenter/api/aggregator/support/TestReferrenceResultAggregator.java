package eventcenter.api.aggregator.support;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eventcenter.api.CommonEventSource;
import eventcenter.api.aggregator.ListenersConsumedResult;

public class TestReferrenceResultAggregator {

	@Test
	public void test() {
		ReferenceResultAggregator<Integer> aggregator = new ReferenceResultAggregator<Integer>(1);
		
		ListenersConsumedResult result = new ListenersConsumedResult();
		result.setSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test", new Object[]{1,2}, null, null));
		Assert.assertEquals(2, aggregator.aggregate(result).intValue());
	}
}
