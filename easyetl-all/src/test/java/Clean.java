import java.io.File;

import icu.etl.util.FileUtils;

/**
 * @author jeremy8551@qq.com
 * @createtime 2023/10/23
 */
public class Clean {

    public static void main(String[] args) {
        FileUtils.clearDirectory(new File("/Volumes/lvzhaojun/project/easyetl-parent/easyetl-all/src/main/java"));
        FileUtils.clearDirectory(new File("/Volumes/lvzhaojun/project/easyetl-parent/easyetl-all/src/main/resources"));
    }
}
