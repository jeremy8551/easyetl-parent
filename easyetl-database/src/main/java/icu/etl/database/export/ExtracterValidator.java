package icu.etl.database.export;

import icu.etl.util.StringUtils;

public class ExtracterValidator {

    public ExtracterValidator() {
    }

    /**
     * 检查参数信息是否正确
     *
     * @param context
     */
    public void check(ExtracterContext context) {
        if (context.getCharFilter() == null) {
            context.setCharFilter("");
        }

        // 最大值不能小于零
        if (context.getMaximum() < 0) {
            context.setMaximum(0);
        }

        if (context.getDataSource() == null) {
            throw new NullPointerException();
        }

        if (context.getFormat() == null) {
            throw new NullPointerException();
        }

        if (StringUtils.isBlank(context.getSource())) {
            throw new IllegalArgumentException();
        }

        if (StringUtils.isBlank(context.getTarget())) {
            throw new IllegalArgumentException();
        }

        if (context.getCacheLines() <= 0) {
            context.setCacheLines(100);
        }

        if (context.getMaximum() < 0) {
            context.setMaximum(0);
        }
    }

}
