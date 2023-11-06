package icu.etl.expression;

import java.math.BigDecimal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/2
 */
public class DataUnitExpressionTest {

    @Test
    public void test2() {
        assertEquals("1.03 KB", DataUnitExpression.toString(new BigDecimal("1056")));
        assertEquals("632.32 MB", DataUnitExpression.toString(new BigDecimal("663040000")));
        assertEquals("632.32 GB", DataUnitExpression.toString(new BigDecimal("678952960000")));
        assertEquals("632.32 TB", DataUnitExpression.toString(new BigDecimal("695247831040000")));
    }

    @Test
    public void testformatHumanExpress() {
        assertEquals(0, DataUnitExpression.parse("1k").compareTo(new BigDecimal("1024")));
        assertEquals(0, DataUnitExpression.parse("1kb").compareTo(new BigDecimal("1024")));
        assertEquals(0, DataUnitExpression.parse("1m").compareTo(new BigDecimal("1048576")));
        assertEquals(0, DataUnitExpression.parse("1mb").compareTo(new BigDecimal("1048576")));
        assertEquals(0, DataUnitExpression.parse("1g").compareTo(new BigDecimal("1073741824")));
        assertEquals(0, DataUnitExpression.parse("1gb").compareTo(new BigDecimal("1073741824")));
        assertEquals(0, DataUnitExpression.parse("1tb").compareTo(new BigDecimal("1099511627776")));
        assertEquals(0, DataUnitExpression.parse("1pb").compareTo(new BigDecimal("1125899906842624")));
        assertEquals(0, DataUnitExpression.parse("1eb").compareTo(new BigDecimal("1152921504606846976")));
    }

}
