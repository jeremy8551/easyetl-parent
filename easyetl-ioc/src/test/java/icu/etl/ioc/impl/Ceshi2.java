package icu.etl.ioc.impl;

import icu.etl.annotation.EasyBean;

@EasyBean(name = "test2")
public class Ceshi2 implements Ceshi {
    
    public String getMessage() {
        return "test2";
    }
}
