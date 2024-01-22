package icu.etl.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import icu.etl.expression.AnalysisImpl;
import icu.etl.io.BufferedLineReader;
import icu.etl.util.ArrayUtils;
import icu.etl.util.CollectionUtils;
import icu.etl.util.IO;
import icu.etl.util.Property;
import icu.etl.util.StringUtils;

public class SQL {

    public SQL() {
    }

    /**
     * 修改建表语句中的表名
     *
     * @param ddl       语句
     * @param tableName 表名
     * @return 替换后的表名
     */
    public static String replaceCreateTableName(String ddl, String tableName) {
        int start = indexOf(ddl, "table", 0, true);
        if (start == -1) {
            throw new IllegalArgumentException(ddl);
        }

        int end = indexOf(ddl, "(", start + "table".length(), true);
        if (end == -1) {
            throw new IllegalArgumentException(ddl);
        } else {
            int length = end - start + 1;
            String str = " " + tableName + " (";
            return StringUtils.replace(ddl, start, length, str);
        }
    }

    /**
     * 对 sql 参数值中的单引号转义 <br>
     * 把单引号替换为2个单引号
     *
     * @param str 参数
     * @return 转义后的字符串
     */
    public static String escapeQuote(String str) {
        return StringUtils.replaceAll(str, "'", "''");
    }

