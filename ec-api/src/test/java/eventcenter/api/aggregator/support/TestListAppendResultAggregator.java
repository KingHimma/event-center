package eventcenter.api.aggregator.support;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eventcenter.api.CommonEventSource;
import eventcenter.api.aggregator.ListenerConsumedResult;
import eventcenter.api.aggregator.ListenersConsumedResult;

public class TestListAppendResultAggregator {

	@Test
	public void test() {
		ListAppendResultAggregator<Integer> aggregator = new ListAppendResultAggregator<Integer>();
		
		ListenersConsumedResult result = new ListenersConsumedResult();
		result.setSource(new CommonEventSource(this, UUID.randomUUID().toString(), "test", new Object[]{new Integer[]{1,2}}, null, null));
		ListenerConsumedResult r1 = new ListenerConsumedResult();
		r1.setResult(Arrays.asList(1));
		ListenerConsumedResult r2 = new ListenerConsumedResult();
		r2.setResult(2);
		result.setResults(Arrays.asList(r1, r2));
		
		List<Integer> list = aggregator.aggregate(result);
		Assert.assertArrayEquals(new Integer[]{1,2}, list.toArray());
	}

}
