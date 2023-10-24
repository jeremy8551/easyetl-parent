package icu.etl.script.compiler;

import java.util.HashMap;
import java.util.Map;

import icu.etl.script.UniversalScriptSession;
import icu.etl.script.session.ScriptMainProcess;
import icu.etl.script.session.ScriptSession;
import icu.etl.script.session.ScriptSessionFactory;
import icu.etl.util.StringUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class StandardScriptAnalysisTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void test() {
        assertTrue(new ScriptAnalysis().indexOf("abc", "aBc", 0, 0, 0) == 0);
        assertTrue(new ScriptAnalysis().indexOf("abc", "Bc", 0, 1, 0) == -1);
        assertTrue(new ScriptAnalysis().indexOf(" bcd", "Bc", 0, 1, 1) == -1);
        assertTrue(new ScriptAnalysis().indexOf(" bc ", "Bc", 0, 1, 1) == 1);
        assertTrue(new ScriptAnalysis().indexOf("*bc<", "Bc", 0, 1, 1) == 1);
    }

    @Test
    public void test1() {
        assertTrue(new ScriptAnalysis().trim("abc", 0, 0).equals("abc"));
        assertTrue(new ScriptAnalysis().trim(" abc ", 0, 0).equals("abc"));
        assertTrue(new ScriptAnalysis().trim("abc   ", 0, 0).equals("abc"));
        assertTrue(new ScriptAnalysis().trim("abc ; ", 0, 1).equals("abc"));
        assertTrue(new ScriptAnalysis().trim(" ; ; abc ; ", 1, 1).equals("abc"));
    }

    @Test
    public void test11() {
        assertTrue(!new ScriptAnalysis().containsQuotation(""));
        assertTrue(new ScriptAnalysis().containsQuotation("''"));
        assertTrue(new ScriptAnalysis().containsQuotation(" ' ' "));
        assertTrue(new ScriptAnalysis().containsQuotation(" '' "));
        assertTrue(new ScriptAnalysis().containsQuotation(" 'a' "));
        assertTrue(!new ScriptAnalysis().containsQuotation(" 'a' \"b\" "));
        assertTrue(!new ScriptAnalysis().containsQuotation(" 'a' '' "));
        assertTrue(!new ScriptAnalysis().containsQuotation(" 'a' 'b' "));
        assertTrue(new ScriptAnalysis().containsQuotation("\" \""));
        assertTrue(!new ScriptAnalysis().containsQuotation("\" \"\"\""));
        assertTrue(new ScriptAnalysis().containsQuotation("\"''\""));
    }

    @Test
    public void test1123() {
        assertTrue(!new ScriptAnalysis().containsSide("", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide("''", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide(" ' ' ", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide(" '' ", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide(" 'a' ", '\'', '\''));
        assertTrue(!new ScriptAnalysis().containsSide(" 'a' \"b\" ", '\'', '\''));
        assertTrue(!new ScriptAnalysis().containsSide(" 'a' '' ", '\'', '\''));
        assertTrue(!new ScriptAnalysis().containsSide(" 'a' 'b' ", '\'', '\''));
        assertTrue(new ScriptAnalysis().containsSide("\" \"", '\"', '\"'));
        assertTrue(!new ScriptAnalysis().containsSide("\" \"\"\"", '\"', '\"'));
        assertTrue(new ScriptAnalysis().containsSide("\"''\"", '\"', '\"'));
    }

    @Test
    public void test112() {
        assertTrue(new ScriptAnalysis().unQuotation("").equals(""));
        assertTrue(new ScriptAnalysis().unQuotation("''").equals(""));
        assertTrue(new ScriptAnalysis().unQuotation(" ' ' ").equals(" "));
        assertTrue(new ScriptAnalysis().unQuotation(" '' ").equals(""));
        assertTrue(new ScriptAnalysis().unQuotation(" 'a' ").equals("a"));
        assertTrue(new ScriptAnalysis().unQuotation(" 'a' \"b\" ").equals(" 'a' \"b\" "));
        assertTrue(new ScriptAnalysis().unQuotation(" 'a' '' ").equals(" 'a' '' "));
        assertTrue(new ScriptAnalysis().unQuotation(" 'a' 'b' ").equals(" 'a' 'b' "));
        assertTrue(new ScriptAnalysis().unQuotation("\" \"").equals(" "));
        assertTrue(new ScriptAnalysis().unQuotation("\" \"\"\"").equals("\" \"\"\""));
        assertTrue(new ScriptAnalysis().unQuotation("\"''\"").equals("''"));
        assertTrue(new ScriptAnalysis().unQuotation("\" '' \"").equals(" '' "));
    }

    @Test
    public void test11267() {
        assertTrue(new ScriptAnalysis().removeSide("", '\'', '\'').equals(""));
        assertTrue(new ScriptAnalysis().removeSide("''", '\'', '\'').equals(""));
        assertTrue(new ScriptAnalysis().removeSide(" ' ' ", '\'', '\'').equals(" "));
        assertTrue(new ScriptAnalysis().removeSide(" '' ", '\'', '\'').equals(""));
        assertTrue(new ScriptAnalysis().removeSide(" 'a' ", '\'', '\'').equals("a"));
        assertTrue(new ScriptAnalysis().removeSide(" 'a' \"b\" ", '\'', '\'').equals(" 'a' \"b\" "));
        assertTrue(new ScriptAnalysis().removeSide(" 'a' '' ", '\'', '\'').equals(" 'a' '' "));
        assertTrue(new ScriptAnalysis().removeSide(" 'a' 'b' ", '\'', '\'').equals(" 'a' 'b' "));
        assertTrue(new ScriptAnalysis().removeSide("\" \"", '"', '"').equals(" "));
        assertTrue(new ScriptAnalysis().removeSide("\" \"\"\"", '"', '"').equals("\" \"\"\""));
        assertTrue(new ScriptAnalysis().removeSide("\"''\"", '"', '"').equals("''"));
        assertTrue(new ScriptAnalysis().removeSide("\" '' \"", '"', '"').equals(" '' "));
    }

    @Test
    public void testReplaceShellFunctionVariable() {
        ScriptAnalysis obj = new ScriptAnalysis();

        UniversalScriptSession session = new ScriptSession(new ScriptSessionFactory()) {

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
                String[] args1 = new String[]{"test", "1", "2", "3"};
                return args1;
            }
        };
        String str = "$1 is equals $2 [$3$ $4 $d";
        assertTrue(obj.replaceShellSpecialVariable(session, str, true).equals("1 is equals 2 [3$ $4 $d"));

        session = new ScriptSession(new ScriptSessionFactory()) {
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
        assertTrue(obj.replaceShellSpecialVariable(session, "call TESTADM.$1('2017-07-31', ?); $# $0", false).equals("call TESTADM.PROC_QYZX_SBC_BAOHANS('2017-07-31', ?); 1 funcs"));

        session = new ScriptSession(new ScriptSessionFactory()) {

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
        assertTrue(obj.replaceShellSpecialVariable(session, "call TESTADM.$1('2017-07-31', ?); $# $0 $?", true).equals("call TESTADM.PROC_QYZX_SBC_BAOHANS('2017-07-31', ?); 1 funcs -1"));

        session = new ScriptSession(new ScriptSessionFactory()) {

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
        assertTrue(obj.replaceShellSpecialVariable(session, "call 'TESTADM.$1(2017-07-31, ?)'; $# $0 $?", true).equals("call 'TESTADM.$1(2017-07-31, ?)'; 1 funcs -1"));

        session = new ScriptSession(new ScriptSessionFactory()) {

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
                String[] args1 = StringUtils.splitByBlank("test 1 2 3 4 5 6 7 8 9 10 11");
                return args1;
            }
        };
        assertTrue(obj.replaceShellSpecialVariable(session, "$# $? $0 $1 $2 $11 $10", true).equals("11 -1 test 1 2 11 10"));
    }

    @Test
    public void testreplaceShellVariable() {
        ScriptAnalysis obj = new ScriptAnalysis();
        Map<String, Object> b = new HashMap<String, Object>();
        b.put("name", "2");
        b.put("key", "51");
        b.put("jdbc", "C:\\Users\\etl\\rpt\\lib\\jdbc.properties");

        assertTrue("".equals(obj.replaceShellVariable("", b, null, true, true)));
        assertTrue("${name1}".equals(obj.replaceShellVariable("${name1}", b, null, true, true)));
        assertTrue("123".equals(obj.replaceShellVariable("123", b, null, true, true)));
        assertTrue("2".equals(obj.replaceShellVariable("${name}", b, null, true, true)));
        assertTrue("51".equals(obj.replaceShellVariable("${key}", b, null, true, true)));
        assertTrue("123".equals(obj.replaceShellVariable("1${name}3", b, null, true, true)));
        assertTrue("C:\\Users\\etl\\rpt\\lib\\jdbc.properties".equals(obj.replaceShellVariable("${jdbc}", b, null, true, true)));

        assertTrue("12512".equals(obj.replaceShellVariable("1${name}${key}${name}", b, null, true, true)));

        b.put("current_table_columns_msg", "");
        b.put("tmp_colname", "reqID");
        assertTrue("reqID".equals(obj.replaceShellVariable("${current_table_columns_msg}${tmp_colname}", b, null, true, true)));
        assertTrue(" reqID".equals(obj.replaceShellVariable("${current_table_columns_msg} ${tmp_colname}", b, null, true, true)));

        assertTrue("2".equals(obj.replaceShellVariable("$name", b, null, true, true)));
        assertTrue("2+51".equals(obj.replaceShellVariable("$name+${key}", b, null, true, true)));
        assertTrue("251".equals(obj.replaceShellVariable("$name${key}", b, null, true, true)));
        assertTrue("251".equals(obj.replaceShellVariable("$name$key", b, null, true, true)));
        assertTrue("$n".equals(obj.replaceShellVariable("$n", b, null, true, true)));
        assertTrue("${n}".equals(obj.replaceShellVariable("${n}", b, null, true, true)));
        assertTrue("${n}$n".equals(obj.replaceShellVariable("${n}$n", b, null, true, true)));
        assertTrue("${n}$n ".equals(obj.replaceShellVariable("${n}$n ", b, null, true, true)));
        assertTrue("$n ".equals(obj.replaceShellVariable("${current_table_columns_msg}$n ", b, null, true, true)));
        assertTrue("$n ".equals(obj.replaceShellVariable("$current_table_columns_msg$n ", b, null, true, true)));
        assertTrue("$$n ".equals(obj.replaceShellVariable("$$current_table_columns_msg$n ", b, null, true, true)));

        assertTrue("'$$current_table_columns_msg'$n ".equals(obj.replaceShellVariable("'$$current_table_columns_msg'$n ", b, null, true, true)));
        assertTrue("'$current_table_columns_msg ' ".equals(obj.replaceShellVariable("'$current_table_columns_msg '$current_table_columns_msg ", b, null, true, true)));
        assertTrue("'$$current_table_columns_msg'reqID ".equals(obj.replaceShellVariable("'$$current_table_columns_msg'$tmp_colname ", b, null, true, true)));

        b.clear();
        assertTrue("".equals(obj.replaceShellVariable("$test", b, null, true, false)));
        assertTrue(" ".equals(obj.replaceShellVariable("$test ", b, null, true, false)));
        assertTrue(" 2".equals(obj.replaceShellVariable("$test 2", b, null, true, false)));
        assertTrue("'$test' 2".equals(obj.replaceShellVariable("'$test'${t} 2", b, null, true, false)));
        assertTrue("'$test'".equals(obj.replaceShellVariable("'$test'${t}", b, null, true, false)));
        assertTrue("\"\"".equals(obj.replaceShellVariable("\"$test\"${t}", b, null, true, false)));
        assertTrue("\"\"1".equals(obj.replaceShellVariable("\"$test\"${t}1", b, null, true, false)));
        assertTrue("\"\"".equals(obj.replaceShellVariable("\"$test\"$t", b, null, true, false)));

    }

}
