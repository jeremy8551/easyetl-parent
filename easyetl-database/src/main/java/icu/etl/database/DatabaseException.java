package icu.etl.database;

import icu.etl.util.ResourcesUtils;
import icu.etl.util.StringUtils;

/**
 * 数据库错误
 *
 * @author jeremy8551@qq.com
 * @createtime 2012-03-06
 */
public class DatabaseException extends RuntimeException {
    private final static long serialVersionUID = 1L;

    public DatabaseException() {
        super(ResourcesUtils.getDatabaseMessage(8));
    }

    public DatabaseException(String message, Throwable cause) {
        super(StringUtils.defaultString(message, ResourcesUtils.getDatabaseMessage(8)), cause);
    }

    public DatabaseException(String message) {
        super(StringUtils.defaultString(message, ResourcesUtils.getDatabaseMessage(8)));
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }

}