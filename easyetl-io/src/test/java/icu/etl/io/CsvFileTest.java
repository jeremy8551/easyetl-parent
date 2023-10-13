package icu.etl.io;

import java.util.ArrayList;

import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/2
 */
public class CsvFileTest {

    @Test
    public void testSplitCsvFileLineString() {
        Assert.assertEquals("String[]", StringUtils.toString(CsvFile.splitCsvFileLine("")));
        Assert.assertEquals("String[1]", StringUtils.toString(CsvFile.splitCsvFileLine("1")));
        Assert.assertEquals("String[12]", StringUtils.toString(CsvFile.splitCsvFileLine("12")));
        Assert.assertEquals("String[12, ]", StringUtils.toString(CsvFile.splitCsvFileLine("12,")));
        Assert.assertEquals("String[12, 3, ]", StringUtils.toString(CsvFile.splitCsvFileLine("12,3,")));
        Assert.assertEquals("String[12, 3, 4]", StringUtils.toString(CsvFile.splitCsvFileLine("12,3,4")));
        Assert.assertEquals("String[12, 3, 4, 吕钊 军, sdf , 吕钊\" 军,  sdf, ]", StringUtils.toString(CsvFile.splitCsvFileLine("12,3,4,\"吕钊 军\",sdf ,\"吕钊\"\" 军\", sdf,")));
        Assert.assertEquals("String[1996, Jep, grand chaeds , must sell\nair sdf sdf, loader, , \", 47900.00]", StringUtils.toString(CsvFile.splitCsvFileLine("1996,Jep,grand chaeds ,\"must sell\nair sdf sdf, loader\",\"\",\"\"\"\",47900.00")));
    }

    @Test
    public void testSplitCsvFileLineStringListOfString() {
        ArrayList<String> list = new ArrayList<String>();
        CsvFile.splitCsvFileLine("", list);
        Assert.assertEquals("ArrayList[]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("1", list);
        Assert.assertEquals("ArrayList[1]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12", list);
        Assert.assertEquals("ArrayList[12]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12,", list);
        Assert.assertEquals("ArrayList[12, ]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12,3,", list);
        Assert.assertEquals("ArrayList[12, 3, ]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12,3,4", list);
        Assert.assertEquals("ArrayList[12, 3, 4]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("12,3,4,\"吕钊 军\",sdf ,\"吕钊\"\" 军\", sdf,", list);
        Assert.assertEquals("ArrayList[12, 3, 4, 吕钊 军, sdf , 吕钊\" 军,  sdf, ]", StringUtils.toString(list));

        list.clear();
        CsvFile.splitCsvFileLine("1996,Jep,grand chaeds ,\"must sell\nair sdf sdf, loader\",\"\",\"\"\"\",47900.00", list);
        Assert.assertEquals("ArrayList[1996, Jep, grand chaeds , must sell\nair sdf sdf, loader, , \", 47900.00]", StringUtils.toString(list));
    }

    @Test
    public void testJoinCsvFields() {
        Assert.assertEquals(",, ,\"this is ,  good dear '' ; \"", CsvFile.joinFields(new String[]{null, "", " ", "this is ,  good dear '' ; "}));
    }
    
}
