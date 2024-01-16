package icu.etl.ioc.impl;

import icu.etl.annotation.EasyBean;
import icu.etl.ioc.EasyContext;

public class CeshiBean {

    @EasyBean
    private EasyContext context;

    @EasyBean(name = "test2")
    private Ceshi ceshi;

    public CeshiBean() {
    }

    public EasyContext getContext() {
        return context;
    }

    public Ceshi getCeshi() {
        return ceshi;
    }
}
