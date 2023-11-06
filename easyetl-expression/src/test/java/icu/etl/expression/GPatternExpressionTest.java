package icu.etl.expression;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GPatternExpressionTest {

    @Test
    public void test() {
        assertTrue(new GPatternExpression("*").match("123"));
        assertTrue(new GPatternExpression("t*").match("t123"));
        assertTrue(!new GPatternExpression("t?").match("t123"));
        assertTrue(new GPatternExpression("t?").match("t1"));
        assertTrue(!new GPatternExpression("t?").match("t"));
        assertTrue(new GPatternExpression("t[a-b][0-9]e").match("ta2e"));
        assertTrue(new GPatternExpression("t[^a-b][^01234]e").match("tc5e"));
        assertTrue(new GPatternExpression("t[!a-b][!01234]e").match("tc5e"));
        assertTrue(new GPatternExpression("t{a,b,{t1,{t2*,t3*}}} ").match("ta "));
        assertTrue(new GPatternExpression("t{a,b,{t1,t{2..5}}} ").match("ta "));
        assertTrue(new GPatternExpression("t{a,b,{t1,t{d..f}}} ").match("ta "));
        assertTrue(!new GPatternExpression("t{a,b,{t1,t{td..f}}} ").match("tttd "));
        assertTrue(!new GPatternExpression("t{a,b,{t1,t{td..f}}} ").match("ttf "));
        assertTrue(new GPatternExpression("").match(""));
    }

}
