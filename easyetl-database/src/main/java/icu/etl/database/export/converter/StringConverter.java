package icu.etl.database.export.converter;

import java.io.IOException;
import java.sql.SQLException;

import icu.etl.util.MessySequence;
import icu.etl.util.StringUtils;

public class StringConverter extends AbstractConverter {

    protected Process process;

    public void init() throws Exception {
        String charsetName = (String) this.getAttribute(PARAM_CHARSET);
        this.process = StringUtils.isNotBlank(charsetName) && this.contains(PARAM_MESSY) ? new Messy(charsetName) : new None();
    }

    public void execute() throws Exception {
        String value = this.resultSet.getString(this.column);
        this.array[this.column] = this.process.execute(value);
    }

    /**
     * 字符串处理接口
     */
    protected interface Process {
        String execute(String str);
    }

    /**
     * 乱码处理接口
     *
     * @author jeremy8551@qq.com
     */
    protected static class Messy implements Process {

        private MessySequence ms;

        public Messy(String charsetName) {
            this.ms = new MessySequence(charsetName);
        }

        public String execute(String str) {
            return this.ms.remove(str);
        }
    }

    protected static class None implements Process {

        public None() {
        }

        public String execute(String str) {
            return str;
        }

    }

}