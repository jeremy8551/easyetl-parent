package icu.etl.zip;

import java.io.File;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.BeanInfo;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * 压缩工具工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-09
 */
@EasyBean
public class CompressBuilder implements BeanBuilder<Compress> {

    public Compress getBean(EasyContext context, Object... args) throws Exception {
        String suffix = null;

        File file = ArrayUtils.indexOf(args, File.class, 0);
        if (file != null) {
            suffix = FileUtils.getFilenameExt(file.getName());
        }

        if (StringUtils.isBlank(suffix)) {
            suffix = ArrayUtils.indexOf(args, String.class, 0);
        }

        // 设置默认值
        if (StringUtils.isBlank(suffix)) {
            suffix = "zip";
        }

        BeanInfo beanInfo = context.getBeanInfo(Compress.class, suffix);
        if (beanInfo == null) {
            throw new UnsupportedOperationException(suffix);
        } else {
            return context.createBean(beanInfo.getType());
        }
    }

}
