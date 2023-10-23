package icu.etl.io;

import java.util.List;

import icu.etl.ioc.BeanBuilder;
import icu.etl.ioc.EasyetlContext;
import icu.etl.util.StringUtils;

public class TableLineRulerBuilder implements BeanBuilder<TableLineRuler> {

    public TableLineRuler build(EasyetlContext context, Object... array) throws Exception {
        TextTable file = this.indexOf(array, TextTable.class, 0);
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

        throw new UnsupportedOperationException(StringUtils.toString(array));
    }

    @SuppressWarnings("unchecked")
    <E> E indexOf(Object[] array, Class<E> cls, int offset) {
        for (int i = offset; i < array.length; i++) {
            Object obj = array[i];
            if (obj != null && cls.isAssignableFrom(obj.getClass())) {
                return (E) obj;
            }
        }
        return null;
    }

    class S0 implements TableLineRuler {

        private String delimiter;

        private char escape;

        public S0(String delimiter, char escape) {
            this.delimiter = delimiter;
            this.escape = escape;
        }

        public void split(String str, List<String> list) {
            StringUtils.split(str, this.delimiter, this.escape, list);
        }

        public String join(TableLine line) {
            int column = line.getColumn();
            int length = 0;
            for (int i = 1; i <= column; i++) {
                length += line.getColumn(i).length() + this.delimiter.length() + 2;
            }

            StringBuilder buf = new StringBuilder(length);
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

    class S1 implements TableLineRuler {

        private String delimiter;

        public S1(String delimiter) {
            this.delimiter = delimiter;
        }

        public void split(String str, List<String> list) {
            StringUtils.split(str, this.delimiter, list);
        }

        public String join(TableLine line) {
            int column = line.getColumn();
            int length = 0;
            for (int i = 1; i <= column; i++) {
                length += line.getColumn(i).length() + this.delimiter.length();
            }

            StringBuilder buf = new StringBuilder(length);
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

    class S2 implements TableLineRuler {

        private char delimiter;

        private char escape;

        public S2(char delimiter, char escape) {
            this.delimiter = delimiter;
            this.escape = escape;
        }

        public void split(String str, List<String> list) {
            StringUtils.split(str, this.delimiter, this.escape, list);
        }

        public String join(TableLine line) {
            int column = line.getColumn();
            int length = 0;
            for (int i = 1; i <= column; i++) {
                length += line.getColumn(i).length() + 1 + 2;
            }

            StringBuilder buf = new StringBuilder(length);
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

    class S3 implements TableLineRuler {

        private char delimiter;

        public S3(char delimiter) {
            this.delimiter = delimiter;
        }

        public void split(String str, List<String> list) {
            StringUtils.split(str, this.delimiter, list);
        }

        public String join(TableLine line) {
            int column = line.getColumn();
            int length = 0;
            for (int i = 1; i <= column; i++) {
                length += line.getColumn(i).length() + 1;
            }

            StringBuilder buf = new StringBuilder(length);
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
