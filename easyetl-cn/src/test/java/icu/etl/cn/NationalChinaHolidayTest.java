package icu.etl.cn;

import icu.etl.ioc.AnnotationEasyetlContext;
import icu.etl.ioc.NationalHoliday;
import icu.etl.util.Dates;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NationalChinaHolidayTest {

    @Test
    public void testIsChinaRestDay() {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        assertFalse(context.getBean(NationalHoliday.class).isRestDay(Dates.parse("2019-08-30")));
        assertTrue(context.getBean(NationalHoliday.class).isRestDay(Dates.parse("20191001")));
    }

    @Test
    public void testIsChinaWorkDay() {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        assertTrue(context.getBean(NationalHoliday.class).isWorkDay(Dates.parse("2019-08-30")));
        assertFalse(context.getBean(NationalHoliday.class).isWorkDay(Dates.parse("2019-08-31")));
    }

    @Test
    public void test1() {
        NationalChinaHoliday context = new NationalChinaHoliday();
        assertFalse(context.isRestDay(Dates.parse("2019-08-30")));
        assertTrue(context.isRestDay(Dates.parse("20191001")));
    }

    @Test
    public void test2() {
        NationalChinaHoliday context = new NationalChinaHoliday();
        assertTrue(context.isWorkDay(Dates.parse("2019-08-30")));
        assertFalse(context.isWorkDay(Dates.parse("2019-08-31")));
    }

    @Test
    public void testReloadLegalHolidays() {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        assertTrue(context.getBean(NationalHoliday.class).getRestDays().size() > 0);
    }

    @Test
    public void testGetLegalRestDay() {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        assertTrue(context.getBean(NationalHoliday.class).getRestDays().size() > 0);
    }

    @Test
    public void testGetLegalWorkDay() {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        assertTrue(context.getBean(NationalHoliday.class).getWorkDays().size() > 0);
    }

    @Test
    public void testIsChinaLegalRestDay() {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        assertTrue(context.getBean(NationalHoliday.class).getRestDays().contains(Dates.parse("2019-10-01")));
        assertFalse(context.getBean(NationalHoliday.class).getRestDays().contains(Dates.parse("2019-08-31")));
        assertFalse(context.getBean(NationalHoliday.class).getRestDays().contains(null));
    }

    @Test
    public void testIsChinaLegalWorkDay() {
        AnnotationEasyetlContext context = new AnnotationEasyetlContext();
        assertTrue(context.getBean(NationalHoliday.class).getWorkDays().contains(Dates.parse("2017-02-04")));
        assertFalse(context.getBean(NationalHoliday.class).getWorkDays().contains(Dates.parse("2017-02-05")));
    }

}
