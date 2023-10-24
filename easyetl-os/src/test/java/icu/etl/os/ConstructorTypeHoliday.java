package icu.etl.os;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.ioc.NationalHoliday;
import icu.etl.util.Dates;

@EasyBeanClass(kind = "zh", mode = "cn", type = NationalHoliday.class)
public class ConstructorTypeHoliday implements NationalHoliday {

    Set<Date> set1 = new HashSet<Date>();
    Set<Date> set2 = new HashSet<Date>();

    public ConstructorTypeHoliday() {
    }

    @Override
    public Set<Date> getRestDays() {
        return set1;
    }

    @Override
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
