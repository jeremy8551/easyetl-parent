package icu.etl.database.db2;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DB2ExportFileTest {

    @Test
    public void testReplaceDB2FieldValue() {
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6", 1, "\"ceshi\"").equals("\"ceshi\",2,\"3,\"\"abc\",4,5,6"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("1,2,\"3,\"\"abc\",4,5,6", 1, "\"ceshi\"").equals("\"ceshi\",2,\"3,\"\"abc\",4,5,6"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("11,2,\"3,\"\"abc\",4,5,6", 1, "\"ceshi\"").equals("\"ceshi\",2,\"3,\"\"abc\",4,5,6"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("111,2,\"3,\"\"abc\",4,5,6", 1, "\"ceshi\"").equals("\"ceshi\",2,\"3,\"\"abc\",4,5,6"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",2,\"3,\"\"abc\",4,5,6", 1, "\"ceshi\"").equals("\"ceshi\",2,\"3,\"\"abc\",4,5,6"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",2,\"3,\"\"abc\",4,5,6", 1, "\"ceshi\"").equals("\"ceshi\",2,\"3,\"\"abc\",4,5,6"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",2,\"3,\"\"abc\",4,5,6", 1, "\"ceshi\"").equals("\"ceshi\",2,\"3,\"\"abc\",4,5,6"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",2,\"3,\"\"abc\",4,5,6", 1, "\"ceshi\"").equals("\"ceshi\",2,\"3,\"\"abc\",4,5,6"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6,", 7, "\"ceshi\"").equals(",2,\"3,\"\"abc\",4,5,6,\"ceshi\""));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6,7", 7, "\"ceshi\"").equals(",2,\"3,\"\"abc\",4,5,6,\"ceshi\""));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6,77", 7, "\"ceshi\"").equals(",2,\"3,\"\"abc\",4,5,6,\"ceshi\""));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6,777", 7, "\"ceshi\"").equals(",2,\"3,\"\"abc\",4,5,6,\"ceshi\""));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6,\"3,\"\"abc\"", 7, "\"ceshi\"").equals(",2,\"3,\"\"abc\",4,5,6,\"ceshi\""));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6,\"3,\"\"abc\"", 7, "\"ceshi\"").equals(",2,\"3,\"\"abc\",4,5,6,\"ceshi\""));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6,\"3,\"\"abc\"", 7, "\"ceshi\"").equals(",2,\"3,\"\"abc\",4,5,6,\"ceshi\""));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,\"3,\"\"abc\",4,5,6,\"3,\"\"abc\"", 7, "\"ceshi\"").equals(",2,\"3,\"\"abc\",4,5,6,\"ceshi\""));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",,3,4,5,6,", 2, "\"ceshi\"").equals("\"3,\"\"abc\",\"ceshi\",3,4,5,6,"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",2,3,4,5,6,7", 2, "\"ceshi\"").equals("\"3,\"\"abc\",\"ceshi\",3,4,5,6,7"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",22,3,4,5,6,77", 2, "\"ceshi\"").equals("\"3,\"\"abc\",\"ceshi\",3,4,5,6,77"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",222,3,4,5,6,777", 2, "\"ceshi\"").equals("\"3,\"\"abc\",\"ceshi\",3,4,5,6,777"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",,3,4,5,\"3,\"\"abc\",", 6, "\"ceshi\"").equals(",,3,4,5,\"ceshi\","));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",2,3,4,5,\"3,\"\"abc\",7", 6, "\"ceshi\"").equals(",2,3,4,5,\"ceshi\",7"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",22,3,4,5,\"3,\"\"abc\",77", 6, "\"ceshi\"").equals(",22,3,4,5,\"ceshi\",77"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",222,3,4,5,\"3,\"\"abc\",777", 6, "\"ceshi\"").equals(",222,3,4,5,\"ceshi\",777"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",,3,4,5,6,", 2, "\"ceshi\"").equals("\"3,\"\"abc\",\"ceshi\",3,4,5,6,"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",2,3,4,5,6,7", 2, "\"ceshi\"").equals("\"3,\"\"abc\",\"ceshi\",3,4,5,6,7"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",22,3,4,5,6,77", 2, "\"ceshi\"").equals("\"3,\"\"abc\",\"ceshi\",3,4,5,6,77"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue("\"3,\"\"abc\",222,3,4,5,6,777", 2, "\"ceshi\"").equals("\"3,\"\"abc\",\"ceshi\",3,4,5,6,777"));
        assertTrue(DB2ExportFile.replaceDB2FieldValue(",,,,,,", 4, "\"ceshi\"").equals(",,,\"ceshi\",,,"));

        String src = "\"C\",\"TEST0001208098080\",\"重庆市綦江区全兴建筑工程有限公司\",\"\",\"测试地址信息\",\"CHN\",\"500110\",\"1997-12-12\",\"2060-12-31\",\"承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件\",\"CNY\",\"5038\",\"1\",\"10\",\"e4700\",\"15\",\"9999-99-99\",\"9999-99-99\",\"0019 \",\"5501020000333512 \",\" \",\" \",\" \",\" \",\" \",\" \",\" \",\" \",\"\",\" \",0,\" \"";
        String[] array = null;
        array = DB2ExportFile.splitDB2ExportFileLine(src, false);
        assertTrue(StringUtils.toString(array).equals("String[C, TEST0001208098080, 重庆市綦江区全兴建筑工程有限公司, , 测试地址信息, CHN, 500110, 1997-12-12, 2060-12-31, 承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件, CNY, 5038, 1, 10, e4700, 15, 9999-99-99, 9999-99-99, 0019 , 5501020000333512 ,  ,  ,  ,  ,  ,  ,  ,  , ,  , 0,  ]"));

        String des = DB2ExportFile.replaceDB2FieldValue(src, 21, "\"1\"");
        array = DB2ExportFile.splitDB2ExportFileLine(des, false);
        assertTrue(StringUtils.toString(array).equals("String[C, TEST0001208098080, 重庆市綦江区全兴建筑工程有限公司, , 测试地址信息, CHN, 500110, 1997-12-12, 2060-12-31, 承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件, CNY, 5038, 1, 10, e4700, 15, 9999-99-99, 9999-99-99, 0019 , 5501020000333512 , 1,  ,  ,  ,  ,  ,  ,  , ,  , 0,  ]"));
    }

    @Test
    public void testSplitDB2ExportFileLineString() {
        String str = "\"C\",\"TEST0001208098080\",\"重庆市綦江区全兴建筑工程有限公司\",\"\",\"测试地址信息\",\"CHN\",\"500110\",\"1997-12-12\",\"2060-12-31\",\"承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件\",\"CNY\",\"5038\",\"1\",\"10\",\"e4700\",\"15\",\"9999-99-99\",\"9999-99-99\",\"0019 \",\"5501020000333512 \",\" \",\" \",\" \",\" \",\" \",\" \",\" \",\" \",\"\",\" \",0,\" \"";
        String[] array = DB2ExportFile.splitDB2ExportFileLine(str, false); // 解析DB2 文本文件中每行数据

        Assert.assertTrue(true //
                && array[0].equals("C") //
                && array[1].equals("TEST0001208098080") //
                && array[2].equals("重庆市綦江区全兴建筑工程有限公司") //
                && array[3].equals("") //
                && array[4].equals("测试地址信息") //
                && array[5].equals("CHN") //
                && array[6].equals("500110") //
                && array[7].equals("1997-12-12") //
                && array[8].equals("2060-12-31") //
                && array[9].equals("承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件") //
                && array[10].equals("CNY") //
                && array[11].equals("5038") //
                && array[12].equals("1") //
                && array[13].equals("10") //
                && array[14].equals("e4700") //
                && array[15].equals("15") //
                && array[16].equals("9999-99-99") //
                && array[17].equals("9999-99-99") //
                && array[18].equals("0019 ") //
                && array[19].equals("5501020000333512 ") //
                && array[20].equals(" ") //
                && array[21].equals(" ") //
                && array[22].equals(" ") //
                && array[23].equals(" ") //
                && array[24].equals(" ") //
                && array[25].equals(" ") //
                && array[26].equals(" ") //
                && array[27].equals(" ") //
                && array[28].equals("") //
                && array[29].equals(" ") //
                && array[30].equals("0") //
                && array[31].equals(" ") //
        );
    }

    @Test
    public void testSplitDB2ExportFileLineKeepQuote() {
        //

        String str = "\"C\",\"TEST0001208098080\",\"测试角色\",\"\",\"测试地址信息\",\"CHN\",\"500110\",\"1997-12-12\",\"2060-12-31\",\"承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件\",\"CNY\",\"5038\",\"1\",\"10\",\"e4700\",\"15\",\"9999-99-99\",\"9999-99-99\",\"0019 \",\"5501020000333512 \",\" \",\" \",\" \",\" \",\" \",\" \",\" \",\" \",\"\",\" \",0,\" \"";
        String[] array = null;
        array = DB2ExportFile.splitDB2ExportFileLine(str, true);
//		for (int i = 0; i < array.length; i++) {
//			String s = array[i];
//			System.out.println(" && array[" + i + "].equals(\"" + ST.replaceAll(s, "\"", "\\\"") + "\") // ");
//		}

        Assert.assertTrue(true //
                && array[0].equals("\"C\"") //
                && array[1].equals("\"TEST0001208098080\"") //
                && array[2].equals("\"测试角色\"") //
                && array[3].equals("\"\"") //
                && array[4].equals("\"测试地址信息\"") //
                && array[5].equals("\"CHN\"") //
                && array[6].equals("\"500110\"") //
                && array[7].equals("\"1997-12-12\"") //
                && array[8].equals("\"2060-12-31\"") //
                && array[9].equals("\"承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件\"") //
                && array[10].equals("\"CNY\"") //
                && array[11].equals("\"5038\"") //
                && array[12].equals("\"1\"") //
                && array[13].equals("\"10\"") //
                && array[14].equals("\"e4700\"") //
                && array[15].equals("\"15\"") //
                && array[16].equals("\"9999-99-99\"") //
                && array[17].equals("\"9999-99-99\"") //
                && array[18].equals("\"0019 \"") //
                && array[19].equals("\"5501020000333512 \"") //
                && array[20].equals("\" \"") //
                && array[21].equals("\" \"") //
                && array[22].equals("\" \"") //
                && array[23].equals("\" \"") //
                && array[24].equals("\" \"") //
                && array[25].equals("\" \"") //
                && array[26].equals("\" \"") //
                && array[27].equals("\" \"") //
                && array[28].equals("\"\"") //
                && array[29].equals("\" \"") //
                && array[30].equals("0") //
                && array[31].equals("\" \"") //
        );
    }

    @Test
    public void testSplitDB2ExportFileLineStringListOfString() {
        List<String> list = new ArrayList<String>();
        String str = "\"C\",\"TEST0001208098080\",\"测试角色\",\"\",\"测试地址信息\",\"CHN\",\"500110\",\"1997-12-12\",\"2060-12-31\",\"承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件\",\"CNY\",\"5038\",\"1\",\"10\",\"e4700\",\"15\",\"9999-99-99\",\"9999-99-99\",\"0019 \",\"5501020000333512 \",\" \",\" \",\" \",\" \",\" \",\" \",\" \",\" \",\"\",\" \",0,\" \"";
        DB2ExportFile.splitDB2ExportFileLine(str, false, list);

        Assert.assertTrue(true //
                && list.get(0).equals("C") //
                && list.get(1).equals("TEST0001208098080") //
                && list.get(2).equals("测试角色") //
                && list.get(3).equals("") //
                && list.get(4).equals("测试地址信息") //
                && list.get(5).equals("CHN") //
                && list.get(6).equals("500110") //
                && list.get(7).equals("1997-12-12") //
                && list.get(8).equals("2060-12-31") //
                && list.get(9).equals("承包:土木工程建筑,房屋防水治理工程,商品房销售,建筑材料,五金,交电,化工,百货.生产销售预制构件") //
                && list.get(10).equals("CNY") //
                && list.get(11).equals("5038") //
                && list.get(12).equals("1") //
                && list.get(13).equals("10") //
                && list.get(14).equals("e4700") //
                && list.get(15).equals("15") //
                && list.get(16).equals("9999-99-99") //
                && list.get(17).equals("9999-99-99") //
                && list.get(18).equals("0019 ") //
                && list.get(19).equals("5501020000333512 ") //
                && list.get(20).equals(" ") //
                && list.get(21).equals(" ") //
                && list.get(22).equals(" ") //
                && list.get(23).equals(" ") //
                && list.get(24).equals(" ") //
                && list.get(25).equals(" ") //
                && list.get(26).equals(" ") //
                && list.get(27).equals(" ") //
                && list.get(28).equals("") //
                && list.get(29).equals(" ") //
                && list.get(30).equals("0") //
                && list.get(31).equals(" ") //
        );
    }

}
