package any.xxx.anypeer;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("any.xxx.anypeer", appContext.getPackageName());
        String msgId = UUID.randomUUID().toString();
        Log.d("Test", "msgId.len="+msgId.length());
        String strUUID = "b3ad6601-e329-498f-8c51-82d4ea14508c";
        UUID uuid = UUID.fromString(strUUID);
        Log.d("Test", "uuid1================"+uuid.toString() + ", isValidUUID="+isValidUUID(strUUID));

        uuid = UUID.fromString(strUUID);
        Log.d("Test", "uuid2================"+uuid.toString() + ", isValidUUID="+isValidUUID(strUUID));

        strUUID = "111b3ad6601-e329-498f-8c51-82d4ea14508c0";
        uuid = UUID.fromString(strUUID);
        Log.d("Test", "uuid3================"+uuid.toString() + ", isValidUUID="+isValidUUID(strUUID));

        strUUID = "b3ad6601-222e329-498f-8c51-82d4ea14508c0";
        uuid = UUID.fromString(strUUID);
        Log.d("Test", "uuid4================"+uuid.toString() + ", isValidUUID="+isValidUUID(strUUID));

        strUUID = "b3ad6601-e329-498f333-8c51-82d4ea14508c0";
        uuid = UUID.fromString(strUUID);
        Log.d("Test", "uuid5================"+uuid.toString() + ", isValidUUID="+isValidUUID(strUUID));

        strUUID = "b3ad6601-e329-498f-8c51444-82d4ea14508c0";
        uuid = UUID.fromString(strUUID);
        Log.d("Test", "uuid6================"+uuid.toString() + ", isValidUUID="+isValidUUID(strUUID));
    }

    @Test
    public void testCheckUUID() {
        int count = 10000;
        while (count > 0) {
            String msgId = UUID.randomUUID().toString();
            Log.d("Test", String.format("count=[%d], msgId=[%s]", count, msgId));
            if (!isValidUUID(msgId)) {
                fail();
            }
            count --;
        }
    }

    public static boolean isValidUUID(String uuid) {
        if (uuid == null) {
            System.out.println("uuid is null");
            return false;
        }

        // "b3ad6601-e329-498f-8c51-82d4ea14508c"
        String regex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        return uuid.matches(regex);
    }
}
