package icu.etl.ioc;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import icu.etl.annotation.EasyBeanClass;
import icu.etl.util.ClassUtils;
import icu.etl.util.StringUtils;

public class CodepageBuilder implements Codepage, BeanBuilder<Codepage>, BeanEventListener {

    /** codepage 与 charset 的映射关系 */
    private Map<String, String> map;

    /** true表示未执行初始化 */
    private boolean notInit;

    /**
     * 初始化
     */
    public CodepageBuilder() {
        this.map = new HashMap<String, String>();
        this.notInit = true;
        this.addAll();
    }

    public Codepage build(EasyetlContext context, Object... array) throws Exception {
        if (this.notInit) {
            this.notInit = false;
            List<BeanConfig> list = context.getImplements(Codepage.class);
            for (BeanConfig bean : list) {
                Class<Codepage> type = bean.getImplementClass();
                Codepage obj = ClassUtils.newInstance(type);
                this.map.putAll(obj.getAll());
            }
        }
        return this;
    }

    public void addImplement(BeanEvent event) {
        Annotation anno = event.getAnnotation();
        if (anno instanceof EasyBeanClass) {
            Codepage obj = ClassUtils.newInstance(event.getImplementClass());
            this.map.putAll(obj.getAll());
        }
    }

    public void removeImplement(BeanEvent event) {
    }

    public String get(String key) {
        if (StringUtils.isNumber(key)) {
            return this.map.get(key);
        } else {
            for (Iterator<Entry<String, String>> it = this.map.entrySet().iterator(); it.hasNext(); ) {
                Entry<String, String> e = it.next();
                if (key.equalsIgnoreCase(e.getValue())) {
                    return e.getKey();
                }
            }
            return null;
        }
    }

    public String get(int codepage) {
        String key = String.valueOf(codepage);
        return this.map.get(key);
    }

    public Map<String, String> getAll() {
        return java.util.Collections.unmodifiableMap(this.map);
    }

