package icu.etl.database.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import icu.etl.collection.CaseSensitivMap;
import icu.etl.database.JdbcConverterMapper;
import icu.etl.util.Attribute;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

@SuppressWarnings({"unchecked", "rawtypes"})
public class StandardJdbcConverterMapper implements JdbcConverterMapper {

    /** 字段类型与类型转换器配置信息映射关系 */
    private Map<String, Configuration> map;

    /**
     * 初始化
     */
    public StandardJdbcConverterMapper() {
        this.map = new CaseSensitivMap<Configuration>();
    }

    /**
     * 初始化
     *
     * @param str    表达式，格式: {@literal field->className?key=val&key1=val1,10->className?key=val }
     * @param condel 类型转换器之间的分隔符, 如: 半角逗号
     * @param mapdel 字段与转换器类信息之间的分隔符, 如: {@literal -> }
     */
    public StandardJdbcConverterMapper(String str, String condel, String mapdel) {
        this();
        this.parse(str, condel, mapdel);
    }

    /**
     * 解析表达式
     *
     * @param str    表达式，格式: {@literal field->className?key=val&key1=val1,10->className?key=val }
     * @param condel 类型转换器之间的分隔符
     * @param mapdel 字段与转换器类信息之间的分隔符
     */
    private void parse(String str, String condel, String mapdel) {
        if (StringUtils.isBlank(str)) {
            return;
        }

        // 解析表达式
        List<String> list = new ArrayList<String>();
        StringUtils.split(str, condel, list);
        for (String convert : list) {
            if (StringUtils.isBlank(convert)) {
                continue;
            }

            int index = convert.indexOf(mapdel);
            if (index == -1) {
                throw new IllegalArgumentException(convert);
            } else {
                String field = StringUtils.trimBlank(convert.substring(0, index));
                String value = StringUtils.trimBlank(convert.substring(index + mapdel.length()));
                this.map.put(field, new Configuration(value));
            }
        }
    }

    /**
     * 添加字段类型与类型转换器的映射关系
     *
     * @param field 字段类型名
     * @param cls   类型转换器类信息
     * @param array 参数名与参数值的数组（偶数位置上的字符串是属性名，奇数位置上的字符串是属性值）
     */
    public void add(String field, Class<?> cls, String... array) {
        if (array.length % 2 != 0) {
            throw new IllegalArgumentException(StringUtils.toString(array));
        }

        Configuration config = new Configuration(cls);
        for (int i = 0; i < array.length; i++) {
            String key = array[i++];
            String value = array[i];
            config.setParameter(key, value);
        }
        this.map.put(field, config);
    }

    public boolean contains(String key) {
        return this.map.containsKey(key);
    }

    public <E> E get(String key) {
        Configuration config = this.map.get(key);
        if (config == null) {
            return null;
        } else {
            Class<Attribute> cls = config.getConverter();
            Attribute attribute = ClassUtils.newInstance(cls);
            Set<String> names = config.getParameterNames();
            for (String name : names) {
                attribute.setAttribute(name, config.getParameter(name));
            }
            return (E) attribute;
        }
    }

    /**
     * 类型转换器配置信息
     */
    private static class Configuration<E> {

        /** 类信息 */
        private Class<E> cls;

        /** 参数集合 */
        private Map<String, String> parameters;

        /**
         * 创建类型转换器配置信息
         *
         * @param cls 类信息
         */
        public Configuration(Class<E> cls) {
            this.parameters = new HashMap<String, String>();
            this.cls = cls;
        }

        /**
         * 初始化
         *
         * @param expression 类型转换器表达式，格式: className?key=val&key1=val1
         */
        public Configuration(String expression) {
            this.parameters = new HashMap<String, String>();

            int index;
            if ((index = expression.indexOf('?')) != -1) {
                this.cls = ClassUtils.loadClass(StringUtils.trimBlank(expression.substring(0, index)));

                // 解析参数
                String[] array = StringUtils.split(expression.substring(index + 1), '&');
                for (String str : array) {
                    String[] property = StringUtils.trimBlank(StringUtils.splitProperty(str));
                    if (property != null) {
                        this.parameters.put(property[0], property[1]);
                    } else {
                        throw new IllegalArgumentException(str);
                    }
                }
            } else {
                this.cls = ClassUtils.loadClass(expression);
            }
        }

        /**
         * 类型转换器的类信息
         *
         * @return 类信息
         */
        public Class<E> getConverter() {
            return this.cls;
        }

        /**
         * 返回参数值
         *
         * @param key 参数名
         * @return 参数值
         */
        public String getParameter(String key) {
            return this.parameters.get(key);
        }

        /**
         * 保存参数
         *
         * @param key   参数名
         * @param value 参数值
         */
        public void setParameter(String key, String value) {
            this.parameters.put(key, value);
        }

        /**
         * 类型转换器的参数
         *
         * @return 参数名集合
         */
        public Set<String> getParameterNames() {
            return this.parameters.keySet();
        }

    }

}
