package any.xxx.anypeer.app;

import android.app.Application;
import android.os.StrictMode;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class AppAppcation extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // For android 7.0
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

        Realm.init(this);
        RealmConfiguration conf = new RealmConfiguration.Builder()
                .name("anypeer.realm")
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(conf);

        ForegroundCallbacks.init(this);
    }
}
