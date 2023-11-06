package icu.etl.database;

import java.sql.Timestamp;
import java.util.Date;

import icu.etl.database.db2.DB2ExportFile;
import icu.etl.database.internal.StandardDatabaseProcedure;
import icu.etl.database.oracle.OracleDialect;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Dates;
import icu.etl.util.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JdbcTest {

    @Test
    public void testremoveSchema() {
        assertEquals(Jdbc.removeSchema(""), "");
        assertEquals(Jdbc.removeSchema("table"), "table");
        assertEquals(Jdbc.removeSchema(".table"), "table");
        assertEquals(Jdbc.removeSchema("1.table"), "table");
        assertEquals(Jdbc.removeSchema("schema.table"), "table");
        assertEquals(Jdbc.removeSchema("schema.sdsf.table"), "table");
    }

    @Test
    public void testgetSchema() {
        assertEquals(Jdbc.getSchema(""), null);
        assertEquals(Jdbc.getSchema("table"), null);
        assertEquals(Jdbc.getSchema(".table"), null);
        assertEquals(Jdbc.getSchema("1.table"), "1");
        assertEquals(Jdbc.getSchema("schema.table"), "schema");
        assertEquals(Jdbc.getSchema("schema.sdsf.table"), "schema");
    }

    @Test
    public void testQuote() {
        assertTrue(StringUtils.quote(null) == null);
        assertTrue(StringUtils.quote("").equals("''"));
        assertTrue(StringUtils.quote(" ").equals("' '"));
    }



    @Test
    public void test3() {
//		assertTrue("abcABC一二三四壹 ".toUpperCase(Locale.CHINESE));
        assertTrue(StringUtils.toCase("abcABC", false, null).equals("ABCABC"));
        assertTrue(StringUtils.toString(StringUtils.toCase(ArrayUtils.asList("a", "b", "c"), false, null)).equals("ArrayList[A, B, C]"));
        assertTrue(StringUtils.toString(StringUtils.toCase(new String[]{"a", "b", "cd"}, false, null)).equals("String[A, B, CD]"));
        assertTrue(StringUtils.toString(new Integer[]{0, 1, 2}).equals("Integer[0, 1, 2]"));
        assertTrue(StringUtils.toString(new int[]{1, 2, 3}).equals("int[1, 2, 3]"));
        assertTrue(StringUtils.toString(StringUtils.toCase(new char[]{'a', 'b', 'c', '1'}, false, null)).equals("char[A, B, C, 1]"));
    }

    @Test
    public void test4() {
        Date date = new Date();
        String time = StringUtils.right(Dates.format21(date), 3); // 毫秒数
        String str = StringUtils.left(Dates.format21(date), 20);
        String str1 = DB2ExportFile.toDB2ExportString(new Timestamp(date.getTime()));
        System.out.println("测试db2数据库中数据日期是否正确");
        System.out.println(str1);
        System.out.println(str + "000" + time);
        String result = str + "000" + time;
        assertTrue(str1.equals(result.replace(':', '.')));
    }

    @Test
    public void test6() {
        String[][] array1 = OracleDialect.resolveDatabaseProcedureParam("procedure name(v1 number(12, 2), dt in varchar2char(100), r  out  int, t char, d in decimal(10,2) )");
        assertTrue(StringUtils.toString(array1[0]).equals("String[V1, IN, NUMBER(12, 2)]"));
        assertTrue(StringUtils.toString(array1[1]).equals("String[DT, IN, VARCHAR2CHAR(100)]"));
        assertTrue(StringUtils.toString(array1[4]).equals("String[D, IN, DECIMAL(10,2)]"));

        String[] array2 = StandardDatabaseProcedure.resolveDatabaseProcedureDDLName("CREATE OR REPLACE PROCEDURE \"LHBB\".\"CUSTAUM_APPEND\" (dt in varchar2)--yyyymmdd \n as");
        assertTrue(StringUtils.toString(array2).equals("String[\"LHBB\", \"CUSTAUM_APPEND\"]"));
    }

}
