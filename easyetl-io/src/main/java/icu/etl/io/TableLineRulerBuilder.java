package icu.etl.io;

import java.util.List;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyBeanBuilder;
import icu.etl.ioc.EasyContext;
import icu.etl.util.ArrayUtils;
import icu.etl.util.StringUtils;

@EasyBean
public class TableLineRulerBuilder implements EasyBeanBuilder<TableLineRuler> {

    public TableLineRuler getBean(EasyContext context, Object... args) {
        TextTable file = ArrayUtils.indexOf(args, TextTable.class, 0);
        if (file != null) {
            String coldel = file.getDelimiter();
            if (coldel.length() == 1) {
                if (file.existsEscape()) {
                    return new S2(coldel.charAt(0), file.getEscape());
                } else {
                    return new S3(coldel.charAt(0));
                }
            } else {
                if (file.existsEscape()) {
                    return new S0(coldel, file.getEscape());
                } else {
                    return new S1(coldel);
                }
            }
        }
        throw new IllegalArgumentException(StringUtils.toString(args));
    }

    /**
     * 字符分隔符长度大于1，有转义字符
     */
    private static class S0 implements TableLineRuler {

        private final String delimiter;

        private final char escape;

        private final StringBuilder buf = new StringBuilder();

        public S0(String delimiter, char escape) {
            this.delimiter = delimiter;
            this.escape = escape;
        }

        public void split(String str, List<String> list) {
            StringUtils.split(str, this.delimiter, this.escape, list);
        }

        public String join(TableLine line) {
            int column = line.getColumn();
            buf.setLength(0);
            for (int i = 1; i <= column; ) {
                buf.append(StringUtils.escape(line.getColumn(i), this.escape));
                if (++i <= column) {
                    buf.append(this.delimiter);
                }
            }
            return buf.toString();
        }

        public String replace(TextTableLine line, int position, String value) {
            int column = line.getColumn();
            int length = 0;
            for (int i = 1; i <= column; i++) {
                length += line.getColumn(i).length() + this.delimiter.length() + 2;
            }

            StringBuilder buf = new StringBuilder(length);
            for (int i = 1; i <= column; ) {
                buf.append(StringUtils.escape(i == position ? value : line.getColumn(i), this.escape));

                if (++i <= column) {
                    buf.append(this.delimiter);
                }
            }
            return buf.toString();
        }
    }

    /**
     * 字符分隔符长度大于1，没有转义字符
     */
    private static class S1 implements TableLineRuler {

        private final String delimiter;

        private final StringBuilder buf = new StringBuilder();

        public S1(String delimiter) {
            this.delimiter = delimiter;
        }

        public void split(String str, List<String> list) {
            StringUtils.split(str, this.delimiter, list);
        }

        public String join(TableLine line) {
            int column = line.getColumn();
            buf.setLength(0);
            for (int i = 1; i <= column; ) {
                buf.append(line.getColumn(i));
                if (++i <= column) {
                    buf.append(this.delimiter);
                }
            }
            return buf.toString();
        }

        public String replace(TextTableLine line, int position, String value) {
            int column = line.getColumn();
            int length = 0;
            for (int i = 1; i <= column; i++) {
                length += line.getColumn(i).length() + this.delimiter.length();
            }

            StringBuilder buf = new StringBuilder(length);
            for (int i = 1; i <= column; ) {
                buf.append(i == position ? value : line.getColumn(i));
                if (++i <= column) {
                    buf.append(this.delimiter);
                }
            }
            return buf.toString();
        }
    }

    /**
     * 单字符分隔符，且有转义字符
     */
    private static class S2 implements TableLineRuler {

        private final char delimiter;

        private final char escape;

        private final StringBuilder buf = new StringBuilder();

        public S2(char delimiter, char escape) {
            this.delimiter = delimiter;
            this.escape = escape;
        }

        public void split(String str, List<String> list) {
            StringUtils.split(str, this.delimiter, this.escape, list);
        }

        public String join(TableLine line) {
            int column = line.getColumn();
            buf.setLength(0);
            for (int i = 1; i <= column; ) {
                buf.append(StringUtils.escape(line.getColumn(i), this.escape));
                if (++i <= column) {
                    buf.append(this.delimiter);
                }
            }
            return buf.toString();
        }

        public String replace(TextTableLine line, int position, String value) {
            int column = line.getColumn();
            int length = 0;
            for (int i = 1; i <= column; i++) {
                length += line.getColumn(i).length() + 1 + 2;
            }

            StringBuilder buf = new StringBuilder(length);
            for (int i = 1; i <= column; ) {
                buf.append(StringUtils.escape(i == position ? value : line.getColumn(i), this.escape));
                if (++i <= column) {
                    buf.append(this.delimiter);
                }
            }
            return buf.toString();
        }
    }

    /**
     * 单字符分隔符，且没有转义字符
     */
    private static class S3 implements TableLineRuler {

        private final char delimiter;

        private final StringBuilder buf = new StringBuilder();

        public S3(char delimiter) {
            this.delimiter = delimiter;
        }

        public void split(String str, List<String> list) {
            StringUtils.split(str, this.delimiter, list);
        }

        public String join(TableLine line) {
            int column = line.getColumn();
            buf.setLength(0);
            for (int i = 1; i <= column; ) {
                buf.append(line.getColumn(i));
                if (++i <= column) {
                    buf.append(this.delimiter);
                }
            }
            return buf.toString();
        }

        public String replace(TextTableLine line, int position, String value) {
            int column = line.getColumn();
            int length = 0;
            for (int i = 1; i <= column; i++) {
                length += line.getColumn(i).length() + 1;
            }

            StringBuilder buf = new StringBuilder(length);
            for (int i = 1; i <= column; ) {
                buf.append(i == position ? value : line.getColumn(i));
                if (++i <= column) {
                    buf.append(this.delimiter);
                }
            }
            return buf.toString();
        }
    }

}
