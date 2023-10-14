package javax.script;

import java.util.Map;

/**
 * 脚本引擎属性集合
 *
 * @author Mike Grogan
 * @since 1.6
 */
public interface Bindings extends Map<String, Object> {

    public Object put(String name, Object value);

    public void putAll(Map<? extends String, ? extends Object> toMerge);

    public boolean containsKey(Object key);

    public Object get(Object key);

    public Object remove(Object key);

}
