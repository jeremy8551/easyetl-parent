package icu.etl.script;

import java.util.HashMap;
import java.util.Map;

import icu.etl.script.compiler.ScriptAnalysis;
import icu.etl.script.session.ScriptMainProcess;
import icu.etl.script.session.UniversalScriptSessionFactoryImpl;
import icu.etl.script.session.UniversalScriptSessionImpl;
import icu.etl.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScriptAnalysisTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        assertEquals(0, new ScriptAnalysis().indexOf("abc", "aBc", 0, 0, 0));
        assertEquals(-1, new ScriptAnalysis().indexOf("abc", "Bc", 0, 1, 0));
        assertEquals(-1, new ScriptAnalysis().indexOf(" bcd", "Bc", 0, 1, 1));
        assertEquals(1, new ScriptAnalysis().indexOf(" bc ", "Bc", 0, 1, 1));
        assertEquals(1, new ScriptAnalysis().indexOf("*bc<", "Bc", 0, 1, 1));
    }

    @Test
    public void test1() {
        assertEquals("abc", new ScriptAnalysis().trim("abc", 0, 0));
        assertEquals("abc", new ScriptAnalysis().trim(" abc ", 0, 0));
        assertEquals("abc", new ScriptAnalysis().trim("abc   ", 0, 0));
        assertEquals("abc", new ScriptAnalysis().trim("abc ; ", 0, 1));
        assertEquals("abc", new ScriptAnalysis().trim(" ; ; abc ; ", 1, 1));
    }

    @Test
    public void test11() {
        assertFalse(new ScriptAnalysis().containsQuotation(""));
        assertTrue(new ScriptAnalysis().containsQuotation("''"));
        assertTrue(new ScriptAnalysis().containsQuotation(" ' ' "));
        assertTrue(new ScriptAnalysis().containsQuotation(" '' "));
        assertTrue(new ScriptAnalysis().containsQuotation(" 'a' "));
        assertFalse(new ScriptAnalysis().containsQuotation(" 'a' \"b\" "));
        assertFalse(new ScriptAnalysis().containsQuotation(" 'a' '' "));
        assertFalse(new ScriptAnalysis().containsQuotation(" 'a' 'b' "));
        assertTrue(new ScriptAnalysis().containsQuotation("\" \""));
        assertFalse(new ScriptAnalysis().containsQuotation("\" \"\"\""));
        assertTrue(new ScriptAnalysis().containsQuotation("\"''\""));
    }

    @Test
    public void test1123() {
        assertFalse(new ScriptAnalysis().containsSide("", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide("''", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide(" ' ' ", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide(" '' ", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide(" 'a' ", '\'', '\''));
        assertFalse(new ScriptAnalysis().containsSide(" 'a' \"b\" ", '\'', '\''));
        assertFalse(new ScriptAnalysis().containsSide(" 'a' '' ", '\'', '\''));
        assertFalse(new ScriptAnalysis().containsSide(" 'a' 'b' ", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide("\" \"", '\"', '\"'));
        assertFalse(new ScriptAnalysis().containsSide("\" \"\"\"", '\"', '\"'));
        assertTrue(new ScriptAnalysis().containsSide("\"''\"", '\"', '\"'));
    }

    @Test
    public void test112() {
        assertEquals("", new ScriptAnalysis().unQuotation(""));
        assertEquals("", new ScriptAnalysis().unQuotation("''"));
        assertEquals(" ", new ScriptAnalysis().unQuotation(" ' ' "));
        assertEquals("", new ScriptAnalysis().unQuotation(" '' "));
        assertEquals("a", new ScriptAnalysis().unQuotation(" 'a' "));
        assertEquals(" 'a' \"b\" ", new ScriptAnalysis().unQuotation(" 'a' \"b\" "));
        assertEquals(" 'a' '' ", new ScriptAnalysis().unQuotation(" 'a' '' "));
        assertEquals(" 'a' 'b' ", new ScriptAnalysis().unQuotation(" 'a' 'b' "));
        assertEquals(" ", new ScriptAnalysis().unQuotation("\" \""));
        assertEquals("\" \"\"\"", new ScriptAnalysis().unQuotation("\" \"\"\""));
        assertEquals("''", new ScriptAnalysis().unQuotation("\"''\""));
        assertEquals(" '' ", new ScriptAnalysis().unQuotation("\" '' \""));
    }

    @Test
    public void test11267() {
        assertEquals("", new ScriptAnalysis().removeSide("", '\'', '\''));
        assertEquals("", new ScriptAnalysis().removeSide("''", '\'', '\''));
        assertEquals(" ", new ScriptAnalysis().removeSide(" ' ' ", '\'', '\''));
        assertEquals("", new ScriptAnalysis().removeSide(" '' ", '\'', '\''));
        assertEquals("a", new ScriptAnalysis().removeSide(" 'a' ", '\'', '\''));
        assertEquals(" 'a' \"b\" ", new ScriptAnalysis().removeSide(" 'a' \"b\" ", '\'', '\''));
        assertEquals(" 'a' '' ", new ScriptAnalysis().removeSide(" 'a' '' ", '\'', '\''));
        assertEquals(" 'a' 'b' ", new ScriptAnalysis().removeSide(" 'a' 'b' ", '\'', '\''));
        assertEquals(" ", new ScriptAnalysis().removeSide("\" \"", '"', '"'));
        assertEquals("\" \"\"\"", new ScriptAnalysis().removeSide("\" \"\"\"", '"', '"'));
        assertEquals("''", new ScriptAnalysis().removeSide("\"''\"", '"', '"'));
        assertEquals(" '' ", new ScriptAnalysis().removeSide("\" '' \"", '"', '"'));
    }

    @Test
    public void testReplaceShellFunctionVariable() {
        ScriptAnalysis obj = new ScriptAnalysis();

        UniversalScriptSession session = new UniversalScriptSessionImpl("engine001", new UniversalScriptSessionFactoryImpl()) {

            @Override
            public String getId() {
                return "sessionid";
            }

            @Override
            public ScriptMainProcess getMainProcess() {
                return new ScriptMainProcess() {

                    @Override
                    public Integer getExitcode() {
                        return -1;
                    }

                };
            }

            @Override
            public String[] getFunctionParameter() {
                return new String[]{"test", "1", "2", "3"};
            }
        };
        String str = "$1 is equals $2 [$3$ $4 $d";
        assertEquals("1 is equals 2 [3$ $4 $d", obj.replaceShellSpecialVariable(session, str, true));

        session = new UniversalScriptSessionImpl("engine001", new UniversalScriptSessionFactoryImpl()) {
            @Override
            public String getId() {
                return "sessionid";
            }

            @Override
            public ScriptMainProcess getMainProcess() {
                return new ScriptMainProcess() {

                    @Override
                    public Integer getExitcode() {
                        return -1;
                    }

                };
            }

            @Override
            public String[] getFunctionParameter() {
                String[] args1 = new String[]{"funcs", "PROC_QYZX_SBC_BAOHANS"};
                return args1;
            }
        };
        assertEquals("call TESTADM.PROC_QYZX_SBC_BAOHANS('2017-07-31', ?); 1 funcs", obj.replaceShellSpecialVariable(session, "call TESTADM.$1('2017-07-31', ?); $# $0", false));

        session = new UniversalScriptSessionImpl("engine001", new UniversalScriptSessionFactoryImpl()) {

            @Override
            public String getId() {
                return "sessionid";
            }

            @Override
            public ScriptMainProcess getMainProcess() {
                return new ScriptMainProcess() {

                    @Override
                    public Integer getExitcode() {
                        return -1;
                    }

                };
            }

            @Override
            public String[] getFunctionParameter() {
                return new String[]{"funcs", "PROC_QYZX_SBC_BAOHANS"};
            }
        };
        assertEquals("call TESTADM.PROC_QYZX_SBC_BAOHANS('2017-07-31', ?); 1 funcs -1", obj.replaceShellSpecialVariable(session, "call TESTADM.$1('2017-07-31', ?); $# $0 $?", true));

        session = new UniversalScriptSessionImpl("engine001", new UniversalScriptSessionFactoryImpl()) {

            @Override
            public String getId() {
                return "sessionid";
            }

            @Override
            public ScriptMainProcess getMainProcess() {
                return new ScriptMainProcess() {

                    @Override
                    public Integer getExitcode() {
                        return -1;
                    }

                };
            }

            @Override
            public String[] getFunctionParameter() {
                return new String[]{"funcs", "PROC_QYZX_SBC_BAOHANS"};
            }
        };
        assertEquals("call 'TESTADM.$1(2017-07-31, ?)'; 1 funcs -1", obj.replaceShellSpecialVariable(session, "call 'TESTADM.$1(2017-07-31, ?)'; $# $0 $?", true));

        session = new UniversalScriptSessionImpl("engine001", new UniversalScriptSessionFactoryImpl()) {

            @Override
            public String getId() {
                return "sessionid";
            }

            @Override
            public ScriptMainProcess getMainProcess() {
                return new ScriptMainProcess() {

                    @Override
                    public Integer getExitcode() {
                        return -1;
                    }
                };
            }

            @Override
            public String[] getFunctionParameter() {
                return StringUtils.splitByBlank("test 1 2 3 4 5 6 7 8 9 10 11");
            }
        };
        assertEquals("11 -1 test 1 2 11 10", obj.replaceShellSpecialVariable(session, "$# $? $0 $1 $2 $11 $10", true));
    }

    @Test
    public void testreplaceShellVariable() {
        ScriptAnalysis obj = new ScriptAnalysis();
        Map<String, Object> b = new HashMap<String, Object>();
        b.put("name", "2");
        b.put("key", "51");
        b.put("jdbc", "C:\\Users\\etl\\rpt\\lib\\jdbc.properties");

        assertEquals("", obj.replaceShellVariable("", b, null, true, true));
        assertEquals("${name1}", obj.replaceShellVariable("${name1}", b, null, true, true));
        assertEquals("123", obj.replaceShellVariable("123", b, null, true, true));
        assertEquals("2", obj.replaceShellVariable("${name}", b, null, true, true));
        assertEquals("51", obj.replaceShellVariable("${key}", b, null, true, true));
        assertEquals("123", obj.replaceShellVariable("1${name}3", b, null, true, true));
        assertEquals("C:\\Users\\etl\\rpt\\lib\\jdbc.properties", obj.replaceShellVariable("${jdbc}", b, null, true, true));

        assertEquals("12512", obj.replaceShellVariable("1${name}${key}${name}", b, null, true, true));

        b.put("current_table_columns_msg", "");
        b.put("tmp_colname", "reqID");
        assertEquals("reqID", obj.replaceShellVariable("${current_table_columns_msg}${tmp_colname}", b, null, true, true));
        assertEquals(" reqID", obj.replaceShellVariable("${current_table_columns_msg} ${tmp_colname}", b, null, true, true));

        assertEquals("2", obj.replaceShellVariable("$name", b, null, true, true));
        assertEquals("2+51", obj.replaceShellVariable("$name+${key}", b, null, true, true));
        assertEquals("251", obj.replaceShellVariable("$name${key}", b, null, true, true));
        assertEquals("251", obj.replaceShellVariable("$name$key", b, null, true, true));
        assertEquals("$n", obj.replaceShellVariable("$n", b, null, true, true));
        assertEquals("${n}", obj.replaceShellVariable("${n}", b, null, true, true));
        assertEquals("${n}$n", obj.replaceShellVariable("${n}$n", b, null, true, true));
        assertEquals("${n}$n ", obj.replaceShellVariable("${n}$n ", b, null, true, true));
        assertEquals("$n ", obj.replaceShellVariable("${current_table_columns_msg}$n ", b, null, true, true));
        assertEquals("$n ", obj.replaceShellVariable("$current_table_columns_msg$n ", b, null, true, true));
        assertEquals("$$n ", obj.replaceShellVariable("$$current_table_columns_msg$n ", b, null, true, true));

        assertEquals("'$$current_table_columns_msg'$n ", obj.replaceShellVariable("'$$current_table_columns_msg'$n ", b, null, true, true));
        assertEquals("'$current_table_columns_msg ' ", obj.replaceShellVariable("'$current_table_columns_msg '$current_table_columns_msg ", b, null, true, true));
        assertEquals("'$$current_table_columns_msg'reqID ", obj.replaceShellVariable("'$$current_table_columns_msg'$tmp_colname ", b, null, true, true));

        b.clear();
        assertEquals("", obj.replaceShellVariable("$test", b, null, true, false));
        assertEquals(" ", obj.replaceShellVariable("$test ", b, null, true, false));
        assertEquals(" 2", obj.replaceShellVariable("$test 2", b, null, true, false));
        assertEquals("'$test' 2", obj.replaceShellVariable("'$test'${t} 2", b, null, true, false));
        assertEquals("'$test'", obj.replaceShellVariable("'$test'${t}", b, null, true, false));
        assertEquals("\"\"", obj.replaceShellVariable("\"$test\"${t}", b, null, true, false));
        assertEquals("\"\"1", obj.replaceShellVariable("\"$test\"${t}1", b, null, true, false));
        assertEquals("\"\"", obj.replaceShellVariable("\"$test\"$t", b, null, true, false));
    }

}
