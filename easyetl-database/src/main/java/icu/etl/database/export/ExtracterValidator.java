package icu.etl.database.export;

import icu.etl.util.Ensure;

public class ExtracterValidator {

    public ExtracterValidator() {
    }

    /**
     * 检查参数信息是否正确
     *
     * @param context 卸数引擎上下文信息
     */
    public void check(ExtracterContext context) {
        if (context.getCharFilter() == null) {
            context.setCharFilter("");
        }

        // 最大值不能小于零
        if (context.getMaximum() < 0) {
            context.setMaximum(0);
        }

        Ensure.notNull(context.getDataSource());
        Ensure.notNull(context.getFormat());
        Ensure.notBlank(context.getSource());
        Ensure.notBlank(context.getTarget());

        if (context.getCacheLines() <= 0) {
            context.setCacheLines(100);
        }

        if (context.getMaximum() < 0) {
            context.setMaximum(0);
        }
    }

}
