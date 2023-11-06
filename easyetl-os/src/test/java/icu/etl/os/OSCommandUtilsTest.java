package icu.etl.os;

import icu.etl.os.internal.OSCommandUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OSCommandUtilsTest {

    @Test
    public void test() {
        String str = OSCommandUtils.START_PREFIX + "test\n1\n2\n3\n" + OSCommandUtils.START_PREFIX + "key";
        OSCommandStdouts map = OSCommandUtils.splitMultiCommandStdout(str);
        assertTrue(map.get("key").isEmpty());
        assertEquals(3, map.get("test").size());

        str = OSCommandUtils.START_PREFIX + "test\n1\n2\n3\n";
        map = OSCommandUtils.splitMultiCommandStdout(str);
        assertNull(map.get("key"));
        assertEquals(3, map.get("test").size());
        assertEquals("1", map.get("test").get(0));
        assertEquals("2", map.get("test").get(1));
        assertEquals("3", map.get("test").get(2));
    }

}
