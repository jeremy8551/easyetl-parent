package icu.etl.ioc;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import icu.etl.util.Dates;

/**
 * 法定假日的集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/27
 */
public class NationalHolidaySet implements NationalHoliday {

    /** 工作日 */
    private Set<Date> work;

    /** 休息日 */
    private Set<Date> rest;

    public NationalHolidaySet() {
        this.work = new HashSet<Date>();
        this.rest = new HashSet<Date>();
    }

    public void add(NationalHoliday holiday) {
        this.work.addAll(holiday.getWorkDays());
        this.rest.addAll(holiday.getRestDays());
    }

    public Set<Date> getRestDays() {
        return this.rest;
    }

    public Set<Date> getWorkDays() {
        return this.work;
    }

    public boolean isRestDay(Date date) {
        if (date == null) {
            return false;
        }

        if (this.getWorkDays().contains(date)) {
            return false;
        } else if (this.getRestDays().contains(date)) {
            return true;
        } else {
            return Dates.isWeekend(date);
        }
    }

    public boolean isWorkDay(Date date) {
        if (date == null) {
            return false;
        }

        if (this.getWorkDays().contains(date)) {
            return true;
        } else if (this.getRestDays().contains(date)) {
            return false;
        } else {
            return !Dates.isWeekend(date);
        }
    }

}
