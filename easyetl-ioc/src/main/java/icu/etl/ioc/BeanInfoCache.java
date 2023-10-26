package icu.etl.ioc;

import java.util.ArrayList;

/**
 * 实现类的缓存
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class BeanInfoCache<E> extends ArrayList<E> {
    private final static long serialVersionUID = 1L;

    /**
     * 初始化集合
     *
     * @param size 集合的容量
     */
    public BeanInfoCache(int size) {
        super(size);
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
    public E getOnlyOne() {
        if (this.size() == 0) {
            return null;
        } else {
            return this.get(0);
        }
    }

}
