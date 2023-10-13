package icu.etl.expression;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HtmlExpressionTest {

    @Test
    public void test() {
        assertEquals(new HtmlExpression("<html />").getTagName(), "html");
        assertEquals(new HtmlExpression("<html/>").getTagName(), "html");
    }

    @Test
    public void test1() {
        HtmlExpression e = new HtmlExpression("<html name='value' />");
        assertEquals(e.getTagName(), "html");
        assertTrue(e.contains("name"));
        assertEquals(e.getAttribute("name"), "value");
    }

    @Test
    public void test2() {
        HtmlExpression e = new HtmlExpression("<html name=\"value\" />");
        assertEquals(e.getTagName(), "html");
        assertTrue(e.contains("name"));
        assertEquals(e.getAttribute("name"), "value");
    }

    @Test
    public void test3() {
        HtmlExpression e = new HtmlExpression("<html name=\"value\" key='value' />");
        assertEquals(e.getTagName(), "html");
        assertTrue(e.contains("name"));
        assertEquals(e.getAttribute("name"), "value");
        assertEquals(e.getAttribute("key"), "value");
    }

    @Test
    public void test4() {
        HtmlExpression e = new HtmlExpression("<html name=\"value\" key='value'></html>");
        assertEquals(e.getTagName(), "html");
        assertTrue(e.contains("name"));
        assertEquals(e.getAttribute("name"), "value");
        assertEquals(e.getAttribute("key"), "value");
        assertEquals(e.getText(), "");
    }

    @Test
    public void test5() {
        HtmlExpression e = new HtmlExpression("<html name=\"value\" key='value'>1</html>");
        assertEquals(e.getTagName(), "html");
        assertTrue(e.contains("name"));
        assertEquals(e.getAttribute("name"), "value");
        assertEquals(e.getAttribute("key"), "value");
        assertEquals(e.getText(), "1");
    }

    @Test
    public void test6() {
        HtmlExpression e = new HtmlExpression("<html name=\"value\" key='value'><table></table></html>");
        assertEquals(e.getTagName(), "html");
        assertTrue(e.contains("name"));
        assertEquals(e.getAttribute("name"), "value");
        assertEquals(e.getAttribute("key"), "value");
        assertEquals(e.getText(), "<table></table>");
    }

    @Test
    public void test7() {
        HtmlExpression e = new HtmlExpression("<th:html name=\"value\" key='value'><table></table></html>");
        assertEquals(e.getTagName(), "html");
        assertEquals(e.getNamespace(), "th");
        assertTrue(e.contains("name"));
        assertEquals(e.getAttribute("name"), "value");
        assertEquals(e.getAttribute("key"), "value");
        assertEquals(e.getText(), "<table></table>");
    }

}
