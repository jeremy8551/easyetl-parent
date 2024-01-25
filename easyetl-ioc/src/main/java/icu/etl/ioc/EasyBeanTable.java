package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import icu.etl.util.Ensure;

/**
 * 组件信息表
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/25
 */
public class EasyBeanTable {

    /** 容器上下文信息 */
    private EasyContext context;

    /** 组件（接口或类）与实现类的映射关系 */
    private LinkedHashMap<Class<?>, EasyBeanTableRow> rows;

    public EasyBeanTable(EasyContext context) {
        this.context = Ensure.notNull(context);
        this.rows = new LinkedHashMap<Class<?>, EasyBeanTableRow>(50);
    }

    /**
     * 清空管理器
     */
    public void clear() {
        this.rows.clear();
    }

    /**
     * 移除某个组件的所有实现类
     *
     * @param type 组件的类信息
     * @return 组件的所有实现类
     */
    public EasyBeanTableRow remove(Class<?> type) {
        return this.rows.remove(type);
    }

    /**
     * 查找组件的所有实现类
     *
     * @param type 组件的类信息
     * @return 组件的所有实现类
     */
    public EasyBeanTableRow get(Class<?> type) {
        EasyBeanTableRow list = this.rows.get(type);
        if (list == null) {
            list = new EasyBeanTableRow(type);
            this.rows.put(type, list);
        }
        return list;
    }

    /**
     * 查找所有非延迟加载的组件信息
     *
     * @return 组件信息集合
     */
    public List<EasyBeanInfoValue> getNolazyBeanInfoList() {
        List<EasyBeanInfoValue> nolazys = new ArrayList<EasyBeanInfoValue>();
        Collection<EasyBeanTableRow> values = this.rows.values();
        for (EasyBeanTableRow list : values) {
            for (EasyBeanInfoValue beanInfo : list) {
                if (!beanInfo.isLazy()) {
                    nolazys.add(beanInfo);
                }
            }
        }
        return nolazys;
    }

    /**
     * 返回已注册的组件的类信息
     *
     * @return 类信息集合
     */
    public Set<Class<?>> keySet() {
        return this.rows.keySet();
    }

    /**
     * 刷新
     */
    public void refresh() {
        // 对同名的组件, 按优先级排序
        Collection<EasyBeanTableRow> values = this.rows.values();
        for (EasyBeanTableRow list : values) {
            list.sortByDesc();
        }

        // 处理非延迟加载的组件
        List<EasyBeanInfoValue> list = this.getNolazyBeanInfoList();
        for (EasyBeanInfoValue beanInfo : list) {
            if (beanInfo.getBean() == null) {
                beanInfo.setBean(this.context.createBean(beanInfo.getType()));
            }
        }
    }

}
