package icu.etl.database.export.inernal;

import icu.etl.annotation.EasyBean;
import icu.etl.database.export.ExtractReader;
import icu.etl.database.export.ExtracterContext;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.BeanInfo;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

/**
 * 数据输入流
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-18
 */
@EasyBean
public class ReaderBuilder implements BeanBuilder<ExtractReader> {

    public ExtractReader getBean(EasyContext context, Object... args) throws Exception {
        ExtracterContext cxt = ArrayUtils.indexOf(args, ExtracterContext.class, 0);
        String source = cxt.getSource();
        if (StringUtils.startsWith(source, "select", 0, true, true)) {
            return new DatabaseReader(cxt);
        } else if (ClassUtils.forName(source, false, context.getClassLoader()) != null) {
            return (ExtractReader) ClassUtils.newInstance(source, context.getClassLoader());
        }

        // 解析 http://xxx/xxx/xxx 格式
        String[] split = StringUtils.split(source, "://");
        if (split.length > 0) {
            BeanInfo beanInfo = context.getBeanInfo(ExtractReader.class, split[0]);
            if (beanInfo != null) {
                return ClassUtils.newInstance(beanInfo.getType());
            }
        }

        throw new UnsupportedOperationException(source);
    }

}
