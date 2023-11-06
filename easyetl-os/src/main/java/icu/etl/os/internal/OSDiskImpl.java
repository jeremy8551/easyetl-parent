package icu.etl.os.internal;

import java.math.BigDecimal;

import icu.etl.expression.DataUnitExpression;
import icu.etl.os.OSDisk;

/**
 * 操作系统存储信息接口实现
 */
public class OSDiskImpl implements OSDisk {

    private String id;
    private String amount;
    private String type;
    private BigDecimal total;
    private BigDecimal free;
    private BigDecimal used;

    public OSDiskImpl() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setFree(BigDecimal free) {
        this.free = free;
    }

    public void setUsed(BigDecimal used) {
        this.used = used;
    }

    public BigDecimal total() {
        return this.total;
    }

    public BigDecimal free() {
        return this.free;
    }

    public BigDecimal used() {
        return this.used;
    }

    public String toString() {
        return "OSDiskImpl [id=" + id + ", amount=" + amount + ", type=" + type + ", total=" + DataUnitExpression.toString(total) + ", free=" + DataUnitExpression.toString(free) + ", used=" + DataUnitExpression.toString(used) + "]";
    }

}
