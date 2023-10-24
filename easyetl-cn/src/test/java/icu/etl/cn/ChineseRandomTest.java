package icu.etl.cn;

import org.junit.Test;

public class ChineseRandomTest {

    @Test
    public void test1() {
        ChineseRandom random = new ChineseRandom();
        random.get().nextInt();
    }

    @Test
    public void test2() {
        String[] xs = {"王", "李", "张"};
        String[] sxs = {"司马", "上官", "欧阳"};

        ChineseRandom random = new ChineseRandom();
        random.setSurname(xs);
        random.setDoubleSurname(sxs);
        random.setNameWords(new String[]{"节", "本", "术"});

        System.out.println("生成随机姓名:");
        System.out.println(random.nextName());
        System.out.println(random.nextName());
        System.out.println(random.nextName());
    }

}
