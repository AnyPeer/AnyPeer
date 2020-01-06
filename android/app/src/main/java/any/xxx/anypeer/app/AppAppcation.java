package any.xxx.anypeer.app;

import android.app.Application;
import android.os.StrictMode;

import io.realm.FieldAttribute;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

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
                .schemaVersion(2)
                .migration((realm, oldVersion, newVersion) -> {
                    RealmSchema schema = realm.getSchema();
                    if (oldVersion == 1) {
                        RealmObjectSchema chatSchema = schema.get("ChatConversation");
                        chatSchema
                                .addField("isGroup", Boolean.class, FieldAttribute.REQUIRED)
                                .addField("groupName", String.class, FieldAttribute.REQUIRED)
                                .transform(obj -> {
                                    obj.set("isGroup", false);
                                    obj.set("groupName", "");
                                });
                        oldVersion++;
                    }
                })
                .build();
        Realm.setDefaultConfiguration(conf);

        ForegroundCallbacks.init(this);
    }
}
