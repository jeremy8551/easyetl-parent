package icu.etl.ioc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * 组件信息管理器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/25
 */
public class BeanInfoManager {

    private EasyContext context;

    /** 组件（接口或类）与实现类的映射关系 */
    private final LinkedHashMap<Class<?>, BeanInfoList> map;

    public BeanInfoManager(EasyContext context) {
        this.map = new LinkedHashMap<Class<?>, BeanInfoList>(50);
        this.context = context;
    }

    public void clear() {
        this.map.clear();
    }

    public BeanInfoList remove(Class<?> type) {
        return this.map.remove(type);
    }

    public BeanInfoList get(Class<?> type) {
        BeanInfoList list = this.map.get(type);
        if (list == null) {
            list = new BeanInfoList(type);
            this.map.put(type, list);
        }
        return list;
    }

    public List<BeanInfoRegister> getNolazyBeanInfoList() {
        List<BeanInfoRegister> nolazys = new ArrayList<BeanInfoRegister>();
        Collection<BeanInfoList> values = this.map.values();
        for (BeanInfoList list : values) {
            for (BeanInfoRegister beanInfo : list) {
                if (!beanInfo.isLazy()) {
                    nolazys.add(beanInfo);
                }
            }
        }
        return nolazys;
    }

    public Set<Class<?>> keySet() {
        return this.map.keySet();
    }

    public void refresh() {
        // 对同名的组件, 按优先级排序
        Collection<BeanInfoList> values = this.map.values();
        for (BeanInfoList list : values) {
            list.sortByDesc();
        }

        // 处理即时加载组件
        List<BeanInfoRegister> list = this.getNolazyBeanInfoList();
        for (BeanInfoRegister beanInfo : list) {
            if (beanInfo.getBean() == null) {
                beanInfo.setBean(this.context.createBean(beanInfo.getType()));
            }
        }
    }

}
