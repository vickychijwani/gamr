package io.github.vickychijwani.gimmick.utility;

import com.squareup.otto.Bus;

public class EventBus extends Bus {

    private static EventBus sInstance;

    private EventBus() {
        super();
    }

    public static EventBus getInstance() {
        if (sInstance == null) {
            sInstance = new EventBus();
        }
        return sInstance;
    }

}
