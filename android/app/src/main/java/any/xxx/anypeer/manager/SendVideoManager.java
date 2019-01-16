package any.xxx.anypeer.manager;

import android.util.Log;
import java.util.concurrent.CopyOnWriteArrayList;

public class SendVideoManager {
    private static final String TAG = "SendVideoManager";
    private static SendVideoManager instance = new SendVideoManager();

    private CopyOnWriteArrayList<Callback> callbacks = new CopyOnWriteArrayList<>();

    public static SendVideoManager getInstance() {
        return instance;
    }

    public interface Callback {
        void call(String path);
    }

    public void addCallback(Callback callback) {
        Log.d(TAG, "addCallback " + callback.getClass().getName());
        if (!callbacks.contains(callback)) {
            callbacks.add(callback);
        }
    }

    public void removeCallback(Callback callback) {
        Log.d(TAG, "removeCallback " + callback.getClass().getName());
        if (callbacks.contains(callback)) {
            callbacks.remove(callback);
        }
    }

    public void notifyAllCallback(String path) {
        for (Callback callback : callbacks) {
            Log.d(TAG, "notifyAllCallback " + callback.getClass().getName());
            callback.call(path);
        }
    }
}
