package eventcenter.remote.dubbo.test;

import eventcenter.api.CommonEventSource;
import eventcenter.api.EventListener;
import eventcenter.api.annotation.ListenerBind;
import org.springframework.stereotype.Component;

@Component
@ListenerBind("beforeSync,afterSync")
public class SyncablePrintListener implements EventListener {

	@Override
	public void onObserved(CommonEventSource source) {
		System.out.println("触发事件了：" + source);
	}

}
