package icu.etl.ioc;

import java.util.ArrayList;

/**
 * 实现类集合
 *
 * @author jeremy8551@qq.com
 * @createtime 2021-02-08
 */
public class BeanAnnotationList extends ArrayList<BeanConfig> {
    private final static long serialVersionUID = 1L;

    /**
     * 初始化集合
     *
     * @param size 集合的容量
     */
    public BeanAnnotationList(int size) {
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
     * @param <E> 元素类型
     * @return 返回类信息
     */
    public <E> Class<E> getOnlyOne() {
        if (this.size() == 0) {
            return null;
        } else {
            return this.get(0).getImplementClass();
        }
    }

}
