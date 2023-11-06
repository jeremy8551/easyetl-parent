package icu.etl.expression;

import icu.etl.util.Dates;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ExpressionTest {

    @Test
    public void test1() {
        assertTrue(new Expression("1 == 1 and 2==2").booleanValue());
        assertTrue(new Expression("1 == 2 or 2==2").booleanValue());
        assertTrue(!new Expression("1 == 2 or 2==3").booleanValue());
        assertTrue(new Expression("1 != 2 ").booleanValue());
        assertTrue(new Expression("1!=2").booleanValue());
        assertTrue(!new Expression("1==2 or 2==3").booleanValue());
        assertTrue(new Expression("'20200101' + 1day-1day + 1 year - 1 month + 1hour +1minute+1second +1millis").dateValue().equals(Dates.parse("2020-12-01 01:01:01:001")));
        assertTrue(new Expression("'20200101' + 1day-1day + 1 year - 1 month + 1hour +1minute+1second +1millis").longValue().longValue() == Dates.parse("2020-12-01 01:01:01:001").getTime());
        assertTrue(new Expression("1 in (1, 2, 3,5)").booleanValue());
        assertTrue(!new Expression("10 in (1, 2, 3,5)").booleanValue());
        assertTrue(new Expression("10 not   in  (1, 2, 3,5)").booleanValue());
        assertTrue(new Expression("'10' in ('1' , '2'  ,'3',10,'10')").booleanValue());
        assertTrue(new Expression("'10' in ( 1, 2  ,3,10,'10' )").booleanValue());
        assertTrue(!new Expression("'10' in ('1' , '2'  ,'3',10)").booleanValue());
        assertTrue(new Expression("'10'  not   in ('1' , '2'  ,'3',10)").booleanValue());
        assertTrue(new Expression("1==1 && 1 in (1, 2, 3,5)").booleanValue());
        assertTrue(new Expression("1==1 && 1.0 in (1.0, 2, 3,5)").booleanValue());
        assertTrue(new Expression("1==1 && 1.0 in (1.00, 2, 3,5)").booleanValue());
        assertTrue(!new Expression("1==1 && 1.01 in (1.00, 2, 3,5, '1.01')").booleanValue());
        assertTrue(new Expression("1==1 && 1.01    not in (1.00, 2, 3,5, '1.01')").booleanValue());
    }

    @Test
    public void test() throws Exception {
        assertTrue(((Double) new Expression("12.00 + 11 + ( 11*2) ").doubleValue()) == 45);
        assertTrue(((Double) new Expression("12.00+11+(11*2)").doubleValue()) == 45);

        assertTrue(!new Expression("1==2 && 1==1 && 2==2").booleanValue());
        assertTrue(!new Expression("1==1 && 1==2 && 2==2").booleanValue());
        assertTrue(!new Expression("1==1 && 2==2 && 3==4").booleanValue());
        assertTrue(!new Expression("1==1 && 2==2 && 3==3 && 4==3").booleanValue());

        assertTrue(new Expression("1==2 && 1==1 || 2==2").booleanValue());
        assertTrue(new Expression("1==2 || 1==1 || 2==2").booleanValue());
        assertTrue(new Expression("1==2 || 3==1 || 2==2").booleanValue());
        assertTrue(new Expression("1==1 || 3==1 || 2==3").booleanValue());

        assertTrue(((Long) new Expression("1==1 ? 1 : 2").longValue()) == 1);
        assertTrue(((Long) new Expression("1==2 ? 1 : 2").longValue()) == 2);
        assertTrue(((String) new Expression("(1==1) ? (1==1 ? '1' : '2') : '3' ").stringValue()).equals("1")); // 1
        assertTrue(((String) new Expression("(1==1) ? (1==3 ? '1' : '2') : '3' ").stringValue()).equals("2")); // 2
        assertTrue(((String) new Expression("(1==2) ? (1==3 ? '1' : '2') : '3' ").stringValue()).equals("3")); // 3
        assertTrue(((String) new Expression("1==2 ? (1==3 ? '1' : '2') : '3' ").stringValue()).equals("3")); // 3
        assertTrue(((String) new Expression("1==1 ? (1==1 ? ':1:):' : '2:)') : '3:)' ").stringValue()).equals(":1:):")); // 3
        assertTrue(((String) new Expression("1==1?(1==1?':1:):':'2:)'):'3:)'").stringValue()).equals(":1:):")); // 3

        assertTrue(((Long) new Expression("1").longValue()) == 1);
        assertTrue(((Double) new Expression("1.0").doubleValue()) == 1);
        assertTrue(((Double) new Expression("1.1").doubleValue()) == 1.1);
        assertTrue(((Double) new Expression("+1.1").doubleValue()) == 1.1);
        assertTrue(((Double) new Expression("-1.1").doubleValue()) == -1.1);
        assertTrue(((String) new Expression("''").stringValue()).equals(""));
        assertTrue(((String) new Expression("'字符串'").stringValue()).equals("字符串"));
        assertTrue(((String) new Expression("'字符串'+'是正确的!'").stringValue()).equals("字符串是正确的!"));
        assertTrue(((String) new Expression("'字符串'+'是正确的!' + 12").stringValue()).equals("字符串是正确的!12"));
        assertTrue(((String) new Expression("'字符串'+'是正确的!' + 12+'abc'").stringValue()).equals("字符串是正确的!12abc"));
        assertTrue(((String) new Expression("'字符串'+'是正确的!'+12+'abc'").stringValue()).equals("字符串是正确的!12abc"));
        assertTrue(((String) new Expression("'aa'+'\\' \\\\' ").stringValue()).equals("aa' \\"));
        assertTrue(!((Boolean) new Expression("'1'+'2'=='3'").booleanValue()));

        assertTrue(((Long) new Expression("24*3600* 1000").longValue()) == 86400000);
        assertTrue(((Long) new Expression("24*3600*1000").longValue()) == 86400000);
        assertTrue(((Boolean) new Expression("'1985-02-01'>='1985-02-01'").booleanValue()));
        assertTrue(((Boolean) new Expression("'1983-02-01'<='1985-02-01'").booleanValue()));
        assertTrue(((Boolean) new Expression("(11+11)==(10+12)").booleanValue()));
        assertTrue(!((Boolean) new Expression("11+11!=10.4+11.6").booleanValue()));
        assertTrue(((Double) new Expression("10+ -11.1234").doubleValue()) == -1.1234);
        assertTrue(((Double) new Expression("10+-11.1234").doubleValue()) == -1.1234);

        assertTrue(((Double) new Expression("4.900000e-324").doubleValue()) == 4.9e-324);
        assertTrue(((Double) new Expression("1.797693e+308").doubleValue()) == 1.797693E308);
    }

}
