package eventcenter.remote.dubbo.test;

import eventcenter.api.EventListener;
import org.springframework.stereotype.Component;

import eventcenter.api.EventSourceBase;
import eventcenter.api.annotation.ExecuteAsyncable;
import eventcenter.api.annotation.ListenerBind;


@Component
@ListenerBind("beforeAsync,afterAsync")
@ExecuteAsyncable
public class AsycnablePrintListener implements EventListener {

	@Override
	public void onObserved(EventSourceBase source) {
		System.out.println("触发事件了：" + source);
	}

}
