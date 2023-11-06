package icu.etl.io;

import java.io.CharArrayReader;
import java.io.IOException;
import java.nio.CharBuffer;

import org.junit.Assert;
import org.junit.Test;

public class AliveReaderTest {

    @Test
    public void test() throws IOException {
        CharArrayReader proxy = new CharArrayReader("0123456789".toCharArray());
        AliveReader in = new AliveReader(proxy);
        Assert.assertTrue(in.ready());

        char c = (char) in.read();
        Assert.assertEquals('0', c);

        int rl = in.read(new char[2]);
        Assert.assertEquals(2, rl);

        if (in.markSupported()) {
            in.mark(7);
            int rl1 = in.read(new char[7]);
            Assert.assertEquals(7, rl1);

            in.reset();
            rl1 = in.read(new char[7]);
            Assert.assertEquals(7, rl1);
        }

        System.out.println("hashcode: " + in.hashCode());
        System.out.println("toString: " + in.toString());
        Assert.assertFalse(in.equals(new Object()));

        // 测试字符输入流是否可以真的被关闭
        in.close();
        try {
            in.reset();
            int rl1 = in.read(new char[7]);
            Assert.assertEquals(7, rl1);
        } catch (IOException e) {
            // 如果发生错误，表示字符输入流已被关闭
            e.printStackTrace();
            Assert.fail();
        }

        // 字符输入流中已没有可读字符
        Assert.assertFalse(in.ready());

        in.reset();
        int rl1 = in.read(new char[7], 0, 7);
        Assert.assertEquals(7, rl1);

        in.reset();
        Assert.assertEquals(7, in.skip(7));
        Assert.assertFalse(in.ready());
        
        // 只是为了测试 read 方法
        try {
            in.read((CharBuffer) null);
            Assert.fail();
        } catch (NullPointerException e) {
        }
    }

}
