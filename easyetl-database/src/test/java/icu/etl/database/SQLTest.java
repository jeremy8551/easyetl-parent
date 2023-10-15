package icu.etl.database;

import java.util.ArrayList;
import java.util.List;

import icu.etl.database.SQL;
import icu.etl.util.ArrayUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/2
 */
public class SQLTest {

    @Test
    public void testIndexSqlBrackedEnd() {
        Assert.assertTrue(SQL.indexOfParenthes("0123456789", 0) == -1);
        Assert.assertTrue(SQL.indexOfParenthes("0123456789", 9) == -1);
        Assert.assertTrue(SQL.indexOfParenthes("012345678)", 0) == 9);
        Assert.assertTrue(SQL.indexOfParenthes("01(3)5()8)", 0) == 9);
        Assert.assertTrue(SQL.indexOfParenthes("0((3))()8)", 0) == 9);
    }

    @Test
    public void testIndexSqlBraceEnd() {
        Assert.assertTrue(SQL.indexOfBrace("0123456789", 0) == -1);
        Assert.assertTrue(SQL.indexOfBrace("0}23456789", 0) == 1);
        Assert.assertTrue(SQL.indexOfBrace("012345678}", 0) == 9);
        Assert.assertTrue(SQL.indexOfBrace("0{2}4{67}}", 0) == 9);
        Assert.assertTrue(SQL.indexOfBrace("0{2}4{{}}}", 0) == 9);
    }

    @Test
    public void testIndexSqlEndOfAnnotation() {
        assertTrue(SQL.indexOfAnnotation("--", 0) == 1);
        assertTrue(SQL.indexOfAnnotation("--2", 0) == 2);
        assertTrue(SQL.indexOfAnnotation("--\r", 0) == 2);
        assertTrue(SQL.indexOfAnnotation("--\n", 0) == 2);
        assertTrue(SQL.indexOfAnnotation("--\r\n", 0) == 3);
        assertTrue(SQL.indexOfAnnotation("--\r\n4", 0) == 3);
        assertTrue(SQL.indexOfAnnotation("--234\n4", 0) == 5);
        assertTrue(SQL.indexOfAnnotation("0--34\n4", 1) == 5);
        assertTrue(SQL.indexOfAnnotation("01--45\n7", 2) == 6);
        assertTrue(SQL.indexOfAnnotation("/**/", 0) == 3);
        assertTrue(SQL.indexOfAnnotation("/*2*/", 0) == 4);
        assertTrue(SQL.indexOfAnnotation("/*2*/5", 0) == 4);
        assertTrue(SQL.indexOfAnnotation("/*234*/7", 0) == 6);
        assertTrue(SQL.indexOfAnnotation("/*2\n4\r\n78\n0*/7", 0) == 12);
        assertTrue(SQL.indexOfAnnotation("0/*2\n4\r\n78\n0*/7", 1) == 13);
        assertTrue(SQL.indexOfAnnotation("01/*2\n4\r\n78\n0*/7", 2) == 14);
    }

    @Test
    public void testIndexSqlIgnoreTextOrAnnotation() {
        assertTrue(SQL.indexOf("", "proc", 0, true) == -1);
        assertTrue(SQL.indexOf("a", "a", 0, false) == 0);
        assertTrue(SQL.indexOf("a", "A", 0, false) == -1);
        assertTrue(SQL.indexOf("a", "A", 0, true) == 0);
        assertTrue(SQL.indexOf("aB", "Ab", 0, true) == 0);
        assertTrue(SQL.indexOf("AB", "Ab", 0, true) == 0);
        assertTrue(SQL.indexOf("proc", "proc", 0, false) == 0);
        Assert.assertEquals(SQL.indexOf("0123456789012345proc", "proc", 0, true), 16);
        assertTrue(SQL.indexOf("012345678901234.proc", "proc", 0, true) == 16);
        assertTrue(SQL.indexOf("--\n345678901234.proc", "proc", 0, true) == 16);
        assertTrue(SQL.indexOf("--\n34--78\r\n1234.proc", "proc", 0, true) == 16);
        assertTrue(SQL.indexOf("--\n34--78\r\n1234'proc','asdf.proc'd.proc", "proc", 0, true) == 35);
        assertTrue(SQL.indexOf("--\n34--78\r\n1234'proc','asdf.proc'd./*123*/12.proc", "proc", 0, true) == 45);
        assertTrue(SQL.indexOf("--\n3/*6\n8\n0*/34.proc 123456789", "proc", 0, true) == 16);
    }

    @Test
    public void testToDatabaseIdentifier() {
        assertTrue(SQL.toIdentifier(null) == null);
        Assert.assertEquals("", SQL.toIdentifier("\"\""));
        Assert.assertEquals("A", SQL.toIdentifier(" \"a\" " + StringUtils.FULLWIDTH_BLANK));
    }

    @Test
    public void testSplitSqlByBlank() {
        String str = "dt in varchar2char(100), v2 in  number(12,  2), r  out  int, a out number( 10,2), t char, d in decimal(10,2),    ' ,  ', m in ('   '||a.loanid),   1'   ', ";
        String[] array = SQL.split(str, ',');
        Assert.assertTrue(StringUtils.join(array, " ").equals("dt in varchar2char(100)  v2 in  number(12,  2)  r  out  int  a out number( 10,2)  t char  d in decimal(10,2)     ' ,  '  m in ('   '||a.loanid)    1'   '  "));

        String[] b = SQL.splitByBlank(str);
        Assert.assertTrue(StringUtils.join(b, " ").equals("dt in varchar2char(100), v2 in number(12,  2), r out int, a out number( 10,2), t char, d in decimal(10,2), ' ,  ', m in ('   '||a.loanid), 1'   ', "));

        String[] b1 = SQL.splitByBlank("'1 ; 2 ' 2;3 4", ';');
//		System.out.println(StringUtils.toString(b1));
        Assert.assertTrue(b1.length == 4 && b1[0].equals("'1 ; 2 '") && b1[1].equals("2") && b1[2].equals("3") && b1[3].equals("4"));
    }

