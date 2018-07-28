package eventcenter.remote.dubbo.test;

import org.springframework.stereotype.Component;

import eventcenter.api.EventSourceBase;
import eventcenter.api.EventListener;
import eventcenter.api.annotation.ListenerBind;

@Component
@ListenerBind("beforeSync,afterSync")
public class SyncablePrintListener implements EventListener {

	@Override
	public void onObserved(EventSourceBase source) {
		System.out.println("触发事件了：" + source);
	}

}
