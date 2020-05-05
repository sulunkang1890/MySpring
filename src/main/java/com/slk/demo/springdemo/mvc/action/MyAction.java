package com.slk.demo.springdemo.mvc.action;

import com.slk.demo.mvcframework.annotation.GPAutowired;
import com.slk.demo.mvcframework.annotation.GPController;
import com.slk.demo.mvcframework.annotation.GPRequestMapping;
import com.slk.demo.mvcframework.annotation.GPRequestParam;
import com.slk.demo.mvcframework.webmvc.servlet.GPModelAndView;
import com.slk.demo.springdemo.service.IModifyService;
import com.slk.demo.springdemo.service.IQueryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@GPController
@GPRequestMapping("/web")
public class MyAction {

    @GPAutowired
    IQueryService queryService;
    @GPAutowired
    IModifyService modifyService;

    @GPRequestMapping("/query.json")
    public GPModelAndView query(HttpServletRequest request, HttpServletResponse response,
                                @GPRequestParam("name") String name){
        String result = queryService.query(name);
        return out(response,result);
    }

    @GPRequestMapping("/add*.json")
    public GPModelAndView add(HttpServletRequest request,HttpServletResponse response,
                              @GPRequestParam("name") String name,@GPRequestParam("addr") String addr){
        String result = modifyService.add(name,addr);
        return out(response,result);
    }

    @GPRequestMapping("/remove.json")
    public GPModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                                 @GPRequestParam("id") Integer id){
        String result = modifyService.remove(id);
        return out(response,result);
    }

    @GPRequestMapping("/edit.json")
    public GPModelAndView edit(HttpServletRequest request, HttpServletResponse response,
                               @GPRequestParam("id") Integer id,
                               @GPRequestParam("name") String name){
        String result = modifyService.edit(id,name);
        return out(response,result);
    }



    private GPModelAndView out(HttpServletResponse resp,String str){
        try {
            resp.getWriter().write(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

