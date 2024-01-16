package icu.etl.ioc.impl;

import icu.etl.annotation.EasyBean;

@EasyBean(name = "test1")
public class Ceshi1 implements Ceshi {
    public String getMessage() {
        return "test1";
    }
}
