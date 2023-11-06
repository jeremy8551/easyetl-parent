/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package javax.script;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 脚本引擎属性集合默认实现类
 *
 * @author Mike Grogan
 * @since 1.6
 */
public class SimpleBindings implements Bindings {

    private Map<String, Object> map;

    public SimpleBindings(Map<String, Object> m) {
        if (m == null) {
            throw new NullPointerException();
        }
        this.map = m;
    }

    public SimpleBindings() {
        this(new HashMap<String, Object>());
    }

    public Object put(String name, Object value) {
        this.checkKey(name);
        return this.map.put(name, value);
    }

    public void putAll(Map<? extends String, ? extends Object> toMerge) {
        if (toMerge == null) {
            throw new NullPointerException("toMerge map is null");
        }

        for (Entry<? extends String, ? extends Object> entry : toMerge.entrySet()) {
            String key = entry.getKey();
            this.checkKey(key);
            put(key, entry.getValue());
        }
    }

    public void clear() {
        this.map.clear();
    }

    public boolean containsKey(Object key) {
        this.checkKey(key);
        return this.map.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    public Set<Entry<String, Object>> entrySet() {
        return this.map.entrySet();
    }

    public Object get(Object key) {
        this.checkKey(key);
        return this.map.get(key);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public Set<String> keySet() {
        return this.map.keySet();
    }

    public Object remove(Object key) {
        this.checkKey(key);
        return this.map.remove(key);
    }

    public int size() {
        return this.map.size();
    }

    public Collection<Object> values() {
        return this.map.values();
    }

    private void checkKey(Object key) {
        if (key == null) {
            throw new NullPointerException("key can not be null");
        }

        if (!(key instanceof String)) {
            throw new ClassCastException("key should be a String");
        }

        if (key.equals("")) {
            throw new IllegalArgumentException("key can not be empty");
        }
    }
}
