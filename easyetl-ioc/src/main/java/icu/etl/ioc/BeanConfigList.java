package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * 组件信息集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/24
 */
public class BeanConfigList extends ArrayList<BeanConfig> {

    /**
     * 判断组件信息集合中是否已添加了参数 {@code beanConfig}
     *
     * @param beanConfig 组件信息
     * @param c          组件信息的排序规则
     * @return 返回true表示已添加
     */
    public boolean contains(BeanConfig beanConfig, Comparator<BeanConfig> c) {
        for (int i = 0, size = this.size(); i < size; i++) {
            BeanConfig bean = this.get(i);

            if (c == null) {
                if (beanConfig.getBeanClass().equals(bean.getBeanClass())) {
                    return true;
                }
            } else if (c.compare(beanConfig, bean) == 0) {
                return true;
            }
        }
        return false;
    }

}