    @Test
    public void testSplitSqlStringChar() {
        String sql = "";
        sql += "select a.name, b.id ,c id_type from table as a, table as b, table as c ";
        sql += " where a.name = b.name ";
        sql += " and a.id = c.id  ";
        sql += " with ur";

        String[] array = SQL.split(sql, ',');
        Assert.assertEquals("select a.name", array[0]);
        Assert.assertEquals(" b.id ", array[1]);
        Assert.assertEquals("c id_type from table as a", array[2]);
        Assert.assertEquals(" table as b", array[3]);
        Assert.assertEquals(" table as c  where a.name = b.name  and a.id = c.id   with ur", array[4]);
    }

    @Test
    public void testSplitSqlStringCharListOfString() {
        String sql = "";
        sql += "select a.name, b.id ,c id_type from table as a, table as b, table as c ";
        sql += " where a.name = b.name ";
        sql += " and a.id = c.id  ";
        sql += " with ur";

        List<String> list = new ArrayList<String>();
        SQL.split(sql, ',', list);
        Assert.assertEquals("select a.name", list.get(0));
        Assert.assertEquals(" b.id ", list.get(1));
        Assert.assertEquals("c id_type from table as a", list.get(2));
        Assert.assertEquals(" table as b", list.get(3));
        Assert.assertEquals(" table as c  where a.name = b.name  and a.id = c.id   with ur", list.get(4));
    }

    @Test
    public void testSplitSqlStringCollectionOfStringBoolean() {
        String sql = "O_ALS_CUSTOMER_RELATIVE_QYZX D      ON C.CUSTID = D.CustomerID    AND D.RELATIONSHIP in ('0100', '0102', '0103')";
        String[] array = SQL.split(sql, ArrayUtils.asList("on"), true);
        Assert.assertTrue(StringUtils.toString(array).equals("String[O_ALS_CUSTOMER_RELATIVE_QYZX D      ,  C.CUSTID = D.CustomerID    AND D.RELATIONSHIP in ('0100', '0102', '0103')]"));

//		Assert.assertEquals("select", array[0]);
//		Assert.assertEquals("select", array[0]);
    }

    @Test
    public void testSplitSqlStringCollectionOfStringCollectionOfStringBoolean() {
        List<String> list = new ArrayList<String>();
        SQL.split("O_ALS_CUSTOMER_RELATIVE_QYZX D      ON C.CUSTID = D.CustomerID    AND D.RELATIONSHIP in ('0100', '0102', '0103')", ArrayUtils.asList("on"), true, list);
//		System.out.println(StringUtils.toString(list));
        Assert.assertTrue(StringUtils.toString(list).equals("ArrayList[O_ALS_CUSTOMER_RELATIVE_QYZX D      ,  C.CUSTID = D.CustomerID    AND D.RELATIONSHIP in ('0100', '0102', '0103')]"));
    }

    @Test
    public void testSplitSqlByUnion() {
        String sql = "";
        sql += "    select CONTRACT_NO from ECC_ENSURECONTRACTS_I a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )" + FileUtils.lineSeparator;
        sql += " UNION ALL" + FileUtils.lineSeparator;
        sql += "    select CONTRACT_NO from ECC_IMPAWNCONTRACT_I  a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )" + FileUtils.lineSeparator;
        sql += " UNION" + FileUtils.lineSeparator;
        sql += "    select CONTRACT_NO from ECC_PLEDGECONTRACTS_I a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )" + FileUtils.lineSeparator;
        sql += " UNION ALL   " + FileUtils.lineSeparator;
        sql += "    select CONTRACT_NO from ECC_ENSURECONTRACTS_BP_I a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )" + FileUtils.lineSeparator;
        sql += " UNION ALL" + FileUtils.lineSeparator;
        sql += "    select CONTRACT_NO from ECC_IMPAWNCONTRACT_BP_I  a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )" + FileUtils.lineSeparator;
        sql += " UNION" + FileUtils.lineSeparator;
        sql += "    select all CONTRACT_NO from ECC_PLEDGECONTRACTS_BP_I a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )" + FileUtils.lineSeparator;

        // System.out.println(sql);
        List<String> list = new ArrayList<String>();
        List<String> deimiters = new ArrayList<String>();
        SQL.splitByUnion(sql, deimiters, list);

        Assert.assertTrue(list.get(0).trim().equals("select CONTRACT_NO from ECC_ENSURECONTRACTS_I a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )"));
        Assert.assertTrue(list.get(1).trim().equals("select CONTRACT_NO from ECC_IMPAWNCONTRACT_I  a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )"));
        Assert.assertTrue(list.get(5).trim().equals("select all CONTRACT_NO from ECC_PLEDGECONTRACTS_BP_I a where TRUSTLOAN_TYPE='1' and not exists (select 1 from ECC_LOANCONTRACTS_I b where a.CONTRACT_NO = b.LOANCONT_NO )"));
    }

}
