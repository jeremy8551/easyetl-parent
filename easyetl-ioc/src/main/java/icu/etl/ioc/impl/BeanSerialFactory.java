package icu.etl.ioc.impl;

import java.util.concurrent.atomic.AtomicInteger;

import icu.etl.ProjectPom;

/**
 * 容器序号工厂
 *
 * @author jeremy8551@qq.com
 * @createtime 2024/2/8 09:12
 */
public class BeanSerialFactory {

    /** 容器编号序号 */
    protected static final AtomicInteger number = new AtomicInteger(0);

    /**
     * 返回一个容器名（唯一）
     *
     * @return 容器名
     */
    public static String createContextName() {
        return ProjectPom.getArtifactID() + "-" + number.incrementAndGet();
    }

}
