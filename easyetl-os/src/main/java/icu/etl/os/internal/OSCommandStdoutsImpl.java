package icu.etl.os.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import icu.etl.os.OSCommandStdouts;

public class OSCommandStdoutsImpl implements OSCommandStdouts {

    private Map<String, List<String>> map;

    public OSCommandStdoutsImpl() {
        this.map = new LinkedHashMap<String, List<String>>();
    }

    public void put(String id, List<String> stdout) {
        this.map.put(id, stdout);
    }

    public List<String> get(String commandid) {
        return this.map.get(commandid);
    }

    public Set<String> keys() {
        return this.map.keySet();
    }

}
