package icu.etl.increment;

import java.util.Date;

import icu.etl.database.DatabaseTableColumn;
import icu.etl.database.DatabaseTableColumnList;
import icu.etl.expression.Analysis;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.BeanContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.Dates;
import icu.etl.util.StringUtils;

public class IncrementReplaceBuilder implements BeanBuilder<IncrementReplace> {

    public IncrementReplace build(BeanContext context, Object... array) throws Exception {
        Analysis analysis = ArrayUtils.indexOf(array, Analysis.class, 0);
        String str = ArrayUtils.indexOf(array, String.class, 0); // 1:date-
        DatabaseTableColumnList columns = ArrayUtils.indexOf(array, DatabaseTableColumnList.class, 0);

        char mapdel = (analysis == null) ? ':' : analysis.getMapdel();
        String[] attributes = StringUtils.split(str, mapdel);
        String field = attributes[0];
        String value = attributes[1];

        if (StringUtils.startsWith(value, "date-", 0, true, false)) {
            return new DateReplace(columns, field, value.substring("date-".length()));
        } else if (value.equalsIgnoreCase("uuid")) {
            return new UUIDReplace(columns, field);
        } else { // 自定义
            Object[] beans = StringUtils.split(value, '/');
            Class<IncrementReplace> cls = context.getImplement(IncrementReplace.class, beans);
            if (cls == null) {
                return new StandardReplace(columns, field, value);
            } else {
                return ClassUtils.newInstance(cls);
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
