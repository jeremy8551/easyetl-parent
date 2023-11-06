package icu.etl.script.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import icu.etl.annotation.EasyBean;
import icu.etl.collection.ByteBuffer;
import icu.etl.jdk.JavaDialectFactory;
import icu.etl.script.Script;
import icu.etl.script.UniversalScriptContext;
import icu.etl.script.UniversalScriptFormatter;
import icu.etl.util.Dates;
import icu.etl.util.FileUtils;
import icu.etl.util.IO;
import icu.etl.util.StringUtils;

/**
 * 脚本引擎内部的类型转换器
 *
 * @author jeremy8551@qq.com
 */
@EasyBean(name = "default")
public class ScriptFormatter extends UniversalScriptFormatter {
    private final static long serialVersionUID = 1L;

    /**
     * 将Jdbc参数 object 转为脚本引擎内部类型
     *
     * @param context 脚本引擎上下文信息
     * @param object  Jdbc参数对象
     * @return
     * @throws IOException
     * @throws SQLException
     */
    public Object formatJdbcParameter(UniversalScriptContext context, Object object) throws IOException, SQLException {
        if (object == null) {
            return null;
        }

        if (object instanceof String //
                || object instanceof Integer //
                || object instanceof BigDecimal //
                || object instanceof Long //
                || object instanceof Double //
                || object instanceof Float //
        ) {
            return object;
        }

        if (object instanceof Date || object instanceof Clob) {
            return StringUtils.toString(object);
        } else if (object.getClass().isArray()) {
            Class<?> clazz = object.getClass().getComponentType();
            if ("byte".equals(clazz.getName())) {
                byte[] array = (byte[]) object;
                return StringUtils.toString(array, context.getCharsetName());
            } else {
                return (Object[]) object;
            }
        } else if (object instanceof java.io.InputStream) {
            java.io.InputStream in = (java.io.InputStream) object;
            return new ByteBuffer().append(in).toString(context.getCharsetName());
        } else if (object instanceof java.io.Reader) {
            java.io.Reader in = (java.io.Reader) object;
            return IO.read(in, new StringBuilder()).toString();
        } else if (object instanceof java.sql.Blob) {
            java.sql.Blob blob = (java.sql.Blob) object;
            File file = new File(FileUtils.getTempDir(UniversalScriptContext.class), FileUtils.getFilenameRandom("ScriptConvert_", ".blob"));
            IO.write(blob.getBinaryStream(), new FileOutputStream(file));
            return file.getAbsolutePath();
        } else if (object instanceof java.net.URL) {
            return ((java.net.URL) object).getPath();
        } else if (object instanceof java.sql.Ref) {
            return ((java.sql.Ref) object).getObject();
        }

        Object val = JavaDialectFactory.getDialect().parseJdbcObject(object);
        if (val == null) {
            return object;
        } else {
            return val;
        }
    }

