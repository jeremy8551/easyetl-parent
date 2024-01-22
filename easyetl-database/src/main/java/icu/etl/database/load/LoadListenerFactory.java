package icu.etl.database.load;

import java.io.IOException;

import icu.etl.io.CommonTextTableFileReaderListener;
import icu.etl.io.TextTableFile;
import icu.etl.io.TextTableFileReaderListener;
import icu.etl.io.TextTableLine;

public class LoadListenerFactory {

    public LoadListenerFactory() {
    }

    /**
     * 生成文件输入流监听器
     *
     * @param context 数据装载引擎上下文信息
     * @return 输入流监听器
     */
    public static TextTableFileReaderListener create(LoadEngineContext context) {
        CommonTextTableFileReaderListener listener = null;

        // 设置不带回车与换行符
        if (context.getAttributes().contains("nocrlf")) {
            listener = new CommonTextTableFileReaderListener(); // 默认自动删除回车与换行符
        } else {
            listener = new CommonTextTableFileReaderListener() {

                public void processLineSeparator(TextTableFile file, TextTableLine line, long lineNumber) throws IOException {
                    // 不做任何操作，保留回车与换行符
                }

            };
        }

        listener.setProgress(context.getProgress());
        return listener;
    }

}
