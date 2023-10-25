package icu.etl.script.internal;

import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;

import icu.etl.util.CollectionUtils;
import icu.etl.util.StringUtils;

public class ScriptCatalog extends Hashtable<String, Properties> {
    private static final long serialVersionUID = 1L;

    public ScriptCatalog() {
        super();
    }

    public synchronized Properties put(String name, Properties value) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException(name);
        } else {
            return super.put(name, value);
        }
    }

    public void addAll(ScriptCatalog catalog) {
        if (catalog != null) {
            Set<String> names = catalog.keySet();
            for (String name : names) {
                Properties src = catalog.get(name);
                Properties copy = CollectionUtils.cloneProperties(src, new Properties());
                this.put(name, copy);
            }
        }
    }

}
