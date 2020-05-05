package com.slk.demo.springdemo.service.Impl;

import com.slk.demo.mvcframework.annotation.GPService;
import com.slk.demo.springdemo.service.IDemoService;

@GPService
public class DemoService implements IDemoService {


    @Override
    public String get(String name) {
        return name+" "+"form Service";
    }

}
