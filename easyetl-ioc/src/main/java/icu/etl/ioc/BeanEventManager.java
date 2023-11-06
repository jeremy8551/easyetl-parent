package icu.etl.ioc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class BeanEventManager {

    private EasyContext context;

    private final List<BeanEventListener> list;

    public BeanEventManager(EasyContext context) {
        this.list = new ArrayList<BeanEventListener>();
        this.context = context;
    }

    public void addListener(BeanEventListener listener) {
        this.list.add(listener);
    }

    public void addBeanEvent(BeanInfoRegister beanInfo) {
        for (BeanEventListener listener : this.list) {
            listener.addBean(new BeanEventImpl(this.context, beanInfo));
        }
    }

    public void removeBeanEvent(BeanInfoRegister beanInfo) {
        for (BeanEventListener listener : this.list) {
            listener.removeBean(new BeanEventImpl(this.context, beanInfo));
        }
    }

    public void clear() {
        this.list.clear();
    }

}
