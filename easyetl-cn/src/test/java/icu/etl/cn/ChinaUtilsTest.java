package icu.etl.cn;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import icu.etl.ioc.BeanContext;
import icu.etl.ioc.NationalHoliday;
import icu.etl.util.Dates;
import icu.etl.util.Ensure;
import icu.etl.util.Property;
import icu.etl.util.StringUtils;
import icu.etl.util.TimeWatch;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/2
 */
public class ChinaUtilsTest {

    @Test
    public void testIsChinaRestDay() {
        BeanContext context = new BeanContext();
        assertTrue(!ChinaUtils.isRestDay(context, Dates.parse("2019-08-30")));
        assertTrue(ChinaUtils.isRestDay(context, Dates.parse("20191001")));
    }

    @Test
    public void testIsChinaWorkDay() {
        BeanContext context = new BeanContext();
        assertTrue(ChinaUtils.isWorkDay(context, Dates.parse("2019-08-30")));
        assertTrue(!ChinaUtils.isWorkDay(context, Dates.parse("2019-08-31")));
    }

    @Test
    public void testTranslateChineseNumberChar() {
        Assert.assertTrue(ChinaUtils.replaceChineseNumber('零') == '0');
        Assert.assertTrue(ChinaUtils.replaceChineseNumber('九') == '9');
        Assert.assertTrue(ChinaUtils.replaceChineseNumber('玖') == '9');
        Assert.assertTrue(ChinaUtils.replaceChineseNumber('壹') == '1');
    }

    @Test
    public void testTranslateChineseNumberString() {
        Assert.assertTrue(ChinaUtils.replaceChineseNumber(null) == null);
        Assert.assertEquals("1998", ChinaUtils.replaceChineseNumber("一九九八"));
        Assert.assertEquals("1998 2月 9 0", ChinaUtils.replaceChineseNumber("一九九八 贰月 玖 零"));
        Assert.assertEquals(" ", ChinaUtils.replaceChineseNumber(" "));
    }

    @Test
    public void testTranslateTraditionalChineseNumberBigDecimal() {
        Assert.assertEquals("壹仟元", ChinaUtils.toChineseNumber(new BigDecimal("1000")));
        Assert.assertEquals("壹拾元", ChinaUtils.toChineseNumber(new BigDecimal("10")));
        Assert.assertEquals("壹拾壹元", ChinaUtils.toChineseNumber(new BigDecimal("11")));
        Assert.assertEquals("捌拾壹元", ChinaUtils.toChineseNumber(new BigDecimal("81")));
        Assert.assertEquals("负壹万贰仟叁佰肆拾伍元陆角柒分", ChinaUtils.toChineseNumber(new BigDecimal("-12345.67")));
        Assert.assertEquals("壹万贰仟叁佰肆拾伍元陆角柒分", ChinaUtils.toChineseNumber(new BigDecimal("12345.67")));
        Assert.assertEquals("壹拾贰万叁仟肆佰伍拾陆元壹角贰分", ChinaUtils.toChineseNumber(new BigDecimal("123456.12")));
        Assert.assertEquals("壹佰贰拾叁万肆仟伍佰陆拾柒元壹角贰分", ChinaUtils.toChineseNumber(new BigDecimal("1234567.12")));
        Assert.assertEquals("壹仟贰佰叁拾肆万伍仟陆佰柒拾捌元壹角贰分", ChinaUtils.toChineseNumber(new BigDecimal("12345678.12")));
        Assert.assertEquals("壹亿贰仟叁佰肆拾伍万陆仟柒佰捌拾玖元壹角贰分", ChinaUtils.toChineseNumber(new BigDecimal("123456789.12")));
        Assert.assertEquals("壹拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾元零壹角贰分", ChinaUtils.toChineseNumber(new BigDecimal("1234567890.12")));
        Assert.assertEquals("壹佰贰拾叁亿肆仟伍佰陆拾柒万捌仟玖佰零壹元壹角贰分", ChinaUtils.toChineseNumber(new BigDecimal("12345678901.12")));

        Assert.assertEquals("肆仟陆佰肆拾万零贰仟玖佰壹拾伍元", ChinaUtils.toChineseNumber(new BigDecimal("46402915.00")));
        Assert.assertEquals("壹仟玖佰玖拾万零陆仟零捌拾元", ChinaUtils.toChineseNumber(new BigDecimal("19906080")));

//		Assert.assertEquals("负壹万贰仟叁佰肆拾伍元陆角柒分捌厘玖豪壹丝", StringUtils.toChineseNumber(new BigDecimal("-12345.67891")));
//		Assert.assertEquals("壹万贰仟叁佰肆拾伍元陆角柒分捌厘玖豪壹丝", StringUtils.toChineseNumber(new BigDecimal("12345.67891")));
//		Assert.assertEquals("壹拾贰万叁仟肆佰伍拾陆元壹角贰分叁厘肆豪伍丝", StringUtils.toChineseNumber(new BigDecimal("123456.12345")));
//		Assert.assertEquals("壹佰贰拾叁万肆仟伍佰陆拾柒元壹角贰分叁厘肆豪伍丝", StringUtils.toChineseNumber(new BigDecimal("1234567.12345")));
//		Assert.assertEquals("壹仟贰佰叁拾肆万伍仟陆佰柒拾捌元壹角贰分叁厘肆豪伍丝", StringUtils.toChineseNumber(new BigDecimal("12345678.12345")));
//		Assert.assertEquals("壹亿贰仟叁佰肆拾伍万陆仟柒佰捌拾玖元壹角贰分叁厘肆豪伍丝", StringUtils.toChineseNumber(new BigDecimal("123456789.12345")));
//		Assert.assertEquals("壹拾贰亿叁仟肆佰伍拾陆万柒仟捌佰玖拾元壹角贰分叁厘肆豪伍丝", StringUtils.toChineseNumber(new BigDecimal("1234567890.12345")));
//		Assert.assertEquals("壹佰贰拾叁亿肆仟伍佰陆拾柒万捌仟玖佰壹元壹角贰分叁厘肆豪伍丝", StringUtils.toChineseNumber(new BigDecimal("12345678901.12345")));
    }

