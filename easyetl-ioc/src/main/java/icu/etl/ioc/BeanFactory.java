package icu.etl.ioc;

import java.util.List;

import icu.etl.annotation.EasyBeanClass;

/**
 * 组件工厂
 *
 * @author jeremy8551@qq.com
 */
public class BeanFactory {

    /** 组件工厂上下文信息 */
    private static BeanContext context;

    /**
     * 初始化
     */
    private BeanFactory() {
    }

    /**
     * 设置上下文信息
     *
     * @param context 上下文信息
     */
    public static void setContext(BeanContext context) {
        BeanFactory.context = context;
    }

    /**
     * 返回组件工厂的上下文信息
     *
     * @return 上下文信息
     */
    public static BeanContext getContext() {
        return BeanFactory.context;
    }

    /**
     * 创建指定类型的实例对象
     *
     * @param <E>   类型
     * @param clazz 接口信息 或 模版类信息
     * @param array 如果存在多个实现类时，会根据参数数组 array 中的值判断使用那个实现类 <br>
     *              第一个参数对应 {@link EasyBeanClass#kind()} 属性，表示一级分类 <br>
     *              第二个参数对应 {@link EasyBeanClass#mode()} 属性，表示一级分类下的子分类信息 <br>
     *              第三个参数对应 {@link EasyBeanClass#major()} 属性，表示大版本号，可以为空 <br>
     *              第四个参数对应 {@link EasyBeanClass#minor()} 属性，表示小版本号，可以为空 <br>
     * @return 实例对象
     */
    public static <E> E get(Class<E> clazz, Object... array) {
        List<BeanCreator> creators = context.getCreators();

        E obj;
        for (BeanCreator c : creators) {
            if ((obj = c.getBean(clazz, array)) != null) {
                return obj;
            }
        }
        return null;
    }

}