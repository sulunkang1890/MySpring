package com.slk.demo.service.Impl;

import com.slk.demo.mvcframework.annotation.GPService;
import com.slk.demo.service.IDemoService;

import java.io.File;
import java.lang.reflect.Field;

@GPService
public class DemoService implements IDemoService {


    @Override
    public String get(String name) {
        return name+" "+"form Service";
    }

}