    @Test
    public void testisChineseLetter() {
        assertTrue(ChinaUtils.isChineseLetter('一'));
        assertTrue(ChinaUtils.isChineseLetter('飞'));
        assertTrue(ChinaUtils.isChineseLetter('中'));
        assertTrue(ChinaUtils.isChineseLetter('国'));
        assertTrue(ChinaUtils.isChineseLetter(''));
        assertTrue(ChinaUtils.isChineseLetter(''));
        assertTrue(ChinaUtils.isChineseLetter(''));
        assertTrue(!ChinaUtils.isChineseLetter('1'));
        assertTrue(!ChinaUtils.isChineseLetter('='));
        assertTrue(!ChinaUtils.isChineseLetter('。'));
    }

    @Test
    public void testParseBigDecimalString() {
        Assert.assertEquals("12345678901.12345", ChinaUtils.parseChineseNumber("壹佰贰拾叁亿肆仟伍佰陆拾柒万捌仟玖佰壹元壹角贰分叁厘肆豪伍丝").toString());
        Assert.assertEquals(BigDecimal.ZERO, ChinaUtils.parseChineseNumber("0"));
        assertTrue(ChinaUtils.parseChineseNumber("0.1").equals(new BigDecimal("0.1")));
        assertTrue(ChinaUtils.parseChineseNumber("0.12").equals(new BigDecimal("0.12")));
        assertTrue(ChinaUtils.parseChineseNumber("120.00").equals(new BigDecimal("120.0")));
        assertTrue(ChinaUtils.parseChineseNumber("120.12345").equals(new BigDecimal("120.12345")));
        assertTrue(ChinaUtils.parseChineseNumber("一百二十").equals(new BigDecimal("120")));

        assertTrue(ChinaUtils.parseChineseNumber("一万二千三百四十五兆 六千7百八十九亿 一千二百三十四万 五千六百七十八元 9角1分2厘3豪4丝56").equals(new BigDecimal("12345678912345678.9123456")));
        assertTrue(ChinaUtils.parseChineseNumber("负一万二千三百四十五兆 六千7百八十九亿 一千二百三十四万 五千六百七十八元 9角1分2厘3豪4丝56").equals(new BigDecimal("-12345678912345678.9123456")));
        assertTrue(ChinaUtils.parseChineseNumber("负一万二千三百四十五兆 六千7百八十九亿 一千二百三十四万 五千六百七十八元").equals(new BigDecimal("-12345678912345678")));
        assertTrue(ChinaUtils.parseChineseNumber("9角1分2厘3豪4丝56789").equals(new BigDecimal("0.9123456789")));

        assertTrue(ChinaUtils.parseChineseNumber("二千三百四十五万").equals(new BigDecimal("23450000")));
        assertTrue(ChinaUtils.parseChineseNumber("一万二千三百四十五万").equals(new BigDecimal("62340")));
        assertTrue(ChinaUtils.parseChineseNumber("一万二千三百四十").equals(new BigDecimal("12340")));
        assertTrue(ChinaUtils.parseChineseNumber("一万二十").equals(new BigDecimal("10020")));
        assertTrue(ChinaUtils.parseChineseNumber("一万零二十").equals(new BigDecimal("10020")));
        assertTrue(ChinaUtils.parseChineseNumber("一万零二兆").equals(new BigDecimal("10002000000000000")));
        assertTrue(ChinaUtils.parseChineseNumber("贰仟伍佰伍拾元").equals(new BigDecimal("2550")));
        assertTrue(ChinaUtils.parseChineseNumber("壹仟叁佰陆拾肆元伍角").equals(new BigDecimal("1364.5")));
    }