    /**
     * 从指定索引位置 from 开始搜索，返回在 sql 语句中首次出现单词 dest 的索引位置（忽略sql中的字符常量与单行注释与多行注释中的字符串）<br>
     *
     * @param sql        SQL语句
     * @param dest       搜索字符串
     * @param from       搜索开始的位置，从0开始
     * @param ignoreCase true表示忽略大小写搜索字符串word
     * @return -1表示字符串参数 str 没有出现
     */
    public static int indexOf(CharSequence sql, String dest, int from, boolean ignoreCase) {
        if (sql == null || dest == null || dest.length() == 0) {
            throw new IllegalArgumentException("indexOf(" + sql + ", " + dest + ", " + from + ", " + ignoreCase + ")");
        }
        if (from < 0 || (sql.length() > 0 && from >= sql.length())) {
            throw new IllegalArgumentException("indexOf(" + sql + ", " + dest + ", " + from + ", " + ignoreCase + ")");
        }

        char ca = dest.charAt(0);
        for (int i = from; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '\'') { // 忽略字符常量
                int end = StringUtils.indexOfQuotation(sql, i, true);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            } else if (c == '-' && StringUtils.substring(sql, i, 0, 1).equals("--")) { // 忽略单行注释
                i = StringUtils.indexOfEOL(sql, i + 2);
                continue;
            } else if (c == '/' && StringUtils.substring(sql, i, 0, 1).equals("/*")) { // 忽略多行注释
                i = SQL.indexOfAnnotation(sql, i);
                continue;
            } else if (StringUtils.equals(c, ca, ignoreCase) && StringUtils.startsWith(sql, dest, i, ignoreCase, false)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 从注释第一个字符所在位置 from 开始搜索 SQL 语句中注释（单行注释和多行注释）结束的索引位置
     *
     * @param sql  字符串
     * @param from 多行注释或单行注释开始位置
     * @return 返回注释结束索引位置
     */
    public static int indexOfAnnotation(CharSequence sql, int from) {
        if (sql == null || from < 0 || from >= sql.length()) {
            throw new IllegalArgumentException("indexOfAnnotation(" + sql + ", " + from + ")");
        }

        char c = sql.charAt(from);
        if (c == '-') {
            if (StringUtils.substring(sql, from, 0, 1).equals("--")) {
                return StringUtils.indexOfEOL(sql, from + 1);
            }
        } else if (c == '/') {
            if (StringUtils.substring(sql, from, 0, 1).equals("/*")) {
                for (int j = from + 2; j < sql.length(); j++) {
                    if (sql.charAt(j) == '*' && StringUtils.substring(sql, j, 0, 1).equals("*/")) {
                        return j + 1;
                    }
                }
                return sql.length() - 1; // 字符串最后一个字符位置
            }
        }

        throw new IllegalArgumentException("indexOfAnnotation(" + sql + ", " + from + ")");
    }

    /**
     * 在SQL语句中搜索小括号的终止位置
     *
     * @param str  字符串
     * @param from 小括号的起始位置
     * @return -1表示小括号没有出现
     */
    public static int indexOfParenthes(CharSequence str, int from) {
        if (str == null || from < 0 || from >= str.length()) {
            throw new IllegalArgumentException("indexOfParenthes(" + str + ", " + from + ")");
        }

        for (int i = from + 1, count = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                int end = StringUtils.indexOfQuotation(str, i, true);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            } else if (c == '(') {
                count++;
            } else if (c == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 返回大括号的结束位置
     *
     * @param str  字符串
     * @param from 大括号的起始位置
     * @return -1表示大括号没有出现
     */
    public static int indexOfBrace(CharSequence str, int from) {
        if (str == null || from < 0 || from >= str.length()) {
            throw new IllegalArgumentException("indexOfBrace(" + str + ", " + from + ")");
        }

        for (int i = from + 1, count = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\'') {
                int end = StringUtils.indexOfQuotation(str, i, true);
                if (end == -1) {
                    return -1;
                } else {
                    i = end;
                    continue;
                }
            } else if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 按指定分隔符分割字段，并把字符串字段添加到 list 集合中 <br>
     * 忽略大括号中的分割字符 <br>
     *
     * @param sql       字符串
     * @param delimiter 字段分隔符
     * @return 字段数组
     */
    public static String[] split(CharSequence sql, char delimiter) {
        List<String> list = new ArrayList<String>(10);
        SQL.split(sql, delimiter, list);
        String[] array = new String[list.size()];
        return list.toArray(array);
    }

    /**
     * 按指定分隔符分割字段，并把字符串字段添加到 list 集合中 <br>
     * 忽略大括号中的分割字符 <br>
     *
     * @param sql       字符串
     * @param delimiter 字段分隔符
     * @param list      字段集合，用于存储解析后的所有字段
     */
    public static void split(CharSequence sql, char delimiter, Collection<String> list) {
        if (sql == null) {
            return;
        }
        if (StringUtils.inArray(delimiter, '(', ')', '\'')) {
            throw new IllegalArgumentException("split(\"" + sql + "\", " + delimiter + ", " + StringUtils.toString(list) + ")");
        }

        int begin = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '(') {
                i = SQL.indexOfParenthes(sql, i);
                if (i == -1) {
                    throw new IllegalArgumentException("split(\"" + sql + "\", " + delimiter + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            if (c == '\'') {
                i = StringUtils.indexOfQuotation(sql, i, true);
                if (i == -1) {
                    throw new IllegalArgumentException("split(\"" + sql + "\", " + delimiter + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            if ((c == '/' && "/*".equals(StringUtils.substr(sql, i, 0, 1))) // 多行注释
                    || (c == '-' && "--".equals(StringUtils.substr(sql, i, 0, 1))) // 单行注释
            ) {
                i = SQL.indexOfAnnotation(sql, i);
                if (i == -1) {
                    throw new IllegalArgumentException("split(\"" + sql + "\", " + delimiter + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            if (c == delimiter) {
                list.add(sql.subSequence(begin, i).toString());
                begin = i + 1;
            }
        }

        if (begin < sql.length()) {
            list.add(sql.subSequence(begin, sql.length()).toString());
        } else if (begin == sql.length()) {
            list.add("");
        }
    }

    /**
     * 使用字符串分隔符集合参数delimiter, 解析提取字符串参数str中的sql语句, 返回sql语句字符串数组 <br>
     * 使用字段分隔时字符串前后不能存在非空白字符 <br>
     *
     * @param sql        字符串
     * @param delimiter  分隔符集合
     * @param ignoreCase true表示忽略大小写
     * @return 字段数组
     */
    public static String[] split(CharSequence sql, Collection<String> delimiter, boolean ignoreCase) {
        List<String> list = new ArrayList<String>();
        SQL.split(sql, delimiter, ignoreCase, list);
        String[] array = new String[list.size()];
        return list.toArray(array);
    }

    /**
     * 使用字符串分隔符集合参数delimiter 解析提取字符串参数str中的sql语句, 保存sql语句到字符串集合参数list
     *
     * @param sql        字符串
     * @param delimiter  分隔符集合
     * @param ignoreCase true表示忽略大小写
     * @param list       Sql集合，用于存储解析后的所有字段
     */
    public static void split(CharSequence sql, Collection<String> delimiter, boolean ignoreCase, Collection<String> list) {
        if (sql == null) {
            return;
        }
        if (CollectionUtils.isEmpty(delimiter)) {
            throw new IllegalArgumentException("split(\"" + sql + "\", " + StringUtils.toString(delimiter) + ", " + ignoreCase + ", " + StringUtils.toString(list) + ")");
        }
        for (String del : delimiter) { // 遍历分隔符集合
            if (del == null || del.length() == 0) {
                throw new IllegalArgumentException("split(\"" + sql + "\", " + StringUtils.toString(delimiter) + ", " + ignoreCase + ", " + StringUtils.toString(list) + ")");
            }
        }

        int begin = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '(') {
                i = SQL.indexOfParenthes(sql, i);
                if (i == -1) {
                    throw new IllegalArgumentException("split(\"" + sql + "\", " + StringUtils.toString(delimiter) + ", " + ignoreCase + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            if (c == '\'') {
                i = StringUtils.indexOfQuotation(sql, i, true);
                if (i == -1) {
                    throw new IllegalArgumentException("split(\"" + sql + "\", " + StringUtils.toString(delimiter) + ", " + ignoreCase + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            if ((c == '/' && "/*".equals(StringUtils.substr(sql, i, 0, 1))) // 多行注释
                    || (c == '-' && "--".equals(StringUtils.substr(sql, i, 0, 1))) // 单行注释
            ) {
                i = SQL.indexOfAnnotation(sql, i);
                if (i == -1) {
                    throw new IllegalArgumentException("split(\"" + sql + "\", " + StringUtils.toString(delimiter) + ", " + ignoreCase + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            for (String del : delimiter) {
                if (ignoreCase) {
                    if (StringUtils.equals(c, del.charAt(0), ignoreCase) && StringUtils.substr(sql, i, 1, del.length()).equalsIgnoreCase(del)) {
                        list.add(sql.subSequence(begin, i).toString());
                        begin = i + del.length();
                        i = begin;
                        break;
                    }
                } else {
                    if (StringUtils.equals(c, del.charAt(0), ignoreCase) && StringUtils.substr(sql, i, 1, del.length()).equals(del)) {
                        list.add(sql.subSequence(begin, i).toString());
                        begin = i + del.length();
                        i = begin;
                        break;
                    }
                }
            }
        }

        if (begin < sql.length()) {
            list.add(sql.subSequence(begin, sql.length()).toString());
        } else if (begin == sql.length()) {
            list.add("");
        }
    }

    /**
     * 使用 union 或 union all 字段分隔符解析 sql 中的语句, 将字符分隔符保存到指定集合 delimiter 中，语句信息保存到集合 list 中
     *
     * @param sql       sql语句
     * @param delimiter 用于存储union 或 union all 语句
     * @param list      Sql集合，用于存储解析后的所有字段
     */
    public static void splitByUnion(CharSequence sql, Collection<String> delimiter, Collection<String> list) {
        if (sql == null) {
            return;
        }

        int begin = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '(') {
                i = SQL.indexOfParenthes(sql, i);
                if (i == -1) {
                    throw new IllegalArgumentException("splitByUnion(\"" + sql + "\", " + StringUtils.toString(delimiter) + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            if (c == '\'') {
                i = StringUtils.indexOfQuotation(sql, i, true);
                if (i == -1) {
                    throw new IllegalArgumentException("splitByUnion(\"" + sql + "\", " + StringUtils.toString(delimiter) + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            if ((c == '/' && "/*".equals(StringUtils.substr(sql, i, 0, 1))) // 多行注释
                    || (c == '-' && "--".equals(StringUtils.substr(sql, i, 0, 1))) // 单行注释
            ) {
                i = SQL.indexOfAnnotation(sql, i);
                if (i == -1) {
                    throw new IllegalArgumentException("splitByUnion(\"" + sql + "\", " + StringUtils.toString(delimiter) + ", " + StringUtils.toString(list) + ")");
                }
                continue;
            }

            if (StringUtils.equals(c, 'u', true) && StringUtils.substr(sql, i, 1, 5).equalsIgnoreCase("union")) {
                list.add(sql.subSequence(begin, i).toString());

                boolean all = false;
                for (int j = i + 5; j < sql.length(); j++) {
                    char n = sql.charAt(j);
                    if (Character.isWhitespace(n)) {
                        continue;
                    } else if (StringUtils.equals(n, 'a', true) && StringUtils.substr(sql, j, 1, 3).equalsIgnoreCase("all")) {
                        begin = j + 3;
                        all = true;
                        break;
                    } else {
                        break;
                    }
                }

                if (all) {
                    if (delimiter != null) {
                        delimiter.add("union all");
                    }
                } else {
                    begin = i + 5;
                    if (delimiter != null) {
                        delimiter.add("union");
                    }
                }
                i = begin;
                continue;
            }
        }

        if (begin < sql.length()) {
            list.add(sql.subSequence(begin, sql.length()).toString());
        } else if (begin == sql.length()) {
            list.add("");
        }
    }

    /**
     * 使用空白字符串作为字段分隔符，解析 sql 中的字段信息（忽略括号中、sql注释中、字符常量中的字符串信息）
     *
     * @param sql       字符串
     * @param delimiter 字段分隔符
     * @return 字段数组
     */
    public static String[] splitByBlank(CharSequence sql, char... delimiter) {
        if (sql == null) {
            return null;
        }

        List<String> list = new ArrayList<String>();
        int begin = 0;
        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '(') { // 忽略括号中的空白字符
                i = SQL.indexOfParenthes(sql, i);
                if (i == -1) {
                    list.add(sql.subSequence(begin, sql.length()).toString());
                    String[] array = new String[list.size()];
                    return list.toArray(array);
                }
                continue;
            }

            if (c == '\'') { // 忽略字符常量中的空白
                i = StringUtils.indexOfQuotation(sql, i, true);
                if (i == -1) {
                    list.add(sql.subSequence(begin, sql.length()).toString());
                    String[] array = new String[list.size()];
                    return list.toArray(array);
                }
                continue;
            }

            if ((c == '/' && "/*".equals(StringUtils.substr(sql, i, 0, 1))) // 多行注释
                    || (c == '-' && "--".equals(StringUtils.substr(sql, i, 0, 1))) // 单行注释
            ) {
                i = SQL.indexOfAnnotation(sql, i);
                if (i == -1) {
                    list.add(sql.subSequence(begin, sql.length()).toString());
                    String[] array = new String[list.size()];
                    return list.toArray(array);
                }
                continue;
            }

            if (Character.isWhitespace(c) || StringUtils.inArray(c, delimiter)) {
                list.add(sql.subSequence(begin, i).toString());
                for (int j = i + 1; j < sql.length(); j++) {
                    char nc = sql.charAt(j);
                    if (Character.isWhitespace(nc) || StringUtils.inArray(nc, delimiter)) {
                        i++;
                    } else {
                        break;
                    }
                }
                begin = i + 1;
            }
        }

        if (begin < sql.length()) {
            list.add(sql.subSequence(begin, sql.length()).toString());
        } else if (begin == sql.length()) {
            list.add("");
        }

        String[] array = new String[list.size()];
        return list.toArray(array);
    }

    /**
     * 从sql语句中删除单行和多行注释信息, 将注释信息添加到参数集合列表中
     *
     * @param sql        sql语句
     * @param mulitiList 多行注释信息集合列表
     * @param singleList 单行注释信息集合列表
     * @return 字符串
     */
    public static String removeAnnotation(CharSequence sql, List<Property> mulitiList, List<Property> singleList) {
        BufferedLineReader in = new BufferedLineReader(sql);
        try {
            StringBuilder str = new StringBuilder();
            int no = 0; // 行号
            String line = null;
            while ((line = in.readLine()) != null) { // 逐行遍历
                no++;

                for (int i = 0; i < line.length(); i++) { // 遍历每行字符
                    char c = line.charAt(i);
                    if (c == '/' && StringUtils.substr(line, i, 0, 1).equals("/*")) { // 解析多行注释
                        StringBuilder memo = new StringBuilder();
                        int end = 0, begin = i;
                        while ((end = line.indexOf("*/", begin)) == -1) { // 搜索多行注释结尾位置
                            memo.append(line.substring(begin));
                            line = in.readLine(); // 向下读取一行
                            no++;
                            if (line == null) {
                                begin = 0;
                                break;
                            } else {
                                memo.append(in.getLineSeparator());
                                begin = 0;
                            }
                        }

                        if (end == -1) {
                            sqlError(sql);
                        }

                        memo.append(line.substring(begin, end + 2));
                        if (mulitiList != null) {
                            mulitiList.add(new Property(memo.toString(), no)); // 解析多行注释
                        }
                        i = end + 1;

                        str.append(SQL.replaceMemo(memo));
                        continue;
                    }

                    if (c == '-' && StringUtils.substr(line, i, 0, 1).equals("--")) { // 解析单行注释
                        String memo = line.substring(i);
                        if (singleList != null) {
                            singleList.add(new Property(memo, no));
                        }
                        str.append(SQL.replaceMemo(memo));
                        break;
                    }

                    str.append(c);
                }

                str.append(in.getLineSeparator());
            }
            return str.toString();
        } catch (Throwable e) {
            throw new RuntimeException("removeAnnotation(\"" + sql + "\", " + StringUtils.toString(mulitiList) + ", " + StringUtils.toString(singleList) + ")", e);
        } finally {
            IO.close(in);
        }
    }

    private static void sqlError(CharSequence sql) {
        throw new IllegalArgumentException(StringUtils.toString(sql));
    }

    /**
     * 替换sql注释为空白字符，保留注释中的回车换行符
     *
     * @param str 字符序列
     * @return SQL语句
     */
    private static String replaceMemo(CharSequence str) {
        StringBuilder buf = new StringBuilder(str.length());
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            if (c == '\r' || c == '\n' || c == '\t' || c == '\b') {
                buf.append(c);
            } else {
                buf.append(' ');
            }
        }
        return buf.toString();
    }

    /**
     * 使用属性信息替换字符串参数str中的shell型变量
     *
     * @param str       字符串
     * @param resultSet 查询结果集
     * @return 字符串
     * @throws SQLException 数据库错误
     */
    public static String replaceVariable(String str, ResultSet resultSet) throws SQLException {
        return str == null || resultSet == null ? str : SQL.replaceVariable(str, resultSet, -1);
    }

    /**
     * 替换字符串中 ${name} 变量 <br>
     * 替换嵌套变量, 如: ${${name}}
     *
     * @param str       字符串
     * @param resultSet 查询结果集
     * @param index     占位符 ${ 起始位置
     * @return 字符串
     * @throws SQLException 数据库错误
     */
    protected static String replaceVariable(String str, ResultSet resultSet, int index) throws SQLException {
        if (index == -1) {
            index = str.indexOf("${");
        }

        int from = 0; // 下一次搜索开始的位置
        while (index != -1) {
            int end = StringUtils.indexOfUnixVariable(str, index + 1, str.length()) + 1; // 搜索变量名结尾的下一个字符
            if (end == 0) {
                return str;
            }

            String var = str.substring(index, end); // 格式: ${name}
            String key = var.substring(2, var.length() - 1); // 格式: name

            // 解析 key 中的嵌套变量
            int start = key.indexOf("${");
            if (start != -1) {
                key = SQL.replaceVariable(key, resultSet, start); // 替换嵌套变量名
            }

            Object obj = resultSet.getObject(key);
            String value = obj == null ? "" : StringUtils.rtrimBlank(obj);
            if (value == null) {
                from = end;
            } else {
                str = StringUtils.replace(str, var, value);
            }

            index = str.indexOf("${", from);
        }
        return str;
    }

    /**
     * 把SQL转成 count(*) 模式
     *
     * @param sql SQL语句
     * @return DDL语句
     */
    public static String toCountSQL(String sql) {
        AnalysisImpl analysis = new AnalysisImpl();
        List<String> list = analysis.split(sql);
        if (list.size() > 3 //
                && "select".equalsIgnoreCase(list.get(0)) //
                && "count(*)".equalsIgnoreCase(StringUtils.removeBlank(list.get(1))) //
                && "from".equalsIgnoreCase(list.get(2)) //
        ) {
            int index = getOrderbyPosition(analysis, sql);
            return index == -1 ? sql : sql.substring(0, index);
        } else {
            StringBuilder buf = new StringBuilder(sql.length() + 30);
            buf.append("select count(*) from (");
            buf.append(sql);
            buf.append(") temp_tab ");
            return buf.toString();
        }
    }

    /**
     * 返回sql语句是否有orderby语句，如果有则返回语句的位置
     *
     * @param sql 语句
     * @return 位置信息
     */
    private static int getOrderbyPosition(AnalysisImpl analysis, String sql) {
        if (sql != null && sql.matches("^.*((?i)order)\\s+((?i)by)\\s+.+")) {
            sql = sql.trim().toLowerCase();
            int index = sql.lastIndexOf("order");
            if (index != -1) {
                String part = sql.substring(index);
                List<String> list = analysis.split(part);
                if (list.size() >= 2 && "order".equalsIgnoreCase(list.get(0)) && "by".equalsIgnoreCase(list.get(1))) {
                    return index;
                }
            }
        }
        return -1;
    }

    /**
     * 判断字符串是否是合法的字段名
     *
     * @param str 字符串
     * @return 返回true表示是字段名 false表示不是字段名
     */
    public static boolean isFieldName(String str) {
        if (str == null) {
            return false;
        }

        String name = StringUtils.trimBlank(str);
        if (name.length() > 1 && name.charAt(0) == '"' && name.charAt(name.length() - 1) == '"') {
            name = name.substring(1, name.length() - 1);
        }
        if (name.length() == 0) { // 字段不能为空
            return false;
        }
        if (StringUtils.isNumber(name.charAt(0))) { // 首字母不可是数字
            return false;
        }
        if (name.length() == 1 && name.charAt(0) == '_') { // 单独一个下划线不能作为字段名
            return false;
        }

        // 遍历字段名中的字符
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!StringUtils.isLetter(c) && !StringUtils.isNumber(c) && c != '_') { // 只能是数字英文字母与下划线的组合
                return false;
            }
        }
        return true;
    }

    /**
     * 返回数据库表的完全限定名 <br>
     * getDatabaseTableName("nschema", "Name") == "nschema.Name" <br>
     * getDatabaseTableName("nschema", "oschema.Name") == "nschema.Name" <br>
     * getDatabaseTableName("", "oschema.Name") == "oschema.Name" <br>
     * getDatabaseTableName("", ".Name") == "Name" <br>
     *
     * @param schema    数据库使用的默认schema
     * @param tableName 数据库表名
     * @return 表的完全限定名
     */
    public static String toTableName(String schema, String tableName) {
        if (StringUtils.isBlank(schema)) {
            String name = StringUtils.ltrimBlank(tableName);
            return (name.length() > 0 && name.charAt(0) == '.') ? name.substring(1) : name;
        } else {
            return StringUtils.trimBlank(schema) + "." + StringUtils.ltrimBlank(ArrayUtils.lastElement(StringUtils.split(tableName, '.')));
        }
    }

    /**
     * 处理数据库中字符串标示符流程： <br>
     * 先删除字符串参数str二端的空白字符, 再删除字符串二端的双引号字符, 强制把所有英文小写转为大写 <br>
     *
     * @param str 字符串
     * @return 字符串
     */
    public static String toIdentifier(String str) {
        if (str == null) {
            return null;
        } else {
            str = StringUtils.trimBlank(str);
            return StringUtils.containsDoubleQuotation(str) ? StringUtils.unquotes(str).toUpperCase() : str.toUpperCase();
        }
    }

    /**
     * 判断字符串 schema 和表名 name 与 schema2 和表 name2 是否匹配 <br>
     * 如果 schema 为null或空白且name与表信息中表名匹配返回true
     *
     * @param schema  schema字符串
     * @param name    表名（表名不能为空）
     * @param schema2 schema字符串
     * @param name2   表名（表名不能为空）
     * @return 返回true表示表名匹配 false表示表名不匹配
     */
    public static boolean matchTables(String schema, String name, String schema2, String name2) {
        if (StringUtils.isBlank(name) || StringUtils.isBlank(name2)) {
            throw new IllegalArgumentException("matchTables(" + schema + ", " + name + ", " + schema2 + ", " + name2 + ")");
        }

        return StringUtils.isNotBlank(schema) && StringUtils.isNotBlank(schema2) //
                && StringUtils.trimBlank(schema).equals(schema2) //
                && StringUtils.trimBlank(name).equalsIgnoreCase(StringUtils.trimBlank(name2)) //
                ;
    }

    /**
     * 把数据库中字段名使用驼峰命名法进行转换
     *
     * @param fieldName 数据库中字段名
     * @return 返回数据库表字段对应的java字段名
     */
    public static String field2javaName(String fieldName) {
        boolean isUpper = false;
        StringBuilder buf = new StringBuilder();
        char[] array = fieldName.toLowerCase().toCharArray();
        for (int i = 0; i < array.length; i++) {
            char c = array[i];
            if (c == '_') {
                isUpper = true;
                continue;
            } else {
                if (isUpper) {
                    buf.append(Character.toUpperCase(c));
                    isUpper = false;
                } else {
                    buf.append(c);
                }
            }
        }
        return buf.toString();
    }

}
