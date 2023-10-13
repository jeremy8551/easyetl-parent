package icu.etl.expression;

import icu.etl.ioc.BeanContext;
import icu.etl.ioc.StrAsIntComparator;
import icu.etl.ioc.StrAsNumberComparator;
import icu.etl.util.StringComparator;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderByExpressionTest {

    @Before
    public void before() {
        BeanContext context = new BeanContext();
    }

    @Test
    public void test() {
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(a, "1 asc", false);
        assertEquals(o.getPosition(), 1);
        assertEquals(o.isAsc(), true);
        assertEquals(o.getComparator().getClass(), StringComparator.class);
    }

    @Test
    public void test1() {
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(a, "1", false);
        assertEquals(o.getPosition(), 1);
        assertEquals(o.isAsc(), false);
        assertEquals(o.getComparator().getClass(), StringComparator.class);
    }

    @Test
    public void test2() {
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(a, "int(1) asc", false);
        assertEquals(o.getPosition(), 1);
        assertEquals(o.isAsc(), true);
        assertEquals(o.getComparator().getClass(), StrAsIntComparator.class);
    }

    @Test
    public void test3() {
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(a, "int(1) desc", true);
        assertEquals(o.getPosition(), 1);
        assertEquals(o.isAsc(), false);
        assertEquals(o.getComparator().getClass(), StrAsIntComparator.class);
    }

    @Test
    public void test4() {
        Analysis a = new XmlAnalysis();
        OrderByExpression o = new OrderByExpression(a, "number(11) desc", true);
        assertEquals(o.getPosition(), 11);
        assertEquals(o.isAsc(), false);
        assertEquals(o.getComparator().getClass(), StrAsNumberComparator.class);
    }

}
