package icu.etl.os;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import icu.etl.annotation.EasyBean;
import icu.etl.cn.NationalHoliday;
import icu.etl.util.Dates;

@EasyBean(name = "zh_cn")
public class ConstructorTypeHoliday implements NationalHoliday {

    Set<Date> set1 = new HashSet<Date>();
    Set<Date> set2 = new HashSet<Date>();

    public ConstructorTypeHoliday() {
    }

    public Set<Date> getRestDays() {
        return set1;
    }

    public Set<Date> getWorkDays() {
        return set2;
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
