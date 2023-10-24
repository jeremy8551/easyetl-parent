package icu.etl.expression;

import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParameterExpressionTest {

    @Test
    public void test() {
        String expr = " -d 20170102 -f /home/user/shell/qyzx/m_file/test.del -c true -p /home/user/shell/qyzx/m_file/txt.del -dd -u -s 'this is params ' parameter1 ";
        ArgumentExpression param = new ArgumentExpression(expr);
        assertEquals(expr.trim(), param.toString());

        assertTrue(!param.containOption("-a"));
        assertTrue(param.getOption("-d").equals("20170102"));
        assertTrue(param.getParameter(1).equals("parameter1"));
        assertTrue(param.getParameterSize() == 1);
        assertTrue(!param.existsOption());
        assertTrue(param.isOptionBlank("-dd"));
        assertTrue(StringUtils.replaceVariable(" 20170301 > ${d}", param.removeMinus()).equals(" 20170301 > 20170102"));
        param.addOption("-key", "yusdf");
        assertTrue(param.getOption("-key").equals("yusdf"));
        assertTrue(param.containOption("-f"));

        String[] names = param.getOptionNames();
        assertTrue("-d".equals(names[0]));
        assertTrue("-f".equals(names[1]));
        assertTrue("-c".equals(names[2]));

        assertTrue("this is params ".equals(param.getOption("-s")));
        assertTrue("-d 20170102 -f /home/user/shell/qyzx/m_file/test.del -c true -p /home/user/shell/qyzx/m_file/txt.del -dd -u -s 'this is params ' parameter1 -key yusdf".equals(param.toString()));

        assertTrue("yusdf".equals(param.removeOption("-key")));
        assertTrue("-d 20170102 -f /home/user/shell/qyzx/m_file/test.del -c true -p /home/user/shell/qyzx/m_file/txt.del -dd -u -s 'this is params ' parameter1".equals(param.toString()));

        ArgumentExpression clone = new ArgumentExpression();
        clone.addOption("-x", "11");
        clone.addOption(param);
        assertTrue("-x 11 -d 20170102 -f /home/user/shell/qyzx/m_file/test.del -c true -p /home/user/shell/qyzx/m_file/txt.del -dd -u -s 'this is params ' parameter1".equals(clone.toString()));
    }

}
