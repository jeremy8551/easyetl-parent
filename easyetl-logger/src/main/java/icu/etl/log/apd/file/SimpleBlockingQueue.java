package icu.etl.log.apd.file;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 非阻塞队列的实现
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/12/2
 */
public class SimpleBlockingQueue<E> implements BlockingQueue<E> {

    private E obj;

    public SimpleBlockingQueue() {
    }

    public boolean add(E e) {
        return false;
    }

    public boolean offer(E e) {
        return false;
    }

    public E remove() {
        return null;
    }

    public E poll() {
        return null;
    }

    public E element() {
        return null;
    }

    public E peek() {
        return null;
    }

    public boolean offer(E e, long timeout, TimeUnit unit) {
        return false;
    }

    public void put(E e) {
        this.obj = e;
    }

    public E take() {
        return this.obj;
    }

    public E poll(long timeout, TimeUnit unit) {
        return null;
    }

    public int remainingCapacity() {
        return 0;
    }

    public boolean remove(Object o) {
        return false;
    }

    public boolean containsAll(Collection<?> c) {
        return false;
    }

    public boolean addAll(Collection<? extends E> c) {
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        return false;
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public void clear() {

    }

    public int size() {
        return 0;
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean contains(Object o) {
        return false;
    }

    public Iterator<E> iterator() {
        return null;
    }

    public Object[] toArray() {
        return new Object[0];
    }

    public <T> T[] toArray(T[] a) {
        return null;
    }

    public int drainTo(Collection<? super E> c) {
        return 0;
    }

    public int drainTo(Collection<? super E> c, int maxElements) {
        return 0;
    }
}
