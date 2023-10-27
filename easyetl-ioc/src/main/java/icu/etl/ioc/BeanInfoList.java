package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import icu.etl.annotation.EasyBean;
import icu.etl.log.STD;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 组件信息集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/24
 */
public class BeanInfoList extends ArrayList<BeanInfoRegister> {

    /**
     * {@linkplain BeanInfoList} 中组件信息所归属的类或接口
     */
    private Class<?> type;

    /**
     * 初始化
     *
     * @param type 集合中元素所属的组件类信息
     */
    public BeanInfoList(Class<?> type) {
        super(10);
        this.type = type;
    }

    public boolean add(BeanInfoRegister beanInfo, Comparator<BeanInfoRegister> comparator) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getClassMessage(21, type.getName(), beanInfo.getType().getName()));
        }
        return !this.contains(beanInfo, comparator) && this.add(beanInfo);
    }

    /**
     * 判断组件信息集合中是否已添加了参数 {@code beanClass}
     *
     * @param beanInfo   组件信息
     * @param comparator 组件信息的排序规则
     * @return 返回true表示已添加
     */
    public boolean contains(BeanInfoRegister beanInfo, Comparator<BeanInfoRegister> comparator) {
        int size = this.size();
        if (comparator == null) {
            for (int i = 0; i < size; i++) {
                BeanInfoRegister bean = this.get(i);
                if (beanInfo.compare(beanInfo, bean) == 0) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                BeanInfoRegister bean = this.get(i);
                if (comparator.compare(beanInfo, bean) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否包含参数 {@code type}
     *
     * @param type 类信息
     * @return 返回true表示包含
     */
    public boolean contains(Class<?> type) {
        for (BeanInfoRegister beanInfo : this) {
            if (beanInfo.equals(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查询与参数 {@code name} 相等的组件名（{@linkplain EasyBean#name()}）的集合
     *
     * @param name 组件名
     * @return 组件信息集合
     */
    public BeanInfoList indexOf(String name) {
        if (StringUtils.isBlank(name)) {
            return this;
        }

        int size = this.size();
        BeanInfoList list = new BeanInfoList(this.type);
        for (int i = 0; i < size; i++) {
            BeanInfoRegister beanInfo = this.get(i);
            if (beanInfo.equals(name)) {
                list.add(beanInfo);
            }
        }
        return list;
    }

    public BeanInfoList indexOf(BeanFilter filter) {
        int size = this.size();
        BeanInfoList list = new BeanInfoList(this.type);
        for (int i = 0; i < size; i++) {
            BeanInfoRegister beanInfo = this.get(i);
            if (filter.accept(beanInfo)) {
                list.add(beanInfo);
            }
        }
        return list;
    }

    /**
     * 返回组件的实现类，如果多个组件名重复了，则取同名下排序值最大的组件
     * <p>
     * 因为集合元素是按优先级从大到小排序的，所以直接判断第一个元素与第二个元素的排序值是否相等，即可判断出来是否有重复组件
     *
     * @return 组件信息
     * @throws RepeatDefineBeanException 有重复组件会报错
     */
    public BeanInfoRegister getBeanInfo() {
        int size = this.size();
        if (size == 0) {
            return null;
        }

        BeanInfoRegister first = this.get(0); // 第一个元素
        if (size == 1) {
            return first;
        }

        if (first.getLevel() == this.get(1).getLevel()) { // 判断排序值是否相等
            throw new RepeatDefineBeanException(this.type, first.getName(), this);
        } else {
            return first;
        }
    }

    /**
     * 对于同名的组件，按 {@linkplain EasyBean#level()} 倒序进行排序
     */
    public void sortByDesc() {
        if (this.size() > 0) {
            Collections.sort(this, this.get(0));
        }
    }

    /**
     * 判断类信息是否唯一
     *
     * @return 返回true表示集合中元素为1或0
     */
    public boolean onlyOne() {
        return this.size() <= 1;
    }

    /**
     * 返回唯一的实现类信息
     *
     * @return 返回第一个元素
     */
    public BeanInfoRegister getOnlyOne() {
        return this.size() == 0 ? null : this.get(0);
    }

}