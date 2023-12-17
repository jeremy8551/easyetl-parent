package icu.etl.increment;

import java.util.ArrayList;
import java.util.List;

import icu.etl.io.TextTableLine;

/**
 * 批量对增量数据进行修改
 *
 * @author jeremy8551@qq.com
 */
public class IncrementReplaceListener implements IncrementListener {

    private List<IncrementReplace> creates;
    private List<IncrementReplace> updates;
    private List<IncrementReplace> deletes;

    public IncrementReplaceListener() {
        this.creates = new ArrayList<IncrementReplace>();
        this.updates = new ArrayList<IncrementReplace>();
        this.deletes = new ArrayList<IncrementReplace>();
    }

    /**
     * 新增数据的字段替换逻辑
     *
     * @return
     */
    public List<IncrementReplace> getNewChgs() {
        return creates;
    }

    /**
     * 变化数据的字段替换逻辑
     *
     * @return
     */
    public List<IncrementReplace> getUpdChgs() {
        return updates;
    }

    /**
     * 删除数据的字段替换逻辑
     *
     * @return
     */
    public List<IncrementReplace> getDelChgs() {
        return deletes;
    }

    public void beforeCreateRecord(TextTableLine line) {
        for (IncrementReplace obj : this.creates) {
            line.setColumn(obj.getPosition(), obj.getValue());
        }
    }

    public void afterCreateRecord(TextTableLine line) {
    }

    public void beforeUpdateRecord(TextTableLine in, TextTableLine oldLine, int position) {
        for (IncrementReplace obj : this.updates) {
            in.setColumn(obj.getPosition(), obj.getValue());
        }
    }

    public void afterUpdateRecord(TextTableLine in, TextTableLine oldIn, int position) {
    }

    public void beforeDeleteRecord(TextTableLine in) {
        for (IncrementReplace obj : this.deletes) {
            in.setColumn(obj.getPosition(), obj.getValue());
        }
    }

    public void afterDeleteRecord(TextTableLine in) {
    }

}