    public String toString(Object obj) {
        if (obj == null) {
            return "";
        }

        if (obj instanceof String) {
            return (String) obj;
        }

        /**
         * yyyy-MM-dd <br>
         * yyyy-MM-dd hh:mm <br>
         * yyyy-MM-dd hh:mm:ss <br>
         * yyyy-MM-dd hh:mm:ss:SS <br>
         */
        if (obj instanceof Date) {
            Date date = (Date) obj;
            Calendar cr = Calendar.getInstance();
            cr.setTime(date);
            int hour = cr.get(Calendar.HOUR_OF_DAY);
            int minute = cr.get(Calendar.MINUTE);
            int second = cr.get(Calendar.SECOND);
            int mills = cr.get(Calendar.MILLISECOND);
            if (mills > 0) {
                return Dates.format21(date);
            } else if (second > 0) {
                return Dates.format19(date);
            } else if (minute > 0 || hour > 0) {
                return Dates.format16(date);
            } else {
                return Dates.format10(date);
            }
        } else if (obj instanceof Throwable) {
            Throwable e = (Throwable) obj;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream stream = new PrintStream(out);
            e.printStackTrace(stream);
            IO.closeQuiet(stream);

            StringBuilder buf = new StringBuilder(out.toString());
            if (e instanceof SQLException) {
                SQLException sqlExp = (SQLException) e;
                while (sqlExp != null) {
                    buf.append(FileUtils.lineSeparator);
                    buf.append(sqlExp.getClass().getName());
                    buf.append("[");
                    buf.append("SQLSTATE = ");
                    buf.append(sqlExp.getSQLState());
                    buf.append(", ERRORCODE = ");
                    buf.append(sqlExp.getErrorCode());
                    buf.append(", MESSAGE = ");
                    buf.append(sqlExp.getMessage());
                    buf.append("]");

                    sqlExp = sqlExp.getNextException();
                }
            }
            return buf.toString();
        }

        /**
         * ArrayList[1, 2, ...]
         */
        else if (obj instanceof Iterable<?>) {
            StringBuilder buf = new StringBuilder(20);
            for (Iterator<?> it = ((Iterable<?>) obj).iterator(); it.hasNext(); ) {
                Object next = it.next();
                if (obj == next || obj.equals(next)) {
                    buf.append(next);
                    break;
                } else {
                    buf.append(StringUtils.toString(next));
                }

                if (it.hasNext()) {
                    buf.append(", ");
                }
            }
            return buf.toString();
        }

        /**
         * HashMap[k1=v1, k2=v2, ...]
         */
        else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            StringBuilder buf = new StringBuilder(map.size() * 5);

            for (Iterator<?> it = map.keySet().iterator(); it.hasNext(); ) {
                Object key = it.next();
                Object val = map.get(key);

                buf.append(key);
                buf.append("=");
                buf.append(obj == val ? val : StringUtils.toString(val));

                if (it.hasNext()) {
                    buf.append(", ");
                }
            }
            return buf.toString();
        }

        /**
         * int[1, 2, ...] <br>
         * String[1, 2, ...]
         */
        else if (obj.getClass().isArray()) {
            try {
                Class<?> clazz = obj.getClass().getComponentType();

                // 基本数据类型数组
                if ("int".equals(clazz.getName())) {
                    return "int" + java.util.Arrays.toString((int[]) obj);
                } else if ("long".equals(clazz.getName())) {
                    return "long" + java.util.Arrays.toString((long[]) obj);
                } else if ("byte".equals(clazz.getName())) {
                    return "byte" + java.util.Arrays.toString((byte[]) obj);
                } else if ("short".equals(clazz.getName())) {
                    return "short" + java.util.Arrays.toString((short[]) obj);
                } else if ("boolean".equals(clazz.getName())) {
                    return "boolean" + java.util.Arrays.toString((boolean[]) obj);
                } else if ("double".equals(clazz.getName())) {
                    return "double" + java.util.Arrays.toString((double[]) obj);
                } else if ("float".equals(clazz.getName())) {
                    return "float" + java.util.Arrays.toString((float[]) obj);
                } else if ("char".equals(clazz.getName())) {
                    return "char" + java.util.Arrays.toString((char[]) obj);
                }

                // 对象数组
                Object[] array = (Object[]) obj;
                StringBuilder buf = new StringBuilder(array.length * 5);
                buf.append(StringUtils.join(array, ", "));
                return buf.toString();
            } catch (ClassCastException e) {
                return obj.toString();
            }
        } else if (obj instanceof Clob) {
            try {
                Clob clob = (Clob) obj;
                return clob.getSubString((long) 1, (int) clob.length());
            } catch (Exception e) {
                Script.out.error(StringUtils.toString(e));
            }
        }

        return obj.toString();
    }

    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        toAppendTo.append(this.toString(obj));
        return toAppendTo;
    }

    public Object parseObject(String source, ParsePosition pos) {
        throw new UnsupportedOperationException();
    }

}
