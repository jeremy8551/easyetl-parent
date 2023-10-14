package icu.etl.increment;

import java.io.IOException;

import icu.etl.io.TextTableFileReader;
import icu.etl.io.TextTableLine;
import icu.etl.util.ResourcesUtils;

/**
 * 表格型数据增量剥离算法
 *
 * @author jeremy8551@qq.com
 */
public class IncrementTableArith implements IncrementArith {

    /** true 表示以终止任务 */
    private volatile boolean terminate;

    public void execute(IncrementRuler rule, TextTableFileReader newIn, TextTableFileReader oldIn, IncrementHandler out) throws IOException {
        try {
            // 读取第一行数据
            TextTableLine nl = newIn.readLine();
            TextTableLine ol = oldIn.readLine();

            // 比较文本 没有数据
            if (ol == null) {
                while (nl != null) {
                    if (this.terminate) {
                        return;
                    }

                    out.handleCreateRecord(nl);
                    nl = newIn.readLine();
                }
                return;
            }

            // 被比较文本 没有数据
            if (nl == null) {
                while (ol != null) {
                    if (this.terminate) {
                        return;
                    }

                    out.handleDeleteRecord(ol);
                    ol = oldIn.readLine();
                }
                return;
            }

            while (nl != null && ol != null) {
                if (this.terminate) {
                    return;
                }

                int c = rule.compareIndex(nl, ol);
                if (c == 0) {
                    int p = rule.compareColumn(nl, ol);
                    if (p != 0) {
                        out.handleUpdateRecord(nl, ol, p);
                    }
                    nl = newIn.readLine();
                    ol = oldIn.readLine();
                } else if (c < 0) {
                    out.handleCreateRecord(nl);
                    nl = newIn.readLine();
                } else {
                    out.handleDeleteRecord(ol);
                    ol = oldIn.readLine();
                }
            }

            while (nl != null) {
                if (this.terminate) {
                    return;
                }

                out.handleCreateRecord(nl);
                nl = newIn.readLine();
            }

            while (ol != null) {
                if (this.terminate) {
                    return;
                }

                out.handleDeleteRecord(ol);
                ol = oldIn.readLine();
            }
        } catch (Throwable e) {
            throw new IOException(ResourcesUtils.getIncrementMessage(72, newIn.getLineNumber(), oldIn.getLineNumber()), e);
        } finally {
            oldIn.close();
            newIn.close();
            out.close();
        }
    }

    public boolean isTerminate() {
        return this.terminate;
    }

    public void terminate() {
        this.terminate = true;
    }

}
