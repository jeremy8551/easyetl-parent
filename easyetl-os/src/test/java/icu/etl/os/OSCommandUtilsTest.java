package icu.etl.os;

import icu.etl.os.internal.OSCommandUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/2
 */
public class OSCommandUtilsTest {

    @Test
    public void testsplitMultiCommandStdout() {
        String str = OSCommandUtils.START_PREFIX + "test\n1\n2\n3\n" + OSCommandUtils.START_PREFIX + "key";
        OSCommandStdouts map = OSCommandUtils.splitMultiCommandStdout(str);
        assertTrue(map.get("key").isEmpty());
        assertTrue(map.get("test").size() == 3);

        str = OSCommandUtils.START_PREFIX + "test\n1\n2\n3\n";
        map = OSCommandUtils.splitMultiCommandStdout(str);
        assertTrue(map.get("key") == null);
        assertTrue(map.get("test").size() == 3);
        assertTrue(map.get("test").get(0).equals("1"));
        assertTrue(map.get("test").get(1).equals("2"));
        assertTrue(map.get("test").get(2).equals("3"));
    }

}
