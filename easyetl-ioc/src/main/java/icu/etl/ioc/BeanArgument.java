package icu.etl.ioc;

import icu.etl.util.FileUtils;
import icu.etl.util.StringUtils;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/26
 */
public class BeanArgument {

    private String name;

    private Object[] args;

    public BeanArgument(String name, Object[] args) {
        this.name = name;
        this.args = args;
    }

    public BeanArgument(Object[] args) {
        if (args.length == 0) {
            this.args = args;
            return;
        }

        Object first = args[0];
        if (first instanceof String) {
            this.name = (String) first;
            this.args = new Object[args.length - 1];
            System.arraycopy(args, 1, this.args, 0, this.args.length);
            return;
        }

        this.args = args;
    }

    public String getName() {
        return this.name;
    }

    public Object[] getArgs() {
        return this.args;
    }

    public Object get(int i) {
        return this.args[i];
    }

    public int size() {
        return this.args.length;
    }

    /**
     * 将参数专为字符串
     *
     * @param args 参数
     * @return 字符串
     */
    public static StringBuilder toString(Object[] args) {
        StringBuilder buf = new StringBuilder();
        if (args.length > 0) {
            buf.append(FileUtils.lineSeparator);
            buf.append(StringUtils.join(args, FileUtils.lineSeparator));
        }
        return buf;
    }

}
