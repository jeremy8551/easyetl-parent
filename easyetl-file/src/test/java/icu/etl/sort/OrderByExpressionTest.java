package icu.etl.sort;

import icu.etl.expression.Analysis;
import icu.etl.expression.XmlAnalysis;
import icu.etl.ioc.EasyBeanContext;
import icu.etl.util.StrAsIntComparator;
import icu.etl.util.StrAsNumberComparator;
import icu.etl.util.StringComparator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OrderByExpressionTest {

    @Test
    public void test() {
        EasyBeanContext context = new EasyBeanContext();
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(context, a, "1 asc");
        assertEquals(o.getPosition(), 1);
        assertTrue(o.isAsc());
        assertEquals(o.getComparator().getClass(), StringComparator.class);
    }

    @Test
    public void test1() {
        EasyBeanContext context = new EasyBeanContext();
        Analysis a = new XmlAnalysis();
        OrderByExpression expression = new OrderByExpression(context, a, "1");
        assertEquals(expression.getPosition(), 1);
        assertTrue(expression.isAsc());
        assertEquals(expression.getComparator().getClass(), StringComparator.class);
    }

    @Test
    public void test2() {
        EasyBeanContext context = new EasyBeanContext();
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(context, a, "int(1) asc");
        assertEquals(o.getPosition(), 1);
        assertTrue(o.isAsc());
        assertEquals(o.getComparator().getClass(), StrAsIntComparator.class);
    }

    @Test
    public void test3() {
        EasyBeanContext context = new EasyBeanContext();
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(context, a, "int(1) desc");
        assertEquals(o.getPosition(), 1);
        assertFalse(o.isAsc());
        assertEquals(o.getComparator().getClass(), StrAsIntComparator.class);
    }

    @Test
    public void test4() {
        EasyBeanContext context = new EasyBeanContext();
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(context, a, "number(11) desc");
        assertEquals(o.getPosition(), 11);
        assertFalse(o.isAsc());
        assertEquals(o.getComparator().getClass(), StrAsNumberComparator.class);
    }

}
