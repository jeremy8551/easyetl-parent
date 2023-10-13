package icu.etl.expression;

import java.math.BigDecimal;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/2
 */
public class DataUnitExpressionTest {

    @Test
    public void test2() {
        assertTrue(DataUnitExpression.toString(new BigDecimal("1056")).equals("1.03 KB"));
        assertTrue(DataUnitExpression.toString(new BigDecimal("663040000")).equals("632.32 MB"));
        assertTrue(DataUnitExpression.toString(new BigDecimal("678952960000")).equals("632.32 GB"));
        assertTrue(DataUnitExpression.toString(new BigDecimal("695247831040000")).equals("632.32 TB"));
    }

    @Test
    public void testformatHumanExpress() {
        assertTrue(DataUnitExpression.parse("1k").compareTo(new BigDecimal("1024")) == 0);
        assertTrue(DataUnitExpression.parse("1kb").compareTo(new BigDecimal("1024")) == 0);
        assertTrue(DataUnitExpression.parse("1m").compareTo(new BigDecimal("1048576")) == 0);
        assertTrue(DataUnitExpression.parse("1mb").compareTo(new BigDecimal("1048576")) == 0);
        assertTrue(DataUnitExpression.parse("1g").compareTo(new BigDecimal("1073741824")) == 0);
        assertTrue(DataUnitExpression.parse("1gb").compareTo(new BigDecimal("1073741824")) == 0);
        assertTrue(DataUnitExpression.parse("1tb").compareTo(new BigDecimal("1099511627776")) == 0);
        assertTrue(DataUnitExpression.parse("1pb").compareTo(new BigDecimal("1125899906842624")) == 0);
        assertTrue(DataUnitExpression.parse("1eb").compareTo(new BigDecimal("1152921504606846976")) == 0);
    }
    
}
