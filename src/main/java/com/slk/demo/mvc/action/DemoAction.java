package com.slk.demo.mvc.action;


import com.slk.demo.mvcframework.annotation.*;
import com.slk.demo.service.IDemoService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@GPController
public class DemoAction {
//    @GPAutowired
//    private IDemoService demoService;
//
//    @GPRequestMapping("/query")
//    public void query(HttpServletRequest req, HttpServletResponse resp,
//                      @GPRequestParam("name") String name){
//        String result = demoService.get(name);
////		String result = "My name is " + name;
//        try {
//            resp.getWriter().write(result);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    @GPAutowired
    IDemoService demoService;



    @GPRequestMapping("/query")
    public void query(HttpServletRequest req,HttpServletResponse resp, @GPRequestParam("name") String name){
        String result=demoService.get(name);
        try {
            resp.getWriter().write(result);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
