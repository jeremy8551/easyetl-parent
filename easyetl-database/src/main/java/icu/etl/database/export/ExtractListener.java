package icu.etl.database.export;

import java.util.ArrayList;
import java.util.List;

import icu.etl.util.ResourcesUtils;

/**
 * 卸载数据功能的监听器
 *
 * @author jeremy8551@qq.com
 * @createtime 2020-11-13
 */
public class ExtractListener {

    private List<ExtractUserListener> list;

    private ExtracterContext context;

    /**
     * 初始化
     *
     * @param context 上下文信息
     */
    public ExtractListener(ExtracterContext context) {
        this.context = context;
        this.list = new ArrayList<ExtractUserListener>();
    }

    /**
     * 添加用户自定义监听器
     *
     * @param listeners 用户自定义监听器集合
     */
    public void setListener(List<ExtractUserListener> listeners) {
        if (listeners != null) {
            this.list.clear();
            for (ExtractUserListener obj : listeners) {
                if (obj != null) {
                    this.list.add(obj);
                }
            }
        }
    }

    /**
     * 返回 true 表示任务已准备就绪可以执行，false 表示任务还未准备就绪不能执行
     *
     * @return
     */
    public boolean ready() {
        for (ExtractUserListener l : this.list) {
            if (!l.ready(this.context)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 卸数任务运行前执行的逻辑
     */
    public void before() {
        for (ExtractUserListener l : this.list) {
            l.before(this.context);
        }
    }

    /**
     * 卸数任务运行发生错误时执行的逻辑
     *
     * @param e
     */
    public void catchError(Throwable e) {
        if (this.list.isEmpty()) {
            throw new ExtractException(ResourcesUtils.getMessage("extract.standard.output.msg003"), e);
        }

        for (ExtractUserListener l : this.list) {
            l.catchException(this.context, e);
        }
    }

    /**
     * 卸数任务运行完毕后执行的逻辑
     */
    public void after() {
        for (ExtractUserListener l : this.list) {
            l.after(this.context);
        }
    }

    /**
     * 退出卸数任务前执行的逻辑
     */
    public void destory() {
        for (ExtractUserListener l : this.list) {
            l.quit(this.context);
        }
    }

}
