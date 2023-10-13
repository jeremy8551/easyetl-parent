package icu.etl.io;

import java.util.Iterator;

import icu.etl.util.StringUtils;

/**
 * 数组迭代器
 *
 * @param <E>
 * @author jeremy8551@qq.com
 * @createtime 2017-2-28
 */
public class ArrayIterator<E> implements Iterator<E> {

    /**
     * 对象数组
     */
    protected E[] array;

    /**
     * 遍历起始位置,从0开始
     */
    protected int index;

    /**
     * 遍历终止位置,从0开始
     */
    protected int endPos;

    /**
     * true表示可以使用 remove 函数移除数据
     */
    protected boolean remove;

    /**
     * 初始化
     *
     * @param array  数组
     * @param offset 数组起始位置
     * @param length 数组长度
     */
    public ArrayIterator(E[] array, int offset, int length) {
        super();
        this.array = array;
        this.index = offset;
        this.endPos = offset + length - 1;
        this.remove = false;
    }

    /**
     * 初始化
     *
     * @param array 数组
     */
    public ArrayIterator(E[] array) {
        this(array, 0, array.length);
    }

    /**
     * 如果仍有元素可以迭代，则返回 true
     */
    public synchronized boolean hasNext() {
        return index <= endPos;
    }

    /**
     * 返回迭代的下一个元素。重复调用此方法直到 hasNext() 方法返回 false，这将精确地一次性返回迭代器指向的集合中的所有元素。
     */
    public synchronized E next() {
        if (index < array.length) {
            remove = true;
            return array[index++];
        } else {
            return null;
        }
    }

    /**
     * 从迭代器指向的集合中移除迭代器返回的最后一个元素（可选操作）。每次调用 next 只能调用一次此方法
     */
    public synchronized void remove() {
        if (remove) {
            if (array.length == 0 || index == 0) {
                return;
            }

            E[] newArray = (E[]) new Object[array.length - 1];
            if (array.length == 1) {
                array = newArray;
                return;
            }

            index--;
            System.arraycopy(array, 0, newArray, 0, index);
            System.arraycopy(array, index + 1, newArray, index, newArray.length - index);
            array = newArray;
            endPos--;

            remove = false;
        }
    }

    /**
     * 迭代器当前指向元素的位置编号
     *
     * @return
     */
    public int index() {
        return this.index;
    }

    @Override
    public synchronized String toString() {
        return StringUtils.toString(array);
    }

    /**
     * 返回迭代器中的数组副本
     *
     * @return
     */
    public synchronized E[] getResult() {
        E[] newArray = (E[]) new Object[array.length];
        System.arraycopy(array, 0, newArray, 0, array.length);
        return array;
    }

    /**
     * 返回迭代器中的数组
     *
     * @return
     */
    public E[] value() {
        return array;
    }

}