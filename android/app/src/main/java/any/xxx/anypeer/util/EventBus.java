package any.xxx.anypeer.util;

import java.util.concurrent.CopyOnWriteArrayList;

public class EventBus {
    private static EventBus mInstance = new EventBus();

    private CopyOnWriteArrayList<Callback> callbacks = new CopyOnWriteArrayList<>();

    public static EventBus getInstance() {
        return mInstance;
    }

    public interface Callback {
        void callback();
    }

    public void addCallback(Callback callback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void removeCallback(Callback callback) {
        if (callbacks.contains(callback)) {
            callbacks.remove(callback);
        }
    }

    public void notifyAllCallbakc() {
        for (Callback callback : callbacks) {
            callback.callback();
        }
    }

}
