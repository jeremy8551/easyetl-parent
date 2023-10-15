package icu.etl.os;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.ioc.NationalHoliday;

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

}
