package any.xxx.anypeer;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testExtra() {
        String test = "0123456789";
        assertEquals('9', test.charAt(test.length() - 1));
        assertEquals("9", test.substring(test.length() - 1));
        assertEquals("012345678", test.substring(0, test.length() - 1));
    }

    @Test
    public void testDouble() {
        String[] array = new String[5];
        array[0] = "0";
        array[1] = "0.0";
        array[2] = "0.00";
        array[3] = "0.000";
        array[4] = "0.0000";
        for (int i = 0; i < 5; i++) {
            for (int j = i + 1; j < 5; j++) {
                assertEquals(Double.doubleToLongBits(Double.parseDouble(array[i])), Double.doubleToLongBits(Double.parseDouble(array[j])));
            }
        }
    }

    @Test
    public void testInt2BytesArray() {
        for (int size = 0; size < 100000000; size ++) {
            byte[] data = int2BytesArray(size);
            int size2 = byteArray2Int(data);
            assertEquals(size, size2);
        }
    }

    @Test
    public void testCombineValue() {
        String value = combineValue("msgid", "124141241412");
        String[] args = parseValue(value.getBytes());
        assertNotNull(args);
        assertEquals(2, args.length);

        value = value.substring(args[0].length() + 1);
        assertEquals("124141241412", value);

        value = combineValue(null, "1241412414123");
        args = parseValue(value.getBytes());
	    assertNotNull(args);
        assertEquals(1, args.length);

        value = "123 456 789";
        assertEquals("456 789", value.substring(value.indexOf(' ') + 1));

        String uuid = UUID.randomUUID().toString();
        assertEquals(36, uuid.length());

        String messageValue = "123456789";
        byte[] bytes = messageValue.getBytes();
        String newValue = new String(bytes);
        assertEquals(messageValue, newValue);
	    newValue = new String(bytes, 5, 4);
	    assertEquals("6789", newValue);
    }

    @Test
    public void testAny() {
        String value = "925B9085-1BC3-49E6-9298-C4E45BEE28EA0 测试";
        String[] args = parseValue(value.getBytes());
        assertEquals("925B9085-1BC3-49E6-9298-C4E45BEE28EA0", args[0]);
        assertEquals("测试", args[1]);

        String msgId = args[0].substring(0, args[0].length() - 1);
        assertEquals("925B9085-1BC3-49E6-9298-C4E45BEE28EA", msgId);
        assertTrue(isValidUUID(msgId));
    }

    private static boolean isValidUUID(String uuid) {
        // "b3ad6601-e329-498f-8c51-82d4ea14508c"
        String regex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        return uuid != null && uuid.matches(regex);
    }

    @Test
    public void testLongDouble() {
        long sl = 123456000;
        double d = (double) sl / 100000000;
        assertEquals(1.23456, d, 0.0001);
    }

    private static String combineValue(String spacial, String value) {
        if (spacial != null && !spacial.isEmpty()) {
            return spacial + " " + value;
        }
        return value;
    }

    private static String[] parseValue(byte[] bytes) {
        try {
            String value = new String(bytes);
            return value.split("\\s+");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int byteArrayToInt(byte[] b) {
        return   b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    private static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    private static byte[] intToByteArray2(int a) {
        byte[] data = new byte[4];
        data[0] = (byte) ((a >> 24) & 0xFF);
        data[1] = (byte) ((a >> 16) & 0xFF);
        data[2] = (byte) ((a >> 8) & 0xFF);
        data[3] = (byte) (a & 0xFF);
        return data;
    }

    private static byte[] int2BytesArray(int a) {
        byte[] data = new byte[4];
        data[0] = (byte) ((a >> 24) & 0xFF);
        data[1] = (byte) ((a >> 16) & 0xFF);
        data[2] = (byte) ((a >> 8) & 0xFF);
        data[3] = (byte) (a & 0xFF);
        return data;
    }

    private static int byteArray2Int(byte[] b) {
        return byteArray2Int(b, 0);
    }

    private static int byteArray2Int(byte[] b, int pos) {
        return b[3 + pos] & 0xFF |
                (b[2 + pos] & 0xFF) << 8 |
                (b[1 + pos] & 0xFF) << 16 |
                (b[pos] & 0xFF) << 24;
    }

}