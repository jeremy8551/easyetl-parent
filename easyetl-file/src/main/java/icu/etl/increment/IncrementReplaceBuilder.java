package icu.etl.increment;

import java.util.Date;

import icu.etl.annotation.EasyBean;
import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseTableColumnList;
import icu.etl.expression.Analysis;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.BeanInfo;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.Dates;
import icu.etl.util.StringUtils;

@EasyBean
public class IncrementReplaceBuilder implements BeanBuilder<IncrementReplace> {

    public IncrementReplace getBean(EasyContext context, Object... args) throws Exception {
        Analysis analysis = ArrayUtils.indexOf(args, Analysis.class, 0);
        String str = ArrayUtils.indexOf(args, String.class, 0); // 1:date-
        DatabaseTableColumnList columns = ArrayUtils.indexOf(args, DatabaseTableColumnList.class, 0);

        char mapdel = (analysis == null) ? ':' : analysis.getMapdel();
        String[] attributes = StringUtils.split(str, mapdel);
        String field = attributes[0];
        String value = attributes[1];

        if (StringUtils.startsWith(value, "date-", 0, true, false)) {
            return new DateReplace(columns, field, value.substring("date-".length()));
        } else if (value.equalsIgnoreCase("uuid")) {
            return new UUIDReplace(columns, field);
        } else { // 自定义
            String[] beans = StringUtils.split(value, '/');
            BeanInfo beanInfo = context.getBeanInfo(IncrementReplace.class, beans[0]);
            if (beanInfo == null) {
                return new StandardReplace(columns, field, value);
            } else {
                return context.createBean(beanInfo.getType());
            }
        }
    }

    /**
     * 将字段替换成日期
     */
    protected class DateReplace implements IncrementReplace {

        private int position;
        private String pattern;

        public DateReplace(DatabaseTableColumnList list, String nameOrPosition, String value) {
            DatabaseTableColumn column = null;
            if (list != null && (column = list.getColumn(nameOrPosition)) != null) {
                this.position = column.getPosition();
            } else if (StringUtils.isNumber(nameOrPosition)) {
                this.position = Integer.parseInt(nameOrPosition);
            } else {
                throw new IllegalArgumentException(nameOrPosition);
            }
            this.pattern = value;
        }

        public int getPosition() {
            return position;
        }

        public String getValue() {
            return Dates.format(new Date(), this.pattern);
        }
    }

    /**
     * 将字段替换成 UUID 值
     */
    protected class UUIDReplace implements IncrementReplace {

        private int position;

        public UUIDReplace(DatabaseTableColumnList list, String nameOrPosition) {
            DatabaseTableColumn column = null;
            if (list != null && (column = list.getColumn(nameOrPosition)) != null) {
                this.position = column.getPosition();
            } else if (StringUtils.isNumber(nameOrPosition)) {
                this.position = Integer.parseInt(nameOrPosition);
            } else {
                throw new IllegalArgumentException(nameOrPosition);
            }
        }

        public int getPosition() {
            return position;
        }

        public String getValue() {
            return StringUtils.toRandomUUID();
        }
    }

    /**
     * 将字段替换成指定值
     */
    protected class StandardReplace implements IncrementReplace {

        private int position;
        private String value;

        public StandardReplace(DatabaseTableColumnList list, String nameOrPosition, String value) {
            DatabaseTableColumn column = null;
            if (list != null && (column = list.getColumn(nameOrPosition)) != null) {
                this.position = column.getPosition();
            } else if (StringUtils.isNumber(nameOrPosition)) {
                this.position = Integer.parseInt(nameOrPosition);
            } else {
                throw new IllegalArgumentException(nameOrPosition);
            }
            this.value = value == null ? "" : value;
        }

        public int getPosition() {
            return position;
        }

        public String getValue() {
            return value;
        }
    }

}
