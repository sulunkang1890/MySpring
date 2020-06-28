package com.slk.demo.springdemo.mvc.action;



import com.slk.demo.mvcframework.annotation.GPAutowired;
import com.slk.demo.mvcframework.annotation.GPController;
import com.slk.demo.mvcframework.annotation.GPRequestMapping;
import com.slk.demo.mvcframework.annotation.GPRequestParam;
import com.slk.demo.mvcframework.webmvc.servlet.GPModelAndView;
import com.slk.demo.springdemo.service.IQueryService;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@GPController
@GPRequestMapping("/")
public class PageAction {

    @GPAutowired
    IQueryService queryService;

    @GPRequestMapping("/first.html")
    public GPModelAndView query(@GPRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new GPModelAndView("first.html",model);
    }

}
