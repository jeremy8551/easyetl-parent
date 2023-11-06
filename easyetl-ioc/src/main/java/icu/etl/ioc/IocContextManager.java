package icu.etl.ioc;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.StringUtils;

/**
 * 容器的管理器
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class IocContextManager {

    /** 组件工厂集合 */
    private final List<IocContext> list;

    public IocContextManager(EasyBeanContext context) {
        this.list = new ArrayList<IocContext>();
        this.add(new IocContextImpl(context));
    }

    public int indexOf(String name) {
        for (int i = 0; i < this.list.size(); i++) {
            IocContext old = this.list.get(i);
            if (old.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    public IocContext add(IocContext ioc) {
        if (ioc == null || StringUtils.isBlank(ioc.getName())) {
            throw new IllegalArgumentException();
        }

        int index = this.indexOf(ioc.getName());
        if (index == -1) {
            this.list.add(ioc);
            return null;
        } else {
            IocContext old = this.list.get(index);
            this.list.set(index, ioc);
            return old;
        }
    }

    public IocContext remove(String name) {
        int index = this.indexOf(name);
        if (index == -1) {
            return null;
        } else {
            return this.list.remove(index);
        }
    }

    public <E> E getBean(Class<E> type, Object[] args) {
        E bean;
        for (int i = 0; i < this.list.size(); i++) {
            IocContext ioc = this.list.get(i);
            if ((bean = ioc.getBean(type, args)) != null) {
                return bean;
            }
        }
        return null;
    }

}
