package icu.etl.database.load.converter;

import java.sql.Types;
import javax.sql.rowset.serial.SerialClob;

public class ClobConverter extends AbstractConverter {

    public void init() throws Exception {
    }

    public void execute(String value) throws Exception {
        if (this.notNull && this.isBlank(value)) {
            this.statement.setNull(this.position, Types.CLOB);
        } else if (value.length() == 0) { // 空字符串表示空指针
            this.statement.setNull(this.position, Types.CLOB);
        } else {
            this.statement.setClob(this.position, new SerialClob(value.toCharArray()));
        }
    }

}
