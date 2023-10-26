package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import icu.etl.annotation.EasyBean;
import icu.etl.log.STD;
import icu.etl.util.ClassUtils;
import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 组件信息集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/24
 */
public class BeanInfoList extends ArrayList<BeanInfo> implements Comparator<BeanInfo> {

    private BeanInfoManager manager;

    /**
     * {@linkplain BeanInfoList} 中组件信息所归属的类或接口
     */
    private Class<?> type;

    /**
     * 初始化
     *
     * @param type 集合中元素所属的组件类信息
     */
    public BeanInfoList(BeanInfoManager manager, Class<?> type) {
        super(10);
        this.manager = manager;
        this.type = type;
    }

    public boolean add(BeanInfo beanInfo, Comparator<BeanInfo> comparator) {
        if (STD.out.isDebugEnabled()) {
            STD.out.debug(ResourcesUtils.getClassMessage(21, type.getName(), beanInfo.getType().getName()));
        }
        return !this.contains(beanInfo, comparator) && this.add(beanInfo);
    }

    public void push(BeanInfo beanInfo) {
        super.add(beanInfo);
    }

    public boolean add(BeanInfo beanInfo) {
        super.add(beanInfo);
        this.manager.addBeanEvent(beanInfo);
        return true;
    }

    public boolean remove(Object o) {
        int index = super.indexOf(o);
        if (index == -1) {
            return false;
        } else {
            BeanInfo beanInfo = super.remove(index);
            this.manager.removeBeanEvent(beanInfo);
            return true;
        }
    }

    public BeanInfo remove(int index) {
        BeanInfo beanInfo = super.remove(index);
        if (beanInfo != null) {
            this.manager.removeBeanEvent(beanInfo);
        }
        return beanInfo;
    }

    /**
     * 判断组件信息集合中是否已添加了参数 {@code beanClass}
     *
     * @param beanInfo 组件信息
     * @param c        组件信息的排序规则
     * @return 返回true表示已添加
     */
    public boolean contains(BeanInfo beanInfo, Comparator<BeanInfo> c) {
        if (c == null) {
            for (int i = 0, size = this.size(); i < size; i++) {
                BeanInfo bean = this.get(i);
                if (ClassUtils.equals(beanInfo.getType(), bean.getType())) {
                    return true;
                }
            }
        } else {
            for (int i = 0, size = this.size(); i < size; i++) {
                BeanInfo bean = this.get(i);
                if (c.compare(beanInfo, bean) == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 判断是否包含参数 {@code cls}
     *
     * @param cls 类信息
     * @return 返回true表示包含
     */
    public boolean contains(Class<?> cls) {
        for (BeanInfo beanInfo : this) {
            if (beanInfo.equals(cls)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 搜索组件名（{@linkplain EasyBean#name()}）与参数 {@code name} 相等的组件信息集合
     *
     * @param name 组件名
     * @return 组件信息集合
     */
    public BeanInfoList indexOf(String name) {
        if (StringUtils.isBlank(name)) {
            return this;
        }

        int size = this.size();
        BeanInfoList list = new BeanInfoList(this.manager, this.type);
        for (int i = 0; i < size; i++) {
            BeanInfo beanInfo = this.get(i);
            if (beanInfo.equals(name)) {
                list.push(beanInfo);
            }
        }
        return list;
    }

    public BeanInfoList indexOf(BeanFilter filter) {
        int size = this.size();
        BeanInfoList list = new BeanInfoList(this.manager, this.type);
        for (int i = 0; i < size; i++) {
            BeanInfo beanInfo = this.get(i);
            if (filter.accept(beanInfo)) {
                list.push(beanInfo);
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
    public BeanInfo getBeanInfo() {
        int size = this.size();
        if (size == 0) {
            return null;
        }

        BeanInfo first = this.get(0); // 第一个元素
        if (size == 1) {
            return first;
        }

        if (first.getOrder() == this.get(1).getOrder()) { // 判断排序值是否相等
            throw new RepeatDefineBeanException(this.type, first.getName(), this);
        } else {
            return first;
        }
    }

    public int compare(BeanInfo o1, BeanInfo o2) {
        int val = o1.getName().compareTo(o2.getName());
        if (val == 0) {
            return o2.getOrder() - o1.getOrder(); // 倒序排序
        } else {
            return val;
        }
    }

    /**
     * 对于同名的组件，按 {@linkplain EasyBean#level()} 倒序进行排序
     */
    public void sortByDesc() {
        Collections.sort(this, this);
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
    public BeanInfo getOnlyOne() {
        if (this.size() == 0) {
            return null;
        } else {
            return this.get(0);
        }
    }

}