    protected void addAll() {
        this.map.put("37", "IBM037");
        this.map.put("273", "IBM273");
        this.map.put("277", "IBM277");
        this.map.put("278", "IBM278");
        this.map.put("280", "IBM280");
        this.map.put("284", "IBM284");
        this.map.put("285", "IBM285");
        this.map.put("297", "IBM297");
        this.map.put("367", "US-ASCII");
        this.map.put("420", "IBM420");
        this.map.put("423", "IBM423");
        this.map.put("424", "IBM424");
        this.map.put("437", "IBM437");
        this.map.put("500", "IBM500");
        this.map.put("808", "IBM808");
        this.map.put("813", "ISO-8859-7");
        this.map.put("819", "ISO-8859-1");
        this.map.put("838", "IBM-Thai");
        this.map.put("850", "IBM850");
        this.map.put("852", "IBM852");
        this.map.put("855", "IBM855");
        this.map.put("857", "IBM857");
        this.map.put("858", "IBM00858");
        this.map.put("862", "IBM862");
        this.map.put("863", "IBM863");
        this.map.put("864", "IBM864");
        this.map.put("866", "IBM866");
        this.map.put("867", "IBM867");
        this.map.put("869", "IBM869");
        this.map.put("870", "IBM870");
        this.map.put("871", "IBM871");
        this.map.put("872", "IBM872");
        this.map.put("874", "TIS-620");
        this.map.put("878", "KOI8-R");
        this.map.put("901", "ISO-8859-13");
        this.map.put("902", "IBM902");
        this.map.put("904", "IBM904");
        this.map.put("912", "ISO-8859-2");
        this.map.put("915", "ISO-8859-5");
        this.map.put("916", "ISO-8859-8-I");
        this.map.put("920", "ISO-8859-9");
        this.map.put("921", "IBM921");
        this.map.put("922", "IBM922");
        this.map.put("923", "ISO-8859-15");
        this.map.put("924", "IBM00924");
        this.map.put("932", "Shift_JIS");
        this.map.put("943", "Windows-31J");
        this.map.put("949", "EUC-KR");
        this.map.put("950", "Big5");
        this.map.put("954", "EUC-JP");
        this.map.put("964", "EUC-TW");
        this.map.put("970", "EUC-KR");
        this.map.put("1004", "Microsoft-Publish");
        this.map.put("1026", "IBM1026");
        this.map.put("1043", "IBM1043");
        this.map.put("1047", "IBM1047");
        this.map.put("1051", "hp-roman8");
        this.map.put("1089", "ISO-8859-6");
        this.map.put("1129", "VISCII");
        this.map.put("1140", "IBM01140");
        this.map.put("1141", "IBM01141");
        this.map.put("1142", "IBM01142");
        this.map.put("1143", "IBM01143");
        this.map.put("1144", "IBM01144");
        this.map.put("1145", "IBM01145");
        this.map.put("1146", "IBM01146");
        this.map.put("1147", "IBM01147");
        this.map.put("1148", "IBM01148");
        this.map.put("1149", "IBM01149");
        this.map.put("1153", "IBM01153");
        this.map.put("1155", "IBM01155");
        this.map.put("1160", "IBM-Thai");
        this.map.put("1161", "TIS-620");
        this.map.put("1162", "TIS-620");
        this.map.put("1163", "VISCII");
        this.map.put("1168", "KOI8-U");
        this.map.put("1200", "UTF-16BE");
        this.map.put("1202", "UTF-16LE");
        this.map.put("1204", "UTF-16");
        this.map.put("1208", "UTF-8");
        this.map.put("1232", "UTF-32BE");
        this.map.put("1234", "UTF-32LE");
        this.map.put("1236", "UTF-32");
        this.map.put("1250", "windows-1250");
        this.map.put("1251", "windows-1251");
        this.map.put("1252", "windows-1252");
        this.map.put("1253", "windows-1253");
        this.map.put("1254", "windows-1254");
        this.map.put("1255", "windows-1255");
        this.map.put("1256", "windows-1256");
        this.map.put("1257", "windows-1257");
        this.map.put("1258", "windows-1258");
        this.map.put("1275", "MACINTOSH");
        this.map.put("1363", "KSC_5601");
        this.map.put("1370", "Big5");
        this.map.put("1381", "GB2312");
        this.map.put("1383", "GB2312");
        this.map.put("1386", "GBK");
        this.map.put("1392", "GB18030");
        this.map.put("4909", "ISO-8859-7");
        this.map.put("5039", "Shift_JIS");
        this.map.put("5346", "windows-1250");
        this.map.put("5347", "windows-1251");
        this.map.put("5348", "windows-1252");
        this.map.put("5349", "windows-1253");
        this.map.put("5350", "windows-1254");
        this.map.put("5351", "windows-1255");
        this.map.put("5352", "windows-1256");
        this.map.put("5353", "windows-1257");
        this.map.put("5354", "windows-1258");
        this.map.put("5488", "GB18030");
        this.map.put("8612", "IBM420");
        this.map.put("8616", "IBM424");
        this.map.put("9005", "ISO-8859-7");
        this.map.put("12712", "IBM424");
        this.map.put("13488", "UTF-16BE");
        this.map.put("13490", "UTF-16LE");
        this.map.put("16840", "IBM420");
        this.map.put("17248", "IBM864");
        this.map.put("17584", "UTF-16BE");
        this.map.put("17586", "UTF-16LE");
        this.map.put("62209", "IBM862");
        this.map.put("62210", "ISO-8859-8");
        this.map.put("62211", "IBM424");
        this.map.put("62213", "IBM862");
        this.map.put("62215", "ISO-8859-8");
        this.map.put("62218", "IBM864");
        this.map.put("62221", "IBM862");
        this.map.put("62222", "ISO-8859-8");
        this.map.put("62223", "windows-1255");
        this.map.put("62224", "IBM420");
        this.map.put("62225", "IBM864");
        this.map.put("62227", "ISO-8859-6");
        this.map.put("62228", "windows-1256");
        this.map.put("62229", "IBM424");
        this.map.put("62231", "IBM862");
        this.map.put("62232", "ISO-8859-8");
        this.map.put("62233", "IBM420");
        this.map.put("62234", "IBM420");
        this.map.put("62235", "IBM424");
        this.map.put("62237", "windows-1255");
        this.map.put("62238", "ISO-8859-8-I");
        this.map.put("62239", "windows-1255");
        this.map.put("62240", "IBM424");
        this.map.put("62242", "IBM862");
        this.map.put("62243", "ISO-8859-8-I");
        this.map.put("62244", "windows-1255");
        this.map.put("62245", "IBM424");
        this.map.put("62250", "IBM420");
    }

}
