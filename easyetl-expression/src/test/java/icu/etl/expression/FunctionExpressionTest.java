package icu.etl.expression;

import org.junit.Assert;
import org.junit.Test;

public class FunctionExpressionTest {

    @Test
    public void test1() {
        FunctionExpression expr = new FunctionExpression(new TestAnalysisImpl(), "int");
        Assert.assertEquals("int", expr.getName());
        Assert.assertTrue(!expr.containParameter());

        expr = new FunctionExpression(new TestAnalysisImpl(), "int()");
        Assert.assertEquals("int", expr.getName());
        Assert.assertTrue(expr.containParameter());
        Assert.assertEquals("", expr.getParameter());

        expr = new FunctionExpression(new TestAnalysisImpl(), "int(1)");
        Assert.assertEquals("int", expr.getName());
        Assert.assertTrue(expr.containParameter());
        Assert.assertEquals("1", expr.getParameter());
        Assert.assertEquals("1", expr.getParameter(1));

        expr = new FunctionExpression(new TestAnalysisImpl(), "decimal(12,2)");
        Assert.assertEquals("decimal", expr.getName());
        Assert.assertTrue(expr.containParameter());
        Assert.assertEquals("12,2", expr.getParameter());
        Assert.assertEquals("12", expr.getParameter(1));
        Assert.assertEquals("2", expr.getParameter(2));
    }

}
