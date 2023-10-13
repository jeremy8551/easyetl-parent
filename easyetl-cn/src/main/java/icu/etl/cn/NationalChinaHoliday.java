package icu.etl.cn;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.ioc.NationalHoliday;
import icu.etl.util.Dates;

@EasyBeanClass(kind = "zh", mode = "cn", major = "", minor = "", description = "2013-2023中国法定节假日", type = NationalHoliday.class)
public class NationalChinaHoliday implements NationalHoliday {

    private Set<Date> reset;
    private Set<Date> works;

    public NationalChinaHoliday() {
        this.reset = new HashSet<Date>();
        this.works = new HashSet<Date>();

        // 2013年法定假日安排
        this.reset.add(Dates.parse("2013-01-01")); // 元旦
        this.reset.add(Dates.parse("2013-01-02")); // 元旦
        this.reset.add(Dates.parse("2013-01-03")); // 元旦
        this.reset.add(Dates.parse("2013-02-09")); // 春节
        this.reset.add(Dates.parse("2013-02-10")); // 春节
        this.reset.add(Dates.parse("2013-02-11")); // 春节
        this.reset.add(Dates.parse("2013-02-12")); // 春节
        this.reset.add(Dates.parse("2013-02-13")); // 春节
        this.reset.add(Dates.parse("2013-02-14")); // 春节
        this.reset.add(Dates.parse("2013-02-15")); // 春节
        this.reset.add(Dates.parse("2013-04-04")); // 清明节
        this.reset.add(Dates.parse("2013-04-05")); // 清明节
        this.reset.add(Dates.parse("2013-04-06")); // 清明节
        this.reset.add(Dates.parse("2013-05-01")); // 劳动节
        this.reset.add(Dates.parse("2013-06-10")); // 端午节
        this.reset.add(Dates.parse("2013-06-11")); // 端午节
        this.reset.add(Dates.parse("2013-06-12")); // 端午节
        this.reset.add(Dates.parse("2013-09-19")); // 中秋节
        this.reset.add(Dates.parse("2013-09-20")); // 中秋节
        this.reset.add(Dates.parse("2013-09-21")); // 中秋节
        this.reset.add(Dates.parse("2013-10-01")); // 国庆节
        this.reset.add(Dates.parse("2013-10-02")); // 国庆节
        this.reset.add(Dates.parse("2013-10-03")); // 国庆节
        this.reset.add(Dates.parse("2013-10-04")); // 国庆节
        this.reset.add(Dates.parse("2013-10-05")); // 国庆节
        this.reset.add(Dates.parse("2013-10-06")); // 国庆节
        this.reset.add(Dates.parse("2013-10-07")); // 国庆节
        this.works.add(Dates.parse("2013-01-05")); // 元旦
        this.works.add(Dates.parse("2013-01-06")); // 元旦
        this.works.add(Dates.parse("2013-02-16")); // 春节
        this.works.add(Dates.parse("2013-02-17")); // 春节
        this.works.add(Dates.parse("2013-04-07")); // 清明节
        this.works.add(Dates.parse("2013-04-27")); // 劳动节
        this.works.add(Dates.parse("2013-04-28")); // 劳动节
        this.works.add(Dates.parse("2013-06-08")); // 端午节
        this.works.add(Dates.parse("2013-06-09")); // 端午节
        this.works.add(Dates.parse("2013-09-22")); // 中秋节
        this.works.add(Dates.parse("2013-09-29")); // 国庆节
        this.works.add(Dates.parse("2013-10-12")); // 国庆节

        // 2014年安排
        this.reset.add(Dates.parse("2014-01-01")); // 元旦
        this.reset.add(Dates.parse("2014-01-31")); // 春节
        this.reset.add(Dates.parse("2014-02-01")); // 春节
        this.reset.add(Dates.parse("2014-02-02")); // 春节
        this.reset.add(Dates.parse("2014-02-03")); // 春节
        this.reset.add(Dates.parse("2014-02-04")); // 春节
        this.reset.add(Dates.parse("2014-02-05")); // 春节
        this.reset.add(Dates.parse("2014-02-06")); // 春节
        this.reset.add(Dates.parse("2014-04-05")); // 清明节
        this.reset.add(Dates.parse("2014-04-06")); // 清明节
        this.reset.add(Dates.parse("2014-04-07")); // 清明节
        this.reset.add(Dates.parse("2014-05-01")); // 劳动节
        this.reset.add(Dates.parse("2014-05-02")); // 劳动节
        this.reset.add(Dates.parse("2014-05-03")); // 劳动节
        this.reset.add(Dates.parse("2014-05-31")); // 端午节
        this.reset.add(Dates.parse("2014-06-01")); // 端午节
        this.reset.add(Dates.parse("2014-06-02")); // 端午节
        this.reset.add(Dates.parse("2014-09-06")); // 中秋节
        this.reset.add(Dates.parse("2014-09-07")); // 中秋节
        this.reset.add(Dates.parse("2014-09-08")); // 中秋节
        this.reset.add(Dates.parse("2014-10-01")); // 国庆节
        this.reset.add(Dates.parse("2014-10-02")); // 国庆节
        this.reset.add(Dates.parse("2014-10-03")); // 国庆节
        this.reset.add(Dates.parse("2014-10-04")); // 国庆节
        this.reset.add(Dates.parse("2014-10-05")); // 国庆节
        this.reset.add(Dates.parse("2014-10-06")); // 国庆节
        this.reset.add(Dates.parse("2014-10-07")); // 国庆节
        this.works.add(Dates.parse("2014-01-26")); // 春节
        this.works.add(Dates.parse("2014-02-08")); // 春节
        this.works.add(Dates.parse("2014-05-04")); // 劳动节
        this.works.add(Dates.parse("2014-09-28")); // 国庆节
        this.works.add(Dates.parse("2014-10-11")); // 国庆节

        // 2015年安排
        this.reset.add(Dates.parse("2015-01-01")); // 元旦
        this.reset.add(Dates.parse("2015-01-02")); // 元旦
        this.reset.add(Dates.parse("2015-01-03")); // 元旦
        this.reset.add(Dates.parse("2015-02-18")); // 春节
        this.reset.add(Dates.parse("2015-02-19")); // 春节
        this.reset.add(Dates.parse("2015-02-20")); // 春节
        this.reset.add(Dates.parse("2015-02-21")); // 春节
        this.reset.add(Dates.parse("2015-02-22")); // 春节
        this.reset.add(Dates.parse("2015-02-23")); // 春节
        this.reset.add(Dates.parse("2015-02-24")); // 春节
        this.reset.add(Dates.parse("2015-04-04")); // 清明节
        this.reset.add(Dates.parse("2015-04-05")); // 清明节
        this.reset.add(Dates.parse("2015-04-06")); // 清明节
        this.reset.add(Dates.parse("2015-05-01")); // 劳动节
        this.reset.add(Dates.parse("2015-05-02")); // 劳动节
        this.reset.add(Dates.parse("2015-05-03")); // 劳动节
        this.reset.add(Dates.parse("2015-06-20")); // 端午节
        this.reset.add(Dates.parse("2015-06-21")); // 端午节
        this.reset.add(Dates.parse("2015-06-22")); // 端午节
        this.reset.add(Dates.parse("2015-09-03")); // 抗日胜利
        this.reset.add(Dates.parse("2015-09-04")); // 抗日胜利
        this.reset.add(Dates.parse("2015-09-05")); // 抗日胜利
        this.reset.add(Dates.parse("2015-09-26")); // 中秋节
        this.reset.add(Dates.parse("2015-09-27")); // 中秋节
        this.reset.add(Dates.parse("2015-10-01")); // 国庆节
        this.reset.add(Dates.parse("2015-10-02")); // 国庆节
        this.reset.add(Dates.parse("2015-10-03")); // 国庆节
        this.reset.add(Dates.parse("2015-10-04")); // 国庆节
        this.reset.add(Dates.parse("2015-10-05")); // 国庆节
        this.reset.add(Dates.parse("2015-10-06")); // 国庆节
        this.reset.add(Dates.parse("2015-10-07")); // 国庆节
        this.works.add(Dates.parse("2015-01-04")); // 元旦
        this.works.add(Dates.parse("2015-02-15")); // 春节
        this.works.add(Dates.parse("2015-02-16")); // 春节
        this.works.add(Dates.parse("2015-02-17")); // 春节
        this.works.add(Dates.parse("2015-02-25")); // 春节
        this.works.add(Dates.parse("2015-02-26")); // 春节
        this.works.add(Dates.parse("2015-02-27")); // 春节
        this.works.add(Dates.parse("2015-02-28")); // 春节
        this.works.add(Dates.parse("2015-09-06")); // 抗日胜利
        this.works.add(Dates.parse("2015-10-08")); // 国庆节
        this.works.add(Dates.parse("2015-10-09")); // 国庆节
        this.works.add(Dates.parse("2015-10-10")); // 国庆节

        // 2016年节假日安排
        this.reset.add(Dates.parse("2016-01-01")); // 元旦
        this.reset.add(Dates.parse("2016-01-02")); // 元旦
        this.reset.add(Dates.parse("2016-01-03")); // 元旦
        this.reset.add(Dates.parse("2016-02-07")); // 春节
        this.reset.add(Dates.parse("2016-02-08")); // 春节
        this.reset.add(Dates.parse("2016-02-09")); // 春节
        this.reset.add(Dates.parse("2016-02-10")); // 春节
        this.reset.add(Dates.parse("2016-02-11")); // 春节
        this.reset.add(Dates.parse("2016-02-12")); // 春节
        this.reset.add(Dates.parse("2016-02-13")); // 春节
        this.reset.add(Dates.parse("2016-04-02")); // 清明节
        this.reset.add(Dates.parse("2016-04-03")); // 清明节
        this.reset.add(Dates.parse("2016-04-04")); // 清明节
        this.reset.add(Dates.parse("2016-04-30")); // 劳动节
        this.reset.add(Dates.parse("2016-05-01")); // 劳动节
        this.reset.add(Dates.parse("2016-05-02")); // 劳动节
        this.reset.add(Dates.parse("2016-06-09")); // 端午节
        this.reset.add(Dates.parse("2016-06-10")); // 端午节
        this.reset.add(Dates.parse("2016-06-11")); // 端午节
        this.reset.add(Dates.parse("2016-09-15")); // 中秋节
        this.reset.add(Dates.parse("2016-09-16")); // 中秋节
        this.reset.add(Dates.parse("2016-09-17")); // 中秋节
        this.reset.add(Dates.parse("2016-10-01")); // 国庆节
        this.reset.add(Dates.parse("2016-10-02")); // 国庆节
        this.reset.add(Dates.parse("2016-10-03")); // 国庆节
        this.reset.add(Dates.parse("2016-10-04")); // 国庆节
        this.reset.add(Dates.parse("2016-10-05")); // 国庆节
        this.reset.add(Dates.parse("2016-10-06")); // 国庆节
        this.reset.add(Dates.parse("2016-10-07")); // 国庆节
        this.reset.add(Dates.parse("2016-12-31")); // 元旦
        this.works.add(Dates.parse("2016-02-06")); // 春节
        this.works.add(Dates.parse("2016-02-14")); // 春节
        this.works.add(Dates.parse("2016-06-12")); // 端午
        this.works.add(Dates.parse("2016-09-18")); // 中秋节
        this.works.add(Dates.parse("2016-10-08")); // 国庆节
        this.works.add(Dates.parse("2016-10-09")); // 国庆节

        // 2017年节假日安排
        this.reset.add(Dates.parse("2017-01-01")); // 元旦
        this.reset.add(Dates.parse("2017-01-02")); // 元旦
        this.reset.add(Dates.parse("2017-01-27")); // 春节
        this.reset.add(Dates.parse("2017-01-28")); // 春节
        this.reset.add(Dates.parse("2017-01-29")); // 春节
        this.reset.add(Dates.parse("2017-01-30")); // 春节
        this.reset.add(Dates.parse("2017-01-31")); // 春节
        this.reset.add(Dates.parse("2017-02-01")); // 春节
        this.reset.add(Dates.parse("2017-02-02")); // 春节
        this.reset.add(Dates.parse("2017-04-02")); // 清明节
        this.reset.add(Dates.parse("2017-04-03")); // 清明节
        this.reset.add(Dates.parse("2017-04-04")); // 清明节
        this.reset.add(Dates.parse("2017-04-29")); // 劳动节
        this.reset.add(Dates.parse("2017-04-30")); // 劳动节
        this.reset.add(Dates.parse("2017-05-01")); // 劳动节
        this.reset.add(Dates.parse("2017-05-28")); // 端午节
        this.reset.add(Dates.parse("2017-05-29")); // 端午节
        this.reset.add(Dates.parse("2017-05-30")); // 端午节
        this.reset.add(Dates.parse("2017-10-01")); // 国庆节
        this.reset.add(Dates.parse("2017-10-02")); // 国庆节
        this.reset.add(Dates.parse("2017-10-03")); // 国庆节
        this.reset.add(Dates.parse("2017-10-04")); // 中秋节
        this.reset.add(Dates.parse("2017-10-05")); // 国庆节
        this.reset.add(Dates.parse("2017-10-06")); // 国庆节
        this.reset.add(Dates.parse("2017-10-07")); // 国庆节
        this.reset.add(Dates.parse("2017-10-08")); // 国庆节
        this.reset.add(Dates.parse("2017-12-30")); // 元旦
        this.reset.add(Dates.parse("2017-12-31")); // 元旦
        this.works.add(Dates.parse("2017-01-22")); // 春节
        this.works.add(Dates.parse("2017-02-04")); // 春节
        this.works.add(Dates.parse("2017-04-01")); // 清明
        this.works.add(Dates.parse("2017-05-27")); // 端午
        this.works.add(Dates.parse("2017-09-30")); // 国庆节

        // 2018年节假日安排
        this.reset.add(Dates.parse("2018-01-01")); // 元旦
        this.reset.add(Dates.parse("2018-02-15")); // 春节
        this.reset.add(Dates.parse("2018-02-16")); // 春节
        this.reset.add(Dates.parse("2018-02-17")); // 春节
        this.reset.add(Dates.parse("2018-02-18")); // 春节
        this.reset.add(Dates.parse("2018-02-19")); // 春节
        this.reset.add(Dates.parse("2018-02-20")); // 春节
        this.reset.add(Dates.parse("2018-02-21")); // 春节
        this.reset.add(Dates.parse("2018-04-05")); // 清明节
        this.reset.add(Dates.parse("2018-04-06")); // 清明节
        this.reset.add(Dates.parse("2018-04-07")); // 清明节
        this.reset.add(Dates.parse("2018-04-29")); // 劳动节
        this.reset.add(Dates.parse("2018-04-30")); // 劳动节
        this.reset.add(Dates.parse("2018-05-01")); // 劳动节
        this.reset.add(Dates.parse("2018-06-16")); // 端午节
        this.reset.add(Dates.parse("2018-06-17")); // 端午节
        this.reset.add(Dates.parse("2018-06-18")); // 端午节
        this.reset.add(Dates.parse("2018-09-22")); // 中秋节
        this.reset.add(Dates.parse("2018-09-23")); // 中秋节
        this.reset.add(Dates.parse("2018-09-24")); // 中秋节
        this.reset.add(Dates.parse("2018-10-01")); // 国庆节
        this.reset.add(Dates.parse("2018-10-02")); // 国庆节
        this.reset.add(Dates.parse("2018-10-03")); // 国庆节
        this.reset.add(Dates.parse("2018-10-04")); // 国庆节
        this.reset.add(Dates.parse("2018-10-05")); // 国庆节
        this.reset.add(Dates.parse("2018-10-06")); // 国庆节
        this.reset.add(Dates.parse("2018-10-07")); // 国庆节
        this.works.add(Dates.parse("2018-02-11")); // 春节
        this.works.add(Dates.parse("2018-02-24")); // 春节
        this.works.add(Dates.parse("2018-04-08")); // 清明
        this.works.add(Dates.parse("2018-04-28")); // 劳动节
        this.works.add(Dates.parse("2018-09-29")); // 国庆节
        this.works.add(Dates.parse("2018-09-30")); // 国庆节

        // 2019年休息安排
        this.reset.add(Dates.parse("2019-01-01")); // 元旦
        this.reset.add(Dates.parse("2019-02-04")); // 春节
        this.reset.add(Dates.parse("2019-02-05")); // 春节
        this.reset.add(Dates.parse("2019-02-06")); // 春节
        this.reset.add(Dates.parse("2019-02-07")); // 春节
        this.reset.add(Dates.parse("2019-02-08")); // 春节
        this.reset.add(Dates.parse("2019-02-09")); // 春节
        this.reset.add(Dates.parse("2019-02-10")); // 春节
        this.reset.add(Dates.parse("2019-04-05")); // 清明节
        this.reset.add(Dates.parse("2019-04-06")); // 清明节
        this.reset.add(Dates.parse("2019-04-07")); // 清明节
        this.reset.add(Dates.parse("2019-05-01")); // 劳动节
        this.reset.add(Dates.parse("2019-05-02")); // 劳动节
        this.reset.add(Dates.parse("2019-05-03")); // 劳动节
        this.reset.add(Dates.parse("2019-05-04")); // 劳动节
        this.reset.add(Dates.parse("2019-06-07")); // 端午节
        this.reset.add(Dates.parse("2019-06-08")); // 端午节
        this.reset.add(Dates.parse("2019-06-09")); // 端午节
        this.reset.add(Dates.parse("2019-09-13")); // 中秋节
        this.reset.add(Dates.parse("2019-09-14")); // 中秋节
        this.reset.add(Dates.parse("2019-09-15")); // 中秋节
        this.reset.add(Dates.parse("2019-10-01")); // 国庆节
        this.reset.add(Dates.parse("2019-10-02")); // 国庆节
        this.reset.add(Dates.parse("2019-10-03")); // 国庆节
        this.reset.add(Dates.parse("2019-10-04")); // 国庆节
        this.reset.add(Dates.parse("2019-10-05")); // 国庆节
        this.reset.add(Dates.parse("2019-10-06")); // 国庆节
        this.reset.add(Dates.parse("2019-10-07")); // 国庆节
        this.works.add(Dates.parse("2019-02-02")); // 春节
        this.works.add(Dates.parse("2019-02-03")); // 春节
        this.works.add(Dates.parse("2019-04-28")); // 劳动节
        this.works.add(Dates.parse("2019-05-05")); // 劳动节
        this.works.add(Dates.parse("2019-09-29")); // 国庆节
        this.works.add(Dates.parse("2019-10-12")); // 国庆节

        // 2020年休息安排
        this.reset.add(Dates.parse("2020-01-01")); // 元旦
        this.reset.add(Dates.parse("2020-01-24")); // 春节
        this.reset.add(Dates.parse("2020-01-25")); // 春节
        this.reset.add(Dates.parse("2020-01-26")); // 春节
        this.reset.add(Dates.parse("2020-01-27")); // 春节
        this.reset.add(Dates.parse("2020-01-28")); // 春节
        this.reset.add(Dates.parse("2020-01-29")); // 春节
        this.reset.add(Dates.parse("2020-01-30")); // 春节
        this.reset.add(Dates.parse("2020-04-04")); // 清明节
        this.reset.add(Dates.parse("2020-04-05")); // 清明节
        this.reset.add(Dates.parse("2020-04-06")); // 清明节
        this.reset.add(Dates.parse("2020-05-01")); // 劳动节
        this.reset.add(Dates.parse("2020-05-02")); // 劳动节
        this.reset.add(Dates.parse("2020-05-03")); // 劳动节
        this.reset.add(Dates.parse("2020-05-04")); // 劳动节
        this.reset.add(Dates.parse("2020-05-05")); // 劳动节
        this.reset.add(Dates.parse("2020-06-25")); // 端午节
        this.reset.add(Dates.parse("2020-06-26")); // 端午节
        this.reset.add(Dates.parse("2020-06-27")); // 端午节
        this.reset.add(Dates.parse("2020-10-01")); // 国庆节
        this.reset.add(Dates.parse("2020-10-02")); // 国庆节
        this.reset.add(Dates.parse("2020-10-03")); // 国庆节
        this.reset.add(Dates.parse("2020-10-04")); // 国庆节
        this.reset.add(Dates.parse("2020-10-05")); // 国庆节
        this.reset.add(Dates.parse("2020-10-06")); // 国庆节
        this.reset.add(Dates.parse("2020-10-07")); // 国庆节
        this.reset.add(Dates.parse("2020-10-08")); // 国庆节
        this.works.add(Dates.parse("2020-01-19")); // 春节
        this.works.add(Dates.parse("2020-02-01")); // 春节
        this.works.add(Dates.parse("2020-04-26")); // 劳动节
        this.works.add(Dates.parse("2020-05-09")); // 劳动节
        this.works.add(Dates.parse("2020-06-28")); // 端午节
        this.works.add(Dates.parse("2020-09-27")); // 国庆节
        this.works.add(Dates.parse("2020-10-10")); // 国庆节

        // 2021年休息安排
        this.reset.add(Dates.parse("2021-01-01")); // 元旦
        this.reset.add(Dates.parse("2021-01-02")); // 元旦
        this.reset.add(Dates.parse("2021-01-03")); // 元旦
        this.reset.add(Dates.parse("2021-02-11")); // 春节
        this.reset.add(Dates.parse("2021-02-12")); // 春节
        this.reset.add(Dates.parse("2021-02-13")); // 春节
        this.reset.add(Dates.parse("2021-02-14")); // 春节
        this.reset.add(Dates.parse("2021-02-15")); // 春节
        this.reset.add(Dates.parse("2021-02-16")); // 春节
        this.reset.add(Dates.parse("2021-02-17")); // 春节
        this.reset.add(Dates.parse("2021-04-03")); // 清明节
        this.reset.add(Dates.parse("2021-04-04")); // 清明节
        this.reset.add(Dates.parse("2021-04-05")); // 清明节
        this.reset.add(Dates.parse("2021-05-01")); // 劳动节
        this.reset.add(Dates.parse("2021-05-02")); // 劳动节
        this.reset.add(Dates.parse("2021-05-03")); // 劳动节
        this.reset.add(Dates.parse("2021-05-04")); // 劳动节
        this.reset.add(Dates.parse("2021-05-05")); // 劳动节
        this.reset.add(Dates.parse("2021-06-12")); // 端午节
        this.reset.add(Dates.parse("2021-06-13")); // 端午节
        this.reset.add(Dates.parse("2021-06-14")); // 端午节
        this.reset.add(Dates.parse("2021-09-19")); // 中秋节
        this.reset.add(Dates.parse("2021-09-20")); // 中秋节
        this.reset.add(Dates.parse("2021-09-21")); // 中秋节
        this.reset.add(Dates.parse("2021-10-01")); // 国庆节
        this.reset.add(Dates.parse("2021-10-02")); // 国庆节
        this.reset.add(Dates.parse("2021-10-03")); // 国庆节
        this.reset.add(Dates.parse("2021-10-04")); // 国庆节
        this.reset.add(Dates.parse("2021-10-05")); // 国庆节
        this.reset.add(Dates.parse("2021-10-06")); // 国庆节
        this.reset.add(Dates.parse("2021-10-07")); // 国庆节
        this.works.add(Dates.parse("2021-02-07")); // 春节
        this.works.add(Dates.parse("2021-02-20")); // 春节
        this.works.add(Dates.parse("2021-04-25")); // 劳动节
        this.works.add(Dates.parse("2021-05-08")); // 劳动节
        this.works.add(Dates.parse("2021-09-18")); // 中秋节
        this.works.add(Dates.parse("2021-09-26")); // 国庆节
        this.works.add(Dates.parse("2021-10-09")); // 国庆节

        // 2022年休息安排
        this.reset.add(Dates.parse("2022-01-01")); // 元旦
        this.reset.add(Dates.parse("2022-01-02")); // 元旦
        this.reset.add(Dates.parse("2022-01-03")); // 元旦
        this.works.add(Dates.parse("2022-01-29")); // 春节上班
        this.works.add(Dates.parse("2022-01-30")); // 春节上班
        this.reset.add(Dates.parse("2022-01-31")); // 春节
        this.reset.add(Dates.parse("2022-02-01")); // 春节
        this.reset.add(Dates.parse("2022-02-02")); // 春节
        this.reset.add(Dates.parse("2022-02-03")); // 春节
        this.reset.add(Dates.parse("2022-02-04")); // 春节
        this.reset.add(Dates.parse("2022-02-05")); // 春节
        this.reset.add(Dates.parse("2022-02-06")); // 春节
        this.works.add(Dates.parse("2022-04-02")); // 清明上班
        this.reset.add(Dates.parse("2022-04-03")); // 清明节
        this.reset.add(Dates.parse("2022-04-04")); // 清明节
        this.reset.add(Dates.parse("2022-04-05")); // 清明节
        this.works.add(Dates.parse("2022-04-24")); // 劳动节上班
        this.reset.add(Dates.parse("2022-04-30")); // 劳动节
        this.reset.add(Dates.parse("2022-05-01")); // 劳动节
        this.reset.add(Dates.parse("2022-05-02")); // 劳动节
        this.reset.add(Dates.parse("2022-05-03")); // 劳动节
        this.reset.add(Dates.parse("2022-05-04")); // 劳动节
        this.works.add(Dates.parse("2022-05-07")); // 劳动节上班
        this.reset.add(Dates.parse("2022-06-03")); // 端午节
        this.reset.add(Dates.parse("2022-06-04")); // 端午节
        this.reset.add(Dates.parse("2022-06-05")); // 端午节
        this.reset.add(Dates.parse("2022-09-10")); // 中秋节
        this.reset.add(Dates.parse("2022-09-11")); // 中秋节
        this.reset.add(Dates.parse("2022-09-12")); // 中秋节
        this.reset.add(Dates.parse("2022-10-01")); // 国庆节
        this.reset.add(Dates.parse("2022-10-02")); // 国庆节
        this.reset.add(Dates.parse("2022-10-03")); // 国庆节
        this.reset.add(Dates.parse("2022-10-04")); // 国庆节
        this.reset.add(Dates.parse("2022-10-05")); // 国庆节
        this.reset.add(Dates.parse("2022-10-06")); // 国庆节
        this.reset.add(Dates.parse("2022-10-07")); // 国庆节
        this.works.add(Dates.parse("2022-10-08")); // 国庆节
        this.works.add(Dates.parse("2022-10-09")); // 国庆节

        // 2023年休息安排
        this.reset.add(Dates.parse("2022-12-31")); // 元旦
        this.reset.add(Dates.parse("2023-01-01")); // 元旦
        this.reset.add(Dates.parse("2023-01-02")); // 元旦

        this.reset.add(Dates.parse("2023-01-21")); // 春节
        this.reset.add(Dates.parse("2023-01-22")); // 春节
        this.reset.add(Dates.parse("2023-01-23")); // 春节
        this.reset.add(Dates.parse("2023-01-24")); // 春节
        this.reset.add(Dates.parse("2023-01-25")); // 春节
        this.reset.add(Dates.parse("2023-01-26")); // 春节
        this.reset.add(Dates.parse("2023-01-27")); // 春节
        this.works.add(Dates.parse("2023-01-28")); // 春节上班
        this.works.add(Dates.parse("2023-01-29")); // 春节上班

        this.reset.add(Dates.parse("2023-04-05")); // 清明
        this.works.add(Dates.parse("2023-04-06")); // 清明上班

        this.works.add(Dates.parse("2023-04-23")); // 劳动节上班
        this.reset.add(Dates.parse("2023-04-29")); // 劳动节
        this.reset.add(Dates.parse("2023-04-30")); // 劳动节
        this.reset.add(Dates.parse("2023-05-01")); // 劳动节
        this.reset.add(Dates.parse("2023-05-02")); // 劳动节
        this.reset.add(Dates.parse("2023-05-03")); // 劳动节
        this.works.add(Dates.parse("2023-05-04")); // 劳动节上班
        this.works.add(Dates.parse("2023-05-05")); // 劳动节上班
        this.works.add(Dates.parse("2023-05-06")); // 劳动节上班

        this.reset.add(Dates.parse("2023-06-22")); // 端午节
        this.reset.add(Dates.parse("2023-06-23")); // 端午节
        this.reset.add(Dates.parse("2023-06-24")); // 端午节
        this.works.add(Dates.parse("2023-06-25")); // 端午节上班

        this.reset.add(Dates.parse("2023-09-29")); // 中秋节
        this.reset.add(Dates.parse("2023-09-30")); // 中秋节

        this.reset.add(Dates.parse("2023-10-01")); // 国庆节
        this.reset.add(Dates.parse("2023-10-02")); // 国庆节
        this.reset.add(Dates.parse("2023-10-03")); // 国庆节
        this.reset.add(Dates.parse("2023-10-04")); // 国庆节
        this.reset.add(Dates.parse("2023-10-05")); // 国庆节
        this.reset.add(Dates.parse("2023-10-06")); // 国庆节
        this.works.add(Dates.parse("2023-10-07")); // 国庆节上班
        this.works.add(Dates.parse("2023-10-08")); // 国庆节上班
    }

    public Set<Date> getRestDays() {
        return this.reset;
    }

    public Set<Date> getWorkDays() {
        return this.works;
    }

}
