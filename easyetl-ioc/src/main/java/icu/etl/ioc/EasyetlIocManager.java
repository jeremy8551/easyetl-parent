package icu.etl.ioc;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.StringUtils;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class EasyetlIocManager {

    /** 组件工厂集合 */
    private List<EasyetlIoc> list;

    public EasyetlIocManager(AnnotationEasyetlContext context) {
        this.list = new ArrayList<EasyetlIoc>();
        this.add(new EasyetlIocImpl(context));
    }

    public int indexOf(String name) {
        for (int i = 0; i < this.list.size(); i++) {
            EasyetlIoc old = this.list.get(i);
            if (old.getName().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    public EasyetlIoc add(EasyetlIoc ioc) {
        if (ioc == null || StringUtils.isBlank(ioc.getName())) {
            throw new IllegalArgumentException();
        }

        int index = this.indexOf(ioc.getName());
        if (index == -1) {
            this.list.add(ioc);
            return null;
        } else {
            EasyetlIoc old = this.list.get(index);
            this.list.set(index, ioc);
            return old;
        }
    }

    public EasyetlIoc remove(String name) {
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
            EasyetlIoc ioc = this.list.get(i);
            if ((bean = ioc.getBean(type, args)) != null) {
                return bean;
            }
        }
        return null;
    }

}
