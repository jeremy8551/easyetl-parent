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

    /** 组件（接口或类）与实现类的映射关系 */
    private final LinkedHashMap<Class<?>, BeanInfoList> map;

    private final List<BeanEventListener> listeners;

    public BeanInfoManager() {
        this.map = new LinkedHashMap<Class<?>, BeanInfoList>(50);
        this.listeners = new ArrayList<BeanEventListener>();
    }

    public void clear() {
        this.map.clear();
        this.listeners.clear();
    }

    public BeanInfoList remove(Class<?> type) {
        return this.map.remove(type);
    }

    public BeanInfoList get(Class<?> type) {
        BeanInfoList list = this.map.get(type);
        if (list == null) {
            list = new BeanInfoList(this, type);
            this.map.put(type, list);
        }
        return list;
    }

    public List<BeanInfo> getNolazyBeanInfoList() {
        List<BeanInfo> nolazys = new ArrayList<BeanInfo>();
        Collection<BeanInfoList> values = this.map.values();
        for (BeanInfoList list : values) {
            for (BeanInfo beanInfo : list) {
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

    public void addListener(BeanEventListener listener) {
        this.listeners.add(listener);
    }

    public void addBeanEvent(BeanInfo beanInfo) {
        for (BeanEventListener listener : this.listeners) {
            listener.addBean(new StandardBeanEvent(beanInfo));
        }
    }

    public void removeBeanEvent(BeanInfo beanInfo) {
        for (BeanEventListener listener : this.listeners) {
            listener.removeBean(new StandardBeanEvent(beanInfo));
        }
    }

    public void refresh() {
        Collection<BeanInfoList> values = this.map.values();
        for (BeanInfoList list : values) {
            list.sortByDesc();
        }
    }

}
