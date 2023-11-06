package icu.etl.os.internal;

import java.math.BigDecimal;

import icu.etl.expression.DataUnitExpression;
import icu.etl.os.OSMemory;

/**
 * 操作系统内存功能接口实现
 */
public class OSMemoryImpl implements OSMemory {

    private BigDecimal total;
    private BigDecimal free;
    private BigDecimal active;

    public OSMemoryImpl() {
        super();
    }

    public BigDecimal total() {
        return this.total;
    }

    public BigDecimal free() {
        return this.free;
    }

    public BigDecimal active() {
        return this.active;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setFree(BigDecimal free) {
        this.free = free;
    }

    public void setActive(BigDecimal active) {
        this.active = active;
    }

    public String toString() {
        return "OSMemoryImpl [total=" + DataUnitExpression.toString(total) + ", free=" + DataUnitExpression.toString(free) + ", active=" + DataUnitExpression.toString(active) + "]";
    }

}