    @Test
    public void testCheckIdCard() {
        assertTrue(ChinaUtils.isIdCard("350424870506202"));
        assertTrue(ChinaUtils.isIdCard("350424198705062025"));
        assertTrue(ChinaUtils.isIdCard("110101196510022029"));
        assertTrue(!ChinaUtils.isIdCard("350424198705062026"));
    }

    @Test
    public void testCheck18IdCard() {
        assertTrue(ChinaUtils.isIdCard18("350424198705062025"));
        assertTrue(ChinaUtils.isIdCard18("110101196505014024"));
    }

    @Test
    public void testIdCard15to18() {
        Assert.assertEquals("350424198705062025", ChinaUtils.idCard15to18("350424870506202"));
        Assert.assertEquals("110101196505014024", ChinaUtils.idCard15to18("110101650501402"));
    }

    @Test
    public void testCheckUniformSocialCreditCode() {
        assertTrue(ChinaUtils.isUniformSocialCreditCode("91231084MA19MPDK19"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("91230822069156716N"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("92230822MA19GE5T1B"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("92230822MA19GFP20W"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("91230822MA19DAQ94X"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("91230822098997389B"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("91230822MA19MKRA5N"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("0011141032317A5272") == false);

        assertTrue(ChinaUtils.isUniformSocialCreditCode("91230822MA1AW7M36X"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("91230822MA1AXF877W"));
        assertTrue(ChinaUtils.isUniformSocialCreditCode("92230822MA1ARU5B9T"));
    }

    @Test
    public void testgetPropertys() {
        int count = 0;
        List<Property> list = ChinaUtils.getPropertys("5527");
        for (Property p : list) {
            System.out.println(p.getKey() + " = " + p.getValue());
            if (++count >= 20) {
                break;
            }
        }
    }

    @Test
    public void test() {
        ChineseRandom random = new ChineseRandom();
        TimeWatch watch = new TimeWatch();
        for (int i = 0; i < 10; i++) {
            String name = random.nextName();
            String idcard = random.nextIdCard();
            String mobile = random.nextMobile();

            // 校验随机生成的身份证号 随机生成的姓名与手机号
            Ensure.isTrue(ChinaUtils.isIdCard(idcard));
            Ensure.isTrue(mobile != null && mobile.length() == 11);
            Ensure.isTrue(StringUtils.isNotBlank(name) && name.length() <= 4);

            // 格式化输出信息 身份证号 手机号 姓名
            System.out.println(StringUtils.left(name, 10, StandardCharsets.UTF_8.name(), ' ') + " " + idcard + " " + mobile);
        }
        System.out.println("use " + watch.useTime());
    }

    @Test
    public void testReloadLegalHolidays() {
        BeanContext context = new BeanContext();
        assertTrue(context.get(NationalHoliday.class).getRestDays().size() > 0);
    }

    @Test
    public void testGetLegalRestDay() {
        BeanContext context = new BeanContext();
        assertTrue(context.get(NationalHoliday.class).getRestDays().size() > 0);
    }

    @Test
    public void testGetLegalWorkDay() {
        BeanContext context = new BeanContext();
        assertTrue(context.get(NationalHoliday.class).getWorkDays().size() > 0);
    }

    @Test
    public void testIsChinaLegalRestDay() {
        BeanContext context = new BeanContext();
        assertTrue(context.get(NationalHoliday.class).getRestDays().contains(Dates.parse("2019-10-01")));
        assertTrue(!context.get(NationalHoliday.class).getRestDays().contains(Dates.parse("2019-08-31")));
        assertTrue(!context.get(NationalHoliday.class).getRestDays().contains(null));
    }

    @Test
    public void testIsChinaLegalWorkDay() {
        BeanContext context = new BeanContext();
        assertTrue(context.get(NationalHoliday.class).getWorkDays().contains(Dates.parse("2017-02-04")));
        assertTrue(!context.get(NationalHoliday.class).getWorkDays().contains(Dates.parse("2017-02-05")));
    }

    @Test
    public void test123() {
        for (int i = 0; i < 65536; i++) {
            char c = (char) i;
            boolean chineseLetter = ChinaUtils.isChineseLetter(c);
            if (chineseLetter) {
                System.out.println("汉字字符: " + Long.toHexString(i) + " " + i + " " + c + " " + chineseLetter);
            }
        }
    }

}
