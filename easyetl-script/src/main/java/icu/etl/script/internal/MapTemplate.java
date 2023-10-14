package icu.etl.script.internal;

import java.util.Enumeration;
import java.util.Hashtable;

import icu.etl.util.IO;

public class MapTemplate<E> {

    /** 属性信息集合 */
    protected Hashtable<String, E> map;

    /**
     * 初始化
     */
    public MapTemplate() {
        this.map = new Hashtable<String, E>();
    }

    /**
     * 判断属性是否存在
     *
     * @param name 属性名
     * @return
     */
    public boolean contains(String name) {
        return this.map.containsKey(name);
    }

    /**
     * 设置属性
     *
     * @param name  属性名
     * @param query 属性值
     */
    public E put(String name, E query) {
        E object = this.map.put(name, query);
        IO.close(object);
        return object;
    }

    /**
     * 返回属性值
     *
     * @param name 属性名
     * @return
     */
    public E get(String name) {
        return this.map.get(name);
    }

    /**
     * 删除属性
     *
     * @param name 属性名
     */
    public E remove(String name) {
        E object = this.map.get(name);
        IO.close(object);
        return this.map.remove(name);
    }

    /**
     * 关闭所有属性，并清空信息
     */
    public void close() {
        for (Enumeration<String> it = this.map.keys(); it.hasMoreElements(); ) {
            String name = it.nextElement();
            E object = this.map.get(name);
            IO.close(object);
        }
        this.map.clear();
    }

}
