package com.slk.demo.mvcframework.webmvc.servlet;


import com.slk.demo.mvcframework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Tom.
 */
public class GPHandlerAdapter {


    public GPModelAndView handler(HttpServletRequest req, HttpServletResponse resp, GPHandlerMapping handler) throws Exception {

        //保存形参列表
        //将参数名称和参数的位置，这种关系保存起来
        Map<String,Integer> paramIndexMapping=new HashMap<String, Integer>();
        Annotation[] [] annotations=handler.getMethod().getParameterAnnotations();
        //method.getParameterAnnotations(); 返回值是二维数组  第一个维度对应参数列表里参数的数目，第二个维度对应参数列表里对应的注解
        //annotations【i】 是i的原因  是因为 这里面 从第i 个开始遍历 前面的 已经不需要遍历了
        Annotation[] [] pa = handler.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length ; i ++) {
            for(Annotation a : pa[i]){
                if(a instanceof GPRequestParam){
                    String paramName = ((GPRequestParam) a).value();
                    if(!"".equals(paramName.trim())){
//                        String value = Arrays.toString(params.get(paramName))
//                                .replaceAll("\\[|\\]","")
//                                .replaceAll("\\s+",",");
//                        paramValues[i] = value;
                        paramIndexMapping.put(paramName,i);
                    }
                }
            }
        }

        //初始化一下
        Class<?> [] paramTypes = handler.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramterType = paramTypes[i];
            if(paramterType == HttpServletRequest.class || paramterType == HttpServletResponse.class){
                paramIndexMapping.put(paramterType.getName(),i);
            }
        }


        //拿到get请求的的参数
        Map<String,String[]> params = req.getParameterMap();
        //拼接实参列表
        Object [] paramValues = new Object[paramTypes.length];


        for (Map.Entry<String,String[]> param : params.entrySet()) {
            String value = Arrays.toString(params.get(param.getKey()))
                    .replaceAll("\\[|\\]","")
                    .replaceAll("\\s+",",");

            if(!paramIndexMapping.containsKey(param.getKey())){continue;}

            int index = paramIndexMapping.get(param.getKey());

            //允许自定义的类型转换器Converter
            paramValues[index] = castStringValue(value,paramTypes[index]);
        }
        if(paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int index = paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[index] = req;
        }

        if(paramIndexMapping.containsKey(HttpServletResponse.class.getName())){
            int index = paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[index] = resp;
        }

        Object result=handler.getMethod().invoke(handler.getController(),paramValues);
        if(result == null || result instanceof Void){return null;}

        boolean isModelAndView = handler.getMethod().getReturnType() == GPModelAndView.class;
        if(isModelAndView){
            return (GPModelAndView)result;
        }
        return null;
    }

    private Object castStringValue(String value, Class<?> paramType) {
        if(String.class == paramType){
            return value;
        }else if(Integer.class == paramType){
            return Integer.valueOf(value);
        }else if(Double.class == paramType){
            return Double.valueOf(value);
        }else {
            if(value != null){
                return value;
            }
            return null;
        }

    }
    //自己写，自己用
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
//        if(chars[0] > )
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
