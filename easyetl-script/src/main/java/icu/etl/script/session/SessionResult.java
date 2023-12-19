package icu.etl.script.session;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎会话的返回值
 *
 * @author jeremy8551@qq.com
 * @createtime 2023/12/19 15:42
 */
public class SessionResult extends CaseSensitivMap<Object> {

    /**
     * 返回会话的返回值 <br>
     * 如果集合中没有任何元素，则返回null<br>
     * 如果集合中只有一个元素，则返回这个元素本身 <br>
     * 如果集合中存在多个元素，则返回当前对象
     *
     * @return 返回值
     */
    public Object value() {
        int size = this.size();
        if (size == 0) {
            return null;
        }

        if (size == 1) {
            return this.values().iterator().next();
        }

        return this;
    }

    public String toString() {
        return StringUtils.toString(this);
    }
}
