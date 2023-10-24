package icu.etl.zip;

import java.io.File;

import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.EasyetlContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.ClassUtils;
import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * 压缩工具工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-09
 */
public class CompressBuilder implements BeanBuilder<Compress> {

    public Compress build(EasyetlContext context, Object... array) throws Exception {
        String suffix = null;

        File file = ArrayUtils.indexOf(array, File.class, 0);
        if (file != null) {
            suffix = FileUtils.getFilenameExt(file.getName());
        }

        if (StringUtils.isBlank(suffix)) {
            suffix = ArrayUtils.indexOf(array, String.class, 0);
        }

        // 设置默认值
        if (StringUtils.isBlank(suffix)) {
            suffix = "zip";
        }

        Class<Compress> cls = context.getImplement(Compress.class, suffix);
        if (cls == null) {
            throw new UnsupportedOperationException(suffix);
        } else {
            return ClassUtils.newInstance(cls);
        }
    }

}
