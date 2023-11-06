package icu.etl.os.internal;

import java.math.BigDecimal;

import icu.etl.os.OSCpu;

/**
 * 操作系统上CPU信息的接口实现类
 */
public class OSProcessorImpl implements OSCpu {

    private String id;
    private String modeName;
    private int cores;
    private String coreId;
    private int siblings;
    private String physicalId;
    private BigDecimal cacheSize;

    public OSProcessorImpl() {
        super();
    }

    public String getId() {
        return this.id;
    }

    public String getModelName() {
        return this.modeName;
    }

    public int getCores() {
        return this.cores;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public void setCoreId(String coreId) {
        this.coreId = coreId;
    }

    public void setSiblings(int siblings) {
        this.siblings = siblings;
    }

    public void setPhysicalId(String physicalId) {
        this.physicalId = physicalId;
    }

    public void setCacheSize(BigDecimal cacheSize) {
        this.cacheSize = cacheSize;
    }

    public String toString() {
        return "OSProcessorImpl [id=" + id + ", modeName=" + modeName + ", cores=" + cores + ", coreId=" + coreId + ", siblings=" + siblings + ", physicalId=" + physicalId + ", cacheSize=" + cacheSize + "]";
    }

}
